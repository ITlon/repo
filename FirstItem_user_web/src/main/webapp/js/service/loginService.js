app.service('loginService',function ($http) {
   this.searchName=function () {
    return  $http.get('../login/name.do');
   }
});