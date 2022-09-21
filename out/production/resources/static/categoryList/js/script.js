function httpGet(url)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

$(() => {
    // 보기 모드 쿠키 읽기
    var viewMode = $.cookie("viewMode")
    if (viewMode == undefined) {
        viewMode = "simple";
    }
    $("input[name='view_mode']").change(() => {
        let changedValue = $("input[name='view_mode']:checked").val();
        if (changedValue == "simple") {
            viewMode = "simple";
            $.cookie("viewmode", viewMode, { expires: 2147483647 });
            window.location.replace("localhost:8888?viewmode=" + viewMode);
        } else if (changedValue == "exact") {
            viewMode = "exact";
            $.cookie("viewmode", viewMode, { expires: 2147483647 });
            window.location.replace("localhost:8888?viewmode=" + viewMode);
        }
    });

    // 로그인 세션 불러오기
    loadSession();

    // 카테고리 원본 가져오고 로딩 표시
    var postSimple = $(".post_simple origin"); // 카테고리 버튼 원본
    var postExact = $(".post_exact origin");

    // httpRequest("GET", "http://localhost:8888/getcategory?page=")
});