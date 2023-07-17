package com.msb.mall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.dto.SkuHasStockDTO;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.common.utils.R;
import com.msb.mall.ware.dao.WareSkuDao;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.feign.ProductFeignService;
import com.msb.mall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        //skuId=&wareId=
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        if (StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_d",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    /**
     * 处理入库，主要修改库存数量，数据库表字段stock
     * 1 如果传过来的skuId 数据库中存在那么更新 wareId 和skuNum
     * 2 不存在，创建一条数据
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities==null){
            // 不存在，新建一条记录
            WareSkuEntity wareSkuEntity1 = new WareSkuEntity();
            wareSkuEntity1.setSkuId(skuId);
            wareSkuEntity1.setWareId(wareId);
            wareSkuEntity1.setStock(skuNum);
            wareSkuEntity1.setStockLocked(0);
            //远程查询 skuName
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");
                wareSkuEntity1.setSkuName((String) map.get("skuName"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 插表
            wareSkuDao.insert(wareSkuEntity1);
        }else{
            //编写SQL语句
            wareSkuDao.addStock(skuId, wareId,skuNum);
        }
    }

    /**
     * 获取每个sku对应的库存
     */
    @Override
    public List<SkuHasStockDTO> hasStockBySkuIds(List<Long> skuIds) {
        /* List<WareSkuEntity> wareSkuEntities = this.list(
                new QueryWrapper<WareSkuEntity>().in("sku_id", skuIds));
        List<SkuHasStockDTO> skuHasStockDTOS = wareSkuEntities.stream()
                .map(wareSkuEntity -> {
                    SkuHasStockDTO skuHasStockDTO = new SkuHasStockDTO();
                    Integer stock = wareSkuEntity.getStock();
                    Integer stockLocked = wareSkuEntity.getStockLocked();
                    skuHasStockDTO.setHasStock(stock - stockLocked > 0);
                    skuHasStockDTO.setSkuId(wareSkuEntity.getSkuId());
                    return skuHasStockDTO;
                })
                .collect(Collectors.toList()); */
        List<SkuHasStockDTO> skuHasStockDTOS = skuIds.stream()
                .map(skuId -> {
                    Long stock = wareSkuDao.getSkuStockBySkuId(skuId);
                    SkuHasStockDTO skuHasStockDTO = new SkuHasStockDTO();
                    skuHasStockDTO.setHasStock(stock > 0);
                    skuHasStockDTO.setSkuId(skuId);
                    return skuHasStockDTO;
                })
                .collect(Collectors.toList());

        return skuHasStockDTOS;

    }
}