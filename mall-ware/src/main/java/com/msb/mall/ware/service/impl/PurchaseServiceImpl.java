package com.msb.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.constant.WareConstant;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.ware.dao.PurchaseDao;
import com.msb.mall.ware.entity.PurchaseDetailEntity;
import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.service.PurchaseDetailService;
import com.msb.mall.ware.service.PurchaseService;
import com.msb.mall.ware.service.WareSkuService;
import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;
import com.msb.mall.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询出状态为 新建 或者 已分配 的采购单
     */
    @Override
    public PageUtils queryUnreceived(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        // List<Integer> list = Arrays.asList(0,1);
        wrapper.in("status", 0,1);// 查询出 新建0 已分配的采购单的数据
        // wrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 批量操作-->合并整理->点击确定
     * 1、先处理采购单详情的信息
     *  1.1将传过来的采购单详情id，找到实体类，然后设置上传过来的采购单id  purchaseId
     *  1.2状态更新成为 已分配
     * 2、再处理采购单信息
     *  2.1更新状态：已分配1， 更新仓库id、创建日期、更新日期
     */
    @Transactional
    @Override
    public Integer merge(MergeVO vo) {
        Long purchaseId = vo.getPurchaseId();
        if (purchaseId==null){
            // 判断采购单id为null, 那么创建一个新的采购单，获取实体类的采购单id
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            Long randomId = System.currentTimeMillis();
            purchaseEntity.setAssigneeId(randomId); //设置分配人id
            purchaseEntity.setAssigneeName("系统分配人"); // 分配人名
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setPriority(1); // 设置默认优先级 1
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());// 设置采购单状态：已分配1
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();  //重新赋值采购订单id
        }
        // 采购单id不为null，需要增加判断，只有新建0和已分配1 状态才能合单
        PurchaseEntity purchaseEntity1 = this.getById(purchaseId);
        if(purchaseEntity1.getStatus()>WareConstant.PurchaseStatusEnum.RECEIVE.getCode()){
            // 不能合单
            return -1;
        }
        final Long finalPurchaseId = purchaseId;
        // 更新采购单详情表 关联采购单id，设置状态
        List<Long> purchaseDetailIds = vo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailIds.stream()
                .map(purchaseDetailId -> {
                    // PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                    PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(purchaseDetailId);
                    System.out.println("purchaseDetailEntity = " + purchaseDetailEntity);
                    // 设置id，更新类的时候通过这个id来更新
                    // purchaseDetailEntity.setId(purchaseDetailId);
                    // 设置采购单id
                    purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                    if (purchaseDetailEntity.getStatus()<WareConstant.PurchaseStatusEnum.RECEIVE.getCode()){
                        //采购单的详情项的状态 <2 也就是新建|分配 才设置成新建，大于不设置状态
                        purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
                    }
                    return purchaseDetailEntity;
                })
                .filter(detailEntity->{
                    System.out.println("newPurchaseDetailEntity = " + detailEntity);
                    // 上面是new出来的详情项，不是数据库中的数据项
                    // PurchaseDetailEntity detailEntity = purchaseDetailService.getById(newPurchaseDetailEntity);
                    return detailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode()
                            || detailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
                })
                .collect(Collectors.toList());
        // 批量更新采购详情表记录
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        // 更新一下采购单的更新时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
        return 1;

    }

    /**
     * 领取采购单
     * 1 更改采购单状态
     * 2 更改采购项的状态
     */
    @Override
    public void receive(List<Long> purchaseIds) {

        List<PurchaseEntity> purchaseEntities = purchaseIds.stream()
                .map(this::getById)
                .filter(purchaseEntity ->
                        purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                        purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(purchaseEntity -> {
                    purchaseEntity.setUpdateTime(new Date());//设置时间
                    //更新：已领取
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return purchaseEntity;
                })
                .collect(Collectors.toList());
        //批量更新采购单表
        this.updateBatchById(purchaseEntities);

        for (Long purchaseId : purchaseIds) {
            List<PurchaseDetailEntity> purchaseDetailsList = purchaseDetailService.getPurchaseDetailEntitiesByPurchaseId(purchaseId);
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailsList.stream()
                    .map(purchaseDetailEntity -> {
                        // PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                        // purchaseDetail.setId(purchaseDetailEntity.getId());
                        //更新状态：已分配2
                        purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                        return purchaseDetailEntity;
                    })
                    .collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        }

    }

    /**
     * 完成采购
     * 1.处理采购单、采购项表中的状态，有逻辑判断
     * 2.处理商品库存表中的数量
     * 3.远程查询skuName 展示在商品库存页面
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVO vo) {
        boolean flag= true; //全部采购项状态，默认全部成功

        List<PurchaseItemDoneVO> items = vo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntityList = new ArrayList<>();
        //采购项
        for (PurchaseItemDoneVO item : items) {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(item.getItemId()); // 设置采购项id 批量更新依据这个来
            if (item.getStatus()==WareConstant.PurchaseDetailStatusEnum.FINISH.getCode()){
                //全部已完成，修改状态
                purchaseDetail.setStatus(item.getStatus());
                //入库
                PurchaseDetailEntity purchaseDetail1 = purchaseDetailService.getById(item.getItemId());
                //传递skuId wareId skuNum（Stock)
                wareSkuService.addStock(purchaseDetail1.getSkuId(), purchaseDetail1.getWareId(), purchaseDetail1.getSkuNum());
            } else if (item.getStatus()==WareConstant.PurchaseDetailStatusEnum.FAILED.getCode()) {
                //失败
                flag = false;
                purchaseDetail.setStatus(item.getStatus());
            }else {
                System.out.println("状态不是完成|失败，而是："+item.getStatus());
            }
            purchaseDetailEntityList.add(purchaseDetail);
        }
        //批量更新采购项
        purchaseDetailService.updateBatchById(purchaseDetailEntityList);

        //采购单更新
        Long purchaseId = vo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode()
                :WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }

}