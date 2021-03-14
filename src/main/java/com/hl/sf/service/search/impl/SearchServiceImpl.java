package com.hl.sf.service.search.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.hl.sf.base.RentValueBlock;
import com.hl.sf.entity.House;
import com.hl.sf.entity.HouseDetail;
import com.hl.sf.entity.HouseTag;
import com.hl.sf.entity.SupportAddress;
import com.hl.sf.repository.HouseDao;
import com.hl.sf.repository.HouseDetailDao;
import com.hl.sf.repository.HouseTagDao;
import com.hl.sf.repository.SupportAddressDao;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.service.search.ISearchService;
import com.hl.sf.service.search.entity.*;
import com.hl.sf.web.dto.HouseBucketDTO;
import com.hl.sf.web.form.MapSearch;
import com.hl.sf.web.form.RentSearch;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hl2333
 */
@Service("searchService")
public class SearchServiceImpl implements ISearchService {

    private static final Logger logger  = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";
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

    @Autowired
    private SupportAddressDao supportAddressDao;

    @Autowired
    @Qualifier("addressService")
    private IAddressService addressService;

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
    public ServiceResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.suggest(suggestBuilder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList());
        }

        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<>();

        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()){
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }

            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return ServiceResult.of(suggests);
    }

    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder(HouseIndexKey.CITY_EN_NAME, cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT)
                        .field(HouseIndexKey.DISTRICT)).size(0);

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (response.status() == RestStatus.OK) {
            Terms terms = response.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()){
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
            } else {
                logger.warn("failed to aggregate for " + HouseIndexKey.AGG_DISTRICT);
            }
        }
        return ServiceResult.of(0L);
    }

    @Override
    public ServiceMultiResult<HouseBucketDTO> mapAggregate(String city) {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, city));

        AggregationBuilder aggBuilder = AggregationBuilders.terms(HouseIndexKey.AGG_REGION).field(HouseIndexKey.REGION_EN_NAME);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(aggBuilder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

//        logger.debug(response.toString());
        List<HouseBucketDTO> buckets = new ArrayList<>();
        if (response.status() != RestStatus.OK) {
            logger.warn("Aggregate status is not ok for " + searchRequest);
            return new ServiceMultiResult<HouseBucketDTO>(0L, buckets);
        }

        Terms terms = response.getAggregations().get(HouseIndexKey.AGG_REGION);
        terms.getBuckets().forEach(bucket -> {
            buckets.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
        });

        return new ServiceMultiResult<HouseBucketDTO>(response.getHits().getTotalHits().value, buckets);
    }

    @Override
    public ServiceMultiResult<Long> mapQuery(String cityEnName,
                                             String orderBy,
                                             String orderDirection,
                                             int start, int size) {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort(orderBy, SortOrder.fromString(orderDirection));
        searchSourceBuilder.from(start).size(size);
        List<Long> houseIds = new ArrayList<>();

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return new ServiceMultiResult<Long>(0L, houseIds);
        }

        if (response.status() != RestStatus.OK) {
            logger.warn("search status id not ok for " + searchRequest);
            return new ServiceMultiResult<Long>(0L, houseIds);
        }
        response.getHits().forEach(hit -> {
            houseIds.add(Longs.tryParse(String.valueOf(
                    hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
        });
        return new ServiceMultiResult<Long>(response.getHits().getTotalHits().value, houseIds);
    }

    @Override
    public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(
                QueryBuilders.geoBoundingBoxQuery("location")
                .setCorners(
                        new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
                        new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
                )
        );

        searchSourceBuilder.query(boolQueryBuilder)
                .sort(mapSearch.getOrderBy(),
                        SortOrder.fromString(mapSearch.getOrderDirection()))
                .from(mapSearch.getStart())
                .size(mapSearch.getSize());

        searchRequest.source(searchSourceBuilder);

        List<Long> houseIds = new ArrayList<>();
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.warn(e.getMessage());
            return new ServiceMultiResult<Long>(0L, houseIds);
        }
        if (RestStatus.OK != response.status()) {
            logger.warn("search status is not ok for " + searchRequest);
            return new ServiceMultiResult<Long>(0L, houseIds);
        }

        response.getHits().forEach(hit -> {
            houseIds.add(Longs.tryParse(String.valueOf(
                    hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
        });
        return new ServiceMultiResult<Long>(response.getHits().getTotalHits().value,
                houseIds);
    }

    private boolean updateSuggest(HouseIndexTemplate indexTemplate){
        List<String> suggests = new ArrayList<>();
        try {
            getAnalyze(indexTemplate.getTitle()).forEach(word -> suggests.add(word));
            getAnalyze(indexTemplate.getLayoutDesc()).forEach(word -> suggests.add(word));
            getAnalyze(indexTemplate.getRoundService()).forEach(word -> suggests.add(word));
            getAnalyze(indexTemplate.getDescription()).forEach(word -> suggests.add(word));
            getAnalyze(indexTemplate.getSubwayLineName()).forEach(word -> suggests.add(word));
            getAnalyze(indexTemplate.getSubwayStationName()).forEach(word -> suggests.add(word));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        //定制化小区自动补全
        List<HouseSuggest> houseSuggests = new ArrayList<>();
        suggests.add(indexTemplate.getDistrict());
        suggests.forEach(s -> {
            HouseSuggest suggestTemp = new HouseSuggest();
            suggestTemp.setInput(s);
            houseSuggests.add(suggestTemp);
        });
        indexTemplate.setSuggest(houseSuggests);
        return true;
    }

    public List<String> getAnalyze(String text) throws Exception {
        List<String> list = new ArrayList<String>();
        Request request = new Request("GET", "_analyze");
        JSONObject entity = new JSONObject();
        entity.put("analyzer", "ik_smart");
        //entity.put("analyzer", "ik_smart");
        /**
         * 设置分词的值
         */
        entity.put("text", text);

        request.setJsonEntity(entity.toJSONString());
        Response response = this.restHighLevelClient.getLowLevelClient().performRequest(request);
        JSONObject tokens = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
        JSONArray arrays = tokens.getJSONArray("tokens");
        for (int i = 0; i < arrays.size(); i++)
        {
            JSONObject obj = JSON.parseObject(arrays.getString(i));
            list.add(obj.getString("token"));
        }
        return list;
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
        SupportAddress city = supportAddressDao.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());

        SupportAddress region = supportAddressDao.findByEnNameAndLevel(house.getRegionEnName(), SupportAddress.Level.REGION.getValue());

        String address = city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict() + detail.getAddress();
        ServiceResult<BaiduMapLocation> location = addressService.getBaiduMapLocation(city.getCnName(), address);

        if (!location.isSuccess()) {
            this.index(message.getHouseId(), message.getRetry() + 1);
            return;
        }
        indexTemplate.setLocation(location.getResult());

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

//        logger.debug("hello ???" + searchRequest.toString());
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
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

        ServiceResult serviceResult = addressService.lbsUpload(location.getResult(), house.getStreet() + house.getDistrict(),
                city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict(),
                message.getHouseId(), house.getPrice(), house.getArea());

        if (!success || !serviceResult.isSuccess()) {
            this.index(message.getHouseId(), message.getRetry() + 1);
        } else {
            logger.debug("Index success with house " + houseId);
        }
    }

    private boolean create(HouseIndexTemplate indexTemplate){
        if (!updateSuggest(indexTemplate)){
            return false;
        }
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
        if (!updateSuggest(indexTemplate)) {
            return false;
        }
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

        ServiceResult serviceResult = addressService.removeLbs(houseId);

        if (!serviceResult.isSuccess() || deleted <= 0) {
            logger.warn("Did not remove data from es for response: " + bulkByScrollResponse);
            // 重新加入消息队列
            this.remove(houseId, message.getRetry() + 1);
        }
    }

    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        boolQueryBuilder.filter(new TermQueryBuilder(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName()));

        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())){
            boolQueryBuilder.filter(new TermQueryBuilder(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName()));
        }

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                rangeQuery.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQuery.gte(price.getMin());
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }

        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }
        boolQueryBuilder.must(
                QueryBuilders.matchQuery(HouseIndexKey.TITLE, rentSearch.getKeywords()).boost(2.0f)
        );
        boolQueryBuilder.should(
                QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                        HouseIndexKey.TRAFFIC,
                        HouseIndexKey.DISTRICT,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                ));
        searchSourceBuilder.query(boolQueryBuilder)
                .sort(rentSearch.getOrderBy(), SortOrder.fromString(rentSearch.getOrderDirection()))
                .from(rentSearch.getStart())
                .size(rentSearch.getSize());
        searchRequest.source(searchSourceBuilder);

//        logger.debug(searchRequest.toString());

        List<Long> houseIds = new ArrayList<>();
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.debug(e.getMessage());
            return new ServiceMultiResult<Long>(0L, houseIds);
        }

        if (response.status() != RestStatus.OK){
            logger.warn("search status is no for " + rentSearch);
            return new ServiceMultiResult<Long>(0L, houseIds);
        }
        response.getHits().forEach(hit -> {
            houseIds.add(Long.parseLong(String.valueOf
                    (hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
        });

        return new ServiceMultiResult<Long>(response.getHits().getTotalHits().value, houseIds);
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
