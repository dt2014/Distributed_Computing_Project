/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

/* Map */
function(doc) {
  emit(doc.harvester_appends.name, 1);
}

/* Reduce */
function (key, values, rereduce) {
  return sum(values);
}
