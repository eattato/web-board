function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

const setViewMode = (viewMode) => {
  // console.log(viewMode);
  if (viewMode == "simple" || viewMode == "exact") {
    $.cookie("viewmode", viewMode, { expires: 2147483647 });
    if (viewMode == "simple") {
      $(".post_simple").css({ display: "block" });
      $(".post_exact").css({ display: "none" });
    } else if (viewMode == "exact") {
      $(".post_simple").css({ display: "none" });
      $(".post_exact").css({ display: "block" });
    }
  }
};

const removeHTML = (target) => {
  let inTag = false;
  let ind = 0;
  while (ind <= target.length - 1) {
    let char = target.charAt(ind);
    let cutThis = false;
    if (char == "<") {
      inTag = true;
      cutThis = true;
    } else if (char == ">") {
      inTag = false;
      cutThis = true;
    } else if (inTag == true) {
      cutThis = true;
    }

    if (cutThis == true) {
      target = target.substring(0, ind) + target.substring(ind + 1);
    } else {
      ind++;
    }
  }
  return target;
};

$(() => {
  // 보기 모드 쿠키 읽기
  let viewMode = $.cookie("viewmode");
  if (viewMode == "exact") {
    $("#view_simple").prop("checked", false);
    $("#view_exact ").prop("checked", true);
  }

  if (viewMode == undefined || (viewMode != "simple" && viewMode != "exact")) {
    viewMode = "simple";
  }
  $("input[name='view_mode']").change(() => {
    let changedValue = $("input[name='view_mode']:checked").val();
    setViewMode(changedValue);
  });
  setViewMode(viewMode);

  $(".post_exact p").each((ind, obj) => {
    let previewText = $(obj);
    let textValue = previewText.text();
    textValue = removeHTML(textValue);

    let longHeight = 120;
    previewText.text(textValue);
    if (previewText.height() > longHeight) {
      while (previewText.height() > longHeight) {
        textValue = textValue.substring(0, textValue.length - 1);
        previewText.text(textValue + "..");
      }
    }
    previewText.css({ display: "block" });
  });

  // 카테고리 원본 가져오고 로딩 표시
  var postSimple = $(".post_simple origin"); // 카테고리 버튼 원본
  var postExact = $(".post_exact origin");

  // httpRequest("GET", "http://localhost:8888/getcategory?page=")
});
