package com.msb.mall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.msb.common.dto.es.SkuESModel;
import com.msb.mall.search.config.MallElasticSearchConfiguration;
import com.msb.mall.search.constant.ESConstant;
import com.msb.mall.search.service.ElasticSearchSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {
    @Autowired
    private RestHighLevelClient client;
    /**
     * 实现数据存储到ES的操作
     */
    @Override
    public Boolean productUpStatus(List<SkuESModel> skuESModels) throws IOException {
        //创建 product对应的索引
        //BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels) {
            //设置索引
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);
            //索引id
            indexRequest.id(skuESModel.getSkuId().toString());
            //文档source 将skuESModel转成json数据
            String skuStr = JSONObject.toJSONString(skuESModel);
            System.out.println("skuStr是否转成了json字符串了？？ = " + skuStr);
            indexRequest.source(skuStr, XContentType.JSON);
            // 转换之后的数据封装到bulk中
            bulkRequest.add(indexRequest);

        }
        //批量向ES中保存数据
        BulkResponse responses = client.bulk(bulkRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        boolean hasFailures = responses.hasFailures();
        return !hasFailures;
    }
}
