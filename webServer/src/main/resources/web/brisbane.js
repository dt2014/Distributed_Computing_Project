/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

var gMap;
var socket;
var markers = [];

$(function initialize() {
    var mapCanvas = document.getElementById('map-canvas');
    var mapOptions = {
      center: new google.maps.LatLng(-27.5, 153.0166667),
      zoom: 10,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    gMap = new google.maps.Map(mapCanvas, mapOptions);
    
    initSocket();
    
});

function initSocket(){
//    socket = new WebSocket('ws://144.6.227.137:9998/ws/');
	socket = new WebSocket('ws://127.0.0.1:9998/ws/');
    socket.onopen = function (event) {
        console.log("socketopen");
//        document.getElementById("serviceStatus").innerHTML=' ON';
        socket.onmessage = function (event) {
           var spot = JSON.parse(event.data);
           markSpot(spot);
           
        };
        socket.onclose = function (event) {
            console.log('Socket has closed', event);
//            document.getElementById("serviceStatus").innerHTML=' OFF';
//            alert('Socket closed !');
        };
        $(window).on("beforeunload", function() { 
        	socket.close();
        });
    }
}

$(function() {
	$("#datepicker").datepicker({
		minDate: new Date(2015, 4 - 1, 21),
		maxDate: 0,
		numberOfMonths: 2,
		dateFormat: "yymmdd",
		onSelect: function(dateText) { 
		      sendRequest("bmrh-date-" + dateText);
		   }
	});
});

function sendRequest(request) { 
	deleteMarkers();
	socket.send(request);
	console.log(request);
}

//$(window).scroll(function(){
//	  $("#map-canvas").css({"margin-top": ($(window).scrollTop()) + "px", "margin-left":($(window).scrollLeft()) + "px"});
//	$("#map-canvas")
//    	.stop()
//    	.animate({"margin-top": ($(window).scrollTop()) + "px", "margin-left":($(window).scrollLeft()) + "px"}, "slow" );
//	});

function markSpot(spot) {
	var loc = new google.maps.LatLng(spot.lat, spot.lng);
	var options = {
            map: gMap,
			position: loc,
            icon: {
            	    path: google.maps.SymbolPath.CIRCLE,
            	    fillColor: 'red',
            	    fillOpacity: 0.9,
            	    scale: 3,
            	    strokeColor: 'green',
            	    strokeWeight: 1
            	}
        }
    var marker = new google.maps.Marker(options);
	markers.push(marker);
}

//Sets the map on all markers in the array.
function setAllMap(map) {
  for (var i = 0; i < markers.length; i++) {
    markers[i].setMap(map);
  }
}

// Removes the markers from the map, but keeps them in the array.
function clearMarkers() {
  setAllMap(null);
}

// Shows any markers currently in the array.
function showMarkers() {
  setAllMap(map);
}

// Deletes all markers in the array by removing references to them.
function deleteMarkers() {
  clearMarkers();
  markers = [];
}
