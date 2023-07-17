package com.msb.mall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.ware.dao.PurchaseDetailDao;
import com.msb.mall.ware.entity.PurchaseDetailEntity;
import com.msb.mall.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {
    @Autowired
    PurchaseDetailDao purchaseDetailDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        //key=guianjianzi+&status=0&wareId=1
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("purchase_id",key).or().eq("sku_id",key);
        }
        if (StringUtils.isNotEmpty(status)){
            wrapper.eq("status",status);
        }
        if (StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    /**
     * 通过purchase_id来找到，详情中的采购项实体类
     */
    @Override
    public List<PurchaseDetailEntity> getPurchaseDetailEntitiesByPurchaseId(Long purchaseId) {
        return purchaseDetailDao.selectList(
                new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", purchaseId));
    }



}