app.controller('baseController',function ($scope) {


    //分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };

    //刷新列表
    $scope.reloadList=function(){
        $scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage );
    }


    $scope.selectIds=[];/*用戶勾选的id集合*/
    $scope.updateselection=function ($event,id) {/*$event表示源*/
        if($event.target.checked){/*target表示input目标*/
            $scope.selectIds.push(id);/*push添加*/
        }else {
            var index = $scope.selectIds.indexOf(id);/*在集合中查找id的位置*/
            $scope.selectIds.splice(index,1);/*根据查找的id位置在集合中移除   参数1:位置,参数2:删除的个数*/
        }
    }


    $scope.jsonToString=function(jsonString,key){

        var json= JSON.parse(jsonString);
        var value="";

        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=",";
            }
            value +=json[i][key];
        }

        return value;
    }

    //在list集合中根据key的值查询对象
    $scope.searchObjectByKey=function (list,key,keyvalue) {
        for (var i=0;i<list.length;i++){
            if(list[i][key]==keyvalue){
                return list[i];
            }
        }
        return null;
    }


});