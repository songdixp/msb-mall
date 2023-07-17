package com.msb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.CategoryDao;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import com.msb.mall.product.vo.Catalog3VO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;
    String redisKey = "catalogJSON";

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> queryPageTree(Map<String, Object> params) {
        // 1 查询所有的商品分类信息
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2 将商品分类信息拆解为树形结构
        // 2.1 遍历出所有的大类 parent_cid=0
        List<CategoryEntity> list = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0) // 过滤出parent_cid=0的元素
                // 2.2 一级大类找到所有的小类 递归查询，例如找到手机 0 那么还要找到下面的子类、品牌，所以怎样存储这个数据
                .map(categoryEntity -> {
                    categoryEntity.setChildrens(getCategoryChildrens(categoryEntity, categoryEntities)); // 添加子类型，这里需要定义方法
                    return categoryEntity;
                })
                // 2.3 进行数据分类排序，热门分类在前，冷门在后 entity1 比较参数
                /*.sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                })*/
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort())))
                // 2.4 收集数据,转换成List集合
                .collect(Collectors.toList());
        return list;
    }


    /**
     * 逻辑删除
     *
     * @param catIds Long 类型的数组 传入catId
     */

    @Override
    public void removeCategoryByIds(List<Long> catIds) {
        System.out.println("逻辑删除的id：" + catIds.toString());
        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 查找大类下面的小类，递归获取
     *
     * @param categoryEntity   某个大类
     * @param categoryEntities 所有的类别数据
     * @return 返回 List 集合 包含所有小类
     */
    private List<CategoryEntity> getCategoryChildrens(CategoryEntity categoryEntity,
                                                      List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream()
                .filter(subCategoryEntity -> {
                    // 根据大类找到直属小类 子类的父id == 当前大类的 id
                    // long 类型比较不在-128 127 之间数据 是Long对象
                    return subCategoryEntity.getParentCid().equals(categoryEntity.getCatId());
                })
                // 找到小类之后，如果还有小类，就找小类的小类，这里就用递归：自己调用自己
                /*
                * .map(subCategoryEntity->{
                    subCategoryEntity.setChildrens(getCategoryChildrens(subCategoryEntity, categoryEntities));
                    return subCategoryEntity;
                })*/
                .peek(subCategoryEntity -> subCategoryEntity.setChildrens(getCategoryChildrens(subCategoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort())))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 通过id 找到父节点，递归查找
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> pathList = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, pathList);
        Collections.reverse(parentPath);
        return pathList.toArray(new Long[parentPath.size()]);
    }
    private List<Long> findParentPath(Long catelogId, List<Long> pathList) {
        pathList.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), pathList);
        }
        return pathList;
    }

    /**
     * 根据所有分类数据结果
     * 通过parent_cid来过滤出各级分类的数据
     * 因此，这里不是查询而是根据查询出来的结果进行过滤
     */
    public List<CategoryEntity> queryPageByParentCid(List<CategoryEntity> allCategorysList, Long parentCid){
        List<CategoryEntity> catalogLevelList = allCategorysList.stream()
                .filter(item -> item.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
        return catalogLevelList;
    }

    /**
     * 级联更新分类名称数据，CategoryBrandRelation表中的 catelog_name字段
     * 操作多表开启事务
     * CacheEvict: 更新 操作的时候清除缓存 category::getCategoryLevel1
     */
    // @CacheEvict(value = {"category"}, key = "'getCategoryLevel1'")
    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getCategoryLevel1'"),
            @CacheEvict(cacheNames = {"category"}, key = "'getCatalog2JSON'")
    })
    // @CacheEvict(cacheNames = {"category"}, allEntries = true) 删除category这个缓存下的所有数据
    @Transactional
    @Override
    public void updateDetails(CategoryEntity category) {
        // 1 先更新 category表自己的name
        this.updateById(category);
        if (StringUtils.isNotEmpty(category.getName())) {
            categoryBrandRelationService.updateCatelogName(category.getCatId(), category.getName());
        }
    }


    /**
     * 获取 一级分类
     * Cacheable 当前方法返回结果是要缓存的，缓存有就拿缓存的
     * 缓存没有数据，就执行方法，然后缓存起来
     * 名称起到分区的作用，一般按照业务来区分
     */
    @Cacheable(cacheNames = {"category","product"}, key = "#root.method.name") // category有缓存，product也有缓存
    @Override
    public List<CategoryEntity> getCategoryLevel1() {
        return baseMapper.getCategoryLevel1();

    }



    /**
     * 直接查询数据库，利用cache完成缓存操作
     */
    @Cacheable(cacheNames = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2VO>> getCatalog2JSON() {
        //一次性查询出所有的分类
        List<CategoryEntity> categoriesList = baseMapper.selectList(new QueryWrapper<>());
        //获取 parent_cid 第一层级的 cat_id 通过这个id来拿到二级的catelog
        List<CategoryEntity> category1Entities = this.queryPageByParentCid(categoriesList, 0L);
        //查询出一级分类、二级分类、三级分类
        Map<String, List<Catalog2VO>> catalog2JSONMap = category1Entities.stream().collect(
                Collectors.toMap(
                        categoryEntity->categoryEntity.getCatId().toString(),
                        categoryEntity->{
                            Long cat1Id = categoryEntity.getCatId();
                            //获取 2级分类对象列表，通过1级的catId 过滤得到（优化）
                            List<CategoryEntity> catelog2Entities = this.queryPageByParentCid(categoriesList, cat1Id);
                            List<Catalog2VO> catalog2VOS = null;
                            if(catelog2Entities!=null){
                                catalog2VOS = catelog2Entities.stream()
                                        .map(category2Entity -> {
                                            // 给 catalog2VO 对象赋值
                                            Catalog2VO catalog2VO = new Catalog2VO();
                                            Long cat2Id = category2Entity.getCatId();
                                            catalog2VO.setCatalog1Id(cat1Id.toString());
                                            catalog2VO.setName(category2Entity.getName());
                                            catalog2VO.setId(cat2Id.toString());
                                            // 查询出来3级分类对象 传入 parentCid 为二级分类的catId
                                            List<CategoryEntity> catelog3Entities = this.queryPageByParentCid(categoriesList,cat2Id);
                                            if (catelog3Entities != null) {
                                                List<Catalog3VO> catalog3VOS = catelog3Entities.stream()
                                                        .map(category3Entity -> {
                                                            String cat3Id = category3Entity.getCatId().toString();
                                                            Catalog3VO catalog3VO = new Catalog3VO();
                                                            catalog3VO.setCatalog2Id(cat2Id.toString());
                                                            catalog3VO.setId(cat3Id);
                                                            catalog3VO.setName(category3Entity.getName());
                                                            return catalog3VO;
                                                        })
                                                        .collect(Collectors.toList());
                                                catalog2VO.setCatalog3List(catalog3VOS);
                                            }
                                            return catalog2VO;
                                        }).collect(Collectors.toList());
                                return catalog2VOS;
                            }
                            return catalog2VOS;
                        }
                ));
        return catalog2JSONMap;
    }

    /**
     * 从缓存服务器redis中获取数据--手动处理缓存 redis
     * 缓存穿透处理：查询null， catalogJSON:0; 5s过期；
     * 缓存雪崩：查询到数据库数据，设置随机的过期时间0-10小时
     */
    // @Override
    @Deprecated
    public Map<String, List<Catalog2VO>> getCatalog2JSONDeprecated() {
        ValueOperations<String, String> operations  = stringRedisTemplate.opsForValue();
        String catalogJSON = operations.get(redisKey);
        if (StringUtils.isEmpty(catalogJSON)){
            //如果缓存中没有，就去数据库中查询
            Map<String, List<Catalog2VO>> catalog2JSONForDb = this.getCatalog2JSONDBWithRedisson();
            if(catalog2JSONForDb==null){
                //缓存穿透处理：数据库中查询出来 null
                operations.set(redisKey,"0", 5L, TimeUnit.SECONDS);
            }
            //数据库中查询出数据，往缓存服务器中存放一份
            //缓存雪崩处理，设置随机的过期时间
            String catalogJSONString = JSON.toJSONString(catalog2JSONForDb);
            long random = new Random().nextInt(10)+5;
            operations.set(redisKey,catalogJSONString, random, TimeUnit.MINUTES);

            return catalog2JSONForDb;
        }
        //命中 就转成Map对象
        Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});
        return stringListMap;
    }

    /**
     * 利用redisson实现分布式锁
     */
    @Deprecated
    public  Map<String, List<Catalog2VO>> getCatalog2JSONDBWithRedisson() {
        //锁的命名粒度要细 区分开
        String lock = "Catalog2JSON-lock";
        //获取锁
        RLock rLock = redissonClient.getLock(lock);
        Map<String, List<Catalog2VO>> data = null;
        try {
            //上锁
            rLock.lock();
            data = getCatelogDataForDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            //释放锁
            rLock.unlock();
        }
        return data;

    }

    /**
     * redis 手动分布式锁实现数据库查询和上锁
     */
    @Deprecated
    public  Map<String, List<Catalog2VO>> getCatalog2JSONDBWithRedis() {
        String lock = "lock";
        String uuid = UUID.randomUUID().toString();
        // SetNX 没有lock就设置uuid锁，有的话就不做操作；
        // 在设置锁的时候指定过期时间，多一分保证锁一定存在过期时间，防止 expire之前就中断的情况；也保证业务执行时间过长导致删除其他锁的可能
        Boolean setLock = stringRedisTemplate.opsForValue().setIfAbsent(lock, uuid, 10, TimeUnit.MINUTES);
        if(Boolean.TRUE.equals(setLock)){
            //过期时间保证了查询数据库异常，导致没有释放锁产生的问题
            // stringRedisTemplate.expire(lock,10,TimeUnit.SECONDS);
            //加锁成功
            Map<String, List<Catalog2VO>> data = null;
            try {
                data = getCatelogDataForDb();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                //找到的是自己的锁，防止删除别人的锁
                String scripts = "if redis.call('get',KEYS[1]) == ARGV[1]  then return redis.call('del',KEYS[1]) else  return 0 end ";
                // lua脚本删除，保证 查询和删除操作的原子性
                stringRedisTemplate.execute(new DefaultRedisScript<>(scripts,Long.class),
                        Arrays.asList(lock),uuid);
            }
            return data;
        }else{
            // 加锁失败，休眠，然后重试
            // Thread.sleep(1000);
            return getCatalog2JSONDBWithRedis();
        }
    }

    /**
     * 从数据库中查询操作
     * 查询缓存，如果有直接返回缓存数据
     * 查询数据库，查询到之后放到redis缓存一份
     */
    @Deprecated
    private Map<String, List<Catalog2VO>> getCatelogDataForDb() {
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String catalogJSON = operations.get(redisKey);
        if (StringUtils.isNotEmpty(catalogJSON)){
            //再查询一次缓存，如果命中返回
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});
        }
        //一次性查询出所有的分类
        List<CategoryEntity> categoriesList = baseMapper.selectList(new QueryWrapper<>());
        //获取 parent_cid 第一层级的 cat_id 通过这个id来拿到二级的catelog
        List<CategoryEntity> category1Entities = this.queryPageByParentCid(categoriesList, 0L);

        Map<String, List<Catalog2VO>> catalog2JSONMap = category1Entities.stream().collect(
                Collectors.toMap(
                        categoryEntity->categoryEntity.getCatId().toString(),
                        categoryEntity->{
                            Long cat1Id = categoryEntity.getCatId();
                            //获取 2级分类对象列表，通过1级的catId 过滤得到（优化）
                            List<CategoryEntity> catelog2Entities = this.queryPageByParentCid(categoriesList, cat1Id);
                            List<Catalog2VO> catalog2VOS = null;
                            if(catelog2Entities!=null){
                                catalog2VOS = catelog2Entities.stream()
                                        .map(category2Entity -> {
                                            // 给 catalog2VO 对象赋值
                                            Catalog2VO catalog2VO = new Catalog2VO();
                                            Long cat2Id = category2Entity.getCatId();
                                            catalog2VO.setCatalog1Id(cat1Id.toString());
                                            catalog2VO.setName(category2Entity.getName());
                                            catalog2VO.setId(cat2Id.toString());
                                            // 查询出来3级分类对象 传入 parentCid 为二级分类的catId
                                            List<CategoryEntity> catelog3Entities = this.queryPageByParentCid(categoriesList,cat2Id);
                                            if (catelog3Entities != null) {
                                                List<Catalog3VO> catalog3VOS = catelog3Entities.stream()
                                                        .map(category3Entity -> {
                                                            String cat3Id = category3Entity.getCatId().toString();
                                                            Catalog3VO catalog3VO = new Catalog3VO();
                                                            catalog3VO.setCatalog2Id(cat2Id.toString());
                                                            catalog3VO.setId(cat3Id);
                                                            catalog3VO.setName(category3Entity.getName());
                                                            return catalog3VO;
                                                        })
                                                        .collect(Collectors.toList());
                                                catalog2VO.setCatalog3List(catalog3VOS);
                                            }
                                            return catalog2VO;
                                        }).collect(Collectors.toList());
                                return catalog2VOS;
                            }
                            return catalog2VOS;
                        }
                ));
        //从数据库中查询出来之后往缓存中存放map
        // cacheMap.put("getCatalog2JSON", catalog2JSONMap);
        //查询数据库之后放到redis缓存
        String toJSONString = JSON.toJSONString(catalog2JSONMap);
        operations.set(redisKey, toJSONString);
        return catalog2JSONMap;
    }

    /**
     * 本地缓存
     */
    @Deprecated
    private final Map<String, Map<String, List<Catalog2VO>>> cacheMap = new HashMap<>();

    /**
     * 过期方法
     * 数据库查询，获取catalog 3级分类
     * 封装成为Map<String, Catalog2VO>对象
     * 缓存击穿：数据库查询锁，一次只允许一个对象进来查询，查询到的放缓存
     */
    @Deprecated
    public  Map<String, List<Catalog2VO>> getCatalog2JSONForDb() {
        synchronized(this){
            /* if (cacheMap.containsKey("getCatalog2JSON")){
        //缓存中有，就从缓存中拿
        return cacheMap.get("getCatalog2JSON");
    } */
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String catalogJSON = operations.get(redisKey);
            if (StringUtils.isNotEmpty(catalogJSON)){
                //再查询一次缓存，如果命中返回
                System.out.println("再查询一次缓存，命中...");
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});
            }
            System.out.println("缓存没有命中，数据库查询....");
            //一次性查询出所有的分类
            List<CategoryEntity> categoriesList = baseMapper.selectList(new QueryWrapper<>());
            //获取 parent_cid 第一层级的 cat_id 通过这个id来拿到二级的catelog
            List<CategoryEntity> category1Entities = this.queryPageByParentCid(categoriesList, 0L);

            Map<String, List<Catalog2VO>> catalog2JSONMap = category1Entities.stream().collect(
                    Collectors.toMap(
                            categoryEntity->categoryEntity.getCatId().toString(),
                            categoryEntity->{
                                Long cat1Id = categoryEntity.getCatId();
                                //获取 2级分类对象列表，通过1级的catId 过滤得到（优化）
                                List<CategoryEntity> catelog2Entities = this.queryPageByParentCid(categoriesList, cat1Id);
                                List<Catalog2VO> catalog2VOS = null;
                                if(catelog2Entities!=null){
                                    catalog2VOS = catelog2Entities.stream()
                                            .map(category2Entity -> {
                                                // 给 catalog2VO 对象赋值
                                                Catalog2VO catalog2VO = new Catalog2VO();
                                                Long cat2Id = category2Entity.getCatId();
                                                catalog2VO.setCatalog1Id(cat1Id.toString());
                                                catalog2VO.setName(category2Entity.getName());
                                                catalog2VO.setId(cat2Id.toString());
                                                // 查询出来3级分类对象 传入 parentCid 为二级分类的catId
                                                List<CategoryEntity> catelog3Entities = this.queryPageByParentCid(categoriesList,cat2Id);
                                                if (catelog3Entities != null) {
                                                    List<Catalog3VO> catalog3VOS = catelog3Entities.stream()
                                                            .map(category3Entity -> {
                                                                String cat3Id = category3Entity.getCatId().toString();
                                                                Catalog3VO catalog3VO = new Catalog3VO();
                                                                catalog3VO.setCatalog2Id(cat2Id.toString());
                                                                catalog3VO.setId(cat3Id);
                                                                catalog3VO.setName(category3Entity.getName());
                                                                return catalog3VO;
                                                            })
                                                            .collect(Collectors.toList());
                                                    catalog2VO.setCatalog3List(catalog3VOS);
                                                }
                                                return catalog2VO;
                                            }).collect(Collectors.toList());
                                    return catalog2VOS;
                                }
                                return catalog2VOS;
                            }
                    ));
            //从数据库中查询出来之后往缓存中存放map
            // cacheMap.put("getCatalog2JSON", catalog2JSONMap);
            //查询数据库之后放到redis缓存
            String toJSONString = JSON.toJSONString(catalog2JSONMap);
            operations.set(redisKey, toJSONString);
            return catalog2JSONMap;
        }
    }


}