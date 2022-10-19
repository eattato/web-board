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
  let addTag = $(".editor_tag_add");
  let tagRecommends = $(".editor_tag_recommend .editor_tag_name");
  let tagInput = $(".editor_tag_input");
  let tagIncluded = {};
  let canPost = true;

  const addTagToTab = (tag) => {
    let idstr = tag.attr("id");
    let id = -1;
    if (idstr.includes("tag") == true) {
      id = Number(idstr.replace("tag", ""));
    }

    if (id != -1) {
      tag.click(() => {
        if (id in tagIncluded == false) {
          let newTag = $("<li class='editor_tag'></li>");
          tagIncluded[id] = newTag;
          tag.clone().appendTo(newTag);
          let closeButton = $("<div class='editor_tag_close'>x</div>");
          newTag.append(closeButton);
          newTag.detach().prependTo(".editor_bottom > ul");

          closeButton.click(() => {
            if (id in tagIncluded) {
              tagIncluded[id].remove();
              delete tagIncluded[id];
            }
          });
        }
      });
      console.log("done");
    } else {
      console.log("id wrong");
    }
  }

  // 태그 추가 버튼
  $(".editor_tag_display").click(() => {
    let enabled = addTag.hasClass("activated");
    if (enabled == false) {
      addTag.addClass("activated");
    } else {
      addTag.removeClass("activated");
    }
  });

  // 태그 추천으로 추가
  tagRecommends.each((ind, obj) => {
    let tag = $(obj);
    addTagToTab(tag);
  });
  // 수정 시 예전에 있던 태그 추가
  $(".past .editor_tag_name").each((ind, obj) => {
    let tag = $(obj);
    let idstr = tag.attr("id");
    let id = -1;
    if (idstr.includes("tag") == true) {
      id = Number(idstr.replace("tag", ""));
    }

    let newTag = $("<li class='editor_tag'></li>");
    tagIncluded[id] = newTag;
    tag.detach().appendTo(newTag);
    let closeButton = $("<div class='editor_tag_close'>x</div>");
    newTag.append(closeButton);
    newTag.detach().prependTo(".editor_bottom > ul");

    closeButton.click(() => {
      if (id in tagIncluded) {
        tagIncluded[id].remove();
        delete tagIncluded[id];
      }
    });
  })

  // 태그 입력 추천
  tagInput.change(() => {
    let inputValue = tagInput.val();
    if (inputValue == null) {
      inputValue = "";
    }
    tagRecommends.each((ind, obj) => {
      let tag = $(obj);
      let tagName = tag.text();
      if (tagName.includes(inputValue)) {
        tag.css({ display: "block" });
      } else {
        tag.css({ display: "none" });
      }
    });
  });

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
            tags: Object.keys(tagIncluded).join(" "),
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
