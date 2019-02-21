package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/*
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //借助cookie工具类从cookie中获取cartList的数据后转为cartList集合为购物车
        String cartList = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if(cartList==null || cartList.equals("")){
            cartList="[]";
           }
        List<Cart> cartList_cookie  = JSON.parseArray(cartList, Cart.class);
        return cartList_cookie;
    }

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            //从cookie中获取购物车
            List<Cart> cartList = findCartList();
            System.out.println(cartList);
            //向购物车添加商品

                cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //把购物车存入cookie
                String jsonString = JSON.toJSONString(cartList);
                util.CookieUtil.setCookie(request, response, "cartList",jsonString ,3600*24 ,"UTF-8" );
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败");
        }
    }
}
*/

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    /**
     * 购物车列表
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        String cartListString = util.CookieUtil.getCookieValue(request,
                "cartList","UTF-8");
        if(cartListString==null || cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")){
            //未登录

            return cartList_cookie;
        }else {
            //登录
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size()>0){
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                util.CookieUtil.deleteCookie(request,response , "cartList");
                cartService.saveCartListToRedis(username, cartList_redis);
            }

            return cartList_redis;
        }


    }
    /**
     * 添加商品到购物车
     * @param
     * @param
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartList(Long itemId,Integer num){
      /*  response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//响应回去可以访问的域
        response.setHeader("Access-Control-Allow-Credentials","true" );//操作cookie必须加这句话*/
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList =findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")){
                util.CookieUtil.setCookie(request, response, "cartList",
                        JSON.toJSONString(cartList),3600*24,"UTF-8");
            }else {
                cartService.saveCartListToRedis(username,cartList );
            }

                return new Result(true, "添加成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}
