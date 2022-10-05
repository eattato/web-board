function httpGet(url) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", url, false); // false for synchronous request
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

function httpRequest(reqType, url, data, dataType) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open(reqType, url, false); // false for synchronous request
  if (reqType == "GET") {
    xmlHttp.send(null);
  } else if (reqType == "POST") {
    if (dataType != null) {
      xmlHttp.setRequestHeader("Content-Type", dataType);
    }
    xmlHttp.send(data);
  }
  return xmlHttp.responseText;
}

const inputField = (field, error) => {
  if (error == null) {
    // field.find(".form_input").addClass("correct");
    field.find(".form_input").removeClass("error");
    field.find(".form_error").removeClass("error");
  } else {
    field.find(".form_error").text(error);
    // field.find(".form_input").removeClass("correct");
    field.find(".form_input").addClass("error");
    field.find(".form_error").addClass("error");
  }
};

const inputBack = (field) => {
  // field.find(".form_input").removeClass("correct");
  field.find(".form_input").removeClass("error");
  field.find(".form_input").removeClass("error");
  field.find(".form_error").removeClass("error");
};

const encode = (target) => {
  let result = "";
  let ind = 0;
  for (let key in target) {
    result += key + "=" + encodeURI(target[key]);
    if (ind != target.length - 1) {
      result += "&";
    }
    ind++;
  }
  return result;
};

$(() => {
  var code = $("#code");
  var commit = $(".form_button");
  let postable = true;

  commit.click(() => {
    let foundError = false;
    if (code.find(".form_input").val().length != 6) {
      foundError = true;
      inputField(code, "코드는 6자리 입니다!");
    }

    if (foundError == false) {
      let data = {
        vcode: code.find(".form_input").val(),
      };
      postable = false;
      fetch("http://localhost:8888/verify", {
        method: "POST",
        body: JSON.stringify(data),
        headers: {
          "Content-Type": "application/json",
        },
      })
        .then((response) => response.text())
        .then((result) => {
          if (result == "ok") {
            //window.location.replace("https://localhost:8888");
            alert("이메일 인증이 완료되었습니다!");
            window.location.href = "http://localhost:8888";
          } else if (result == "code does not match") {
            inputField(code, "인증 코드가 일치하지 않습니다!");
          } else if (result == "failed") {
            inputField(code, "인증에 실패하였습니다!");
          }
        })
        .finally(() => {
          postable = true;
        });
    }
  });
});
