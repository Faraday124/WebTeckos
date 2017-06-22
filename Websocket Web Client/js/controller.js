var activeCorrespondent;

$(function () {
    $('#welcome').text("Hello " + name);
    $('.logout').text("Logout " + name);
    sendRequests();
});

function sendRequests() {
    var page = window.location.href;

    if (page.includes("#t1")) {
        waitForSocketConnection(sendRequestForBattery, 0);
    } else if (page.includes("#t2")) {
        waitForSocketConnection(sendRequestForPhonebook, 0);
    } else if (page.includes("#t3")) {
        waitForSocketConnection(sendRequestForSms, 0);
    } else if (page.includes("#t4")) {
        waitForSocketConnection(sendRequestForDirectories, 0);
    }
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}

function sortByKey(array, key, order) {
    'use strict';
    return array.sort(function (a, b) {
        var x = a[key].toUpperCase(),
            y = b[key].toUpperCase();
        if (order === "asc")
            return ((x < y) ? -1 : ((x > y) ? 1 : 0));
        else if (order === "desc")
            return ((x > y) ? -1 : ((x < y) ? 1 : 0));
    });
}

function removeSms(e, phone) {
    var r = confirm("Are you sure?");
    if (r == true) {
        sendRequestForSmsElement('sms-remove', phone);
    } else {
        console.log("You pressed Cancel!");
    }
    if (!e) var e = window.event;
    e.cancelBubble = true;
    if (e.stopPropagation) e.stopPropagation();
}

function appendPhonebookHeaders() {
    $('#phonebookHeader').append('<li class="icon-users-1" onclick="createNewContact(event);">New</li>');
    $('#phonebookHeader').append('<li class = "icon-mobile" onclick="sendRequestForBackup();">Backup</li>');
}

function appendPhonebookElements(message) {
    'use strict';
    endLoader();
    showContent();
    clearPhonebook();
    appendPhonebookHeaders();
    var json = JSON.parse(message),
        phonecontacts = sortByKey(json.phonecontacts, "name", "asc");

    phonecontacts.forEach(function (item) {
        $('#phone-names').append('<li value="' + item.number + '" onClick="openContact(event)">' + item.name + '</li>');
    });

    phonecontacts.forEach(function (item) {

        $('#phone-numbers').append('<li data-value="' + item.name + '" onClick="openContact(event)">' + item.number + '</li>');
    });

}

function appendMessageBody(data) {
    'use strict';
    $("#messages-items").empty();
    var json = JSON.parse(data),
        messages = json.messages;

    messages.forEach(function (item) {
        if (item.direction === "incoming")
            $('#messages-items').append('<li class="incoming">' + item.body + '</li>');
        else if (item.direction === "outgoing")
            $('#messages-items').append('<li class="outgoing">' + item.body + '</li>');
    });
    $('#messages-items').scrollTop($('#messages-items')[0].scrollHeight);
}

function appendMessageNames(data) {
    'use strict';
    endLoader();
    showContent();
    $("#messages-list").empty();
    var json = JSON.parse(data),
        contactNames = sortByKey(json.contacts, "date", "desc");
    contactNames.forEach(function (item) {
        $('#messages-list').append('<li value=\'' + item.address + '\'onClick="sendRequestForSmsElement(\'sms-body\', \'' + item.address + '\')"><div class="icon-cancel deleteSms" onclick="removeSms(event,\'' + item.address + '\')"></div>' + item.name + '</li>');
    });
    updateActiveCorrespondentName();
}

function updateMessages(data) {
    'use strict';
    var json = JSON.parse(data),
        changedAddress;
    appendMessageNames(data);
    setPreviousActiveCorrespondent();
    changedAddress = json.contacts[0].address;
    if (changedAddress === activeCorrespondent.val().toString()) {
        appendMessageBody(data);
    }
}

function openMostRecentMessage() {
    activeCorrespondent = $('#messages-list li:first');
    activeCorrespondent.addClass("active");
    $('#messages-list').scrollTop(0);

}

function setPreviousActiveCorrespondent() {
    if (activeCorrespondent) {
        $("#messages-list li").each(function (i, item) {
            if (item.value === activeCorrespondent.val()) {
                $(this).addClass("active");
            }
        });
    }
}

function updateActiveCorrespondentName() {
    $("#messages-list li").on("click", function () {
        $("#messages-list li").removeClass("active");
        $(this).addClass("active");
        activeCorrespondent = $(this);
    });
}

