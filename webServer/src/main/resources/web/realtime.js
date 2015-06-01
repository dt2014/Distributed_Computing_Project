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
    	      center: new google.maps.LatLng(-27.5, 153.0166667),
    	      zoom: 10,
    	      mapTypeId: google.maps.MapTypeId.ROADMAP
    	    }
    var mapOptionsMel = {
      center: new google.maps.LatLng(-37.8166667, 144.9666667),
      zoom: 10,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    var mapOptionsSyd = {
    	      center: new google.maps.LatLng(-33.8482430125, 150.968707421183),
    	      zoom: 10,
    	      mapTypeId: google.maps.MapTypeId.ROADMAP
    	    }
    gMapBri = new google.maps.Map(mapCanvasBri, mapOptionsBri);
    gMapMel = new google.maps.Map(mapCanvasMel, mapOptionsMel);
    gMapSyd = new google.maps.Map(mapCanvasSyd, mapOptionsSyd);
    
    initSocket();    
});

function initSocket(){
    socket = new WebSocket('ws://144.6.227.137:9998/ws/?realtime');
//	socket = new WebSocket('ws://127.0.0.1:9998/ws/?realtime');
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


function markSpot(spot) {
	var gMap;
	if (spot.city == "Brisbane") {
		gMap = gMapBri;
//		console.log("Brisbane?");
	} else if (spot.city == "Melbourne") {
		gMap = gMapMel;
//		console.log("Melbourne?");
	} else {
		gMap = gMapSyd;
//		console.log("Sydney?");
	}
	var loc = new google.maps.LatLng(spot.lat, spot.lng);
	var options = {
            map: gMap,
			position: loc,
			animation: google.maps.Animation.DROP,
//		    icon: 'redMarder.png'
//            icon: {
//            	    path: google.maps.SymbolPath.CIRCLE,
//            	    fillColor: 'red',
//            	    fillOpacity: 0.9,
//            	    scale: 3,
//            	    strokeColor: 'green',
//            	    strokeWeight: 1
//            	}
        }
    var marker = new google.maps.Marker(options);
	console.log("marking spot");
}
