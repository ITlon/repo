 //控制层
app.controller('userController' ,function($scope,$controller,userService){
    $scope.reg=function () {
        //校验密码
        if ($scope.entity.password!=$scope.passcode){
            alert("两次输入的密码不一致,请重新输入");
            $scope.entity.password="";
            $scope.passcode="";
            return;
        }
       userService.add($scope.entity,$scope.smsCode).success(
         function (response) {
            if (response.success){
            alert(response.message);
            $scope.entity="";
            $scope.passcode="";
            $scope.smsCode="";
            }
         }
       );
    }
    $scope.sendCode=function () {
        if ($scope.entity.phone==null||$scope.entity.phone==""){
            alert("请填写你正确的电话号码");
            return;
        }
       userService.sendCode($scope.entity.phone).success(
           function (response) {
               alert(response.message);
           }
       );
    }



});	
