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

async function readFileAsDataURL(file) {
    let result_base64 = await new Promise((resolve) => {
        let fileReader = new FileReader();
        fileReader.onload = (e) => resolve(fileReader.result);
        fileReader.readAsDataURL(file);
    });

    return result_base64;
}

const encodeImg = (imageSource) => {
    let reader = new FileReader();
    return FileReaderSync.readAsDataURL(imageSource);
}

$(() => {
    var faceimg = $(".menu_profile_imgframe");
    var password = $("#password");
    var nickname = $("#nickname");
    var commit = $(".form_button");
    var imgSelect = $("#profile_img_select");
    imgSelect.attr("title", " ");

    // 로그인 세션 불러오기
    let profileResult = loadSession();
    if (profileResult != "no session" && profileResult != "no profile" && profileResult != null) {
        let parsed = JSON.parse(profileResult);
        $("#email_text").text(parsed.email);
        nickname.find(".form_input").val(parsed.name);
        if (parsed.img != null) {
            $(".profile_img_frame img").attr("src", "http://localhost:8888/images/" + parsed.img);
        } else {
            $(".profile_img_frame img").attr("src", "http://localhost:8888/images/profiles/default.png");
        }
    }

    const imgChanged = async function() {
        imgSelect.attr("title", " ");
        if (imgSelect[0].files.length == 1) {
            let encoded = await readFileAsDataURL(imgSelect[0].files[0]);
            $(".profile_img_frame img").attr("src", encoded);
        }
        // imgSelect.val("");
    }
    const nicknameChanged = function() {
        let input = nickname.find(".form_input").val();
        let error = null

        if (input.length < 1 || input.length > 50) {
            error = "닉네임은 최소 1자, 최대 50자 입니다!";
        }

        inputField(nickname, error);
        return error;
    }
    const changedEvents = [nicknameChanged];
    nickname.find(".form_input").change(nicknameChanged);
    imgSelect.change(imgChanged);

    commit.click(async () => {
        let foundError = false;
        for (let ind = 0; ind < changedEvents.length; ind++) {
            let error = changedEvents[ind]();
            if (error != null) {
                foundError = true;
            }
        }

        if (foundError == false) {
            //let bodyData = encodeURI("email=" + email.find(".form_input").val() + "&password=" + password.find(".form_input").val() + "&nickname=" + nickname.find(".form_input").val());
            let data = {
                "password": password.find(".form_input").val(),
                "nickname": nickname.find(".form_input").val(),
                "image": null
            }
            if (imgSelect[0].files.length == 1) {
                let encoded = await readFileAsDataURL(imgSelect[0].files[0]);
                encoded = encoded.replace("data:image/png;base64,", "");
                encoded = encoded.replaceAll("+", "-");
                encoded = encoded.replaceAll("/", "_");
                data["image"] = encoded;
            }

            let result = httpRequest("POST", "http://localhost:8888/setprofile", encode(data), "application/x-www-form-urlencoded");
            if (result == "ok") {
                alert("프로필을 변경했습니다!");
                window.location.replace("http://localhost:8888/profile");
            } else if (result == "password wrong") {
                inputField(password, "비밀번호가 옳지 않습니다.");
            } else if (result == "nickname is invalid") {
                inputField(nickname, "닉네임 형식이 옳지 않습니다.");
            }
        }
    });
});