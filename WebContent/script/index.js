//API keys
var mapBoxApiKey = "pk.eyJ1IjoiZHN0cmF0aGllIiwiYSI6ImNqcDNucG9oaDBpYnozcHA4anlqa2RwbzEifQ.v5lVXT3Wocd0C0r_bx59yg";
var url = "api/user";

var me;
var map;
var marker;
var markers = [];


//the document ready function
try {
    $(function () {
        init()
    })
} catch (e) {
    alert("*** jQuery not loaded. ***")
}

//
// Initialise page.
//
function init() {
    map = makeMap("map", 2, 0, 0);

    $("#login").click(function () {
        var username = $("#user").val();

        $.getJSON(url + "/" + username)
            .success(function (data) {
                me = data;
                marker = currentLocation();

                $("#login").prop('disabled', true);
                $("#newuser").prop('disabled', true);

                getsubscriptions();
                getsubscriptionRequests();
                checkin()
            })
            .fail(function (d) {
                console.log(d);
                alert("User not found")
            })
    });
    // new user handling
    $("#newuser").click(function () {
        $("#newusername").val("");
        $("#newuserbox").dialog("open", true)
    });
    $("#newuserbox").dialog({
        modal: true,
        autoOpen: false,
        title: "Create User",
        minWidth: 500,
        minHeight: 400
    });
    $("#adduser").click(function () {
        createNewUser($("#newusername").val());
        $("#newuserbox").dialog("close")
    });
    $("#usercancel").click(function () {
        $("#usercancel").dialog("close")
    });
    // subscription request
    $("#requestbox").dialog({
        modal: true,
        autoOpen: false,
        title: "Send Request"
    });
    $("#sendrequest").click(function () {
        $("#requestbox").dialog("open", true)
    });
    $("#requestsend").click(function () {
        sendsubscriptionRequest($("#friendname").val());
        $("#requestbox").dialog("close")
    });
    $("#cancelrequest").click(function () {
        $("#subscriptionname").val("");
        $("#requestbox").dialog("close")
    });

    $("#refresh").click(function () {
        refresh()
    });

    $("#checkin").click(function () {
        checkin();
        refresh()
    })
}

function createNewUser(name) {
    var latitude = 33;
    var longitude = 22;

    var data = {
        "name": name,
        "latitude": latitude,
        "longitude": longitude
    };

    $.ajax(url, {
        type: "POST",
        data: data,
        statusCode: {
            201: function () {
                alert("User saved: " + name + " (" + latitude + "," + longitude + ")")
            },
            400: function () {
                alert("Not valid coordinates")
            },
            500: function (d) {
                console.log(d);
                alert("Server Error")
            }
        }
    });
}

function getsubscriptions() {

    $("#subslist").empty();

    for (var i of markers) map.removeLayer(i);

    markers = [];

    $.getJSON(url + "/" + me.name, function (d) {
        for (var i of d.subscriptions) {
            $.getJSON(url + "/" + i, function (data) {
                $("#subslist").append(
                    "<li id='" + data.name + "'>" +
                    data.name + "<p class='small'> last checked in at lat: " +
                    data.latitude + " long: " + data.longitude +
                    "</p></li>"
                );

                markers.push(makesubscriptionMarker(data["latitude"], data["longitude"]));
                // add name to the marker just added to find it later
                markers[markers.length - 1].name = data.name;

                $("#subslist li").click(function () {
                    var mark = markers.find(i => i.name === $(this).attr("id"));
                    map.setZoom(9);
                    map.panTo(mark.getLatLng());
                })

            })

        }
    })
}

function makeMap(divId, zoomLevel, latitude, longitude) {
    var location = L.latLng(latitude, longitude);
    var tempMap = L.map(divId).setView(location, zoomLevel);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=' + mapBoxApiKey,
        {
            attribution: 'Map data &copy <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: mapBoxApiKey
        }
    ).addTo(tempMap);

    return tempMap
}

function currentLocation() {
    var location = L.latLng({lat: me.latitude, lon: me.longitude});
    marker = L.marker(location, {draggable: true});
    marker.addTo(map);
    return marker
}

function makesubscriptionMarker(latitude, longitude) {
    var marker = L.marker(L.latLng({lat: latitude, lon: longitude}));
    marker.addTo(map);
    return marker
}

function refresh() {
    $.getJSON(url + "/" + me.name)
        .success(function (data) {
            me = data
        })
        .fail(function () {
            alert("FAILURE", "Unknown Error")
        });

    getsubscriptions();
    getsubscriptionRequests();
    checkin()
}

// send a subscription request
function sendsubscriptionRequest(otherUser) {
    $.ajax(url + "/" + me.name + "/" + otherUser, {
        type: "POST",
        compvare: function (data) {
            alert(data.statusText, data.responseText)
        }
    });
}

// get subscription requests
function getsubscriptionRequests() {
    $("#requestslist").empty();

		$.getJSON(url + "/" + me.name, function (data) {
			for (var i of data.subRec)
					$("#requestslist").append("<li id='" + i + "'>" + i + "</li>");

					$("#requestslist li").click(function () {
							requestClicked($(this).attr("id"))
					})
		})

}


function requestClicked(subscriptionName) {
    $(function () {
        $("#confirm").dialog({
            resizable: false,
            title: "subscription request",
            modal: true,
            buttons: {
                "Accept": function () {
                    sendsubscriptionRequest(subscriptionName);
                    $('#' + subscriptionName).remove();
                    $("#confirm").dialog("close")
                },
                "Deny": function () {
                    $.ajax(url + "/" + me.name + "/" + subscriptionName, {
                        type: "DELETE",
                        statusCode: {
                            200: function () {
                                alert("Success", "subscription request removed")
                            },
                            403: function () {
                                alert("Failure", "User is already your subscription")
                            },
                            404: function () {
                                alert("Failure", "User does not have a request from you")
                            },
                            500: function () {
                                alert("Failure", "Server Error")
                            }
                        }
                    });
                    $('#' + subscriptionName).remove();
                    $("#confirm").dialog("close")
                }
            }
        })
    })
}

// update CURRENTUSER location
function checkin() {
    var data = {
        "latitude": marker.getLatLng().lat,
        "longitude": marker.getLatLng().lng
    };

    $.ajax(url + "/" + me.name, {
        type: "POST",
        data: data,
        statusCode: {
            201: function () {
                $("#lat").val(data.latitude);
                $("#long").val(data.longitude)
            },
            400: function () {
                alert("FAILURE", "Not a valid number");
                $("#lat").val(me.latitude);
                $("#long").val(me.longitude)
            },
            404: function () {
                alert("FAILURE", "User not found")
            },
            500: function () {
                alert("FAILURE", "Server error")
            }
        }
    });
}
