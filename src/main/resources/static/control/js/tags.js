const getId = (comment) => {
  let id = null;
  let idText = comment.attr("id");
  if (idText.includes("tag") == true) {
    id = Number(idText.replace("tag", ""));
  }
  return id;
};

const rgb2hex = (rgb) =>
  `#${rgb
    .match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/)
    .slice(1)
    .map((n) => parseInt(n, 10).toString(16).padStart(2, "0"))
    .join("")}`;

$(() => {
  let addTag = $(".setting_tag_add");
  let tagInput = $(".setting_tag_input");
  let tagIncluded = {};
  let canPost = true;

  let tagDatas = [];
  $(".setting").each((ind, obj) => {
    let tag = $(obj);
    let data = {
      obj: tag,
    };
    data.title = tag.find(".setting_title").text();
    data.desc = tag.find(".setting_right_bottom").text();
    data.id = getId(tag);
    data.color = rgb2hex(tag.find(".setting_color").css("background-color"));
    tagDatas.push(data);
  });

  $(".setting_title").on("DOMSubtreeModified", function () {
    if (canPost == true) {
      let input = $(this).text();
      let target = $(this).parent().parent().parent().parent().parent();
      let tag = null;

      // 카테고리 배열 검색
      for (let ind in tagDatas) {
        let check = tagDatas[ind];
        if (check.obj.is(target)) {
          tag = check;
          break;
        }
      }

      if (tag != null) {
        tag.title = input;
        setTimeout(() => {
          if (tag.title == input) {
            canPost = false;
            let data = {
              act: "changeName",
              id: tag.id,
              target: input,
            };
            fetch("http://localhost:8888/tagset", {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(data),
            })
              .then((response) => response.text())
              .then((result) => {
                if (result == "ok") {
                } else if (result == "wrong name") {
                  alert("잘못된 카테고리명 입니다!");
                } else if (result == "wrong length") {
                  alert("카테고리명은 1 ~ 100자 입니다!");
                } else if (result == "no such tag") {
                  alert("존재하지 않는 카테고리입니다!");
                } else if (result == "no session") {
                  alert("로그인이 필요합니다!");
                } else if (result == "no access") {
                  alert("권한이 없습니다!");
                }
                canPost = true;
              })
              .finally(() => {
                canPost = true;
              });
          }
        }, 3000);
      }
    }
  });

  $(".setting_right_bottom").on("DOMSubtreeModified", function () {
    if (canPost == true) {
      let input = $(this).text();
      let target = $(this).parent().parent().parent();
      let tag = null;

      // 카테고리 배열 검색
      for (let ind in tagDatas) {
        let check = tagDatas[ind];
        if (check.obj.is(target)) {
          tag = check;
          break;
        }
      }

      if (tag != null) {
        tag.desc = input;
        setTimeout(() => {
          if (tag.desc == input) {
            canPost = false;
            let data = {
              act: "changeAbout",
              id: tag.id,
              target: input,
            };
            fetch("http://localhost:8888/tagset", {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(data),
            })
              .then((response) => response.text())
              .then((result) => {
                if (result == "ok") {
                } else if (result == "wrong name") {
                  alert("잘못된 카테고리 설명 입니다!");
                } else if (result == "wrong length") {
                  alert("카테고리 설명은 1 ~ 300자 입니다!");
                } else if (result == "no such tag") {
                  alert("존재하지 않는 카테고리입니다!");
                } else if (result == "no session") {
                  alert("로그인이 필요합니다!");
                } else if (result == "no access") {
                  alert("권한이 없습니다!");
                }
                canPost = true;
              })
              .finally(() => {
                canPost = true;
              });
          }
        }, 3000);
      } else {
        console.log("no tag");
      }
    }
  });

  $(".setting_admin").change(function () {
    console.log("change");
    if (canPost == true) {
      let input = $(this).is(":checked");
      let target = $(this).parent().parent().parent();

      let tag = null;
      // 카테고리 배열 검색
      for (let ind in tagDatas) {
        let check = tagDatas[ind];
        if (check.obj.is(target)) {
          tag = check;
          break;
        }
      }

      if (tag != null) {
        let data = {
          act: "changeAdmin",
          id: tag.id,
          boolTarget: input,
        };

        fetch("http://localhost:8888/tagset", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        })
          .then((response) => response.text())
          .then((result) => {
            if (result == "ok") {
            } else if (result == "no such tag") {
              alert("존재하지 않는 카테고리입니다!");
            } else if (result == "no session") {
              alert("로그인이 필요합니다!");
            } else if (result == "no access") {
              alert("권한이 없습니다!");
            }
            canPost = true;
          })
          .finally(() => {
            canPost = true;
          });
      }
    }
  });

  let removingCategory = -1;
  $(".setting_close").click(function () {
    let target = $(this).parent().parent().parent().parent().parent();
    let tag = null;

    // 카테고리 배열 검색
    for (let ind in tagDatas) {
      let check = tagDatas[ind];
      if (check.obj.is(target)) {
        tag = check;
        break;
      }
    }

    if (tag != null) {
      removingCategory = tag.id;
      $(".popup_title").text("정말 " + tag.title + " 태그를 삭제하시겠습니까?");
      $(".popup").addClass("activated");
    }
  });

  $(".popup_accept").click(() => {
    if (canPost == true) {
      if (removingCategory != -1) {
        canPost = false;
        let data = {
          id: removingCategory,
          act: "removeTag",
        };

        fetch("http://localhost:8888/tagset", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        })
          .then((response) => response.text())
          .then((result) => {
            if (result == "ok") {
              alert("태그를 삭제했습니다!");
              window.location.href = window.location.href;
            } else if (result == "no session") {
              alert("로그인이 필요합니다!");
            } else if (result == "no access") {
              alert("권한이 없습니다!");
            } else if (result == "no access") {
              alert("권한이 없습니다!");
            }
            canPost = true;
          })
          .finally(() => {
            canPost = true;
          });
      }
    }
  });

  $(".popup_cancel").click(() => {
    removingCategory = -1;
    $(".popup").removeClass("activated");
  });
  $(".popup_close").click(() => {
    removingCategory = -1;
    $(".popup").removeClass("activated");
  });

  $(".control_create").click(() => {
    if (canPost == true) {
      let tagName = $("#category_name").val();
      let tagAbout = $("#category_about").val();
      if (tagName.length >= 1 && tagName.length <= 100) {
        if (tagAbout.length >= 1 && tagAbout.length <= 300) {
          let data = {
            tag: tagName,
            about: tagAbout,
            color: rgb2hex($(".control_color").css("background-color")).replace(
              "#",
              ""
            ),
            admin: $(".control_checkbox_frame input").is(":checked"),
          };

          fetch("http://localhost:8888/addtag", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => response.text())
            .then((result) => {
              canPost = true;
              if (result == "ok") {
                alert("태그를 생성했습니다!");
                window.location.href = window.location.href;
              } else if (result == "no session") {
                alert("로그인이 필요합니다!");
              }
            })
            .finally(() => {
              canPost = true;
            });
        } else {
          alert("태그 설명은 1 ~ 300자 입니다!");
        }
      } else {
        alert("태그명은 1 ~ 30자 입니다!");
      }
    }
  });

  let colorEditting = null;
  var colorPicker = new iro.ColorPicker("#picker", {
    width: 170,
    color: "#ffffff",
  });
  colorPicker.on("color:change", function (color) {
    if (colorEditting != null) {
      if (colorEditting != "create") {
        colorEditting.color = colorPicker.color.hexString;
        colorEditting.obj.find(".setting_color").css({
          "background-color": colorPicker.color.hexString,
        });
      } else {
        $(".control_color").css({
          "background-color": colorPicker.color.hexString,
        });
      }
    }
  });

  const uploadColor = (tag) => {
    if (canPost == true) {
      canPost = false;
      let data = {
        act: "changeColor",
        id: tag.id,
        target: tag.color.replace("#", ""),
      };

      fetch("http://localhost:8888/tagset", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => response.text())
        .then((result) => {
          if (result == "ok") {
          } else if (result == "no session") {
            alert("로그인이 필요합니다!");
          } else if (result == "no access") {
            alert("권한이 없습니다!");
          }
          canPost = true;
        })
        .finally(() => {
          canPost = true;
        });
    }
  };

  $(".setting_color").click(function () {
    let input = $(this).text();
    let target = $(this).parent().parent().parent();
    let tag = null;

    // 카테고리 배열 검색
    for (let ind in tagDatas) {
      let check = tagDatas[ind];
      if (check.obj.is(target)) {
        tag = check;
        break;
      }
    }

    if (tag != null) {
      let cancel = false;
      if (colorEditting != null) {
        if (colorEditting != "create") {
          colorEditting.obj
            .find(".setting_color_editting")
            .removeClass("activate");
          if (colorEditting == tag) {
            cancel = true;
          }
          uploadColor(colorEditting);
        } else {
          $(".control_img_frame")
            .find(".setting_color_editting")
            .removeClass("activate");
        }
      }

      if (cancel == false) {
        colorEditting = tag;
        colorEditting.obj.find(".setting_color_editting").addClass("activate");
        console.log(colorEditting.color);
        colorPicker.color.hexString = colorEditting.color;
      } else {
        colorEditting = null;
      }
    }
  });
  $(".control_color").click(function () {
    let cancel = false;
    if (colorEditting != null) {
      if (colorEditting != "create") {
        colorEditting.obj
          .find(".setting_color_editting")
          .removeClass("activate");
        uploadColor(colorEditting);
      } else {
        $(".control_img_frame")
          .find(".setting_color_editting")
          .removeClass("activate");
        cancel = true;
      }
    }

    if (cancel == false) {
      colorEditting = "create";
      $(this).parent().find(".setting_color_editting").addClass("activate");
      colorPicker.color.hexString = rgb2hex($(this).css("background-color"));
    } else {
      colorEditting = null;
    }
  });
  //colorPicker.color.hexString
});
