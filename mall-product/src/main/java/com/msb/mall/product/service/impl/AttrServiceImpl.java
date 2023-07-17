package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.constant.ProductConstant;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.dao.AttrDao;
import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.AttrService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrResponseVO;
import com.msb.mall.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryService categoryService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    AttrGroupDao attrGroupDao;

    /**
     * 分页查询
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 前端 新增规格参数，保存实体类到数据库
     * @param attrVO 前端传过来的 View Object
     */
    @Transactional
    @Override
    public void saveAttrVO(AttrVO attrVO) {
        //1 保存基本信息
        AttrEntity attrEntity = new AttrEntity();
        // 2复制VO对象到实体类
        BeanUtils.copyProperties(attrVO,attrEntity);
        this.save(attrEntity);

        // 3 处理规格参数中的属性分组字段赋值
        if (attrVO.getAttrGroupId()!=null &&
            attrVO.getAttrType().equals(ProductConstant.Attr.ATTR_TYPE_BASE.getCode())){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
            attrAttrgroupRelationService.save(relationEntity);

        }
    }

    /**
     * 根据id key查询数据，以及分页查询
     */
    @Override
    public PageUtils queryBasePage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        wrapper.eq("attr_type","base".equalsIgnoreCase(attrType) ? 1 : 0);
        // 1 根据id获取信息
        if (catelogId !=0){
            wrapper.eq("catelog_id", catelogId);
        }else{
            System.out.println("catelogId传递不正确:"+catelogId+"  类型："+catelogId.getClass());
        }
        // 2 根据key 模糊查询信息
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("attr_id",key).or().like("attr_name",key);
        }
        //3 分页查询
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);
        // 4 查找所属分类、所属分组，所属分组需要通过中间表来查询
        List<AttrEntity> attrEntities = page.getRecords();
        List<AttrResponseVO> list = attrEntities.stream().map((attrEntity) -> {
            AttrResponseVO attrResponseVO = new AttrResponseVO();
            BeanUtils.copyProperties(attrEntity, attrResponseVO);
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVO.setCatelogName(categoryEntity.getName());
            }
            // 如果是基础属性base 才设置属性组名称字段
            if ("base".equalsIgnoreCase(attrType)){
                // 通过中间表找到所属分组 attr_group_name
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));

                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                    attrResponseVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return attrResponseVO;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    /**
     * 修改的思路和创建的思路差不多
     * 1 将attrEntity 基本信息赋值给 responseVO
     * 2 将所属分类名称查找到，赋值
     * 3 将所属分组名称查找到，赋值
     */
    @Override
    public AttrResponseVO getAttrInfo(Long attrId) {
        AttrResponseVO vo = new AttrResponseVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, vo);
        // CatelogName
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if(categoryEntity !=null){
            vo.setCatelogName(categoryEntity.getName());
        }
        // 如果是base 基本属性 才查中间表设置分组名称；销售属性没有属性分组
        if (attrEntity.getAttrType().equals(ProductConstant.Attr.ATTR_TYPE_BASE.getCode())){
            // 中间表查询 所属分组
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity !=null){
                Long attrGroupId = relationEntity.getAttrGroupId();
                if (attrGroupId!=null){
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                    vo.setGroupName(attrGroupEntity.getAttrGroupName());
                    vo.setAttrGroupId(attrGroupId); // 前端设置的所属分类字段

                }
            }
        }

        // 所属分类 [2,22,333]
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        if(catelogPath!=null){
            vo.setCatelogPath(catelogPath);
        }

        return  vo;
    }

    /**
     * 规格参数 点击修改，提交更新
     * 1 更新基本数据
     * 2 更新中间表对象
     * 3 更新中间表数据库
     */
    @Transactional
    @Override
    public void updateBaseAttr(AttrVO attrVO) {
        // 1 更新 attrEntity的基本属性
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType().equals(ProductConstant.Attr.ATTR_TYPE_BASE.getCode())){
            // 2 修改中间表对象 attr_id attr_group_id 两个字段
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            System.out.println("更新之前："+relationEntity);
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attrVO.getAttrGroupId()); // 只需要更新 VO中多出来的字段

            // 3 更新数据库关联表 attr_group 属性分组的数据
            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (count>0){
                // 有数据，需要把关联表数据也更新掉
                attrAttrgroupRelationDao.update(
                        relationEntity,
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrEntity.getAttrId()));
            }else {
                //直接插入数据就行
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 如果是基本属性base 接口，那么删除基本属性和关联中间表的字段信息
     */
    @Override
    public void removeByIdsDetail(Long[] attrIds) {
        for (Long attrId : attrIds) {
            // 根据属性id查找到attrEntity
            AttrEntity attrEntity = this.getById(attrId);
            if (attrEntity.getAttrType().equals(ProductConstant.Attr.ATTR_TYPE_BASE.getCode())){
                //删除中间表信息
                attrAttrgroupRelationDao.delete(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            }else{
                System.out.println("销售属性不用删除中间表");
            }
        }
        //删除基本属性表 attr 的记录
        this.removeByIds(Arrays.asList(attrIds));
    }

    /**
     * 在中间表 attr_group_relation，通过 attr_group_id 找到attr_id
     * 在通过 attr_id找到attrEntity实体类组装成列表返回
     * @param attrGroupId 属性分组id
     * @return attr实体类列表集合
     */
    @Override
    public List<AttrEntity> getAttrByGroupId(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationList = attrAttrgroupRelationDao.
                selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));

        List<AttrEntity> attrEntityList = relationList.stream()
                .map((entity) -> this.getById(entity.getAttrId()))
                .filter((entity) -> entity != null)
                .collect(Collectors.toList());
        return attrEntityList;
    }

    /**
     * 1. 通过分组属性id查询到当前的分类
     * 2. 通过当前分类id查询到所有的规格属性
     * 3. 通过分组属性id查询所有中间表的规格属性id
     * 4. 做排除功能
     * @param params
     * @param attrGroupId
     */
    @Override
    public PageUtils queryAttrNoRelation(Map<String, Object> params, Long attrGroupId) {
        // 1 通过传过来分组id 找到当前分类ID
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2 通过分类id 在attrGroup表中找到所有的组 groups 拿到所有group的id列表
        List<AttrGroupEntity> attrGroupList = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Object> attrGroupIdsList = attrGroupList.stream()
                .map((attrGroup) -> attrGroup.getAttrGroupId())
                .collect(Collectors.toList());
        // 3通过中间表中，找到所有 group_id 对应的attr_id
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdsList));
        List<Long> attrIdsRelationList = relationEntities.stream()
                .map((relationEntity) -> relationEntity.getAttrId())
                .collect(Collectors.toList());
        // 4 查找所有当前分类所有的attr_id 不在 中间表中的attr_id 就是自己想要的id
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", catelogId)
                //基本信息类型，销售类型不查询
                .eq("attr_type",ProductConstant.Attr.ATTR_TYPE_BASE.getCode());
        if (attrIdsRelationList.size() > 0){
            wrapper.notIn("attr_id", attrIdsRelationList);
        }
        // 5 封装 查询条件 id 和 规格参数名称
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("attr_id", key).or().like("attr_name",key);
        }
        // 6 封装PageUtils 返回
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);
        return  new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchTypeIds(List<Long> attr_ids) {
        List<AttrEntity> attrEntities = this.list(new QueryWrapper<AttrEntity>()
                .in("attr_id", attr_ids).eq("search_type", 1));
        return attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());

    }
}