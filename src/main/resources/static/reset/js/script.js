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
    field.find(".form_input").addClass("correct");
    field.find(".form_input").removeClass("error");
    field.find(".form_error").removeClass("error");
  } else {
    field.find(".form_error").text(error);
    field.find(".form_input").removeClass("correct");
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
  var email = $("#email");
  var code = $("#code");
  var commit = $(".form_button");
  var verify = $(".form_send");
  let postable = true;

  const emailChanged = function () {
    let input = email.find(".form_input").val();
    let error = null;

    emailChecking = false;
    let split = input.split("@");
    if (split.length == 2) {
      if (split[0].length < 1 || split[1].length < 1) {
        error = "올바른 이메일 형태가 아닙니다!";
      }
    } else {
      error = "올바른 이메일 형태가 아닙니다!";
    }

    if (error == null && input.length > 320) {
      error = "이메일이 너무 깁니다!";
    }

    inputField(email, error);
    return error;
  };

  verify.click(() => {
    let foundError = false;
    let emailError = emailChanged();
    if (emailError != null) {
      foundError = true;
      inputField(email, emailError);
    }

    if (foundError == false) {
      let data = {
        email: email.find(".form_input").val(),
      };
      postable = false;
      fetch("http://localhost:8888/reset", {
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
            inputField(email);
          } else if (result == "no email") {
            inputField(email, "이메일을 작성하지 않았습니다!");
          } else if (result == "could not find user") {
            inputField(email, "해당 계정이 존재하지 않습니다!");
          } else if (result == "email is not verified") {
            inputField(email, "해당 계정은 인증되지 않았습니다!");
          }
        })
        .finally(() => {
          postable = true;
        });
    }
  });

  commit.click(() => {
    let foundError = false;
    let emailError = emailChanged();
    if (emailError != null) {
      foundError = true;
      inputField(email, emailError);
    }
    if (code.find(".form_input").val().length != 6) {
      foundError = true;
      inputField(code, "코드는 6자리 입니다!");
    }

    if (foundError == false) {
      let data = {
        email: email.find(".form_input").val(),
        vcode: code.find(".form_input").val(),
      };
      postable = false;
      fetch("http://localhost:8888/reset", {
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
            window.location.href = "http://localhost:8888/reset"; // 새로고침
          } else if (result == "no email") {
            inputField(email, "이메일을 작성하지 않았습니다!");
          } else if (result == "could not find user") {
            inputField(email, "해당 계정이 존재하지 않습니다!");
          } else if (result == "email is not verified") {
            inputField(email, "해당 계정은 인증되지 않았습니다!");
          } else if (result == "verify code not found") {
            inputField(code, "인증 코드가 발급되지 않았습니다!");
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
