/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

/* Map */
function(doc) {
  var created_at;
  var date;
  var time;
  var mt; //military time
  var time_tag;

  if (doc.created_at) {
    created_at = new Date(doc.created_at);
    date = created_at.toLocaleDateString();
    time = created_at.toLocaleTimeString();
    mt = Number(time.slice(0,5).replace(":",""));
    if (mt >= 600 && mt <= 900) {
      time_tag = "morning rush";
    } else if (mt >= 1630 && mt <= 1900) {
       time_tag = "evening rush";
    } else {
      time_tag = "off peak";
    }
  }

  emit(time_tag,1);
}

/* Reduce */
function (key, values, rereduce) {
    return sum(values);
}
