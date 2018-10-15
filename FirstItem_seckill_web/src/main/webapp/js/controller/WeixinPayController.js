app.controller('WeixinPayController', function ($scope, $location, WeixinPayService) {

    $scope.createNative = function () {
        WeixinPayService.createNative().success(
            function (response) {
                $scope.total_fee = (response.total_fee / 100).toFixed(2);//金额
                $scope.out_trade_no = response.out_trade_no;//订单号
                //二维码
                var qr = new QRious(
                    {
                        element: document.getElementById('qrious'),
                        size: 250,
                        level: 'H',
                        value:response.code_url
                    });
                //调用查询方法查询交易状态
                $scope.queryPayStatus($scope.out_trade_no);
            }
        );
    };
    $scope.getMoney = function () {
        return $location.search()['total_fee'];
    };
   //查询订单状态
    $scope.queryPayStatus = function (out_trade_no) {
        WeixinPayService.queryPayStatus(out_trade_no).success(
            function (response) {
                if (response.success) {
                    location.href = 'paysuccess.html#?total_fee=' + $scope.total_fee;
                } else {
                    if ("二维码超时"==response.message){
                        location.href="payTimeOut.html";
                    }else {
                        location.href="payfail.html";
                    }
                }
            }
        );
    };
    //刷新
    $scope.updateNative=function () {
        $scope.createNative();
    }


});