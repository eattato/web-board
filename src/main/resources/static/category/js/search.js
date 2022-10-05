const search = (parameters) => {
  let queryString = "";
  let first = true;
  for (key in parameters) {
    let val = parameters[key];
    if (val != null) {
      if ((typeof val == "string" && val.length == 0) == false) {
        if (first == true) {
          first = false;
          queryString += "?";
        } else {
          queryString += "&";
        }
        queryString += key + "=" + parameters[key];
      }
    }
  }
  let id = getUrlParameter("id");
  if (id == false) {
    id = 1;
  }
  window.location.href = "http://localhost:8888/category/" + id + queryString;
};
