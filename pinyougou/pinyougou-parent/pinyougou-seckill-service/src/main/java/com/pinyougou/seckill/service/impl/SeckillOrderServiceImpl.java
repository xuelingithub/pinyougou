package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**',
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;
	@Override
	public void submitOrder(Long seckillId, String userId) {
		//1 从redis中查询
		TbSeckillGoods tbSeckillGoods= (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if (tbSeckillGoods==null){
			throw new RuntimeException("商品不存在");
		}
		if(tbSeckillGoods.getStockCount()<=0){
			throw new RuntimeException("商品已抢购一空");
		}

		//扣减(redis)缓存
		tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, tbSeckillGoods);
		if (tbSeckillGoods.getStockCount()==0){//库存已售空//更新数据库
			seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//缓存中删除
			System.out.println("同步到数据库");
		}
		//redis中保存订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder=new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(tbSeckillGoods.getCostPrice());//秒杀价格
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(tbSeckillGoods.getSellerId());
		seckillOrder.setUserId(userId);//设置用户 ID
		seckillOrder.setStatus("0");//状态
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
		System.out.println("订单提交到redis");


	}

	/**
	 * 从缓存中取出订单
	 * @param userId
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		//1 从缓存中拿出来
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder==null){
			throw new RuntimeException("订单不存在");
		}
		if (seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单不相符");
		}

		//2 修改状态
		seckillOrder.setTransactionId(transactionId);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//已支付状态
		//3 插入数据库
		seckillOrderMapper.insert(seckillOrder);

		//4 清理缓存
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

    /**
     * 超时5分钟处理
     * @param userId
     * @param orderId
     */
    @Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1  从数据库中查询订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder!=null && seckillOrder.getId().longValue() == orderId.longValue()){
            //2  清除缓存中的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
        //3回退库存
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
        if (seckillGoods!=null){
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
            redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
        }else {
			seckillGoods=new TbSeckillGoods();

			TbSeckillGoods tbSeckillGoods = tbSeckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());

			seckillGoods.setId(seckillOrder.getSeckillId());
			seckillGoods.setStockCount(1);//数量为1
			seckillGoods.setId(tbSeckillGoods.getId());
			seckillGoods.setTitle(tbSeckillGoods.getTitle());
			seckillGoods.setSmallPic(tbSeckillGoods.getSmallPic());
			seckillGoods.setPrice(tbSeckillGoods.getPrice());
			seckillGoods.setCostPrice(tbSeckillGoods.getCostPrice());
			seckillGoods.setSellerId(tbSeckillGoods.getSellerId());
			seckillGoods.setCreateTime(tbSeckillGoods.getCreateTime());
			seckillGoods.setEndTime(tbSeckillGoods.getEndTime());
			seckillGoods.setNum(tbSeckillGoods.getNum());
			seckillGoods.setIntroduction(tbSeckillGoods.getIntroduction());
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
		}
        System.out.println("订单取消"+orderId);

    }
}
