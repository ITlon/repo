//模板
var app=angular.module('firstItem',[]);
//过滤器 $sce 信任服务
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);

