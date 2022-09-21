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

const loadSession = () => {
    let profileResult = httpGet("http://localhost:8888/logindata");
    if (profileResult != "no session" && profileResult != "no profile" && profileResult != null) {
        let parsed = JSON.parse(profileResult);

        let login = $(".menu_login");
        let profile = $(".menu_profile");
        login.css({display: "none"});
        profile.css({display: "inline-block"});
        profile.find(".menu_profile_name").text(parsed.name);

        if (parsed.img != null) {
            profile.find("img").attr("src", "http://localhost:8888/images/" + parsed.img);
        } else {
            profile.find("img").attr("src", "http://localhost:8888/images/profiles/default.png");
        }
    }
    return profileResult;
}