/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

function(doc) {
  var lat=0.0;
  var lon=0.0;
  var created_at;
  var time_dist_in_sec;
  var value_for_elki;
  
  var harvested_from = new Date("Mon Apr 20 00:00:00 +0000 2015").getTime();
  
  if (doc.created_at) {
    created_at = new Date(doc.created_at).getTime();
    //the numbers of seconds between a specified date and midnight April 20, 2015
    time_dist_in_sec = (created_at - harvested_from) / 1000;
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
  
  value_for_elki = time_dist_in_sec + " " + lat + " " + lon;
  
  emit(doc.id, value_for_elki);
}
