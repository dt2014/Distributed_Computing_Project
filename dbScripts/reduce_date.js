/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

/* Map */
function(doc) {
  var created_at;
  var date;
  var date_tag;

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
  
  emit(date_tag, 1);
}

/* Reduce */
function (key, values, rereduce) {
    return sum(values);
}
