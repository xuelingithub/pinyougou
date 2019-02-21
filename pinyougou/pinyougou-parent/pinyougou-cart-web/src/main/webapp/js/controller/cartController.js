//购物车控制层
app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                $scope.totalValue= cartService.sum($scope.cartList);
            }
        );
    }

    //数量加减
    $scope.addGoodsToCartList=function(itemId,num){
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
                if(response.success){//如果成功
                    $scope.findCartList();//刷新列表
                }else{
                    alert(response.message);
                }
            }
        );
    }

    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
                for (var i=0;i< $scope.addressList.length;i++){
                    if ( $scope.addressList[i].isDefault=='1'){
                        $scope.address= $scope.addressList[i];
                    }
                }
            }
        )
    }


    $scope.selectAdress=function (address) {
        $scope.address=address;
    }
    $scope.isselectAdress=function (address) {
        if(address==$scope.address){
            return true;
        }else {
            return false;
        }
    }

    //支付方式
    $scope.order={paymentType:'1'};

    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }

    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiver=$scope.address.contact;//联系人
        $scope.order.receiverMobile=$scope.address.mobile;//电话号码
        cartService.submitOrder($scope.order).success(
            function (response) {
               if(response.success){//成功跳转到支付页面
                   if ( $scope.order.paymentType=='1'){
                       location.href="pay.html";
                   }else {
                       location.href="paysuccess.html";
                   }

               }else {//失败了
                   alert(response.message)
               }
            }
        )
    }

});


/*
//购物车控制层
app.controller('cartController',function($scope,cartService) {
//查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue=cartService.sum($scope.cartList);//求合计数
            }
        );
    }

    $scope.addGoodsToCartList=function (itemId,num) {
        $scope.addGoodsToCartList(itemId,num).success(function (response) {
            if(response.success()){
                $scope.findCartList();
            }else {
                alert(response.message)
            }
        })
    }
}*/
