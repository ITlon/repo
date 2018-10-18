//控制层
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService) {
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        );
    };
    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                var allTime = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);
                var time = $interval(function () {
                    allTime = allTime - 1;
                    $scope.timeString =$scope.convertTimeString(allTime);
                    if (allTime <= 0) {
                        $interval.cancel(time);
                        alert("秒杀活动结束");
                    }
                }, 1000);
            }
        );
    };
    //转换时间格式
    $scope.convertTimeString = function (param) {
        var days = Math.floor(param / (60 * 60 * 24));//天
        var hours = Math.floor((param - days * 60 * 60 * 24) / (60 * 60));//小时
        var minutes = Math.floor((param - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分
        var seconds = param - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60;//秒
        var timeString = "";
        if (days > 0) {
            timeString = days + "天 ";
        }
        return timeString + hours + ":" + minutes + ":" + seconds;
    };
    //提交订单
    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success) {
                    location.href = "pay.html";
                } else {
                    alert(response.message);
                }
            }
        );
    };

});	
