//服务层
app.service('seckillGoodsService',function($http){

	//查询实体
	this.findOne=function(id){
		return $http.get('seckillGoods/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('seckillGoods/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('seckillGoods/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('seckillGoods/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('seckillGoods/search.do?page='+page+"&rows="+rows, searchEntity);
	}
	this.findList=function () {
		return $http.get('seckillGoods/findList.do');
    }
    this.submitOrder=function (seckillId) {
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId)
    }
});
