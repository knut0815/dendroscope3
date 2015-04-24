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

package dendroscope.autumn.hybridnumber;

import dendroscope.consensus.Taxa;
import dendroscope.util.RerootingUtils;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.*;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * determines best rooting of two trees by hybridization number
 * Daniel Huson, 7.2011
 */
public class RerootByHybridNumber {

    /**
     * reroot both trees so as to minimize the hybrid number
     *
     * @param origTree1
     * @param origTree2
     * @param progressListener
     * @return hybrid number
     * @throws IOException
     * @throws CanceledException
     */
    // todo: can delete this, too slow
    public static int apply(PhyloTree origTree1, PhyloTree origTree2, ProgressListener progressListener) throws IOException, CanceledException {
        long startTime = System.currentTimeMillis();

        progressListener.setTasks("Rooting trees by hybrid number", "Initialization");

        PhyloTree tree1 = (PhyloTree) origTree1.clone();
        PhyloTree tree2 = (PhyloTree) origTree2.clone();

        while (tree1.getRoot() != null && tree1.getRoot().getOutDegree() == 1) {
            Node w = tree1.getRoot().getFirstOutEdge().getTarget();
            tree1.deleteNode(tree1.getRoot());
            tree1.setRoot(w);
        }

        while (tree2.getRoot() != null && tree2.getRoot().getOutDegree() == 1) {
            Node w = tree2.getRoot().getFirstOutEdge().getTarget();
            tree2.deleteNode(tree2.getRoot());
            tree2.setRoot(w);
        }

        Edge[] number2edge1 = new Edge[tree1.getNumberOfEdges()];
        Map<Edge, Integer> edge2number1 = new HashMap<Edge, Integer>();
        int count1 = 0;
        for (Edge e = tree1.getFirstEdge(); e != null; e = tree1.getNextEdge(e)) {
            number2edge1[count1] = e;
            edge2number1.put(e, count1);
            count1++;
        }

        Edge[] number2edge2 = new Edge[tree2.getNumberOfEdges()];
        Map<Edge, Integer> edge2number2 = new HashMap<Edge, Integer>();
        int count2 = 0;
        for (Edge e = tree2.getFirstEdge(); e != null; e = tree2.getNextEdge(e)) {
            number2edge2[count2] = e;
            edge2number2.put(e, count2);
            count2++;
        }

        // sorted list of all pairs of rooting in increasing sum of rooting unbalancedness
        SortedSet<Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>>> allPairs =
                new TreeSet<Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>>>
                        (new Comparator<Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>>>() {
                            public int compare(Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>> a,
                                               Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>> b) {
                                double scoreA = Math.max(Math.abs(a.get1().get2() - a.get1().get3()), Math.abs(a.get2().get2() - a.get2().get3()));
                                double scoreB = Math.max(Math.abs(b.get1().get2() - b.get1().get3()), Math.abs(b.get2().get2() - b.get2().get3()));
                                if (scoreA < scoreB)
                                    return -1;
                                else if (scoreA > scoreB)
                                    return 1;
                                else if (a.get1().get1() < b.get1().get1())
                                    return -1;
                                else if (a.get1().get1() > b.get1().get1())
                                    return 1;
                                else if (a.get2().get1() < b.get2().get1())
                                    return -1;
                                else if (a.get2().get1() > b.get2().get1())
                                    return 1;
                                else
                                    return 0;
                            }
                        });

        // setup all pairs of rootings
        {
            SortedSet<Triplet<Edge, Float, Float>> rerootingTriplets1 = RerootingUtils.getRankedMidpointRootings(tree1);
            SortedSet<Triplet<Edge, Float, Float>> rerootingTriplets2 = RerootingUtils.getRankedMidpointRootings(tree2);

            System.err.println("Determining all pairs of possible rootings");
            for (Triplet<Edge, Float, Float> triplet1 : rerootingTriplets1) {
                Triplet<Integer, Float, Float> newTriplet1 = new Triplet<Integer, Float, Float>(edge2number1.get(triplet1.get1()), triplet1.get2(), triplet1.get3());
                for (Triplet<Edge, Float, Float> triplet2 : rerootingTriplets2) {
                    if (triplet2.get1().getTarget().getOutDegree() > 0) {
                        Triplet<Integer, Float, Float> newTriplet2 = new Triplet<Integer, Float, Float>(edge2number2.get(triplet2.get1()), triplet2.get2(), triplet2.get3());

                        Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>>
                                pair = new Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>>(newTriplet1, newTriplet2);
                        allPairs.add(pair);
                    }
                }
            }
            rerootingTriplets1.clear();
            rerootingTriplets2.clear();
        }

        int bestScore = ComputeHybridNumber.LARGE;
        int originalH = bestScore;
        int bestE1 = -1;
        float bestSourceLength1 = 0;
        float bestTargetLength1 = 0;
        float bestSourceLength2 = 0;
        float bestTargetLength2 = 0;

        int bestE2 = -1;

        if (ProgramProperties.isUseGUI()) {
            String result = JOptionPane.showInputDialog(null, "Enter max h", "" + bestScore);
            if (result != null && Basic.isInteger(result))
                bestScore = Integer.parseInt(result);
        }
        System.err.println("Rooting trees by hybrid number");
        progressListener.setTasks("Rooting trees by hybrid number", "Comparing trees");
        progressListener.setMaximum(allPairs.size());
        progressListener.setProgress(0);

        ComputeHybridNumber computeHybridNumber = null;

        try {
            computeHybridNumber = new ComputeHybridNumber(progressListener);
            computeHybridNumber.silent = true;

            int count = 0;
            Taxa allTaxa = new Taxa();

            progressListener.setSubtask(count + " of " + allPairs.size() + (bestScore < 1000 ? ", best h=" + bestScore : ""));

            originalH = computeHybridNumber.run(tree1, tree2, allTaxa);
            System.err.println("Original rooting has hybridization number: " + originalH);
            bestScore = originalH;

            for (Pair<Triplet<Integer, Float, Float>, Triplet<Integer, Float, Float>> pair : allPairs) {
                count++;

                Integer ie1 = pair.get1().get1();
                Integer ie2 = pair.get2().get1();

                Edge e1 = number2edge1[ie1];
                if (e1 == null || e1.getOwner() == null)
                    System.err.println("ie1 " + ie1 + ": " + e1);
                float weight1 = (float) tree1.getWeight(e1);
                float halfOfTotal1 = (pair.get1().get2() + pair.get1().get3() + weight1) / 2;
                float sourceLength1 = halfOfTotal1 - pair.get1().get2();
                float targetLength1 = pair.get1().get2() + weight1 - halfOfTotal1;
                tree1.setRoot((Node) null);
                tree1.setRoot(e1, sourceLength1, targetLength1);
                tree1.redirectEdgesAwayFromRoot();


                Edge e2 = number2edge2[ie2];
                if (e2 == null || e2.getOwner() == null)
                    System.err.println("ie2 " + ie2 + ": " + e2);
                float weight2 = (float) tree2.getWeight(e2);
                float halfOfTotal2 = (pair.get2().get2() + pair.get2().get3() + weight2) / 2;
                float sourceLength2 = halfOfTotal2 - pair.get2().get2();
                float targetLength2 = pair.get2().get2() + weight2 - halfOfTotal2;
                tree2.setRoot((Node) null);
                tree2.setRoot(e2, sourceLength2, targetLength2);
                tree2.redirectEdgesAwayFromRoot();

                try {
                    progressListener.setSubtask(count + " of " + allPairs.size() + (bestScore < 1000 ? ", best h=" + bestScore : ""));

                    int h = computeHybridNumber.run(tree1, tree2, allTaxa);

                    progressListener.setMaximum(allPairs.size());
                    progressListener.setProgress(count);

                    // System.err.println("+++"+ie1+" "+ie2+" nested="+triplet.getThird()+" h="+h);

                    if (h < bestScore) {
                        bestE1 = ie1;
                        bestSourceLength1 = sourceLength1;
                        bestTargetLength1 = targetLength1;
                        bestSourceLength2 = sourceLength2;
                        bestTargetLength2 = targetLength2;

                        bestE2 = ie2;
                        if (bestScore < ComputeHybridNumber.LARGE)
                            System.err.println("Improving best score from: " + bestScore + " to " + h);
                        bestScore = h;
                    }
                } finally {
                    number2edge1[ie1] = tree1.delDivertex(tree1.getRoot());
                    tree1.setWeight(number2edge1[ie1], weight1);
                    number2edge2[ie2] = tree2.delDivertex(tree2.getRoot());
                    tree2.setWeight(number2edge2[ie2], weight2);
                }
            }
        } catch (CanceledException ex) {
            if (bestScore == 1000)
                throw ex;
            progressListener.close();
            System.err.println("USER CANCELED, result not necessarily optimal");
        } finally {
            if (computeHybridNumber != null)
                computeHybridNumber.done();
        }
        if (bestScore < originalH) {
            tree1.setRoot(number2edge1[bestE1], bestSourceLength1, bestTargetLength1);
            tree1.redirectEdgesAwayFromRoot();
            Set<Node> divertices1 = new HashSet<Node>();
            for (Node v = tree1.getFirstNode(); v != null; v = tree1.getNextNode(v)) {
                if (v.getInDegree() == 1 && v.getOutDegree() == 1 && tree1.getLabel(v) == null)
                    divertices1.add(v);
            }
            for (Node v : divertices1) {
                tree1.delDivertex(v);
            }
            tree2.setRoot(number2edge2[bestE2], bestSourceLength2, bestTargetLength2);
            tree2.redirectEdgesAwayFromRoot();
            Set<Node> divertices2 = new HashSet<Node>();
            for (Node v = tree2.getFirstNode(); v != null; v = tree2.getNextNode(v)) {
                if (v.getInDegree() == 1 && v.getOutDegree() == 1 && tree2.getLabel(v) == null)
                    divertices2.add(v);
            }
            for (Node v : divertices2) {
                tree2.delDivertex(v);
            }
            origTree1.copy(tree1);
            origTree2.copy(tree2);
        }
        System.out.println("Best hybridization number: " + bestScore);
        System.err.println("Time: " + ((System.currentTimeMillis() - startTime) / 1000) + " secs");
        System.gc();
        return bestScore;
    }
}