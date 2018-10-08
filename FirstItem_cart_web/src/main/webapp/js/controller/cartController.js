app.controller('cartController', function ($scope, cartService) {

    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
            }
        );
    }































});