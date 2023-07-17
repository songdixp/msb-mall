package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.AttrService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrgroupWithAttrsVO;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key"); // 前端出过来的关键字
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if (catelogId == 0) {
            List<CategoryEntity> categoryEntities = categoryService.list();

            System.out.println("打印所有的分类"+categoryEntities);
            // 如果是根种类就查询所有
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
            // this.queryPage(params);
        }
        // 不等于0 ，就根据id来查询
        queryWrapper.eq("catelog_id", catelogId);
        // 封装page对象
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    /**
     * 通过 分类id 获取到所有的属性组
     * 拿到属性组id 通过中间表获取到所有的attr_id
     */
    @Override
    public List<AttrgroupWithAttrsVO> getAttrgroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrgroupWithAttrsVO> voList = attrGroupEntities.stream()
                .map((attrGroupEntity) -> {
                    AttrgroupWithAttrsVO vo = new AttrgroupWithAttrsVO();
                    BeanUtils.copyProperties(attrGroupEntity,vo);
                    //根据属性组找到所有的属性id 其中attrService中已经实现了该方法
                    List<AttrEntity> attrEntities = attrService.getAttrByGroupId(attrGroupEntity.getAttrGroupId());
                    vo.setAttrs(attrEntities);
                    return vo;
                })
                .collect(Collectors.toList());
        return voList;
    }

    /**
     *  通过连表查询找到属性组对应的属性id和值
     */
    @Override
    public List<SpuItemGroupAttrVo> getAttrgroupBySpuId(Long spuId, Long catalogId) {

        return attrGroupDao.getAttrgroupBySpuId(spuId, catalogId);
    }


}