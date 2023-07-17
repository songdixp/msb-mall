package com.msb.mall.search.vo;

import com.msb.common.dto.es.SkuESModel;
import lombok.Data;

import java.util.List;

/**
 * 封装检索后的响应信息
 */
@Data
public class SearchResultVO {

    private List<SkuESModel> products; // 查询到的所有的商品信息 满足条件
    // 分页信息
    private Integer pageNum; // 当前页
    private Long total;  // 总的记录数
    private Integer totalPages; // 总页数
    private List<Integer> navs ; // 分页的页码

    // 当前查询的所有的商品涉及到的所有的品牌信息
    private List<BrandVO> keywordBrands;
    // 当前查询的所有的商品涉及到的所有的属性信息
    private List<AttrVO> keywordAttrs;

    // 当前查询的所有商品涉及到的所有的类别信息
    private List<CatalogVO> keywordCatalogs;


    @Data
    public static class CatalogVO{
        private Long catalogId;
        private String catalogName;
    }

    /**
     * 品牌的相关信息
     */
    @Data
    public static class BrandVO{
        private Long brandId; // 品牌的编号
        private String brandName; // 品牌的名称
        private String brandImg; // 品牌的图片
    }

    @Data
    public static class AttrVO{
        private Long attrId; // 属性的编号
        private String attrName; // 属性的名称
        private List<String> attrValue; // 属性的值
    }

}
