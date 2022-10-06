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

const setSortType = (sortType) => {
  if (sortType == "loved" || sortType == "new") {
    $.cookie("sorttype", sortType, { expires: 2147483647 });
  }
};

const setSortMode = (sortMode) => {
  if (sortMode == "up" || sortMode == "down") {
    $.cookie("sortmode", sortMode, { expires: 2147483647 });
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
  let sortType = $.cookie("sorttype");
  let sortMode = $.cookie("sortmode");

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
  if (viewMode == "exact") {
    $("#view_simple").prop("checked", false);
    $("#view_exact ").prop("checked", true);
  }
  $("input[name='view_mode']").change(() => {
    viewMode = $("input[name='view_mode']:checked").val();
    setViewMode(viewMode);
    sendSearch();
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
        sendSearch();
      });
    });

  if (sortType == "new") {
    $("#view_loved").prop("checked", false);
    $("#view_new").prop("checked", true);
  }
  if (sortType == undefined || (sortType != "loved" && sortType != "new")) {
    sortType = "loved";
  }
  setSortType(sortType);
  $("input[name='sort']").change(() => {
    let changedValue = $("input[name='sort']:checked").val();
    setSortType(changedValue);
    sendSearch();
  });

  if (sortMode == "down") {
    $("#view_up").prop("checked", false);
    $("#view_down").prop("checked", true);
  }
  if (sortMode == undefined || (sortMode != "up" && sortMode != "down")) {
    sortMode = "up";
  }
  $("input[name='direction']").change(() => {
    let changedValue = $("input[name='direction']:checked").val();
    setSortMode(changedValue);
    sendSearch();
  });
  setSortMode(sortMode);
});
