//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        //判断 有id这说明是修改.没有则是添加.就没必要查询数据了.
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //转换字符串图片为对象类型
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //转换扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格选项
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //SKU 列表规格列转换
                for (var i=0 ; i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec=JSON.parse( $scope.entity.itemList[i].spec);
                }
            }
        );

    }

    //增加商品
    $scope.save= function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;
        if ($scope.entity.goods.id!=null){
            //修改
          serviceObject= goodsService.update($scope.entity);
        }else {
            serviceObject= goodsService.add($scope.entity)
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("保存成功");
                    location.href="goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        );


    }

    $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};

    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }
    //查询一级分类商品
    $scope.findItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }
    //查询二级分类商品
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (oldValue != undefined) {
            $scope.entity.goods.category2Id = -1;
        }
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                }
            );
        }
    });
    //查询三级分类商品
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (oldValue != undefined) {
            $scope.entity.goods.category3Id = -1;
        }
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            );
        }
    });
    //查询模板id
    $scope.$watch('entity.goods.category3Id', function (newValue) {

            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            );

    });
    //查询模板id对应的品牌
    $scope.$watch('entity.goods.typeTemplateId', function (newValue) {

            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//解析json格式
                    if ($location.search()['id'] == null) {
                        //没有id值则加载模板中的扩展数据
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }

                }
            );
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                }
            );

    });
    $scope.updateSpec = function ($event, name, value) {
        var object = $scope.findObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            //如果对象存在，并且勾选了
            if ($event.target.checked) {
                //往attributeValue里添加元素
                object.attributeValue.push(value);
            } else {
                //勾选后又取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                //如果集合中不存在元素。则删除此记录
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                    $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {//没有，则往集合里面添加新对象
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue)
        }
    }

    //克隆添加列
    addColumn = function (list, columnName, columnValues) {
        //定义新集合列表
        var newList = [];
        //遍历
        for (var m = 0; m < list.length; m++) {
            var oldRow = list[m];
            for (var n = 0; n < columnValues.length; n++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                //往新列表中的键spec对应的值中添加（键columnName,其对应的值为columnValues[n]）
                newRow.spec[columnName] = columnValues[n];
                //将新的列加入到新的集合列表中
                newList.push(newRow);
            }
        }
        return newList;
    }
    //定义审核状态集合
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];
    //定义itemCatList集合
    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                //遍历结果集合
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name
                }
            }
        );
    }
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.findObjectByKey(items, "attributeName", specName);
        if (object == null) {
            return false;
        } else {
            //判断该对象中的attributeValue的值中是否含有optionName
            if (object.attributeValue.indexOf(optionName) >= 0) {
                //有,说明存入数据库中了.即勾选了该选项
                return true;
            } else {
                return false;
            }
        }
    }
});	
