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

const getUrlParameter = function getUrlParameter(sParam) {
  var sPageURL = window.location.search.substring(1),
    sURLVariables = sPageURL.split("&"),
    sParameterName,
    i;

  for (i = 0; i < sURLVariables.length; i++) {
    sParameterName = sURLVariables[i].split("=");

    if (sParameterName[0] === sParam) {
      return sParameterName[1] === undefined
        ? true
        : decodeURIComponent(sParameterName[1]);
    }
  }
  return false;
};

$(() => {
  // 쿠키 읽기
  let viewMode = $.cookie("viewmode");
  let searchFilters = $.cookie("searchFilter");
  let sortType = $.cookie("sorttype");
  let sortMode = $.cookie("sortmode");

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
    search({
      viewmode: viewMode,
      search: $(".search_input").val(),
      title: $("#search_filter").find("input[value='title']").val(),
      author: $("#search_filter").find("input[value='author']").val(),
      content: $("#search_filter").find("input[value='content']").val(),
      date: $("#search_filter").find("input[value='date']").val(),
    });
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

        search({
          viewmode: viewMode,
          search: $(".search_input").val(),
          title: $("#search_filter")
            .find("input[value='title']")
            .attr("checked"),
          author: $("#search_filter")
            .find("input[value='author']")
            .attr("checked"),
          content: $("#search_filter")
            .find("input[value='content']")
            .attr("checked"),
          date: $("#search_filter").find("input[value='date']").attr("checked"),
        });
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
    search({
      viewmode: viewMode,
      search: $(".search_input").val(),
      title: $("#search_filter").find("input[value='title']").val(),
      author: $("#search_filter").find("input[value='author']").val(),
      content: $("#search_filter").find("input[value='content']").val(),
      date: $("#search_filter").find("input[value='date']").val(),
    });
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
    search({
      viewmode: viewMode,
      search: $(".search_input").val(),
      title: $("#search_filter").find("input[value='title']").val(),
      author: $("#search_filter").find("input[value='author']").val(),
      content: $("#search_filter").find("input[value='content']").val(),
      date: $("#search_filter").find("input[value='date']").val(),
    });
  });
  setSortMode(sortMode);
});
