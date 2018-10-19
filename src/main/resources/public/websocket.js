var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat");
webSocket.onmessage = function (msg) {receievMsg(JSON.parse(msg.data)) }
webSocket.onclose = function() { alert("Server Disconnect You"); }

webSocket.onopen = function() {
    var name = "";
    while (name == "") name = prompt("Enter your name");
    sendMessage("join", name);
}

$("#send").click(function () {
    sendMessage("say", $("#msg").val());
});
$("#msg").keypress(function(e) {
    if(e.which == 13) sendMessage("say", e.target.value);
});
function sendMessage(type, data) {
    if (data !== "") {
        webSocket.send(JSON.stringify({type: type, data: data}));
        $("#msg").val("");
        $("#msg").focus();
    }
}
function receievMsg(msg) {
    console.log("new msg",msg)
    if (msg.msgType == "say") {
        $("#chatbox").append("<p>"+msg.data+"</p>");
    }
    else if (msg.msgType == "join") {
        addUser(msg.data);
    }
    else if (msg.msgType == "users") {
        msg.data.forEach(function(el) { addUser(el); });
    }
    else if (msg.msgType == "left") {
        $("#user-"+msg.data.id).remove();
    }
}
function addUser(user) {
    $("#userlist").append("<li id='user-"+user.id+"'>"+user.name+"</li>");
}