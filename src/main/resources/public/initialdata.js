var request = new XMLHttpRequest();

request.open('GET',"http://"+ location.hostname + ":" + location.port + "/tests/showvars", true);
request.onload = function () {

    // Begin accessing JSON data here
    var data = JSON.stringify(this.response);

    if (request.status >= 200 && request.status < 400) {

        console.log("success",this.response)
        document.getElementById("demo").innerHTML = data;
    } else {
        console.log('error');
    }
}

request.send();