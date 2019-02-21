app.controller('searchController',function ($scope, $location,searchService) {


    /*构建搜索对象结构,category:商品分类*/
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
    $scope.search=function () {
        $scope.searchMap.pageNo=parseInt(  $scope.searchMap.pageNo);//保证当前页是int   (确定到几页的model参数为String)
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
           /* $scope.searchMap.pageNo=1;*/
            buildPageLabel();//构建分页栏
        });

    }
//构建分页栏
    buildPageLabel=function () {
        $scope.pageLabel=[];
         var fastPage=1;
        var lastPage=$scope.resultMap.totalPages;
        $scope.fastdot=true;//前面有点
        $scope.lastdot=true;//后面没点

        if($scope.resultMap.totalPages>5){
            if($scope.searchMap.pageNo<=3){
                lastPage=5;
                $scope.fastdot=false;
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
                fastPage=$scope.resultMap.totalPages-4;
                $scope.lastdot=false;
            }else {
                fastPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;

            }
        }else {
            $scope.fastdot=false;//总页数小于5的时候,前后都没有点
            $scope.lastdot=false;
        }
        for (var i=fastPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }



    //添加搜索的值
    $scope.andSearchItem=function(key,value){
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]=value;
    }else{
        $scope.searchMap.spec[key]=value;
    }
        //查询
        $scope.search();
}

    //撤销搜索的值
    $scope.removeSearchItem=function(key){
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]="";
        }else{
            delete $scope.searchMap.spec[key];
        }
        //查询
        $scope.search();
    }
    //分页查询
    $scope.queryByPage=function (pageNo) {
        if(pageNo<1||pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
        //判断当前是否是第一页
    $scope.isTopPage=function () {
            if(  $scope.searchMap.pageNo==1){
                return true;
            }else {
                return false;
            }
    }
    //判断当前是否是最后一页
    $scope.isEndPage=function () {
        if( $scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }

    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }


    $scope.keywordsIsBrand=function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }
    //加载关键字
    $scope.loadkeywords=function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

});