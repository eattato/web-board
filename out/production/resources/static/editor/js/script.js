function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

function httpRequest(reqType, url, data, dataType) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open(reqType, url, false); // false for synchronous request
  if (reqType == "GET") {
    xmlHttp.send(null);
  } else if (reqType == "POST") {
    if (dataType != null) {
      xmlHttp.setRequestHeader("Content-Type", dataType);
    }
    xmlHttp.send(data);
  }
  return xmlHttp.responseText;
}

const encode = (target) => {
  let result = "";
  let ind = 0;
  for (let key in target) {
    result += key + "=" + encodeURI(target[key]);
    if (ind != target.length - 1) {
      result += "&";
    }
    ind++;
  }
  return result;
};

$(() => {
  var oEditors = [];
  nhn.husky.EZCreator.createInIFrame({
    oAppRef: oEditors,
    elPlaceHolder: "editor_text",
    sSkinURI: "../../../library/smarteditor2-2.8.2.3/SmartEditor2Skin.html",
    fCreator: "createSEditor2",
  });

  let category = $("#editor_category");
  let title = $("#editor_title");
  let post = $("#editor_post");
  let canPost = true;

  post.click(() => {
    if (canPost == true) {
      canPost = false;
      if (title.val().length > 0 && title.val().length <= 100) {
        // 에디터 내용을 editor_text로 이동
        oEditors.getById["editor_text"].exec("UPDATE_CONTENTS_FIELD", []);
        let content = $("#editor_text").val();
        if (content != "<p>&nbsp;</p>") {
          let data = {
            title: title.val(),
            content: content,
            category: category.val(),
            tag: "",
          };

          let errors = [
            "no session",
            "post not valid",
            "title not valid",
            "category does not exist",
            "data save failed",
          ];
          fetch("http://localhost:8888/post", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => response.text())
            .then((result) => {
              canPost = true;
              if (errors.indexOf(result) == -1) {
                let link = "http://localhost:8888/posts/" + result;
                window.location.href = link;
              }
            })
            .catch((result) => {
              canPost = true;
            });
        } else {
          alert("내용을 1자 이상 적어주세요.");
        }
      } else {
        alert("제목은 1 ~ 100자로 설정해주세요.");
      }
    }
  });
});
