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
      //console.log("done");
    } else {
      //console.log("id wrong");
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
  tagInput.on("input", () => {
    let inputValue = tagInput.val();
    if (inputValue == null) {
      inputValue = "";
    }
    let inputSplit = Hangul.disassemble(inputValue);

    tagRecommends.each((ind, obj) => {
      let tag = $(obj);
      let tagName = tag.text();
      let searched = true;

      // fuzzy searching
      let tagSplit = Hangul.disassemble(tagName);
      for (let ind in inputSplit) {
        let foundChar = false;
        for (let tagInd in tagSplit) {
          if (inputSplit[ind] === tagSplit[tagInd]) {
            foundChar = true;
            tagSplit.splice(tagInd, 1);
            //console.log(tagName + ":" + inputSplit[ind] + "(" + ind + ")" + " == " + cutted + "(" + tagInd + ")" + + ", used " + tagSplit[tagInd] + ", now " + tagSplit.join(", ") + " current");
            break;
          }
        }

        if (foundChar == false) {
          searched = false;
          break;
        }
      }

      if (searched == true) {
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
          let id = null;
          if (getPathParameter(1) != null) {
            id = Number(getPathParameter(1));
            data.id = id;
          }

          let errors = {
            "no session": "로그인이 필요합니다!",
            "post not valid": "내용이 유효하지 않습니다!",
            "title not valid": "제목이 유효하지 않습니다!",
            "category does not exist": "존재하지 않는 카테고리입니다!",
            "data save failed": "글을 게시하는 데 실패했습니다.",
            "tag not found": "존재하지 않는 태그를 사용했습니다!",
            "post not found": "수정할 글을 찾지 못했습니다!",
            "no access": "해당 글의 수정 권한이 없습니다!"
          };

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
              if (result in errors) {
                alert(errors[result]);
              } else {
                let link = "http://localhost:8888/posts/" + result;
                window.location.href = link;
              }
            })
            .catch((result) => {
              canPost = true;
              console.log(result);
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
