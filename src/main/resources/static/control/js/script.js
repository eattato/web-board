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

      let data = {
        id: category.id,
        act: "addAdmin",
        target: input,
      };

      if (category != null) {
        canPost = false;
        fetch("http://localhost:8888/categoryset", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        })
          .then((response) => response.text())
          .then((result) => {
            if (result == "ok") {
              let html =
                "<div class='setting_tag'><div class='setting_tag_name'>" +
                input +
                "</div><div class='setting_tag_close'>x</div></div>";
              let newAdmin = target
                .find(".setting_tags")
                .prepend($.parseHTML(html));
              newAdmin.find(".setting_tag_close").click(function () {
                adminRemove($(this));
              });
            } else if (result == "user not found") {
              alert("존재하지 않는 유저입니다!");
            } else if (result == "already exist") {
              alert("해당 관리자가 이미 존재합니다!");
            } else if (result == "no such category") {
              alert("존재하지 않는 카테고리입니다!");
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

  const adminRemove = (button) => {
    let input = button.parent().find(".setting_tag_name").text();
    let target = button.parent().parent().parent().parent().parent();
    let category = null;

    // 카테고리 배열 검색
    for (let ind in categoryDatas) {
      let check = categoryDatas[ind];
      if (check.obj.is(target)) {
        category = check;
        break;
      }
    }

    let data = {
      id: category.id,
      act: "removeAdmin",
      target: input,
    };

    if (category != null) {
      canPost = false;
      fetch("http://localhost:8888/categoryset", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => response.text())
        .then((result) => {
          if (result == "ok") {
            button.parent().remove();
            button.off("click");
          } else if (result == "user not found") {
            alert("존재하지 않는 유저입니다!");
          } else if (result == "no such category") {
            alert("존재하지 않는 카테고리입니다!");
          } else if (result == "no session") {
            alert("로그인이 필요합니다!");
          }
          canPost = true;
        })
        .finally(() => {
          canPost = true;
        });
    }
  };
  $(".setting_tag_close").click(function () {
    adminRemove($(this));
  });

  $(".setting_title").on("DOMSubtreeModified", function () {
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
      category.title = input;
      setTimeout(() => {
        if (category.title == input) {
          let data = {
            act: "changeName",
            id: category.id,
            target: input,
          };
          fetch("http://localhost:8888/categoryset", {
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
              } else if (result == "no such category") {
                alert("존재하지 않는 카테고리입니다!");
              } else if (result == "no session") {
                alert("로그인이 필요합니다!");
              }
              canPost = true;
            })
            .finally(() => {
              canPost = true;
            });
        }
      }, 3000);
    }
  });

  $(".setting_right_bottom").on("DOMSubtreeModified", function () {
    let input = $(this).text();
    let target = $(this).parent().parent().parent();
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
      category.desc = input;
      setTimeout(() => {
        if (category.desc == input) {
          let data = {
            act: "changeAbout",
            id: category.id,
            target: input,
          };
          fetch("http://localhost:8888/categoryset", {
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
              } else if (result == "no such category") {
                alert("존재하지 않는 카테고리입니다!");
              } else if (result == "no session") {
                alert("로그인이 필요합니다!");
              }
              canPost = true;
            })
            .finally(() => {
              canPost = true;
            });
        }
      }, 3000);
    } else {
      console.log("no category");
    }
  });

  async function readFileAsDataURL(file) {
    let result_base64 = await new Promise((resolve) => {
      let fileReader = new FileReader();
      fileReader.onload = (e) => resolve(fileReader.result);
      fileReader.readAsDataURL(file);
    });

    return result_base64;
  }
  const imgChanged = async function () {
    $("#profile_img_select").attr("title", " ");
    if ($("#profile_img_select")[0].files.length == 1) {
      let encoded = await readFileAsDataURL(
        $("#profile_img_select")[0].files[0]
      );
      $(".profile_img_frame img").attr("src", encoded);
    }
    // imgSelect.val("");
  };
  $("#profile_img_select").change(imgChanged);

  $(".control_create").click(async () => {
    let data = {
      category: $("#category_name").val(),
      about: $("#category_about").val(),
      image: null,
    };
    if (imgSelect[0].files.length == 1) {
      let encoded = await readFileAsDataURL(imgSelect[0].files[0]);
      encoded = encoded.replace("data:image/png;base64,", "");
      encoded = encoded.replaceAll("+", "-");
      encoded = encoded.replaceAll("/", "_");
      data["image"] = encoded;
    }

    fetch("http://localhost:8888/addcategory", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data);
    })
    .then((response) => response.text())
    .then((result) => {
      if (result == "ok") {
        alert("카테고리를 생성했습니다!");
        window.location.replace("http://localhost:8888/profile");
      } else if (result == "no session") {
        alert("로그인이 필요합니다!");
      }
    });
  });
});
