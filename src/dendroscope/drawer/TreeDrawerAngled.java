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

package dendroscope.drawer;

import dendroscope.window.TreeViewer;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeDoubleArray;
import jloda.graph.NodeSet;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphView;
import jloda.graphview.NodeView;
import jloda.phylo.PhyloTree;
import jloda.util.Alert;
import jloda.util.Geometry;
import jloda.util.PolygonDouble;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * draws a tree using angled lines
 * Daniel Huson & Markus Franz, 1.2007
 */
public class TreeDrawerAngled extends TreeDrawerBase implements IOptimizedGraphDrawer {
    final static public String DESCRIPTION = "Draw slanted cladogram";
    private static boolean warnedSlantedCladogram = false;

    /**
     * constructor
     *
     * @param treeView
     * @param tree
     */
    public TreeDrawerAngled(TreeViewer treeView, PhyloTree tree) {
        super(treeView, tree);
        setupGraphView(treeView);
    }

    /**
     * set up the graphview
     *
     * @param graphView
     */
    public void setupGraphView(GraphView graphView) {
        graphView.setAllowInternalEdgePoints(false);
        graphView.setMaintainEdgeLengths(false);

        graphView.setAllowMoveNodes(true);
        graphView.setAllowMoveInternalEdgePoints(false);
        graphView.setKeepAspectRatio(false);
        graphView.setAllowRotationArbitraryAngle(false);
        trans.adjustAngleToNorthSouthEastWest();
        trans.getMagnifier().setInRectilinearMode(false);
    }

    /**
     * compute an embedding of the graph
     *
     * @param toScale if true, build to-scale embedding
     * @return true, if embedding was computed
     */
    public boolean computeEmbedding(boolean toScale) {
        this.toScale = toScale;
        if (tree.getNumberOfNodes() == 0)
            return true;

        viewer.removeAllInternalPoints();
        nodesWithMovedLabels.clear();
        edgesWithMovedLabels.clear();

        Node root = tree.getRoot();
        if (root == null) {
            root = tree.getFirstNode();
            tree.setRoot(root);
        }

        NodeDoubleArray yCoord = computeYCoordinates(tree.getRoot());

        computeEmbeddingRec(root, yCoord, new NodeDoubleArray(tree), new NodeDoubleArray(tree), new NodeSet(tree));

        recomputeOptimization(null);
        return true;
    }

