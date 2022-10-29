const getId = (comment) => {
  let id = null;
  let idText = comment.attr("id");
  if (idText.includes("category") == true) {
    id = Number(idText.replace("category", ""));
  }
  return id;
};

$(() => {
  let addTag = $(".setting_tag_add");
  let tagInput = $(".setting_tag_input");
  let tagIncluded = {};
  let canPost = true;

  let categoryDatas = [];
  $(".setting").each((ind, obj) => {
    let category = $(obj);
    let data = {
      obj: category,
    };
    data.title = category.find(".setting_title").text();
    data.desc = category.find(".setting_right_bottom").text();
    data.id = getId(category);

    let admins = [];
    category.find(".setting_tag_name").each((ind, obj) => {
      let tag = $(obj);
      admins.push(tag.text());
    });
    data.admins = admins;
    categoryDatas.push(data);
  });

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
  };

  // 태그 추가 버튼
  $(".setting_tag_display").click(() => {
    let enabled = addTag.hasClass("activated");
    if (enabled == false) {
      addTag.addClass("activated");
    } else {
      addTag.removeClass("activated");
    }
  });
  $(".setting_tag_commit").click(function () {
    if (canPost == true) {
      let input = tagInput.val();
      let target = $(this).parent().parent().parent().parent().parent();
      let category = null;

      // 카테고리 배열 검색
      for (let ind in categoryDatas) {
        let check = categoryDatas[ind];
        if (check.obj.is(target)) {
          if (check.admins.includes(input) == false) {
            category = check;
          } else {
            alert("해당 관리자가 이미 존재합니다!");
          }
          break;
        }
      }

      if (category != null) {
        canPost = false;
        fetch("http://localhost:8888/categoryset", {
          method: "POST",
          header: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            id: category.id,
            act: "addAdmin",
            target: input,
          }),
        })
          .then((response) => response.text())
          .then((result) => {
            if (result == "ok") {
              let html =
                "<div class='setting_tag'><div class='setting_tag_name'>" +
                input +
                "</div><div class='setting_tag_close'>x</div></div>";
              target.find(".setting_tags").append($.parseHTML(html));
            } else if (result == "user not found") {
              alert("존재하지 않는 유저입니다!");
            } else if (result == "already exist") {
              alert("해당 관리자가 이미 존재합니다!");
            } else if (result == "no session") {
              alert("로그인이 필요합니다!");
            }
            canPost = true;
          })
          .finally(() => {
            canPost = true;
          });
      }
    }
  });

  $(".setting_title").change(function () {
    let input = $(this).text();
    let target = $(this).parent().parent().parent().parent().parent();
    let category = null;

    // 카테고리 배열 검색
    for (let ind in categoryDatas) {
      let check = categoryDatas[ind];
      if (check.obj.is(target)) {
        category = check;
        break;
      }
    }

    if (category != null) {
      fetch("http://localhost:8888/categoryset", {
        method: "POST",
        header: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          act: "changeName",
          id: category.id,
          target: input,
        }),
      })
        .then((response) => response.text())
        .then((result) => {
          if (result == "ok") {
          } else if (result == "wrong name") {
            alert("잘못된 카테고리명 입니다!");
          } else if (result == "no session") {
            alert("로그인이 필요합니다!");
          }
          canPost = true;
        })
        .finally(() => {
          canPost = true;
        });
    }
  });

  // post.click(() => {
  //   if (canPost == true) {
  //     canPost = false;
  //     if (title.val().length > 0 && title.val().length <= 100) {
  //       // 에디터 내용을 editor_text로 이동
  //       oEditors.getById["editor_text"].exec("UPDATE_CONTENTS_FIELD", []);
  //       let content = $("#editor_text").val();
  //       if (content != "<p>&nbsp;</p>") {
  //         let data = {
  //           title: title.val(),
  //           content: content,
  //           category: category.val(),
  //           tags: Object.keys(tagIncluded).join(" "),
  //         };
  //         let id = null;
  //         if (getPathParameter(1) != null) {
  //           id = Number(getPathParameter(1));
  //           data.id = id;
  //         }

  //         let errors = {
  //           "no session": "로그인이 필요합니다!",
  //           "post not valid": "내용이 유효하지 않습니다!",
  //           "title not valid": "제목이 유효하지 않습니다!",
  //           "category does not exist": "존재하지 않는 카테고리입니다!",
  //           "data save failed": "글을 게시하는 데 실패했습니다.",
  //           "tag not found": "존재하지 않는 태그를 사용했습니다!",
  //           "post not found": "수정할 글을 찾지 못했습니다!",
  //           "no access": "해당 글의 수정 권한이 없습니다!",
  //         };

  //         fetch("http://localhost:8888/post", {
  //           method: "POST",
  //           headers: {
  //             "Content-Type": "application/json",
  //           },
  //           body: JSON.stringify(data),
  //         })
  //           .then((response) => response.text())
  //           .then((result) => {
  //             canPost = true;
  //             if (result in errors) {
  //               alert(errors[result]);
  //             } else {
  //               let link = "http://localhost:8888/posts/" + result;
  //               window.location.href = link;
  //             }
  //           })
  //           .catch((result) => {
  //             canPost = true;
  //             console.log(result);
  //           });
  //       } else {
  //         alert("내용을 1자 이상 적어주세요.");
  //       }
  //     } else {
  //       alert("제목은 1 ~ 100자로 설정해주세요.");
  //     }
  //   }
  // });
});
