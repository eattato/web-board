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