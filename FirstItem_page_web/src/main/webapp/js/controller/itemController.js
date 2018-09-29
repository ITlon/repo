app.controller('itemController',function($scope){
	//商品的添加或减少
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	//定义一个变量用于存储用户选择的商品信息
	$scope.selectSpecificationItems={};
	//确认用户已选择的选项信息
	$scope.selectSpecification=function(key,value){
		
		//将好的信息存入selectSpecification中
		$scope.selectSpecificationItems[key]=value;
		searchSku();
	}
	
	//判断用户是否选中
	$scope.isSelected=function(key,value){
		
		if($scope.selectSpecificationItems[key]==value){
			return true;
		}else{
			return false;
		}
	}
	//定义一个sku
	$scope.sku={};
	//加载默认的sku
	$scope.loadDefaultSku=function(){
		
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	//比较两个对象是否一样
	$scope.compareObject=function(mapA,mapB){
		for (var key in mapA){
			if(mapA[key]!=mapB[key]){
				return false;
			}
		}
		for(var key in mapB){
			if(mapB[key]!=mapA[key]){
				return false;
			}
		}
		return true;
	}
	//动态获取sku
	$scope.searchSku=function(){
		for(var i=0;i<skuList.length;i++){
		     if(compareObject($scope.selectSpecificationItems,skuList[i].spec)){
			$scope.sku=skuList[i];
			return;
		     }else{
			     $scope.sku={id:'0', title:'你选的商品太抢手了,正在加紧补货中', price:'0'
			     }
		      }
		}	
	}
	
});