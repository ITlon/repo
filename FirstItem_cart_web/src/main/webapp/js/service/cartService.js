app.service('cartService',function ($http) {
    //查询购物车列表
   this.findCartList=function () {
       return $http.get('/cart/findCartList.do');
   }
   //添加商品到购物车
   this.addGoodsToCart=function (itemId,num) {
       return $http.get('/cart/addGoodsToCart.do?itemId='+itemId+'&num='+num);
   }
   //合计数
   this.sum=function (cartList) {
       var totalValue={totalFee:0.00,totalNum:0};
        //遍历购物车列表
       for(var i=0;i<cartList.length;i++){
          var cart= cartList[i];
        for (var j=0;j<cart.orderItemList.length;j++){
            //遍历商品明细(订单)列表
           var orderItem = cart.orderItemList[j];
         totalValue.totalFee+=orderItem.totalFee;
         totalValue.totalNum+=orderItem.num;
        }
       }
       return totalValue;
   }
    this.findAddressList=function () {
       return $http.get('/cart/findAddressList.do');
    }

    this.add=function (address) {
        return $http.post('/address/add.do',address);
    }
    this.update=function (address) {
        return $http.post('address/update.do',address);
    }
    this.findOne=function (id) {
        return $http.get('address/findOne.do?id='+id);
    }
    this.dele=function (id) {
        return $http.get('address/delete.do?id='+id);
     }
    this.submitOrder=function (order) {
        return $http.post('order/add.do',order);
    }




























});