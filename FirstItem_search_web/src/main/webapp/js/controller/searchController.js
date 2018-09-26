app.controller('searchController', function ($scope, searchService) {
    //初始化搜索条件
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sort': '',
        'sortField': ''
    };
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                $scope.buildPageLabel();//调用
            }
        );
    }
    //添加搜索条件
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    };
//移除搜索条件
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };
//排序查询
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    };
//构建分页栏
    $scope.buildPageLabel = function () {
        //新增分页栏属性
        $scope.pageLabel = [];
         
        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalPage;//截止页码
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点
        //总页数大于5
        if ($scope.resultMap.totalPage > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;//前面没点
			}else if( $scope.searchMap.pageNo>= $scope.resultMap.totalPage-2 ){//显示后5页
                firstPage = $scope.resultMap.totalPage - 4;
                $scope.lastDot = false;//后边没点
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点
        }
        //构建页码
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    };
    $scope.queryByPage = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPage) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    };
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }
//判断当前页是否未最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPage) {
            return true;
        } else {
            return false;
        }
    }
});