function createNewMessage() {
    if (!document.getElementById('newSmsInput')) {
        $('#newSms').empty();

        $('#newSms').append('<input id="newSmsInput" type="phone" placeholder="Type number..."></input>');
        $('#newSmsInput').focus();
        $("#newSmsInput").keyup(function (event) {
            if (event.keyCode == 13) {
                addNewSmsNumber();
            }
        });
    }
}

function addNewSmsNumber() {
    var phoneNumber = $('#newSmsInput').val(),
        correspondents = $('#messages-list');
    correspondents.prepend('<li value=\'' + phoneNumber + '\' onClick="sendRequestForSmsElement(\'sms-body\',\'' + phoneNumber + '\')">' + phoneNumber + '</li>');
    $("#messages-list li").removeClass("active");
    openMostRecentMessage();
    updateActiveCorrespondentName();
    $('#newSms').empty();
    $('#newSms').text("New");
    $('#messages-items').empty();
    $('#input_message').focus();
}

function clearPhonebook() {
    $("#phone-names").empty();
    $("#phone-numbers").empty();
    $('#phonebookHeader').empty();
}

function batUpdate(charge) {

    document.getElementById("batteryStatus").innerHTML = charge + "%";
    // console.log("Charge: ",charge);
    if (charge < 20) {
        // Red - Danger!
        col = ["#750900", "#c6462b", "#b74424", "#df0a00", "#590700"];
    } else if (charge < 40) {
        // Yellow - Might wanna charge soon...
        col = ["#754f00", "#f2bb00", "#dbb300", "#df8f00", "#593c00"];
    } else {
        // Green - All good!
        col = ["#316d08", "#60b939", "#51aa31", "#64ce11", "#255405"];
    }
    $("#battery").css("background-image", "linear-gradient(to right, transparent 5%, " + col[0] + " 5%, " + col[0] + " 7%, " + col[1] + " 8%, " + col[1] + " 10%, " + col[2] + " 11%, " + col[2] + " " + (charge - 3) + "%, " + col[3] + " " + (charge - 2) + "%, " + col[3] + " " + charge + "%, " + col[4] + " " + charge + "%, black " + (charge + 5) + "%, black 95%, transparent 95%), linear-gradient(to bottom, rgba(255,255,255,0.5) 0%, rgba(255,255,255,0.4) 4%, rgba(255,255,255,0.2) 7%, rgba(255,255,255,0.2) 14%, rgba(255,255,255,0.8) 14%, rgba(255,255,255,0.2) 40%, rgba(255,255,255,0) 41%, rgba(255,255,255,0) 80%, rgba(255,255,255,0.2) 80%, rgba(255,255,255,0.4) 86%, rgba(255,255,255,0.6) 90%, rgba(255,255,255,0.1) 92%, rgba(255,255,255,0.1) 95%, rgba(255,255,255,0.5) 98%)");
}

function convertToArrayByte(str) {
    var bytes = [];

    for (var i = 0; i < str.length; ++i) {
        bytes.push(str.charCodeAt(i));
    }
    return bytes;
}

function getExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}

function appendDirectories(data) {
    'use strict';
    endLoader();
    showContent();
    var file,
        json = JSON.parse(data),
        images = json.images,
        music = json.music,
        appFiles = json.app,
        jstreeData = [
            {
                'text': 'Android',
                'state': {
                    'opened': true,
                    'selected': true
                },
                'children': [
                    {
                        'text': 'Music',
                        'children': music,
                },
                    {
                        'text': 'Gallery',
                        'children': images

                        },
                    {
                        'text': 'WebSocket',
                        'children': appFiles
                    }
                ]
            }
        ];

    jsTree(jstreeData);
    $('#filesButtons').show();
    $('#download').off();
    $('#download').on('click', function () {
        file = $('.jstree-clicked').text();
        sendRequestForFile(file);
    });
    $('#deleteFileBtn').off();
    $('#deleteFileBtn').on('click', function () {
        file = $('.jstree-clicked').text();
        sendRequestForDeleteFile(file);
    });
    $('#jstree').jstree(true).settings.core.data = jstreeData;
    $('#jstree').jstree("refresh");

}

function jsTree(data) {

    $('#jstree').jstree({
        'core': {
            'data': data
        },
        "types": {
            "children": {
                "icon": "//jstree.com/tree.png"
            },
        },
        "plugins": ["types"]
    });

    $('#jstree').off("changed.jstree"); //remove previous listeners
}

function startBodyMessageLoader() {
    $('#messages-items').empty();
    $('#messages-items').append('<li class="icon-spinner-1">Loading</li>');
    $('#messages-items').scrollTop($('#messages-items')[0].scrollHeight);
}

