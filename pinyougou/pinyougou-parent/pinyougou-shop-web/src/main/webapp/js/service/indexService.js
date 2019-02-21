app.service("indexService",function ($http) {
    this.indexservice=function () {
        return $http.get("../login/name.do");
    }
})