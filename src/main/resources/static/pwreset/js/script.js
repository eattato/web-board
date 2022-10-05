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
  var password = $("#password");
  var pwCheck = $("#pwCheck");
  var commit = $(".form_button");
  var cancel = $(".form_cancel");
  let postable = true;

  const passwordChanged = function () {
    let input = password.find(".form_input").val();
    let error = null;

    if (error == null) {
      let hasEng = false;
      let hasNum = false;
      let hasSpc = false;
      for (let ind = 0; ind < input.length; ind++) {
        let word = input.charCodeAt(ind);
        if ((word >= 65 && word <= 90) || (word >= 97 && word <= 122)) {
          hasEng = true;
        } else if (word >= 48 && word <= 57) {
          hasNum = true;
        } else if (word >= 33 && word <= 38) {
          hasSpc = true;
        } else {
          error = "사용할 수 없는 문자가 포함되어 있습니다!";
          break;
        }
      }
      if (!hasEng || !hasNum || !hasSpc) {
        error = "영문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.";
      }
    }

    if (error == null && input.length < 8) {
      error = "비밀번호는 최소 8자 이상입니다!";
    }

    inputField(password, error);
    pwCheckChanged();
    return error;
  };
  const pwCheckChanged = function () {
    let input = pwCheck.find(".form_input").val();
    let error = null;

    if (input != password.find(".form_input").val()) {
      error = "비밀번호가 일치하지 않습니다!";
    }
    inputField(pwCheck, error);
    return error;
  };
  const changedEvents = [passwordChanged, pwCheckChanged];

  password.find(".form_input").change(passwordChanged);
  pwCheck.find(".form_input").change(pwCheckChanged);

  cancel.click(() => {
    postable = false;
    fetch("http://localhost:8888/cancel", {
      method: "POST",
    }).finally(() => {
      postable = true;
      window.location.href = "http://localhost:8888/reset";
    });
  });

  commit.click(() => {
    let foundError = false;
    for (let ind = 0; ind < changedEvents.length; ind++) {
      let error = changedEvents[ind]();
      if (error != null) {
        foundError = true;
        console.log(error);
      }
    }

    if (foundError == false) {
      let data = {
        password: password.find(".form_input").val(),
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
            alert("비밀번호를 변경했습니다!");
            window.location.href = "http://localhost:8888/login";
          } else if (result == "password is invalid") {
            inputField(password, "사용할 수 없는 비밀번호입니다!");
          } else if (result == "failed") {
            inputField(password, "비밀번호 변경에 실패하였습니다!");
          }
        })
        .finally(() => {
          postable = true;
        });
    }
  });
});