    /**
     * recursively compute the embedding
     *
     * @param v
     */
    private void computeEmbeddingRec(Node v, NodeDoubleArray yCoord, NodeDoubleArray yMin, NodeDoubleArray yMax, NodeSet visited) {

        if (!warnedSlantedCladogram && tree.getSpecialEdges().size() > 0) {
            new Alert("Slanted cladogram:\nThis visualization is not well-defined for networks\nDrawing may contain errors");
            warnedSlantedCladogram = true;
        }
        final NodeView nv = viewer.getNV(v);
        if (v.getOutDegree() == 0) {
            double y = yCoord.getValue(v);
            yMin.set(v, y);
            yMax.set(v, y);
            nv.setLocation(new Point2D.Double(0, y));
        } else {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            /*
            for(Iterator it=getLSAChildren(v).iterator();it.hasNext();) {
                Node w = (Node)it.next();            
             */
            for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
                Node w = f.getTarget();
                if (!visited.contains(w)) {
                    visited.add(w);
                    computeEmbeddingRec(w, yCoord, yMin, yMax, visited);
                }
                min = Math.min(yMin.getValue(w), min);
                max = Math.max(yMax.getValue(w), max);
            }

            if (yCoord.getValue(v) > 0) // must be a pseudo leaf in a network
            {
                double y = yCoord.getValue(v);
                min = Math.min(y, min);
                max = Math.max(y, max);
            }

            yMin.set(v, min);
            yMax.set(v, max);

            if (v.getOutDegree() == 1) {
                Node w = v.getFirstOutEdge().getTarget();
                Point2D wPt = viewer.getLocation(w);
                nv.setLocation(new Point2D.Double(wPt.getX() - 0.1, wPt.getY()));
            } else {
                double x = -0.5 * (max - min);
                if (v.getInDegree() > 1 && v.getOutDegree() > 0 && x == 0)
                    x = -0.25; // is reticulate node above a leaf
                if (v == tree.getRoot() && v.getOutDegree() == 1)
                    x -= 0.5;
                double y = 0.5 * (min + max);
                nv.setLocation(new Point2D.Double(x, y));
            }
        }
    }

    /**
     * compute the optimization
     *
     * @param nodes if non-null, need only recompute for given nodes
     */
    public void recomputeOptimization(NodeSet nodes) {
        Node root = tree.getRoot();
        node2bb.clear();
        node2ProxyShape.clear();
        recomputeOptimizationRec(root, 0, new NodeSet(tree));

        for (Node v : collapsedNodes) {
            viewer.setCollapsedShape(v, computeCollapsedShape(v));
        }
    }

    /**
     * recursively computes the optimization datastructures
     *
     * @param v
     * @return index of last leaf
     */
    private int recomputeOptimizationRec(Node v, int leaves, NodeSet visited) {
        final NodeView nv = viewer.getNV(v);
        final Point2D location = nv.getLocation();

        if ((v.getDegree() == 1 && v != tree.getRoot())) {
            Rectangle2D.Double bb = new Rectangle2D.Double(location.getX(), location.getY() - 1, 0, 2);
            node2bb.set(v, bb);
            return leaves + 1;
        } else {
            int firstNewLeaf = leaves + 1;

            for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
                Node w = f.getOpposite(v);
                if (!visited.contains(w)) {
                    visited.add(w);
                    leaves = recomputeOptimizationRec(w, leaves, visited);
                }
            }
            final double x = location.getX();
            final double y = location.getY();

            Rectangle2D.Double bb = new Rectangle2D.Double(x, y, 0, 0);
            for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
                Node w = f.getTarget();
                bb.add(node2bb.get(w));
            }
            // set bounding box
            node2bb.set(v, bb);
            // set proxy shape
            if ((leaves - firstNewLeaf) >= MIN_LEAVES_FOR_PROXY) {
                Point2D[] points = new Point2D[]{
                        new Point2D.Double(bb.getX(), viewer.getLocation(v).getY()),
                        new Point2D.Double(bb.getMaxX(), firstNewLeaf),
                        new Point2D.Double(bb.getMaxX(), leaves)};
                node2ProxyShape.set(v, new PolygonDouble(3, points));
            }
            return leaves;
        }
    }

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll
     */
    public void resetLabelPositions(boolean resetAll) {
        byte leafOr;
        byte rootOr;
        float labelAngle = 0;

        double angle = Geometry.moduloTwoPI(trans.getAngle());
        if (angle >= 0.25 * Math.PI && angle < 0.75 * Math.PI) // south
        {
            if (radialLabels) {
                leafOr = rootOr = NodeView.RADIAL;
                labelAngle = (float) Math.PI / 2;
            } else {
                leafOr = NodeView.SOUTH;
                rootOr = NodeView.NORTH;
                labelAngle = 0;
            }
        } else if (angle >= 0.75 * Math.PI && angle < 1.25 * Math.PI) // west
        {
            leafOr = (!trans.getFlipH() ? NodeView.WEST : NodeView.EAST);
            rootOr = (trans.getFlipH() ? NodeView.WEST : NodeView.EAST);
        } else if (angle >= 1.25 * Math.PI && angle < 1.75 * Math.PI) // north
        {
            if (radialLabels) {
                leafOr = rootOr = NodeView.RADIAL;
                labelAngle = (float) (1.5 * Math.PI);
            } else {
                leafOr = NodeView.NORTH;
                rootOr = NodeView.SOUTH;
                labelAngle = 0;
            }
        } else // east
        {
            leafOr = (!trans.getFlipH() ? NodeView.EAST : NodeView.WEST);
            rootOr = (trans.getFlipH() ? NodeView.EAST : NodeView.WEST);
        }

        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = viewer.getNV(v);
            if (resetAll || nv.getLabelLayout() != NodeView.USER) {
                nv.setLabelPositionRelative(0, 0);
                if (v != tree.getRoot())
                    nv.setLabelLayout(leafOr);
                else
                    nv.setLabelLayout(rootOr);

                nv.setLabelAngle(labelAngle);
            }
        }
        for (Edge e = tree.getFirstEdge(); e != null; e = e.getNext()) {
            EdgeView ev = viewer.getEV(e);
            if (resetAll || ev.getLabelLayout() != EdgeView.USER) {
                ev.setLabelLayout(EdgeView.CENTRAL);
                ev.setLabelAngle(0);
            }
        }
    }

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll
     */


    public void resetLabelPositionsOld(boolean resetAll) {
        byte leafOr;
        byte rootOr;

        double angle = Geometry.moduloTwoPI(trans.getAngle());
        if (angle >= 0.25 * Math.PI && angle < 0.75 * Math.PI) // north
        {
            /*
            leafOr = (!trans.getFlipH() ? NodeView.SOUTH : NodeView.NORTH);
            rootOr = (trans.getFlipH() ? NodeView.SOUTH : NodeView.NORTH);
            */
            leafOr = rootOr = NodeView.RADIAL;
        } else if (angle >= 0.75 * Math.PI && angle < 1.25 * Math.PI) // west
        {
            leafOr = (!trans.getFlipH() ? NodeView.WEST : NodeView.EAST);
            rootOr = (trans.getFlipH() ? NodeView.WEST : NodeView.EAST);
        } else if (angle >= 1.25 * Math.PI && angle < 1.75 * Math.PI) // south
        {
            /*
            leafOr = (!trans.getFlipH() ? NodeView.NORTH : NodeView.SOUTH);
            rootOr = (trans.getFlipH() ? NodeView.NORTH : NodeView.SOUTH);
            */
            leafOr = rootOr = NodeView.RADIAL;
        } else // east
        {
            leafOr = (!trans.getFlipH() ? NodeView.EAST : NodeView.WEST);
            rootOr = (trans.getFlipH() ? NodeView.EAST : NodeView.WEST);
        }

        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = viewer.getNV(v);
            if (resetAll || nv.getLabelLayout() != NodeView.USER) {
                nv.setLabelPositionRelative(0, 0);
                nv.setLabelAngle(0);
                if (v != tree.getRoot())

                    nv.setLabelLayout(leafOr);
                else
                    nv.setLabelLayout(rootOr);
            }
        }
        for (Edge e = tree.getFirstEdge(); e != null; e = e.getNext()) {
            EdgeView ev = viewer.getEV(e);
            if (resetAll || ev.getLabelLayout() != EdgeView.USER)
                ev.setLabelLayout(EdgeView.CENTRAL);
        }
    }

    /**
     * must we visit the subtree rooted at this node when drawing or looking for a mouse click?
     *
     * @param v
     * @return true, if we must look at subtree below v
     */
    protected boolean mustVisitSubTreeBelowNode(Node v) {
        if (v.getDegree() == 1 && v != tree.getRoot())
            return true;
        if (node2ProxyShape.get(v) == null)
            return true;
        if (isCollapsed(v))
            return false;
        Rectangle2D bbW = node2bb.get(v);
        Rectangle bbD = trans.w2d(bbW).getBounds();
        if (visibleRect != null && bbD.intersects(visibleRect) == false)
            return false; // not visible on screen
        if (bbD.height <= 2 || bbD.width < 2)
            return false;
        // divide height by number of children:
        int children = Math.max(v.getDegree() - 1, 1);
        return bbD.getBounds().height / children >= 2;
    }

    /**
     * compute the shape used to represent a collapsed subtree
     *
     * @param v
     * @return
     */
    public CollapsedShape computeCollapsedShape(Node v) {
        double[] xMinMax = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
        double[] yMinMax = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
        computeMinMaxRec(v, xMinMax, yMinMax);

        Point2D[] points = new Point2D[]{viewer.getLocation(v), new Point2D.Double(xMinMax[0], yMinMax[0]),
                new Point2D.Double(xMinMax[1], yMinMax[1])};
        return new CollapsedShape(points);
    }
}