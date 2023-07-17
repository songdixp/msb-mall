package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.constant.ProductConstant;
import com.msb.common.dto.MemberPriceDTO;
import com.msb.common.dto.SkuHasStockDTO;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.dto.SpuBoundsDTO;
import com.msb.common.dto.es.SkuESModel;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.common.utils.R;
import com.msb.mall.product.dao.SpuInfoDao;
import com.msb.mall.product.entity.*;
import com.msb.mall.product.feign.CouponFeignService;
import com.msb.mall.product.feign.ElasticSearchSaveService;
import com.msb.mall.product.feign.WareSkuFeignService;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    AttrService attrService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    // 远程调用 会员折扣，满减信息
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    BrandService brandService;
    @Autowired
    WareSkuFeignService wareSkuFeignService;

    @Autowired
    ElasticSearchSaveService elasticSearchSaveService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存Spu信息
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVO vo) {
        // 1 保存spu_info表中的基本信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setPublishStatus(0); // 新建状态
        this.save(spuInfoEntity);

        // 2 保存spu_info_desc表中的信息 商品详情图片地址
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> descript = vo.getDecript();// 把商品详情字段拿到，拼接成一个字符串保存到实体类
        String images_url = String.join(",", descript);
        // 2.1 这里这里需要设置 spu_id 因为字段id不是自增的
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(images_url);
        spuInfoDescService.save(spuInfoDescEntity);

        // 3 保存spu_images信息 商品图集 images_url的链接集合
        List<String> images = vo.getImages();
        List<SpuImagesEntity> spuImagesEntityList = images.stream()
                .map((image_url_str) -> {
                    SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                    spuImagesEntity.setSpuId(spuInfoEntity.getId());
                    spuImagesEntity.setImgUrl(image_url_str);
                    return spuImagesEntity;
                })

                .collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntityList);
        // 4 保存规格参数信息 product_attr_value 表，也就是第二步：规格参数 商品和属性的管子
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream()
                .map((baseAttr) -> {
                    ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                    productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                    productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                    AttrEntity attrEntity = attrService.getById(baseAttr.getAttrId());
                    productAttrValueEntity.setAttrName(attrEntity.getAttrName());
                    productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                    productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                    return productAttrValueEntity;
                })
                .collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);


        // 5 保存 当前的spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus.size()>0){
            for (Skus sku : skus) {
                // 5.1 保存skuInfoEntity基本信息
                // 基本字段：skuName skuTitle skuSubtitle price
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                // 其他字段需要从 spuInfo里面拉过来
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L); // saleCount 给一个默认值0
                // 默认图片
                List<Images> imagesList = sku.getImages();
                String defaultImageUrl = "";
                for (Images image : imagesList) {
                    if (image.getDefaultImg()==1){
                        defaultImageUrl = image.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImageUrl);
                skuInfoService.save(skuInfoEntity);

                // 5.2 保存sku图片信息
                List<SkuImagesEntity> skuImagesEntitiesList = imagesList.stream()
                        .map((image) -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        })// TODO: defaultImage为空的图片不需要保存，感觉这里逻辑错误了
                        .filter(image->image.getDefaultImg() ==1)
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntitiesList);

                // 5.4 保存sku销售属性 sku_sale_attr_value
                List<Attr> skuSaleAttrValues = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrValues.stream()
                        .map((skuSaleAttrValue) -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(skuSaleAttrValue, skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                            return skuSaleAttrValueEntity;
                        })
                        .collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                // 远程调用feign
                // 5.3 保存sku满减信息 折扣，会员价格；
                // 跨服务 mall-sms:sms_sku_ladder \ sms_sku_full_reduction  sms_sku_member_price
                SkuReductionDTO dto = new SkuReductionDTO();
                BeanUtils.copyProperties(sku, dto);
                dto.setSkuId(skuInfoEntity.getSkuId());
                //设置会员价
                if(sku.getMemberPrice() != null && sku.getMemberPrice().size()>0){
                    List<MemberPriceDTO> memberPriceDTOS = sku.getMemberPrice().stream()
                            .map(memberPrice -> {
                                MemberPriceDTO memberPriceDTO = new MemberPriceDTO();
                                BeanUtils.copyProperties(memberPrice, memberPriceDTO);
                                return memberPriceDTO;
                            })
                            .collect(Collectors.toList());
                    dto.setMemberPrice(memberPriceDTOS);
                }

                R r = couponFeignService.saveFullReductionInfo(dto);
                if (!r.get("code").equals(0)){
                    log.error("保存满减折扣会员价错误，错误信息：");
                    R.error(502,"R, 保存满减折扣会员价错误");
                }


            }
        }

        // 6 保存 spu积分信息 sms_spu_bounds
        Bounds voBounds = vo.getBounds();
        SpuBoundsDTO boundsDTO = new SpuBoundsDTO();
        BeanUtils.copyProperties(voBounds, boundsDTO);
        boundsDTO.setSpuId(spuInfoEntity.getId());
        if (voBounds.getBuyBounds().compareTo(new BigDecimal(0))>0
                || voBounds.getGrowBounds().compareTo(new BigDecimal(0))>0){
            R r = couponFeignService.saveSpuBounds(boundsDTO);
            if (!r.get("code").equals(0)){
                log.error("保存会员积分 spu_bounds错误，错误信息：");
                R.error(503,"R, 保存会员积分 spu_bounds错误");
            }
        }



    }

    /**
     * 根据条件进行组合搜索
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        //id  名称 描述 模糊查询
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("id",key)
                    .or().like("spu_name",key)
                    .or().like("spu_description",key);
        }
        //status
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        // catalogId
        String catalogId = (String) params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        // brandId
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),wrapper);
        // 根据查询到的分页信息，在查询出分类名称、品牌名称
        List<SpuInfoVO> spuInfoVOList = page.getRecords().stream()
                .map(spuInfoEntity -> {
                    Long catalogId1 = spuInfoEntity.getCatalogId();
                    Long brandId1 = spuInfoEntity.getBrandId();
                    SpuInfoVO spuInfoVO = new SpuInfoVO();
                    BeanUtils.copyProperties(spuInfoEntity, spuInfoVO);
                    spuInfoVO.setCatelogName(categoryService.getById(catalogId1).getName());
                    spuInfoVO.setBrandName(brandService.getById(brandId1).getName());
                    return spuInfoVO;
                })
                .collect(Collectors.toList());

        IPage<SpuInfoVO> iPage = new Page<>();
        iPage.setRecords(spuInfoVOList);
        iPage.setCurrent(page.getCurrent());
        iPage.setPages(page.getPages());
        iPage.setSize(page.getSize());
        iPage.setTotal(page.getTotal());

        return new PageUtils(iPage);
    }

    /**
     * 根据id找到相关信息，封装到model对象中
     * 封装model存储到ES中，远程调用
     * 更新spu状态字段：已上架
     */
    @Override
    public void upBySpuId(Long spuId) {
        //根据spuId获取sku
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.getSkuBySpuId(spuId);
        // 1 获取规格参数 attrs 根据spuId
        List<SkuESModel.Attrs> attrsModel = getAttrsModel(spuId);
        // 2 远程调用 wms_ware_sku信息中的库存信息{1:true,2:false...}
        Map<Long, Boolean> skuHasStockMap = skuHasStock(
                skuInfoEntityList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList()));

        //3 封装SkuESModel对象
        List<SkuESModel> skuESModelList = skuInfoEntityList.stream().map(item -> {
                SkuESModel skuESModel = new SkuESModel();
                //属性设置 复制了 skuId\spuId\salecount\catelogid\brandid
                BeanUtils.copyProperties(item, skuESModel);
                skuESModel.setSubTitle(item.getSkuSubtitle());
                skuESModel.setSkuPrice(item.getPrice());
                skuESModel.setSkuImg(item.getSkuDefaultImg());

                //设置库存字段 1 hasStock 调用ware服务中的表
                if (skuHasStockMap==null){
                    skuESModel.setHasStock(false);
                }else{
                    //有库存就设置每个sku的库存状态 true 或 false
                    skuESModel.setHasStock(skuHasStockMap.get(item.getSkuId()));
                }

                //设置attrs字段
                skuESModel.setAttrs(attrsModel);
                return skuESModel;
            })
            .collect(Collectors.toList());

        // 4 远程调用mall-search，将skuESModel对象存储到ES服务上，更改publish_status状态
        R r = elasticSearchSaveService.productUpStatus(skuESModelList);
        if(r.get("code").equals(0)){
            //更新spu_info publish_status 为1
            System.out.println("修改上架状态...");
            baseMapper.updateSpuPublishStatus(spuId, ProductConstant.PublishStatus.UP.getCode());
        }

    }

    /**
     * 根据skuIds列表，来判断sku是否有库存
     * @return 返回map集合，每个sku是否有库存
     */
    private Map<Long, Boolean> skuHasStock(List<Long> skuIds){
        List<SkuHasStockDTO> skuHasStockDTOS=null;
        if (skuIds==null){
            return null;
        }
        try{
            skuHasStockDTOS = wareSkuFeignService.hasStockBySkuIds(skuIds);
            System.out.println("skuHasStockDTOS =======>>> " + skuHasStockDTOS);
            Map<Long, Boolean> skuHasStockMap = skuHasStockDTOS.stream()
                    .collect(Collectors.toMap(SkuHasStockDTO::getSkuId, SkuHasStockDTO::getHasStock));
            System.out.println("skuHasStockMap = " + skuHasStockMap);
            return skuHasStockMap;
        }catch (Exception e){
            log.error("skuHasStockMap获取库存失败：",e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 封装方法：根据spuId找到符合条件的 规格参数 attrs
     * 过滤 search_type=1 的spuAttrs
     * 转换 ProductAttrValueEntity -> SkuESModel.Attrs
     */
    private List<SkuESModel.Attrs> getAttrsModel(Long spuId) {
        // 1.1 product_attr_value表中存储了spu相关的attrs
        List<ProductAttrValueEntity> spuAttrs = productAttrValueService.getAttrsBySpuId(spuId);
        // 1.2 筛选条件 先收集成id列表
        List<Long> attr_ids = spuAttrs.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        // 1.3 去attr表中查询attr的search_type 为1的才, 得到可以检索的attrIds
        List<Long> attrIds = attrService.selectSearchTypeIds(attr_ids);
        // 1.4 得到符合条件的spuAttrs
        List<SkuESModel.Attrs> attrsModel = spuAttrs.stream()
                .filter(item -> attrIds.contains(item.getAttrId()))
                .map(item->{
                    SkuESModel.Attrs attrs = new SkuESModel.Attrs();
                    /* attrs.setAttrId(item.getAttrId());
                    attrs.setAttrName(item.getAttrName());
                    attrs.setAttrValue(item.getAttrValue()); */
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());
        return attrsModel;
    }

}