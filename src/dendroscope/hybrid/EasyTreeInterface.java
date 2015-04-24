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

package dendroscope.hybrid;

import jloda.phylo.PhyloTree;

import java.util.Iterator;
import java.util.Vector;

public interface EasyTreeInterface {

    EasyNode getRoot();

    Iterator<EasyNode> postOrderWalk();

    Vector<EasyNode> getLeaves();

    void deleteNode(EasyNode v);

    PhyloTree getPhyloTree();

    EasyTree pruneSubtree(EasyNode v);

    Vector<EasyNode> getNodes();

    void setLabel(EasyNode v, String s);

}