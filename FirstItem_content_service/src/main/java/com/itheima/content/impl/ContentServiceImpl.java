package com.itheima.content.impl;

import java.util.List;

import com.itheima.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.TbContentMapper;
import com.itheima.pojo.TbContent;
import com.itheima.pojo.TbContentExample;
import com.itheima.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //缓存中删除
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        contentMapper.insert(content);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //查询原所在分类的id (id唯一且不会改变)
        TbContent oldContent = contentMapper.selectByPrimaryKey(content.getId());
        Long categoryId = oldContent.getCategoryId();
        //从缓存中删除数据
        redisTemplate.boundHashOps("content").delete(categoryId);
        //更新数据到数据库
        contentMapper.updateByPrimaryKey(content);
        //删除现在所在分组的id
        if (categoryId != content.getCategoryId()) {
            //原来分组id和现在分组id不一样则执行删除现在所在分组id对应的缓存数据
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //查询要删除的分类id
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            //从缓存中删除
            redisTemplate.boundHashOps("content").delete(categoryId);
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //先查询缓存
        List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (list != null) {
            //缓存中有数据
            System.out.println("缓存中有数据,直接返回");
            return list;
        } else {
            System.out.println("缓存中没有数据,从数据库中查询");
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            // 有效广告:
            criteria.andStatusEqualTo("1");
            criteria.andCategoryIdEqualTo(categoryId);
            example.setOrderByClause("sort_order");//排序
            list = contentMapper.selectByExample(example);
            redisTemplate.boundHashOps("content").put(categoryId, list);
            return list;
        }
    }
}
