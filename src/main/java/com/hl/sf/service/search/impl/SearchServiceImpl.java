package com.hl.sf.service.search.impl;

import com.alibaba.fastjson.JSON;
import com.hl.sf.entity.House;
import com.hl.sf.entity.HouseDetail;
import com.hl.sf.entity.HouseTag;
import com.hl.sf.repository.HouseDao;
import com.hl.sf.repository.HouseDetailDao;
import com.hl.sf.repository.HouseTagDao;
import com.hl.sf.service.search.HouseIndexKey;
import com.hl.sf.service.search.HouseIndexTemplate;
import com.hl.sf.service.search.ISearchService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String INDEX_TYPE = "hosue";

    @Autowired
    private HouseDao houseDao;

    @Autowired
     private HouseDetailDao houseDetailDao;

    @Autowired
    private HouseTagDao houseTagDao;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean index(Long houseId) {
        House house = houseDao.findById(houseId);
        if (house == null) {
            logger.error("index house {} dose not exist!", houseId);
            return;
        }

        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail detail = houseDetailDao.findByHouseId(houseId);
        if (detail == null){
            logger.error("detail is null");
            return false;
        }

        modelMapper.map(detail, indexTemplate);

        List<HouseTag> tags = houseTagDao.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
            indexTemplate.setTags(tagStrings);
        }


        //create

        //update

        //delete & create
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
        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME);

        updateRequest.timeout(TimeValue.timeValueSeconds(1));

        updateRequest.id(esId);
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

    private boolean deleteAndCreate(int totalHit, HouseIndexTemplate indexTemplate){
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
            return true;
        }

        /*SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId());

        searchRequestBuilder.setQuery(termQueryBuilder);
        searchRequest.source(searchRequestBuilder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        deleteRequest.id(search.getHits().iterator().next().getId());*/
    }

    @Override
    public void remove(Long houseId) {

    }
}
