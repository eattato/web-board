function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

$(() => {
  // 카테고리 원본 가져오고 로딩 표시
  var postSimple = $(".post_simple origin"); // 카테고리 버튼 원본
  var postExact = $(".post_exact origin");

  // httpRequest("GET", "http://localhost:8888/getcategory?page=")
});

$(window).on("load", () => {
  // 자세히 보기 내용 HTML 태그 제거
  $(".post_exact p").each((ind, obj) => {
    let previewText = $(obj);
    let textValue = previewText.text();

    let limit = 140;
    if (textValue.length > limit) {
      while (textValue.length > limit) {
        textValue = textValue.substring(0, textValue.length - 1);
      }
      textValue += "..";
    }
    previewText.text(textValue);
    previewText.css({ display: "block" });
  });
});
