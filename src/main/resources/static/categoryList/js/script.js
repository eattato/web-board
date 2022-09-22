function httpGet(url)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

const setViewMode = (viewMode) => {
    if (viewMode == "simple" || viewMode == "exact") {
        console.log(viewMode);
        $.cookie("viewmode", viewMode, { expires: 2147483647 });
        if (viewMode == "simple") {
            $(".post_simple").css({display: "block"});
            $(".post_exact").css({display: "none"});
        } else if (viewMode == "exact") {
            $(".post_simple").css({display: "none"});
            $(".post_exact").css({display: "block"});
        }
    }
}

$(() => {
    // 보기 모드 쿠키 읽기
    let viewMode = $.cookie("viewmode")
    if (viewMode == "exact") {
        $("#view_simple").prop("checked", false);
        $("#view_exact").prop("checked", true);
    }

    if (viewMode == undefined || (viewMode != "simple" && viewMode != "exact")) {
        viewMode = "simple";
    }
    $("input[name='view_mode']").change(() => {
        let changedValue = $("input[name='view_mode']:checked").val();
        setViewMode(changedValue);
    });
    setViewMode(viewMode);

    // 카테고리 원본 가져오고 로딩 표시
    var postSimple = $(".post_simple origin"); // 카테고리 버튼 원본
    var postExact = $(".post_exact origin");

    // httpRequest("GET", "http://localhost:8888/getcategory?page=")
});