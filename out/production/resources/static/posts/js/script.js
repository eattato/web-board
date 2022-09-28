function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

$(() => {
  console.log("loading");
  let main = $(".post_main");
  let content = $.parseHTML(main.text());
  main.text("");
  main.append(content);
});
