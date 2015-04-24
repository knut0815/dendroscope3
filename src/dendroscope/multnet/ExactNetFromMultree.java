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

package dendroscope.multnet;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * constructs an exact network for a given multi-labeled tree.
 *
 * @author thomas bonfert, 6.2009
 */

public class ExactNetFromMultree {

    private final PhyloTree tree;


    public ExactNetFromMultree(PhyloTree tree) {
        this.tree = tree;
    }

    public PhyloTree apply() {
        //preprocessing
        long startTime = new Date().getTime();
        MultilabeledTree t = new MultilabeledTree(this.tree);
        //this map contains for every height in the tree a HeightList. Each of this lists contains the multisets of nodes
        //with equivalent heights. the map is sorted after decreasing heights and initialized with the root's heightlist.
        HashMap<Integer, HeightList> h_max = new HashMap<>();
        Node root = t.getRoot();
        int rootHeight = t.getHeight(root);
        HeightList h = new HeightList();
        h.addSorted(root, t.getMultiset(root));
        h_max.put(rootHeight, h);

        //we iterate through h_max till we have reached the heightlist containing nodes of height 1.
        for (int i = rootHeight; i >= 0; i--) {
            HeightList l_h = h_max.get(i);
            while (!l_h.isEmpty()) {
                //take the first tree T(t_max) of the current heightlist and add all children of height j (j < i) to
                //the map h_max for later iterations.
                Node t_max = (Node) l_h.get(0);
                Iterator outEdgesIt = t_max.getOutEdges();
                while (outEdgesIt.hasNext()) {
                    Node target = ((Edge) outEdgesIt.next()).getTarget();
                    int targetHeight = t.getHeight(target);
                    HeightList targetList = h_max.get(targetHeight);
                    if (targetList == null) {
                        targetList = new HeightList();
                        h_max.put(targetHeight, targetList);
                    }
                    targetList.addSorted(target, t.getMultiset(target));
                }
                //now check if the current heightlist contains isomorphs to T(t_max).
                ArrayList<Node> isomorphs = new ArrayList<>();
                for (int j = 1; j < l_h.size(); j++) {
                    Node w = (Node) l_h.get(j);
                    //found isomorph subtrees
                    if (t.getMultiset(t_max).equals(t.getMultiset(w))) {
                        isomorphs.add(w);
                    } else break;
                }
                //found one or more isomorph subtree(s) to T(t_max).
                if (!isomorphs.isEmpty()) {
                    //insert in the incoming edge of T(t_max) a new node u.
                    Edge toDel = t_max.getFirstInEdge();
                    Node source = toDel.getSource();
                    Node u = t.newNode();
                    t.deleteEdge(toDel);
                    Edge reticulation1 = t.newEdge(source, u);
                    t.setSpecial(reticulation1, true);
                    t.setWeight(reticulation1, 0);

                    t.newEdge(u, t_max);
                    //now delete the isomorph subtrees and add new edges to the new generated node u.
                    for (Node w : isomorphs) {
                        Node w_father = w.getFirstInEdge().getSource();
                        t.deleteSubtree(w);
                        Edge reticulation2;
                        reticulation2 = t.newEdge(w_father, u);
                        t.setSpecial(reticulation2, true);
                        t.setWeight(reticulation2, 0);
                    }
                    //finally remove T(t_max) and the associated iosomorphs from the heightlist.
                    for (Node isomorph : isomorphs) l_h.remove(isomorph);
                }
                l_h.remove(t_max);
            }
        }
        long seconds = (new Date().getTime() - startTime);
        System.err.println("Algorithm required " + seconds / 1000.0 + " seconds");
        System.err.println("number of reticulations: " + t.getSpecialEdges().size() / 2);
        return t;
    }
}