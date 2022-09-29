function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

$(() => {
  let main = $(".post_main");
  let content = $.parseHTML(main.text());
  main.text("");
  main.append(content);

  // 댓글 작성
  let replying = false;
  let replyTo = null;
  let send = $("#comments_send");
  let input = $("#comments_input");
  let replyDisplay = $(".comments_reply");
  replyDisplay.find(".comments_reply_close").click(() => {
    replying = false;
    replyDisplay.removeClass("replying");
    input.removeClass("replying");
  });
  send.click(() => {
    let content = input.val();
    if (content.length > 0 && content.length <= 200) {
      alert(content.length);
    } else {
      alert(
        "댓글은 0 ~ 200자까지 작성할 수 있습니다!\n현재 " +
          content.length +
          "자 입력했습니다."
      );
    }
  });

  // 댓글 메뉴
  $(".comment_ellipsis").each((ind, obj) => {
    let menuActivated = false;
    let button = $(obj).find(".comment_ellipsis_button");
    let menu = $(obj).find(".comment_ellipsis_menu");
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
      replying = true;
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
});
