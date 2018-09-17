app.service('uploadService', function ($http) {
    this.uploadFile = function () {
        var formdata = new FormData();
        formdata.append('file', file.files[0]);//文件上传框的name
        return $http({
            url: '../upload/upload.do',
            method: 'post',
            data: formdata,
            headers: {'Content-Type': undefined},//解除传送的默认json格式。
            transformRequest: angular.identity //angular 将文件二进制序列化
        });
    }
});