app.controller('brandController',function($scope,$http,brandService,$controller) {
    $controller('baseController',{$scope:$scope})
    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }



    //分页
    $scope.findPage=function(page,size){
        /*/brand/findPage.do?pageNum=1&pageSize=10*/
        brandService.findPage(page,size).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
    /*新增*/
    $scope.save=function(){
        var object=null;
        if($scope.entity.id!=null){
            object=brandService.update($scope.entity)
        }else {
            object=brandService.add($scope.entity);
        }
        object.success(function (response) {
            if(response.success){
                $scope.reloadList();
            }else {
                alert(response.message);
            }
        });
    }

    /*id查詢*/
    $scope.findOne=function(id){
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            });
    }

    $scope.dele=function(){
        alert($scope.selectIds);
        brandService.dele($scope.selectIds).success(function (response) {
            if(response.success){

                $scope.reloadList();
            }else {
                alert(response.message);
            }
        });
    }
    $scope.searchEntity={};/*初始化,保证该值不为null*/
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }


});
