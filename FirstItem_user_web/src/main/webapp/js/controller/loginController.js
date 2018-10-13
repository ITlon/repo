app.controller('loginController',function ($scope,loginService) {
    $scope.searchName=function () {
        loginService.searchName().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        );
    }
});