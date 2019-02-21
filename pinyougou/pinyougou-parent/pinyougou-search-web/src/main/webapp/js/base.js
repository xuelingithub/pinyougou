var app=angular.module('pinyougou',[]);
/*$sce 服务写成过滤器*/
app.filter('trusthtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);