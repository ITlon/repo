 //品牌控制层 
app.controller('baseController' ,function($scope){	
	
    //重新加载列表 数据
    $scope.reloadList=function(){
    	//切换页码  
    	$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);	   	
    }
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function($event, id) {		
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);			
		}else{
			var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
		}
	}
	//提取 json 字符串数据中某个属性，返回拼接字符串 逗号分隔
	$scope.jsonToString=function (jsonString,key) {
        var json= JSON.parse(jsonString);
        var value="";
        for(var i = 0 ;i<json.length;i++){
        	if (i>0){
        		value+=",";
			}
            value+=json[i][key];
		}
		return value;
    }
	/*json取值方式  var  a = {"id":1,"手机":"联想"}
	* 取id的key值方式 （1）a.id （2）a['id'] （3）a[0][key]
	* 上式中的key是个变量 .根据第3中方式取值。
    */
});	