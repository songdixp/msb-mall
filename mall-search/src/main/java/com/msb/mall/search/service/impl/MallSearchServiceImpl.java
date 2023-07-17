package com.msb.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.dto.es.SkuESModel;
import com.msb.mall.search.config.MallElasticSearchConfiguration;
import com.msb.mall.search.constant.ESConstant;
import com.msb.mall.search.service.MallSearchService;
import com.msb.mall.search.vo.SearchParamVO;
import com.msb.mall.search.vo.SearchResultVO;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;

    @Override
    public SearchResultVO search(SearchParamVO paramVO) {
        //1 准备检索请求
        SearchRequest searchRequest =  buildSearchRequest(paramVO);
        // 2.执行检索操作
        SearchResultVO result=null;
        try {
            //点击执行请求按钮
            SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
            System.out.println("response = " + response);

            // 3.把检索信息封装成为SearchResultVO
             result = buildSearchResponse(response, paramVO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }



    /**
     * 构建请求信息
     * 1 关键字匹配
     * 2 过滤：类别、品牌、属性、价格区间、是否有库存
     * 3 排序
     * 4 分页
     * 5 高亮
     * 6 聚合分析
     * @param paramVO 前端的参数VO对象
     * @return SearchRequest
     */
    private SearchRequest buildSearchRequest(SearchParamVO paramVO) {
        SearchRequest request = new SearchRequest();
        request.indices(ESConstant.PRODUCT_INDEX); // 查询的索引(数据库)
        //1. 构建检索的条件--Query部分
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1.1 构建bool查询- 通过QueryBuilders来内部查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //  1.1.1 must 关键字匹配
        if (StringUtils.isNotEmpty(paramVO.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("subTitle", paramVO.getKeyword()));
        }
        //  1.1.2 过滤类别catalogID
        if (paramVO.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",paramVO.getCatalog3Id()));
        }
        //  1.1.3 过滤品牌 brandId
        if (paramVO.getBrandId()!=null&&paramVO.getBrandId().size()>0){
            List<Long> brandIdList = paramVO.getBrandId();
            boolQuery.filter(QueryBuilders.termsQuery("brandId",brandIdList));
        }
        //  1.1.4 skuPrice范围过滤
            /* 三种情况
            skuPrice=200_300
            skuPrice=_300
            skuPrice=200_
            */
        if (StringUtils.isNotEmpty(paramVO.getSkuPrice())){
            String skuPrice = paramVO.getSkuPrice();
            String[] priceArray = skuPrice.split("_");
            if (priceArray.length==2){
                //从 200 到300的范围
                boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").gte(priceArray[0]).lte(priceArray[1]));
            }
            if (skuPrice.startsWith("_")&&priceArray.length==1){
                //_300
                boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").lte(priceArray[0]));
            }
            if (skuPrice.endsWith("_")&&priceArray.length==1){
                //200_
                boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").gte(priceArray[0]));
            }

        }
        //  1.1.5 是否有库存 stock
        if (paramVO.getHasStock()!=null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",paramVO.getHasStock()==1));
        }
        //  1.1.6 nest嵌套查询 attrs
        // 前端提交的检索条件  attr=20_8英寸:10寸 & attr=19_64GB:32GB
        if (paramVO.getAttrs()!=null && paramVO.getAttrs().size()>0){
            List<String> attrs = paramVO.getAttrs();
            for (String attr : attrs) {
                //20_8:10 先做分割
                String[] attrArray = attr.split("_");
                //属性编号 attr_id
                String attrId = attrArray[0];
                // 8:10
                String[] attrValArray = attrArray[1].split(":");
                //按照nest结构，拼接builder
                BoolQueryBuilder nestQueryBool = QueryBuilders.boolQuery();
                nestQueryBool.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestQueryBool.must(QueryBuilders.termQuery("attrs.attrValue", attrValArray));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",nestQueryBool, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        sourceBuilder.query(boolQuery); // 绑定sourceBuilder


        // 1.2 sort排序
        // sort=salaCount_asc/desc
        if (StringUtils.isNotEmpty(paramVO.getSort())){
            String[] sortArray = paramVO.getSort().split("_");
            String field = sortArray[0];
            SortOrder sortOrder = sortArray[1].equalsIgnoreCase("asc")? SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(field, sortOrder);
        }
        // 1.3 翻页 from size
        if (paramVO.getPageNum()!=null){
            //分页处理 pagesize=5
            //pageNum:1 from:0 [0,1,2,3,4]
            //pageNum:2 from:5 [5,6,7,8,9]
            //from = (pageNum- 1)*pagesize
            sourceBuilder.from((paramVO.getPageNum()-1) * ESConstant.PRODUCT_PAGESIZE);
            sourceBuilder.size(ESConstant.PRODUCT_PAGESIZE);
        }

        // 1.4 高亮
        if (StringUtils.isNotEmpty(paramVO.getKeyword())){
            // 有关键字才高亮处理
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightBuilder subTitle = highlightBuilder.field("subTitle");
            if (subTitle!=null){
                highlightBuilder.preTags("<b style='color:red'>");
                highlightBuilder.postTags("</b>");
                sourceBuilder.highlighter(highlightBuilder);
            }

        }

        // 1.5 聚合分析
        // 1.5.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId");
        brand_agg.size(50);
        // 品牌的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(10));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(10));
        sourceBuilder.aggregation(brand_agg);

        //1.5.2 类别聚合
        TermsAggregationBuilder catalogId = AggregationBuilders.terms("catalog_agg");
        catalogId.field("catalogId");
        catalogId.size(10);
        //类别的子聚合
        catalogId.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(10));
        sourceBuilder.aggregation(catalogId);

        //1.5.3 attrs属性聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10);
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10);
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attr_id_agg.subAggregation(attr_name_agg);
        attr_id_agg.subAggregation(attr_value_agg);
        nested.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(nested);

        System.out.println("sourceBuilder--->"+sourceBuilder);
        request.source(sourceBuilder);  // 请求和构建器绑定
        return request;
    }

    /**
     * 构建响应信息
     * @param response ES查出来的响应信息
     * @return SearchResultVO
     */
    private SearchResultVO buildSearchResponse(SearchResponse response, SearchParamVO paramVO) {
        SearchResultVO resultVO = new SearchResultVO();
        //1 当前关键字下面的所有商品
        SearchHits hits = response.getHits();
        SearchHit[] hitsArray = hits.getHits();
        List<SkuESModel> productsModelList = new ArrayList<>();
        if (hitsArray!=null && hitsArray.length>0){
            for (SearchHit product : hitsArray) {
                String productJSON = product.getSourceAsString();
                SkuESModel model = JSON.parseObject(productJSON, SkuESModel.class);
                if(StringUtils.isNotEmpty(paramVO.getKeyword())){
                    //设置高亮
                    HighlightField subTitle = product.getHighlightFields().get("subTitle");
                    String subTitleHighlight = subTitle.getFragments()[0].string();
                    model.setSubTitle(subTitleHighlight);
                }
                productsModelList.add(model);

            }
        }
        resultVO.setProducts(productsModelList);

        // 处理聚合信息部分
        // 2当前关键字下面的品牌信息
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = brand_agg.getBuckets();
        List<SearchResultVO.BrandVO> brandVOS = new ArrayList<>();
        if (buckets!=null&&buckets.size()>0){
            for (Terms.Bucket bucket : buckets) {
                SearchResultVO.BrandVO brandVO = new SearchResultVO.BrandVO();
                String brandId = bucket.getKeyAsString();
                brandVO.setBrandId(Long.parseLong(brandId)); // 设置品牌的编号 id
                // 然后获取品牌的图片和地址
                //设置品牌图片地址
                ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
                List<? extends Terms.Bucket> brandImgAggBuckets = brand_img_agg.getBuckets();
                if (brandImgAggBuckets!=null&&brandImgAggBuckets.size()>0){
                    String img = brandImgAggBuckets.get(0).getKeyAsString();
                    brandVO.setBrandImg(img);
                }
                //设置品牌名称
                ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
                List<? extends Terms.Bucket> brandNameAggBuckets = brand_name_agg.getBuckets();
                if (brandNameAggBuckets!=null&&brandNameAggBuckets.size()>0){
                    String brandName = brandNameAggBuckets.get(0).getKeyAsString();
                    brandVO.setBrandName(brandName);
                }
                brandVOS.add(brandVO);

            }
        }
        resultVO.setKeywordBrands(brandVOS);

        //3 当前关键字下的类别信息
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalogAggBuckets = catalog_agg.getBuckets();
        List<SearchResultVO.CatalogVO> catalogVOS = new ArrayList<>();
        if (catalogAggBuckets!=null&&catalogAggBuckets.size()>0){
            for (Terms.Bucket aggBucket : catalogAggBuckets) {
                SearchResultVO.CatalogVO catalogVO = new SearchResultVO.CatalogVO();
                String catalogId = aggBucket.getKeyAsString();
                catalogVO.setCatalogId(Long.parseLong(catalogId));
                //类别名称
                ParsedStringTerms catalog_name_agg = aggBucket.getAggregations().get("catalog_name_agg");
                if (catalog_name_agg.getBuckets()!=null && catalog_name_agg.getBuckets().size()>0){
                    String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
                    catalogVO.setCatalogName(catalogName);
                    catalogVOS.add(catalogVO);
                }

            }
        }
        resultVO.setKeywordCatalogs(catalogVOS);

        
        //4 当前关键字下的所有属性信息
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms  attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attr_id_agg.getBuckets();
        List< SearchResultVO.AttrVO> attrVOS = new ArrayList<>();
        if (attrIdAggBuckets!=null&&attrIdAggBuckets.size()>0){
            for (Terms.Bucket attrIdAggBucket : attrIdAggBuckets) {
                SearchResultVO.AttrVO attrVO = new SearchResultVO.AttrVO();
                String attrId = attrIdAggBucket.getKeyAsString();
                attrVO.setAttrId(Long.parseLong(attrId));
                //分别获取属性的名称和值
                ParsedStringTerms attr_name_agg = attrIdAggBucket.getAggregations().get("attr_name_agg");
                String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
                attrVO.setAttrName(attrName);
                ParsedStringTerms attr_value_agg = attrIdAggBucket.getAggregations().get("attr_value_agg");
                if (attr_value_agg.getBuckets()!=null&&attr_value_agg.getBuckets().size()>0){
                    List<String> valuesList = attr_value_agg.getBuckets().stream()
                            .map(item -> item.getKeyAsString())
                            .collect(Collectors.toList());
                    // TODO 要将查询出来多个值处理成List集合
                    attrVO.setAttrValue(valuesList);
                }
                attrVOS.add(attrVO);
            }
        }
        resultVO.setKeywordAttrs(attrVOS);

        //5 分页信息
        long total = hits.getTotalHits().value;
        resultVO.setTotal(total); //设置总数 6 /5 =1
        resultVO.setPageNum(paramVO.getPageNum());//设置当前页数
        long totalPage = total % ESConstant.PRODUCT_PAGESIZE ==0 ? total/ESConstant.PRODUCT_PAGESIZE:(total/ESConstant.PRODUCT_PAGESIZE+1);
        resultVO.setTotalPages((int) totalPage); // 设置总页数
        List<Integer> navs = new ArrayList<>();
        for (int i = 1; i < totalPage; i++) {
            navs.add(i);
        }
        resultVO.setNavs(navs);
        return resultVO;
    }
}
