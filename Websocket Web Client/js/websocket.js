var sessionId = '',
    name = getCookie('login'),
    socket_url = ipAddressPath,
    port = '8080',
    webSocket;

$(document).ready(function () {
    join();
});

function join() {
    openSocket();
    return false;
}

function openSocket() {
    // Ensures only one connection is open at a time
    if (webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED) {
        return;
    }

    webSocket = new WebSocket("ws://" + socket_url + ":" + port + "/WebsocketServer/websocket?name=" + name);

    webSocket.onopen = function (event) {
        if (event.data === undefined)
            return;
    };
    waitForSocketConnection(sendRequestForBattery, 0);
    webSocket.onmessage = function (event) {
        if (event.data instanceof Blob) {
            var blob = event.data;
            decodeFile(blob);
        } else
            parseMessage(event.data);
    };
    webSocket.onclose = function (event) {};
}

function waitForSocketConnection(callback, count) {
    setTimeout(
        function () {
            if (webSocket != undefined && webSocket.readyState === 1) {
                if (callback != null) {
                    callback();
                }
                return;

            } else {
                if (count < 100) {
                    count++;
                    waitForSocketConnection(callback, count);
                    console.log("Trying to connect: " + count);
                } else
                    console.log("Can't connect!");
            }

        }, 5);
}

function sendSms(event) {
    event.preventDefault();
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    var message = $('#input_message').val();
    if (message.trim().length > 0)
        myObject.message = message;
    else {
        alert("Empty message!");
        return false;
    }
    myObject.flag = "request";
    myObject.subject = "sms-send";
    myObject.phone = activeCorrespondent.val();
    json = JSON.stringify(myObject);
    webSocket.send(json);
    $('#input_message').val('');
    startBodyMessageLoader();
}

function sendRequestForBackup() {
    startLoader('Phonebook');
    sendRequest('phonebook-backup');
}

function sendRequestForPhonebook() {
    startLoader('Phonebook');
    sendRequest("phonebook");
}

function sendRequestForFile(file) {
    sendRequestForDownloadOrDeleteFile('files-download', file);
}

function sendRequestForDeleteFile(file) {
    sendRequestForDownloadOrDeleteFile('files-delete', file);
}

function sendRequestForDirectories() {
    startLoader('Files');
    sendRequest("files-directory");
}

function sendRequestForBattery() {
    endLoader();
    sendRequest("battery");
}

function sendRequestToRemoveMessage() {
    sendRequest("sms-remove");
}

function sendRequestForSms() {
    startLoader('Messages');
    sendRequest('sms');
}

function sendRequestForSmsElement(subject, phone) {
    startBodyMessageLoader();
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    myObject.subject = subject;
    myObject.flag = "request";
    myObject.phone = phone;
    json = JSON.stringify(myObject);
    webSocket.send(json);
}

function sendRequestForDownloadOrDeleteFile(subject, extras) {
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    myObject.subject = subject;
    myObject.flag = "request";
    myObject.extras = extras;
    json = JSON.stringify(myObject);
    webSocket.send(json);
}

function sendRequestForPhonebookEdit(extras) {
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    myObject.subject = 'phonebook-edit';
    myObject.flag = "request";
    myObject.extras = extras;
    json = JSON.stringify(myObject);
    webSocket.send(json);
}

function sendRequestForPhonebookAddOrDelete(subject, contact) {
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    myObject.subject = subject;
    myObject.flag = "request";
    myObject.phone = contact;
    json = JSON.stringify(myObject);
    webSocket.send(json);
}

function closeSocket() {
    webSocket.close();
    sessionId = '';
    name = '';
}

function parseMessage(message) {
    var jObj = $.parseJSON(message);
    if (jObj.flag == 'self') {
        sessionId = jObj.sessionId;
    }

    if (jObj.flag == 'response') {
        var json = jObj.message,
            subject = jObj.subject;

        if (subject === 'phonebook') {
            appendPhonebookElements(json);
        } else if (subject === 'battery') {
            batUpdate(json);
        } else if (subject === 'sms') {
            appendMessageNames(json);
            appendMessageBody(json);
            openMostRecentMessage();
            updateActiveCorrespondentName();
        } else if (subject === 'sms-body') {
            appendMessageBody(json);
        } else if (subject === 'sms-change') {
            updateMessages(json);
        } else if (subject === 'files-directory') {
            appendDirectories(json);
        }
    }
}


function sendRequest(subject) {
    var json = '{""}';
    var myObject = new Object();
    myObject.sessionId = sessionId;
    myObject.subject = subject;
    myObject.flag = "request";
    myObject.name = name;
    json = JSON.stringify(myObject);
    webSocket.send(json);
}

function sendFile(tag, flag) {

    var file = document.getElementById(tag + 'Chooser').files[0];
    if (!file) {
        alert("No file was chosen!");
        return false;
    }
    var type = getExtension(file.name),
        reader = new FileReader(),
        rawData = new ArrayBuffer();

    type = convertToArrayByte(type + ",");
    var name = convertToArrayByte(file.name + ","),
        flag = convertToArrayByte(flag + ","),
        tag = convertToArrayByte(tag),
        countHeader = type.length + name.length + flag.length + tag.length;

    reader.loadend = function () {}
    reader.onload = function (e) {
        rawData = e.target.result;
        //trzeba przekazac nazwe pliku, login, oraz typ;
        var count = convertToArrayByte(countHeader.toString()),
            data = new Uint8Array(rawData.byteLength + flag.length + count.length + type.length + name.length + tag.length);

        data.set(new Uint8Array(count), 0);
        data.set(new Uint8Array(type), count.length);
        data.set(new Uint8Array(name), count.length + type.length);
        data.set(new Uint8Array(flag), count.length + type.length + name.length);
        data.set(new Uint8Array(tag), count.length + type.length + name.length + flag.length);
        data.set(new Uint8Array(rawData), count.length + type.length + name.length + flag.length + tag.length);

        webSocket.send(data);
    }
    reader.readAsArrayBuffer(file);

}
