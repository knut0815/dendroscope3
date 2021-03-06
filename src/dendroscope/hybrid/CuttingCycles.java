/*
 *   CuttingCycles.java Copyright (C) 2020 Daniel H. Huson
 *
 *   (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) This is third party code.
 */
package dendroscope.hybrid;

import jloda.graph.Edge;
import jloda.graph.Node;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class CuttingCycles {

    public void run(Vector<HybridTree> forest, HybridTree t1, HybridTree t2) {

        // System.out.println("BEGIN - cutting cycles... "+forest.size());

        HybridTree[] cyclePair = getCycle(forest, t1, t2);
        while (cyclePair != null) {
            cutCyclePair(cyclePair[0], forest, t1, t2);
            cutCyclePair(cyclePair[1], forest, t1, t2);
            cyclePair = getCycle(forest, t1, t2);
        }

        // System.out.println("END - cutting cycles... "+forest.size());
    }

    private void cutCyclePair(HybridTree t, Vector<HybridTree> forest, HybridTree t1, HybridTree t2) {
        final Iterator<Edge> it = t.getRoot().outEdges().iterator();
        Node c1 = it.next().getTarget();
        Node c2 = it.next().getTarget();
        HybridTree s1 = t.getSubtree(c1, true);
        HybridTree s2 = t.getSubtree(c2, true);
        forest.remove(t);
        forest.add(s1);
        forest.add(s2);
    }

    public HybridTree[] getCycle(Vector<HybridTree> forest, HybridTree t1, HybridTree t2) {

        Hashtable<HybridTree, BitSet> treeToLCA = new Hashtable<>();
        Hashtable<BitSet, HybridTree> LCAtoTree = new Hashtable<>();
        Vector<BitSet> LCAclusters = new Vector<>();

        for (HybridTree f : forest) {
            if (f.getNumberOfNodes() != 1) {

                BitSet fCluster = f.getNodeToCluster().get(f.getRoot());

                // finding the node in t1 representing the root of f
                Node v1 = t1.findLCA(fCluster);
                BitSet v1Cluster = t1.getNodeToCluster().get(v1);

                LCAclusters.add(v1Cluster);
                LCAtoTree.put(v1Cluster, f);

                // finding the node in t2 representing the root of f
                Node v2 = t2.findLCA(fCluster);
                BitSet v2Cluster = t2.getNodeToCluster().get(v2);
                treeToLCA.put(f, v2Cluster);
            }
        }

        // create pairs, first tree is a subtree of the second tree in t1
        Vector<HybridTree[]> pairs = new Vector<>();
        for (int i = 0; i < LCAclusters.size() - 1; i++) {
            BitSet b1 = LCAclusters.get(i);
            for (int j = i + 1; j < LCAclusters.size(); j++) {
                BitSet b2 = LCAclusters.get(j);
                BitSet test1 = (BitSet) b1.clone();
                test1.and(b2);
                BitSet test2 = (BitSet) b2.clone();
                test2.and(b1);
                if (test1.equals(b1)) {
                    HybridTree[] pair = {LCAtoTree.get(b1), LCAtoTree.get(b2)};
                    pairs.add(pair);
                } else if (test2.equals(b2)) {
                    HybridTree[] pair = {LCAtoTree.get(b2), LCAtoTree.get(b1)};
                    pairs.add(pair);
                }
            }
        }

        // check if each pairs holds in t2
        for (HybridTree[] pair : pairs) {
            BitSet b1 = treeToLCA.get(pair[0]);
            BitSet b2 = treeToLCA.get(pair[1]);
            BitSet test = (BitSet) b1.clone();
            test.and(b2);
            if (test.cardinality() != 0 && !test.equals(b1))
                return pair;
        }

        return null;
    }
}
