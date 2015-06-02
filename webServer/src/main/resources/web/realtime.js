/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

var gMapBri;
var gMapMel;
var gMapSyd;
var socket;

$(function initialize() {
	var mapCanvasBri = document.getElementById('map-canvas-bri');
	var mapCanvasMel = document.getElementById('map-canvas-mel');
	var mapCanvasSyd = document.getElementById('map-canvas-syd');
	var mapOptionsBri = {
		center : new google.maps.LatLng(-27.4667, 153.0333),
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	}
	var mapOptionsMel = {
		center : new google.maps.LatLng(-37.8136, 144.9631),
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	}
	var mapOptionsSyd = {
		center : new google.maps.LatLng(-33.8650, 151.2094),
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	}
	gMapBri = new google.maps.Map(mapCanvasBri, mapOptionsBri);
	var trafficLayerBri = new google.maps.TrafficLayer();
	trafficLayerBri.setMap(gMapBri);
	
	gMapMel = new google.maps.Map(mapCanvasMel, mapOptionsMel);
	var trafficLayerMel = new google.maps.TrafficLayer();
	trafficLayerMel.setMap(gMapMel);
	
	gMapSyd = new google.maps.Map(mapCanvasSyd, mapOptionsSyd);
	var trafficLayerSyd = new google.maps.TrafficLayer();
	trafficLayerSyd.setMap(gMapSyd);

	initSocket();
});

function initSocket() {
	socket = new WebSocket('ws://144.6.227.137:9998/ws/?realtime');
//	 socket = new WebSocket('ws://127.0.0.1:9998/ws/?realtime');
	socket.onopen = function(event) {
		console.log("socketopen");
		// document.getElementById("serviceStatus").innerHTML=' ON';
		socket.onmessage = function(event) {
			var spot = JSON.parse(event.data);
			markSpot(spot);

		};
		socket.onclose = function(event) {
			console.log('Socket has closed', event);
			// document.getElementById("serviceStatus").innerHTML=' OFF';
			// alert('Socket closed !');
		};
		$(window).on("beforeunload", function() {
			socket.close();
		});
	}
}

function markSpot(spot) {
	var gMap;
	if (spot.city == "Brisbane") {
		gMap = gMapBri;
		// console.log("Brisbane?");
	} else if (spot.city == "Melbourne") {
		gMap = gMapMel;
		// console.log("Melbourne?");
	} else {
		gMap = gMapSyd;
		// console.log("Sydney?");
	}
	var loc = new google.maps.LatLng(spot.lat, spot.lon);
	
	var windowContent = "<p><b>Time: </b>"+ spot.time + "</p>" + 
	"<p><b>User: </b>"+ spot.user + "</p>" + 
	"<p><b>Text: </b>"+ spot.text + "</p>";

	var infowindow = new google.maps.InfoWindow({
	      content: windowContent
	  });
	
	var options = {
		map : gMap,
		position : loc,
		animation : google.maps.Animation.DROP,
        title: spot.text
	}
	var marker = new google.maps.Marker(options);
	
	google.maps.event.addListener(marker, 'click', function() {
	    infowindow.open(gMap, marker);
	  });
	
//	console.log("marking spot");
}
