/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

/* Map */
function(doc) {
  var created_at;
  var date;
  var day;
  var days = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];

  if (doc.created_at) {
    created_at = new Date(doc.created_at);
    date = created_at.toLocaleDateString();
    day = days[new Date(date).getDay()];
  }
  
  emit(day,1);
}

/* Reduce */
function (key, values, rereduce) {
    return sum(values);
}