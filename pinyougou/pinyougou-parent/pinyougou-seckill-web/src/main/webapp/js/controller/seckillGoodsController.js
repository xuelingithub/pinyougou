//控制层
app.controller('seckillGoodsController' ,function($scope,seckillGoodsService,$location,$interval){
//读取列表数据绑定到表单中
    $scope.findList=function(){
        seckillGoodsService.findList().success(
            function(response){
                $scope.list=response;
            }
        );
    }
    $scope.findOne=function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(function (response) {
            $scope.entity=response;

            allTime=Math.floor(  ((new Date($scope.entity.endTime).getTime())-(new Date().getTime()))/1000   );


           time= $interval(function () {
                   allTime=allTime-1;
                   $scope.timeString= convertTimeString(allTime);

                if(allTime<=0){
                    $interval.cancel(time);
                    alert("秒杀已经结束!")
                }
            },1000);


        });
    };

    convertTimeString=function (allsecond) {
        var days=Math.floor(allsecond/(60*60*24));//天数
        var hours=Math.floor((allsecond-days*60*60*24)/(60*60) ); //小时
        var minutes=Math.floor((allsecond-days*60*60*24-hours*60*60)/60);
        var second=allsecond-days*60*60*24-hours*60*60-minutes*60;
        var timeString="";
        if (days>0){
            timeString=days+"天";
        }
        return timeString+hours+":"+minutes+":"+second;
    }


    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if (response.success){//下单成功,跳转到pay页面
                alert("抢购成功,请在5分钟之类完成支付");
                location.href="pay.html";

            }else {
                alert(response.message);
            }
        })
    }



});