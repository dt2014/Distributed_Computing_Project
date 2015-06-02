/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

function(doc) {
  var lat=0.0;
  var lon=0.0;
  var created_at;
  var date;
  var date_tag;
  var congestion = false;

  if (doc.created_at) {
    created_at = new Date(doc.created_at);
    date = new Date(created_at.toLocaleDateString());
    var dd = date.getDate();
    if ( dd < 10 ) dd = '0' + dd;
    var mm = date.getMonth() + 1;
    if ( mm < 10 ) mm = '0' + mm;
    var yy = date.getFullYear();
    date_tag = yy + mm + dd;

  }
  
  if (doc.geo) {
    lat = doc.geo.coordinates[0]; 
    lon = doc.geo.coordinates[1];
  } else if (doc.coordinates) {
    lat = doc.coordinates.coordinates[1]; 
    lon = doc.coordinates.coordinates[0];
  } else {
    lat = doc.harvester_appends.geo.latitude; 
    lon = doc.harvester_appends.geo.longitude;
  }
  
  if (doc.cluster_info) {
    if (doc.cluster_info.cluster_name != "Noise") {
      congestion = true;
    }
  }
  
  emit(date_tag, {lat:lat,lon:lon,time:created_at,congestion:congestion,road:doc.harvester_appends.name,user:doc.user.screen_name,text:doc.text});
}
