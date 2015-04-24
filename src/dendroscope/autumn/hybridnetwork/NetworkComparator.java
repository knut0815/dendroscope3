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

package dendroscope.autumn.hybridnetwork;

import dendroscope.autumn.Root;

import java.util.Comparator;

/**
 * comparator for networks
 * Daniel Huson, 10.2011
 */

public class NetworkComparator implements Comparator<Root> {
    public int compare(Root root1, Root root2) {
        String string1 = root1.toStringNetwork();
        String string2 = root2.toStringNetwork();
        return string1.compareTo(string2);
    }
}