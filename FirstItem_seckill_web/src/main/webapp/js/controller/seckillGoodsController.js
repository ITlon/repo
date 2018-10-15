//控制层
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService) {
    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        seckillGoodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };
    //分页
    $scope.findPage = function (page, rows) {
        seckillGoodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };
    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = seckillGoodsService.update($scope.entity); //修改
        } else {
            serviceObject = seckillGoodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    };
    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        seckillGoodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };
    $scope.searchEntity = {};//定义搜索对象
    //搜索
    $scope.search = function (page, rows) {
        seckillGoodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
                var second = Math.floor((new Date($scope.list.endTime).getTime() - (new Date().getTime())) / 1000);
                var time = $interval(function () {
                    if (second > 0) {
                        second = second - 1;
                        $scope.timeString = convertTimeString(second);
                    } else {
                        $interval.cancel(time);
                        alert("秒杀活动结束");
                    }
                }, 1000);
            }
        );
    };
    //转换时间格式
    $scope.convertTimeString = function (timeValue) {
        var timeString = "";
        var day = Math.floor(timeValue / (60 * 60 * 24));   //天
        var hour = Math.floor((timeValue - day * 24 * 60 * 60) / (60 * 60));//小时
        var minute = Math.floor((timeValue - day * 24 * 60 * 60 - hour * 60 * 60) / 60);//分
        var second = timeValue - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60;//秒
        if (day > 0) {
            return timeString = day + "天 ";
        }
        return timeString + hour + ":" + minute + ":" + second;
    };
    //查询实体
    $scope.findOne = function () {
        var id = location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };
    //提交订单
    $scope.submitOrder=function () {
       seckillGoodsService.submitOrder($scope.entity.id).success(
           function (response) {
               if (response.success){
                   location.href="pay.html";
               }else {
                   alert(response.message);
               }
           }
       );
    };

});	
