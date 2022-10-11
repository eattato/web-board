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

const saveCookie = (data) => {
  $.cookie("boardarc", JSON.stringify(data), { expires: 2147483647 });
};

$(() => {
  // 쿠키 읽기
  let viewMode = $.cookie("viewmode");
  let searchFilters = $.cookie("searchFilter");
  let sortType = $.cookie("sorttype");
  let sortMode = $.cookie("sortmode");

  let cookieData = $.cookie("boardarc");
  let originData = {
    viewMode: "simple",
    searchFilters: {
      title: true,
      author: true,
      content: true,
      date: false,
    },
    sortType: "loved",
    sortMode: "down",
  };

  if (cookieData == null) {
    cookieData = originData;
  } else {
    cookieData = JSON.parse(cookieData);
  }

  const sendSearch = () => {
    let filterResult = {};
    for (key in searchFilters) {
      if (searchFilters[key] == true) {
        filterResult[key] = "on";
      } else {
        filterResult[key] = null;
      }
    }

    search({
      viewmode: viewMode,
      search: $(".search_input").val(),
      title: filterResult["title"],
      author: filterResult["author"],
      content: filterResult["content"],
      date: filterResult["date"],
      sort: sortType,
      direction: sortMode,
    });
  };

  // 보기 모드 쿠키 설정
  if (cookieData.viewMode == "exact") {
    $("#view_simple").prop("checked", false);
    $("#view_exact").prop("checked", true);
  }
  $("input[name='view_mode']").change(() => {
    cookieData.viewMode = $("input[name='view_mode']:checked").val();
    saveCookie(cookieData);
    setViewMode(cookieData.viewMode);
    sendSearch();
  });
  setViewMode(cookieData.viewMode);

  // 검색 필터 쿠키 설정
  setSearchFilter(cookieData.searchFilters);
  $("#search_filter")
    .find("input")
    .each((ind, obj) => {
      $(obj).change(() => {
        cookieData.searchFilters[$(obj).attr("name")] = $(obj).is(":checked");
        saveCookie(cookieData);
        sendSearch();
      });
    });

  if (cookieData.sortType == "new") {
    $("#view_loved").prop("checked", false);
    $("#view_new").prop("checked", true);
  }
  $("input[name='sort']").change(() => {
    cookieData.sortType = $("input[name='sort']:checked").val();
    saveCookie(cookieData);
    sendSearch();
  });

  if (cookieData.sortMode == "down") {
    $("#view_up").prop("checked", false);
    $("#view_down").prop("checked", true);
  }
  $("input[name='direction']").change(() => {
    cookieData.sortMode = $("input[name='direction']:checked").val();
    saveCookie(cookieData);
    sendSearch();
  });
});
