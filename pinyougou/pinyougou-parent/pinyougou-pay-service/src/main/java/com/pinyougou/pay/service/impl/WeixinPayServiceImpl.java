
package com.pinyougou.pay.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;
@Override
    public Map createNative(String out_trade_no, String total_fee) {

        //1,根据api封装数据
        Map param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());

        //sign 签名略..调用方法自动生成
        param.put("body", "品优购");
        param.put("out_trade_no", out_trade_no);//交易订单号
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "http://www.itcast.cn");
        param.put("trade_type","NATIVE" );
        try {
          String xmlParam= WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" +xmlParam);
            //2,发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//启用https协议
            httpClient.setXmlParam(xmlParam);//设置发送请求的参数
            httpClient.post();//执行post发起请求
            //3,获取结果

            String content = httpClient.getContent();
            System.out.println("返回结果:" +content);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            Map<String,String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//生成二维码的url
            map.put("out_trade_no", out_trade_no);//商户id
            map.put("total_fee", total_fee);//总金额(分)
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //封装参数
           Map param = new HashMap<>();
           param.put("appid", appid);
           param.put("mch_id", partner);
           param.put("out_trade_no", out_trade_no);
           param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //获得结果
            String xmlResult = httpClient.getContent();
            System.out.println("返回结果:"+xmlResult);
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closePay(String out_trade_no) {
        //封装参数
        Map param = new HashMap<>();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //获得结果
            String xmlResult = httpClient.getContent();
            System.out.println("返回结果:" + xmlResult);
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