function startLoader(page) {
    if (page == 'Phonebook') {
        $("#contact-list").hide();
        $("#phonebookHeader").hide();
    } else if (page === 'Messages')
        $("#messages-container").hide();
    else if (page === 'Files') {
        $("#jstree").hide();
        $("#sendFileWrapper").hide();
        $("#filesButtons").hide();

    }
    $('.loader').hide();
    $('#loader' + page).show();
}

function endLoader() {
    $('.loader').hide();

}

function showContent() {
    $(".wrapper").fadeIn("slow");
}

function openContact(e) {

    if (!$('.popup:visible').length) {
        var name, number;
        if ($(e.srcElement).parents('#phone-names').length > 0) {
            name = e.srcElement.innerHTML.toString();
            number = e.srcElement.value.toString();
        } else {
            name = e.srcElement.attributes[0].textContent;
            number = e.srcElement.innerHTML.toString();
        }

        $('#editContactInputName').attr('name', name);
        $('#editContactInputName').val(name);
        $('#editContactInputPhone').attr('name', number);
        $('#editContactInputPhone').val(number);
        $('.popup').fadeIn();

        $('#deleteContactBtn').show();
        $('#editContactBtn').show();
        $('#createContactBtn').hide();
    }
    e.preventDefault();

    $('.popup .close, .popup .bg').click(function () {
        $(this).parents('.popup').fadeOut();
    });
}

function editContactName() {
    var nameBefore = $('#editContactInputName').attr('name'),
        nameAfter = $('#editContactInputName').val(),
        numberBefore = $('#editContactInputPhone').attr('name'),
        numberAfter = $('#editContactInputPhone').val(),
        json = '{""}',
        myObject = new Object();
    myObject.nameBefore = nameBefore;
    myObject.nameAfter = nameAfter;
    myObject.numberBefore = numberBefore;
    myObject.numberAfter = numberAfter;
    sendRequestForPhonebookEdit(myObject);
    $('.popup').fadeOut();

}

function createNewContact(e) {

    if (!$('.popup:visible').length) {

        $('.popup').fadeIn();
        $('#deleteContactBtn').hide();
        $('#editContactBtn').hide();
        $('#createContactBtn').show();
        $('#editContactInputName').val('');
        $('#editContactInputPhone').val('');
    }
    e.preventDefault();

    $('.popup .close, .popup .bg').click(function () {
        $(this).parents('.popup').fadeOut();
    });
}

function createContact() {
    var name = $('#editContactInputName').val(),
        number = $('#editContactInputPhone').val(),
        json = '{""}',
        myObject = new Object();
    myObject.name = name;
    myObject.number = number;
    sendRequestForPhonebookAddOrDelete('phonebook-new', myObject);
    $('.popup').fadeOut();
}

function deleteContact() {
    var r = confirm("Are you sure?");
    if (r == true) {
        var name = $('#editContactInputName').attr('name'),
            number = $('#editContactInputPhone').attr('name'),
            json = '{""}',
            myObject = new Object();
        myObject.name = name;
        myObject.number = number;
        sendRequestForPhonebookAddOrDelete('phonebook-delete', myObject);
        $('.popup').fadeOut();
    }
}

function decodeFile(blob) {
    var slice = blob.slice(0, 2, 'count'),
        reader = new FileReader(),
        header,
        count;

    reader.onload = function (event) {
        count = parseInt(reader.result);
        header = blob.slice(2, count + 2, 'header');
        var fileReader = new FileReader();
        fileReader.onload = function (event) {
            var data = fileReader.result,
                array = [];
            array = data.split(',');
            var name = array[1],
                flag = array[2],
                file = blob.slice(count + 2, blob.size, 'file');
            if (flag === "response") {
                saveAs(file, name);
                endLoader();
                showContent();
            }

        };
        fileReader.readAsText(header);
    };
    reader.readAsText(slice);
}

function changePassword() {
    var oldPassword = $('#oldPassword').val(),
        newPassword = $('#newPassword').val(),
        newPasswordConfirm = $('#newPasswordConfirm').val();

    if (newPassword != newPasswordConfirm) {
        alert("Passwords are not equal!");
        return;
    }
    $.post("http://localhost:9000/database/index.php", {
            tag: "change_password",
            login: name,
            old_password: oldPassword,
            new_password: newPassword
        })
        .done(function (json) {
            var data = JSON.parse(json);
            if (!data.error) {
                alert("Password changed");
            } else {
                alert(data.error_msg);
            }
        });

}
