app.controller('searchController', function ($scope, $location, searchService) {
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
            $scope.searchMap.pageNo=1;
            $scope.search();
        };
        //移除搜索条件
        $scope.removeSearchItem = function (key) {
            if (key == 'category' || key == 'brand' || key == 'price') {
                $scope.searchMap[key] = "";
            } else {
                delete $scope.searchMap.spec[key];
            }
            $scope.searchMap.pageNo=1;
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
            //开始页码
            var firstPage = 1;
            //截止页码
            var lastPage = $scope.resultMap.totalPage;
            //分页栏前面有点
            $scope.firstDot = true;
            //分页栏后面有点
            $scope.lastDot = true;
            //如果页码数量大于5
            if ($scope.resultMap.totalPage > 5) {
                //如果当前页码小于等于3 ，显示前5页
                if ($scope.searchMap.pageNo <= 3) {
                    lastPage = 5;
                    //分页栏前面没点
                    $scope.firstDot = false;
                } else if ($scope.searchMap.pageNo >= lastPage - 2) {
                    //显示后5页
                    firstPage = lastPage - 4;
                    //分页栏后面没点
                    $scope.lastDot = false;
                } else {
                    //显示以当前页为中心的5页
                    firstPage = $scope.searchMap.pageNo - 2;
                    lastPage = $scope.searchMap.pageNo + 2;
                    //分页栏前后都有点.即默认的初始值
                }
            } else {
                //分页栏前后都没点
                $scope.firstDot = false;
                $scope.lastDot = false;
            }
            //构建页码
            for (var i = firstPage; i <= lastPage; i++) {
                $scope.pageLabel.push(i);
            }
        }

        $scope.queryByPage = function (pageNo) {
            //查询的当前页大于总页数或者当前页小于起始页则不查询
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
        //判断查询的关键字是否是品牌
        $scope.keywordsIsBrand = function () {
            for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
                //输入的关键字是否包含返回的结果集中的品牌
                if ($scope.searchMap.keywords.indexOf(resultMap.brandList[i].text) >= 0) {
                    return true;
                }
            }
            return false;
        }

        $scope.initSearch = function () {
            //用$location服务接收广告首页查询传过来的参数
            $scope.searchMap.keywords = $location.search()['keywords'];
            $scope.search();
        }
    }
);

