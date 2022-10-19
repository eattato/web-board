$(() => {
  // 세션 데이터 읽기
  let sidebarMenu = {
    viewmode: $("input[name='view_mode']:checked").val(),
    title: $("input[name='title']").is(":checked"),
    author: $("input[name='author']").is(":checked"),
    content: $("input[name='content']").is(":checked"),
    date: $("input[name='date']").is(":checked"),
    sort: $("input[name='sort']:checked").val(),
    direction: $("input[name='direction']:checked").val(),
  }

  const applySidebar = () => {
    fetch("http://localhost:8888/sidebar", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(sidebarMenu)
    }).then(() => {
      window.location.href = window.location.href;
    });
  }

  // 보기 모드 변경
  $("input[name='view_mode']").change(() => {
    sidebarMenu.viewmode = $("input[name='view_mode']:checked").val();
    applySidebar();
  });

  // 검색 필터 쿠키 설정
  $("#search_filter")
    .find("input")
    .each((ind, obj) => {
      $(obj).change(() => {
        sidebarMenu[$(obj).attr("name")] = $(obj).is(":checked");
        applySidebar();
      });
    });

  $("input[name='sort']").change(() => {
    console.log("sort changed");
    sidebarMenu.sort = $("input[name='sort']:checked").val();
    applySidebar();
  });

  $("input[name='direction']").change(() => {
    console.log("direction changed");
    sidebarMenu.direction = $("input[name='direction']:checked").val();
    applySidebar();
  });
});
