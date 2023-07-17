package com.msb.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.dto.MemberPriceDTO;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.coupon.dao.SkuFullReductionDao;
import com.msb.mall.coupon.entity.MemberPriceEntity;
import com.msb.mall.coupon.entity.SkuFullReductionEntity;
import com.msb.mall.coupon.entity.SkuLadderEntity;
import com.msb.mall.coupon.service.MemberPriceService;
import com.msb.mall.coupon.service.SkuFullReductionService;
import com.msb.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存满减折扣和会员价的相关信息 在这一个接口中完成三个表的插入
     * 从Product 服务中 product/spuinfo/save接口中传递过来
     */
    @Override
    public void saveSkuFullReduction(SkuReductionDTO dto) {
        // 5.3 保存sku满减信息 折扣，会员价格；
        // 跨服务 mall-sms:sms_sku_ladder \ sms_sku_full_reduction  sms_sku_member_price
        // 折扣
        SkuLadderEntity skuLadder = new SkuLadderEntity();
        skuLadder.setSkuId(dto.getSkuId());
        skuLadder.setFullCount(dto.getFullCount());
        skuLadder.setDiscount(dto.getDiscount());
        skuLadder.setAddOther(dto.getPriceStatus());
        if (dto.getFullCount() > 0) {
            skuLadderService.save(skuLadder);
        }
        // 满减
        SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
        BeanUtils.copyProperties(dto, skuFullReduction);
        skuFullReduction.setAddOther(dto.getPriceStatus());
        if (dto.getFullPrice().compareTo(new BigDecimal(0)) > 0){
            this.save(skuFullReduction);
        }
        //会员价格
        if (dto.getMemberPrice()!=null && dto.getMemberPrice().size()>0){
            List<MemberPriceDTO> memberPriceList = dto.getMemberPrice();
            List<MemberPriceEntity> memberPriceEntities = memberPriceList.stream()
                    .map((memberPriceDTO) -> {
                        MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                        memberPriceEntity.setSkuId(dto.getSkuId());
                        memberPriceEntity.setMemberLevelId(memberPriceDTO.getId());
                        memberPriceEntity.setMemberPrice(memberPriceDTO.getPrice());
                        memberPriceEntity.setMemberLevelName(memberPriceDTO.getName());
                        memberPriceEntity.setAddOther(dto.getPriceStatus());
                        return memberPriceEntity;
                    })
                    .collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntities);
        }

    }
}