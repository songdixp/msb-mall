package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 属性组和基本属性中间表，删除数据就能实现移除功能
     * 前端传过来 [{attr_id:1, attr_group_id；1},...]
     * @param vos 多个VO对象
     */
    @Override
    public void deleteAttrRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.stream(vos)
                .map((vo) -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(vo, relationEntity);
                    return relationEntity;
                })
                .collect(Collectors.toList());
        // 批量删除 中间表的数据
        attrAttrgroupRelationDao.removeRelationBatch(list);

    }

    /**
     * 点击确认新增之后保存前端传过来的数组对象
     * @param vos [{attr_id:11,attr_group_id:123},...]
     */
    @Override
    public void saveBatch(List<AttrGroupRelationVO> vos) {
        List<AttrAttrgroupRelationEntity> relationEntityList = vos.stream()
                .map((vo) -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(vo, relationEntity);
                    return relationEntity;
                })
                .collect(Collectors.toList());
        this.saveBatch(relationEntityList);
    }

}