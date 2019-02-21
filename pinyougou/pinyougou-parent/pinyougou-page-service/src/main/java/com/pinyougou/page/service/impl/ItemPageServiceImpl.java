package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
        @Autowired
        private FreeMarkerConfigurer freeMarkerConfigurer;

        @Value("${pagedir}")
        private String pagedir;

        @Autowired
        private TbGoodsMapper tbGoodsMapper;

        @Autowired
        private TbGoodsDescMapper tbGoodsDescMapper;
        @Autowired
        private TbItemCatMapper tbItemCatMapper;
        @Autowired
        private TbItemMapper tbItemMapper;
    @Override
    public boolean genItemHtml(Long goodsId) {
        //1.配置configuration对象
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            //2获取template模板
            Template template = configuration.getTemplate("item.ftl");
            //3创建数据model
            Map dataModel=new HashMap();
            //4查询goods数据put到map中
            TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);
            //5查询goodsDesc数据put到map中
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);

            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = tbItemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);


            //查询商品分类
            String itemCat1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //6创建输出流到模板中
            Writer out=new FileWriter(pagedir+goodsId+".html");
            //7数据输出到模板上
            template.process(dataModel,out);
            //8关闭输出流
            out.close();
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long ids : goodsIds) {
                new File(pagedir+ids+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
