app.controller('cartController', function ($scope, cartService) {

    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                 $scope.cartList = response;
                 //调用service层的合计方法
                 $scope.totalValue= cartService.sum($scope.cartList);
            }
        );
    }

    $scope.addGoodsToCart = function (itemId,num) {
        cartService.addGoodsToCart(itemId,num).success(
            function (response) {
                if (response.success) {
                    //刷新页面
                    $scope.findCartList();
                } else {
                alert(response.message)
                }
            }
        );
    }
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
                //设置默认地址
                for(var i=0;i<$scope.addressList.length;i++){
                    //默认值为1 表示默认选择的地址
                    if ($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }
    //用户选择的地址选项
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //用户是否选中的地址
    $scope.isSelectedAddress=function (address) {
        if ($scope.address==address){
            return true;
        }else {
            return false;
        }
    }
    $scope.order={paymentType:'1'};
    //支付方式
    $scope.selectedPayType=function (type) {
        $scope.order.paymentType=type;
    }

      //修改或者增加地址
     $scope.save=function () {
        var objectService;
        if($scope.address.id!=null){
            objectService=cartService.update($scope.address);
        }else {
            objectService=cartService.add($scope.address);
        }
         objectService.success(
             function (response) {
                 if (response.success){
                     alert(response.message);
                     $scope.findAddressList();
                 }else {
                     alert(response.message);
                 }
             }
         );
     }
     $scope.findOne=function (id) {
         cartService.findOne(id).success(
             function (response) {
                 $scope.address=response;
             }
         );
     }
     $scope.dele=function (id) {
         cartService.dele(id).success(
             function (response) {
                 if (response.success){
                     alert(response.message);
                    $scope.findAddressList();
                 }else {
                     alert(response.message)
                 }
             }
         );
     }
     //提交订单
     $scope.submitOrder=function () {
         $scope.order.receiverAreaName=$scope.address.address;//地址
         $scope.order.receiverMobile=$scope.address.mobile;//手机
         $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    if ($scope.order.paymentType=='1'){
                        //如果是微信支付，跳转到支付页面
                        location.href='pay.html';
                    }else {
                        //货到付款跳转页面
                        location.href='paysuccess.html'
                    }
                }else {
                    alert(response.message);
                }
            }
        );
     }











});