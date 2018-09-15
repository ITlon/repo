app.controller('loginController',function ($scope,$controller,loginService) {
    $scope.showLoginName=function () {
        loginService.findLoginName().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        );
    }
});