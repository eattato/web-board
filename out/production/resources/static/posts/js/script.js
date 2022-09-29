function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

const getId = (comment) => {
  let classList = comment.attr("class");
  classList = classList.split(" ");

  let commentId = null;
  let replyId = null;
  for (let ind = 0; ind < classList.length; ind++) {
    let className = classList[ind];
    if (className.includes("id") == true) {
      commentId = Number(className.replace("id", ""));
    } else if (className.includes("reply") == true) {
      replyId = Number(className.replace("reply", ""));
    }
  }
  return [commentId, replyId];
};

$(() => {
  let main = $(".post_main");
  let content = $.parseHTML(main.text());
  main.text("");
  main.append(content);

  // 댓글 작성
  let sendable = true;
  let replyTo = -1;
  let send = $("#comments_send");
  let input = $("#comments_input");
  let replyDisplay = $(".comments_reply");
  replyDisplay.find(".comments_reply_close").click(() => {
    replyTo = -1;
    replyDisplay.removeClass("replying");
    input.removeClass("replying");
  });
  send.click(() => {
    let content = input.val();
    if (content.length > 0 && content.length <= 200) {
      sendable = false;
      let currentPage = window.location.href;
      let pageUrl = currentPage.split("/");
      let url = "http://localhost:8888/comment";
      let data = {
        content: content,
        at: pageUrl[pageUrl.length - 1],
        to: replyTo,
      };
      fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => response.text())
        .then((result) => {
          if (result == "ok") {
            input.val("");
            alert("댓글을 성공적으로 전송했습니다!");
            window.location.href = window.location.href;
          } else if (result == "target comment not found") {
            alert("해당 댓글은 존재하지 않거나 삭제된 댓글입니다.");
          } else {
            alert("댓글을 전송하지 못했습니다!");
          }
        })
        .catch(() => {
          alert("댓글을 전송하지 못했습니다!");
        })
        .finally(() => {
          sendable = true;
        });
    } else {
      alert(
        "댓글은 0 ~ 200자까지 작성할 수 있습니다!\n현재 " +
          content.length +
          "자 입력했습니다."
      );
    }
  });

  // 댓글 메뉴
  let comments = [];
  const findInComments = (id) => {
    let result = null;
    for (let ind = 0; ind < comments.length; ind++) {
      let comment = comments[ind];
      if (comment[0] == id) {
        result = comment;
        break;
      }
    }
    return result;
  };
  $(".comment").each((ind, obj) => {
    let comment = $(obj);
    let ids = getId(comment);
    let commentId = ids[0];
    let replyId = ids[1];
    comments.push([commentId, replyId, comment]);

    let menuActivated = false;
    let button = comment.find(".comment_ellipsis_button");
    let menu = comment.find(".comment_ellipsis_menu");
    let reply = menu.find(".comment_ellipsis_reply");
    let report = menu.find(".comment_ellipsis_report");

    let menuDisplay = () => {
      if (menuActivated == true) {
        menu.addClass("activated");
      } else {
        menu.removeClass("activated");
      }
    };

    button.click(() => {
      if (menuActivated == false) {
        menuActivated = true;
      } else {
        menuActivated = false;
      }
      menuDisplay();
    });

    reply.click(() => {
      let ids = getId(comment);
      let commentId = ids[0];
      replyTo = commentId;

      let replyingAuthor = comment.find(".comments_name");
      replyDisplay
        .find(".comments_reply_target")
        .text(replyingAuthor.text() + "에게 다는 답글");
      replyDisplay.addClass("replying");
      input.addClass("replying");
      menuActivated = false;
      menuDisplay();
    });

    report.click(() => {
      menuActivated = false;
      menuDisplay();
    });
  });

  // 댓글 정렬
  $(".comment").each((ind, obj) => {
    let comment = $(obj);
    let ids = getId(comment);
    let commentId = ids[0];
    let replyId = ids[1];

    if (commentId != null && replyId != null) {
      if (replyId != -1) {
        let parentComment = findInComments(replyId);
        if (parentComment != null) {
          comment.detach().appendTo(parentComment.find(".comment_holder"));
        } else {
          // comment.remove();
        }
      }
    }
  });
});
