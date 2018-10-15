app.service('WeixinPayService',function ($http) {

    this.createNative=function () {
      return  $http.get('/pay/createNative.do');
    }

    this.queryPayStatus=function (out_trade_no) {
        return $http.get('/pay/query.do?out_trade_no='+out_trade_no);
    }

});