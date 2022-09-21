function httpGet(url)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

function httpRequest(reqType, url, data, dataType)
{
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
}

const inputBack = (field) => {
    // field.find(".form_input").removeClass("correct");
    field.find(".form_input").removeClass("error");
    field.find(".form_input").removeClass("error");
    field.find(".form_error").removeClass("error");
}

const encode = (target) => {
    let result = ""
    let ind = 0;
    for (let key in target) {
        result += key + "=" + encodeURI(target[key]);
        if (ind != target.length - 1) {
            result += "&";
        }
        ind++;
    }
    return result;
}

$(() => {
    var email = $("#email");
    var password = $("#password");
    var commit = $(".form_button");

    const emailChanged = function() {
        let input = email.find(".form_input").val();
        let error = null

        emailChecking = false;
        let split = input.split("@");
        if (split.length == 2) {
            if (split[0].length < 1 || split[1].length < 1) {
                error = "올바른 이메일 형태가 아닙니다!"
            }
        } else {
            error = "올바른 이메일 형태가 아닙니다!"
        }

        if (error == null && input.length > 320) {
            error = "이메일이 너무 깁니다!"
        }

        inputField(email, error);
        return error;
    }
    const passwordChanged = function() {
        let input = password.find(".form_input").val();
        let error = null

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
                error = "영문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
            }
        }

        if (error == null && input.length < 8) {
            error = "비밀번호는 최소 8자 이상입니다!";
        }

        inputField(password, error);
        return error;
    }
    const changedEvents = [emailChanged, passwordChanged];

    email.find(".form_input").change(emailChanged);
    password.find(".form_input").change(passwordChanged);

    commit.click(() => {
        let foundError = false;
        for (let ind = 0; ind < changedEvents.length; ind++) {
            let error = changedEvents[ind]();
            if (error != null) {
                foundError = true;
            }
        }

        if (foundError == false) {
            let data = {
                "email": email.find(".form_input").val(),
                "password": password.find(".form_input").val(),
            }
            let result = httpRequest("POST", "http://localhost:8888/access", encode(data), "application/x-www-form-urlencoded");
            if (result == "ok") {
                //window.location.replace("https://localhost:8888");
                window.location.href = "http://localhost:8888";
            } else if (result == "email not found") {
                inputField(email, "해당 이메일로 생성된 계정이 존재하지 않습니다!");
            } else if (result == "password invalid") {
                inputField(password, "비밀번호가 옳지 않습니다!");
            }
        }
    });
});