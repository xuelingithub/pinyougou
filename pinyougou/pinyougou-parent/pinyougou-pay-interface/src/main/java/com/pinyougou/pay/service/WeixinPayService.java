package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     *
     * @param out_trade_no 商户id
     * @param total_fee 商品金额(分)
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);
    public Map queryPayStatus(String out_trade_no);
    public Map closePay(String out_trade_no);
}
