/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

var url = "http://144.6.227.137:9999/"
//var url = "http://127.0.0.1:9999/"

$(function() {
//	document.getElementById("date").innerHTML = "Harvested Tweets from " + 
//		new Date("21 April 2015").toDateString() + " to  " + new Date().toDateString();
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
			var response = xmlhttp.responseText;
			console.log('response: ', response);
			var numTweets = JSON.parse(response);
			// minus 3 for 3 _design documents in the databases
			document.getElementById("bn").innerHTML = "Brisbane: " + (numTweets.bn - 3);
			document.getElementById("mn").innerHTML = "Melbourne: " + (numTweets.mn - 3);
			document.getElementById("sn").innerHTML = "Sydney: " + (numTweets.sn - 3);
	  }
	}
	xmlhttp.open("GET", url + "?num_tweets", true);
	xmlhttp.send();
});


