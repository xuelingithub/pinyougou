package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 5000)//在服务的提供方设置服务执行操作超时时间
public class ItemSearchServiceImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;
    /*@Override
    public Map search(Map searchMap) {
        Map map = new HashMap<>();
        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());
        return map;
    }*/
    @Override
     public Map search(Map searchMap){
         Map map=new HashMap();
        //空格处理
         String keywords= (String)searchMap.get("keywords");
         searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格
         map.putAll(searchList(searchMap));

        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //查询品牌和规格列表

        String category = (String) searchMap.get("category");
        if (!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else {
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }



        return map;
}




    //高亮查询
private Map searchList(Map searchMap){
    Map map=new HashMap();
    HighlightQuery query=new SimpleHighlightQuery();
    HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
    highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
    highlightOptions.setSimplePostfix("</em>");//高亮后缀
    query.setHighlightOptions(highlightOptions);//设置高亮选项


    //1.1按照关键字查询
    Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
    query.addCriteria(criteria);

    //1.2 按照分类过滤
    if(!"".equals(searchMap.get("category"))){
        FilterQuery filterQuery=new SimpleFilterQuery();
        Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }
    //1.3按照品牌过滤
    if (!"".equals(searchMap.get("brand"))){
        FilterQuery filterQuery=new SimpleFilterQuery();
        Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }

    //1.4按照规格过滤
    if (searchMap.get("spec")!=null){
        Map<String,String> specmap = (Map<String, String>) searchMap.get("spec");
        for (String key : specmap.keySet()) {
            FilterQuery filterquery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_spec_"+key).is(specmap.get(key));
            filterquery.addCriteria(filterCriteria);
            query.addFilterQuery(filterquery);
        }
    }
    //1.5按照价格过滤
    if(!"".equals(searchMap.get("price"))){
        //price已经是500  -  1000形式
        String [] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        if(!price[1].equals("*")){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
    }

    //1.6分页处理
    Integer pageNo = (Integer) searchMap.get("pageNo"); //获取当前第几页,页码
    if(pageNo==null){//默认第一页
        pageNo=1;
    }
    Integer pageSize = (Integer) searchMap.get("pageSize"); //获取每页多少条目,size
    if(pageSize==null){//默认每页20个商品显示
        pageSize=20;
    }
        query.setOffset((pageNo-1)*pageSize);//设置分页查询起始索引
        query.setRows(pageSize);//设置查询数目

            //1.7字段排序
             String sortValue = (String) searchMap.get("sort");
             String sortField = (String) searchMap.get("sortField");
             if (sortValue!=null&&!sortValue.equals("")){
               if(sortValue.equals("ASC")){
                   Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                   query.addSort(sort);
               }
               if(sortValue.equals("DESC")){
                   Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
                   query.addSort(sort);
               }
             }


    //************获得高亮结果集***************
    HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
    for(HighlightEntry<TbItem> h: page.getHighlighted()){//循环高亮入口集合
        TbItem item = h.getEntity();//获取原实体类
        if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
            item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
        }
    }
    map.put("rows",page.getContent());
    map.put("totalPages",page.getTotalPages());//总页数
    map.put("total",page.getTotalElements());//总记录数
    return map;
}

//category分类分组查询
     private List<String>  searchCategoryList(Map searchMap){
        List<String> list=new ArrayList();
         Query query=new SimpleQuery("*:*");
         //where 添加条件
         Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
         query.addCriteria(criteria);
         //group 添加分组
         GroupOptions groupoptions=new GroupOptions().addGroupByField("item_category");
         query.setGroupOptions(groupoptions);
         //获得分组页
         GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //分组页中获取分组结果对象
         GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
         //获取分组入口页
         Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
         List<GroupEntry<TbItem>> content = groupEntries.getContent();
         for (GroupEntry<TbItem> entry : content) {
             list.add( entry.getGroupValue());
         }
         return list;
}


@Autowired
private RedisTemplate redisTemplate;
public Map searchBrandAndSpecList(String category){
     Map map= new HashMap<>();
    Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
    if(templateId!=null){
        List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
        List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
        map.put("brandList",brandList);
        map.put("specList",specList);
    }
    return map;
}



    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query =new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
};
