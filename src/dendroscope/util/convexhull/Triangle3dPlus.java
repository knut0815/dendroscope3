/**
 * Copyright 2015, Daniel Huson
 *
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package dendroscope.util.convexhull; /**
 * author: Tim Lambert, UNSW, 2000
 */

/**
 * A 3d triangle with a list of associated points
 */

public class Triangle3dPlus extends Triangle3d {
    /* the points associated with this. inside() is true for each point in here */

    final Object3dList pts;

    public Triangle3dPlus(Point3d a, Point3d b, Point3d c, int frameNo) {
        super(a, b, c, frameNo);
        pts = new Object3dList(10);
    }

    /**
     * try to add a point to association list.  Return true if succesful
     */
    public boolean add(Point3dObject3d p) {
        if (inside(p)) {
            pts.addElement(p);
            return true;
        } else {
            return false;
        }
    }

    /**
     * return list of points associated with this triangle
     */
    public Object3dList getPoints() {
        return pts;
    }

    /**
     * return point farthest from support plane of this triangle
     */
    public Point3dObject3d extreme() {
        Point3dObject3d res = null;
        double maxd = Double.MIN_VALUE;
        for (int i = 0; i < pts.size(); i++) {
            double d = h.normal.dot((Point3d) pts.elementAt(i));
            if (d > maxd) {
                res = (Point3dObject3d) pts.elementAt(i);
                maxd = d;
            }
        }
        return res;
    }
}