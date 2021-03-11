package com.hl.sf.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hl.sf.SfApplicationTests;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.search.ISearchService;
import com.hl.sf.web.form.RentSearch;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class SearchServiceImplTest extends SfApplicationTests {

    @Autowired
    private ISearchService searchService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Test
    public void testIndex() {
        Long targetHouseId = 15L;
        searchService.index(targetHouseId);
    }

    @Test
    public void testRemove() {
        Long targerHouseId = 15L;
        searchService.remove(targerHouseId);

    }

    @Test
    public void testQuery() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(3);
        ServiceMultiResult<Long> query = searchService.query(rentSearch);
        query.getResult().forEach(System.out::println);
    }

    @Test
    public void testGetAnalyze() throws Exception {
        List<String> 小区 = getAnalyze("小区");
        小区.forEach(System.out::println);
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

}
