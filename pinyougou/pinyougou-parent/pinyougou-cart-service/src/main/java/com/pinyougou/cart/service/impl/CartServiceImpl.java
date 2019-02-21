package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/*
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartlist, Long itemId, Integer num) {
        //1   根据商品的id获得商家对象
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        if (tbItem==null){
            throw new RuntimeException("商品不存在");
        }
        if(!tbItem.getStatus().equals("1")){
            throw new RuntimeException("商品不合法");
        }

        //2   根据商家对象获得商家id
        String sellerId = tbItem.getSellerId();

        //3   根据商家id查询购物车对象  (购物车对象里面有商家id)
        Cart cart = searchCartById(cartlist, sellerId);
        if (cart==null){//4   判断购物车是否存在.如果不存在就创建新的购物车对象
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            List<TbOrderItem> orderItemList=new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(tbItem, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.1  把新的购物车对象加入到购物车列表里面
            cartlist.add(cart);
        }else {  //5  如果购物车存在就判断购物车对象里面是否有该商品明细,
            TbOrderItem orderItem = searchOrderItemById(cart.getOrderItemList(), itemId);
            System.out.println(orderItem);
            if (orderItem==null){ //5.1 如果没有就创建新的商品明细
                orderItem=createOrderItem(tbItem,num);
                cart.getOrderItemList().add(orderItem);
            }else {  //5.2 如果有就加入该商品明细,在原有的数量上加数量,并更新价格
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
          //购物条目数量小于0,就从购物车中移除
                if (orderItem.getNum()<=0){
             cart.getOrderItemList().remove(orderItem);
          }
          //如果购物车的条目数量等于0,该购物车就从购物车集合中移除
          if (cart.getOrderItemList().size()==0){
              cartlist.remove(cart);
          }
            }

        }
        System.out.println(cartlist);
        return cartlist;
    }

    private Cart searchCartById(List<Cart> cartlist, String sellerId){
        for (Cart cart : cartlist) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    private TbOrderItem searchOrderItemById(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem tbItem,Integer num){
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(tbItem.getGoodsId());
        tbOrderItem.setItemId(tbItem.getId());
        tbOrderItem.setNum(tbItem.getNum());
        tbOrderItem.setPicPath(tbItem.getImage());
        tbOrderItem.setPrice(tbItem.getPrice());
        tbOrderItem.setSellerId(tbItem.getSellerId());
        tbOrderItem.setTitle(tbItem.getTitle());
        tbOrderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*num));
        return tbOrderItem;
    }
}
*/
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num)
    {
//1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        System.out.println(item.getId());
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
//2.获取商家 ID
        String sellerId = item.getSellerId();
//3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);
//4.如果购物车列表中不存在该商家的购物车
        if(cart==null){
//4.1 新建购物车对象 ，
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item,num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
//4.2 将购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
//5.如果购物车列表中存在该商家的购物车
// 判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem =
                    searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null){
//5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{
//5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new
                        BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()) );
//如果数量操作后小于等于 0，则移除
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
//如果移除后 cart 的明细数量为 0，则将 cart 移除北京市昌平区建材城西路金燕龙办公楼一层 电话： 400-618-9090
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("获取购物车"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("存入购物车"+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);

    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, tbOrderItem.getItemId(), tbOrderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家 ID 查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){
        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    /**
     * 根据商品明细 ID 查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long
            itemId ){
        for(TbOrderItem orderItem :orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
    /**
     * 创建订单明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
}

