package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;

import com.pinyougou.pojo.TbPayLog;
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
    private OrderService orderService;
    @RequestMapping("/createNative")
    public Map createNative(){
        //分布式id生成 idworker   推特雪花算法
        IdWorker idWorker = new IdWorker();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(username);
        if (tbPayLog!=null){
            System.out.println(tbPayLog.getTotalFee());
            Map map = weixinPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");
       return map;
        }else {
            return  new HashMap();
        }

    }
@RequestMapping("/queryPayStatus")
public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x=0;
    while (true){
        Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
        if (map==null){//获得结果传到这是个null(出现异常)
            result=new Result(false,"支付失败");
           break;
        }
        if (map.get("trade_state").equals("SUCCESS")){//支付成功
            orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
            result=new Result(true,"支付成功");
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
            break;
        }

    }
    return result;

}

}
