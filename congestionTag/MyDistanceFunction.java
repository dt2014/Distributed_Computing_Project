/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 
 * Modified By: Fengmin Deng (Student ID: 659332)
 * 
 * This class defines a customized distance function for ELKI platform.
 * Here distance means the spatio-temporal measurement between two tweets.
 * It consists of two values: distance value of time in second and
 *   the distance value of earth coordinates in meter.
 */

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2014
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.SimpleTypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.distance.distancefunction.AbstractNumberVectorDistanceFunction;

public class MyDistanceFunction extends AbstractNumberVectorDistanceFunction {
  @Override
  public double distance(NumberVector o1, NumberVector o2) {
    // distance value of time in second
    double dt = Math.abs(o1.doubleValue(0) - o2.doubleValue(0));
    // distance value of earth coordinates in meter
    double dc = getDistance(o1.doubleValue(1), o1.doubleValue(2), o2.doubleValue(1), o2.doubleValue(2));
    return dt + dc;
  }

  private static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        Double dlat = radian(lat1) - radian(lat2);
        Double dlon = radian(lon1) - radian(lon2);
        Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(radian(lat1)) * Math.cos(radian(lat2)) * Math.pow(Math.sin(dlon / 2), 2)));
        s = s * 6378.137 ;  // EARTH_RADIUS
        s = s * 1000;
        return s;
    }

    private static Double radian(Double degree){
        return degree * Math.PI / 180.0;
    }
}
