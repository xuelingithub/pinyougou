app.controller("payController",function ($scope,$location,payService) {
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            /*显示订单号*/
            $scope.out_trade_no=response.out_trade_no;
            //显示金额
            $scope.money= (response.total_fee/100).toFixed(2) ;
            //生成二维码
            var qRious = new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.code_url
            });
          $scope.queryPayStatus( $scope.out_trade_no);//在生成二维码之后就一直查询是否支付成功

        })
    }


    $scope.queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if(response.success){
                location.href="paysuccess.html#?money="+$scope.money;
            }else {
                if(response.message=='二维码超时'){
                  //  $scope.createNative();//重新生成二维码
                    alert("二维码超时");
                }else {
                    location.href="payfail.html";
                }

            }
        })
    }
    $scope.getMoney=function () {
        return $location.search()['money'];//获取当前url的参数的序列化json对象   获取url后面参数money(key) 对应的值
    }
    
})