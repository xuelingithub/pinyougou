package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("执行了任务调度" + new Date());

        List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(ids);
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于 0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        if (ids.size() > 0) {
            criteria.andIdNotIn(ids);
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(), tbSeckillGoods);
            System.out.println("新增id"+tbSeckillGoods.getId());
        }
        System.out.println("....end....");
    }

    /**
     * 超过最后时间缓存中移除,同步数据库
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeseckillGoods(){
    List<TbSeckillGoods> seckillGoodsList  = (List<TbSeckillGoods>) redisTemplate.boundHashOps("seckillGoods");
    for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
        if (tbSeckillGoods.getEndTime().getTime()<new Date().getTime()){
            //同步数据库
            seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
            //缓存中移除
            redisTemplate.boundHashOps("seckillGoods").delete(tbSeckillGoods.getId());
            System.out.println("移除秒杀商品"+tbSeckillGoods.getId());
            }
    }
        System.out.println("移除秒杀商品任务结束");
}
}
