app.controller('itemController',function ($scope,$http) {
	//数量增减
	$scope.addNum=function(x){
	$scope.num=$scope.num+x;
	if($scope.num<1){
	$scope.num=1;
	}
}

//用户选择规格
$scope.specificationItems={};

$scope.selectSpecification=function(key,value){
	$scope.specificationItems[key]=value;
	searchSku();
	
}

//判断是否选择了
$scope.isSelected=function(key,value){
	if($scope.specificationItems[key]==value){
		return true;
	}else{
		return false;
	}
}

$scope.sku={};
$scope.loadSku=function(){
	$scope.sku=skuList[0];	
	$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));

}
//匹配对象
		matchObject=function(map1,map2){
		for(var k in map1){
		if(map1[k]!=map2[k]){
		return false;
		}
		}
		for(var k in map2){
		if(map2[k]!=map1[k]){
		return false;
		}
		}
		return true;
		}

//按照规格查找sku
		searchSku=function(){
		for(var i=0;i<skuList.length;i++ ){
		if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
		$scope.sku=skuList[i];
		return ;
		}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
		}


		//加入到购物车
		$scope.andToCart=function(){
			$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='
				+$scope.num,{'withCredentials':true}).success(function (response) {
				if(response.success){
					//加入购物车成功
					location.href='http://localhost:9107/cart.html';
				}else {
					//加入失败
					alert(response.message);
				}
            })

		}




})