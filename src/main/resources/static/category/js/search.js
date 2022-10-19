const urlEncode = (parameters) => {
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
  return queryString;
}

const search = (parameters) => {
  let queryString = urlEncode(parameters);
  let id = getPathParameter(1);
  if (id != null) {
    //console.log("http://localhost:8888/category/" + id + queryString);
    window.location.href = "http://localhost:8888/category/" + id + queryString;
  } else {
    window.location.href = "http://localhost:8888/" + getPathParameter(0) + queryString
  }
};
