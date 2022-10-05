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

const setSearchFilter = (searchFilter) => {
  for (key in searchFilter) {
    $("#search_filter")
      .find("input[name='" + key + "']")
      .prop("checked", searchFilter[key]);
  }
};

$(() => {
  // 쿠키 읽기
  let viewMode = $.cookie("viewmode");
  let searchFilters = $.cookie("searchFilter");

  // 보기 모드 쿠키 설정
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

  // 검색 필터 쿠키 설정
  if (searchFilters == null) {
    searchFilters = {
      title: true,
      author: true,
      content: true,
      date: false,
    };
    $.cookie("searchFilter", JSON.stringify(searchFilters), {
      expires: 2147483647,
    });
  } else {
    searchFilters = JSON.parse(searchFilters);
  }
  setSearchFilter(searchFilters);
  $("#search_filter")
    .find("input")
    .each((ind, obj) => {
      $(obj).change(() => {
        searchFilters[$(obj).attr("name")] = $(obj).is(":checked");
        $.cookie("searchFilter", JSON.stringify(searchFilters), {
          expires: 2147483647,
        });
      });
    });
});
