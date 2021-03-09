package com.hl.sf.service.search.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.sf.entity.House;
import com.hl.sf.entity.HouseDetail;
import com.hl.sf.entity.HouseTag;
import com.hl.sf.repository.HouseDao;
import com.hl.sf.repository.HouseDetailDao;
import com.hl.sf.repository.HouseTagDao;
import com.hl.sf.service.search.HouseIndexKey;
import com.hl.sf.service.search.HouseIndexMessage;
import com.hl.sf.service.search.HouseIndexTemplate;
import com.hl.sf.service.search.ISearchService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hl2333
 */
@Service("searchService")
public class SearchServiceImpl implements ISearchService {

    private static final Logger logger  = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";
//    private static final String INDEX_TYPE = "house";
    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseDao houseDao;

    @Autowired
     private HouseDetailDao houseDetailDao;

    @Autowired
    private HouseTagDao houseTagDao;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @KafkaListener(topics =  INDEX_TOPIC)
    private void handleMessage(String content){
        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (message.getOperation()){
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    logger.warn("not support message content " + content);
                    break;

            }
        } catch (IOException e) {
            logger.error("cannot parse json for " + content, e);
        }
    }

    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry){
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("retry index times over 3 for house: " + houseId);
            return ;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
    }
    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();

        House house = houseDao.findById(houseId);
        if (house == null) {
            logger.error("index house {} dose not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return ;
        }

        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail detail = houseDetailDao.findByHouseId(houseId);
        if (detail == null){
            logger.error("detail is null");
            return;
        }

        modelMapper.map(detail, indexTemplate);

        List<HouseTag> tags = houseTagDao.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
            indexTemplate.setTags(tagStrings);
        }


        //create/update/delete&create
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(HouseIndexKey.HOUSE_ID, houseId);

        searchSourceBuilder.query(termQueryBuilder);

        /****
         * 记得把查询条件添加进去
         */
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return;
        }


        boolean success;
        long totalHit = response.getHits().getTotalHits().value;

        if (totalHit == 0){
            success = create(indexTemplate);
        } else if (totalHit == 1){
            String esId = response.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);
        } else {
            success = deleteAndCreate(totalHit, indexTemplate);
        }

        if (success) {
            logger.debug("index success with house" + houseId);
        }
    }

    private boolean create(HouseIndexTemplate indexTemplate){
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);

        indexRequest.timeout(TimeValue.timeValueSeconds(1));

        indexRequest.source(JSON.toJSONString(indexTemplate), XContentType.JSON);

        IndexResponse response = null;
        try {
            response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }

        logger.debug("create index with house " + indexTemplate.getHouseId());

        if (response.status() == RestStatus.CREATED){
            return true;
        } else {
            return false;
        }
    }

    private boolean update(String esId, HouseIndexTemplate indexTemplate){
        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, esId);

        updateRequest.timeout(TimeValue.timeValueSeconds(1));

        updateRequest.doc(JSON.toJSONString(indexTemplate), XContentType.JSON);
        UpdateResponse response = null;
        try {
            response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
        logger.debug("create index with house " + indexTemplate.getHouseId());

        if (response.status() == RestStatus.OK){
            return true;
        } else {
            return false;
        }

    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate){
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX_NAME);

        QueryBuilder queryBuilder = new TermQueryBuilder(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId());

        deleteByQueryRequest.setQuery(queryBuilder);

        BulkByScrollResponse bulkByScrollResponse = null;
        try {
            bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        long deleted = bulkByScrollResponse.getDeleted();
        if (deleted != totalHit){
            logger.warn("need delete {}. but {} was deleted", totalHit, deleted);
            return false;
        } else {
            return create(indexTemplate);
        }

        /*SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId());

        searchRequestBuilder.setQuery(termQueryBuilder);
        searchRequest.source(searchRequestBuilder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        deleteRequest.id(search.getHits().iterator().next().getId());*/
    }


    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();

        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX_NAME);

        QueryBuilder queryBuilder = new TermQueryBuilder(HouseIndexKey.HOUSE_ID, houseId);

        deleteByQueryRequest.setQuery(queryBuilder);

        BulkByScrollResponse bulkByScrollResponse = null;
        try {
            bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        long deleted = bulkByScrollResponse.getDeleted();
        logger.debug("delete total " + deleted);

        if (deleted <= 0){
            this.remove(houseId, message.getRetry() + 1);
        }
    }

    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    public void remove(Long houseId, int retry){
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for house: " + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Cannot encode json for " + message, e);
        }
    }
}
