package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;
    @RequestMapping("/createNative")
    public Map createNative(){
        //分布式id生成 idworker   推特雪花算法
        IdWorker idWorker = new IdWorker();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        if (seckillOrder!=null){
            Map map = weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
       return map;
        }else {
            return  new HashMap();
        }

    }
@RequestMapping("/queryPayStatus")
public Result queryPayStatus(String out_trade_no){
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result=null;
        int x=0;
    while (true){
        Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
        if (map==null){//获得结果传到这是个null(出现异常)
            result=new Result(false,"支付失败");
           break;
        }
        if (map.get("trade_state").equals("SUCCESS")){//支付成功

            result=new Result(true,"支付成功");
            seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
            break;
        }

        try {
            Thread.sleep(3000);//睡眠3秒再继续无线查询微信返回结果是否交易状态success
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        x++;
        if (x>=100){
            result= new Result(false,"二维码超时");
            Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
            if (payresult!=null && "FAIL".equals(payresult.get("result_code"))){
                if ("ORDERPAID".equals(payresult.get("err_code"))){
                    result=new Result(true,"支付成功");
                    seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
                }
            }

            //删除缓存,订单回退
            if (result.isSuccess()==false){
                seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
            }
            break;
        }

    }
    return result;

}

}
