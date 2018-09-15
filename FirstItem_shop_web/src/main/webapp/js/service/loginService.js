app.service('loginService',function ($http) {
    this.findLoginName=function () {
       return $http.get('../login/name.do');
    }
})