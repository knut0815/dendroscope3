package dendroscope.hybroscale.model.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import dendroscope.hybroscale.util.graph.MyEdge;
import dendroscope.hybroscale.util.graph.MyNode;
import dendroscope.hybroscale.util.graph.MyPhyloTree;

public class GetNetworkCluster {

	private HashMap<MyNode, HashSet<MyNode>> nodeToPreds;
	private HashMap<MyNode, HashSet<MyNode>> nodeToRetPreds;

	public Vector<MyPhyloTree> run(MyPhyloTree n) {

		nodeToPreds = new HashMap<MyNode, HashSet<MyNode>>();
		nodeToRetPreds = new HashMap<MyNode, HashSet<MyNode>>();
		for (MyNode v : n.getLeaves())
			cmpPredecessorsRec(v, new HashSet<MyNode>(), new HashSet<MyNode>());

		HashSet<MyNode> rootOfClusters = new HashSet<MyNode>();
		HashSet<MyNode> visited = new HashSet<MyNode>();
		for (MyNode v : n.getLeaves())
			cmpRootOfClustersRec(v, true, rootOfClusters, visited);
		if (!rootOfClusters.contains(n.getRoot()))
			rootOfClusters.add(n.getRoot());

		Vector<MyPhyloTree> networkClusters = new Vector<MyPhyloTree>();
		for (MyNode r : rootOfClusters) {
			MyPhyloTree netCluster = new MyPhyloTree();
			HashMap<MyNode, MyNode> vistedNodes = new HashMap<MyNode, MyNode>();
			cmpNetClusterRec(r, netCluster.getRoot(), vistedNodes, rootOfClusters);
			for (MyNode v : netCluster.getNodes()) {
				if (v.getInDegree() == 0)
					netCluster.setRoot(v);
			}
			networkClusters.add(netCluster);
		}

		return networkClusters;

	}

	private void cmpNetClusterRec(MyNode v, MyNode vCopy, HashMap<MyNode, MyNode> vistedNodes,
			HashSet<MyNode> rootOfClusters) {
		Iterator<MyEdge> it = v.getOutEdges();
		while (it.hasNext()) {
			MyNode c = it.next().getTarget();
			if (vistedNodes.containsKey(c)) {
				MyNode cCopy = vistedNodes.get(c);
				vCopy.getOwner().newEdge(vCopy, cCopy);
			} else {
				MyNode cCopy = vCopy.getOwner().newNode(c);
				vCopy.getOwner().newEdge(vCopy, cCopy);
				vistedNodes.put(c, cCopy);
				if (!rootOfClusters.contains(c))
					cmpNetClusterRec(c, cCopy, vistedNodes, rootOfClusters);
			}
		}
	}

	private void cmpRootOfClustersRec(MyNode v, boolean bC, HashSet<MyNode> rootOfClusters, HashSet<MyNode> visited) {
		if (!visited.contains(v)) {
			visited.add(v);
			boolean bV = isRootOfCluster(v);
			if (!bC && bV)
				rootOfClusters.add(v);
			Iterator<MyEdge> it = v.getInEdges();
			while (it.hasNext()) {
				MyNode p = it.next().getSource();
				cmpRootOfClustersRec(p, bV, rootOfClusters, visited);
			}
		}
	}

	private void cmpPredecessorsRec(MyNode v, HashSet<MyNode> preds, HashSet<MyNode> retPreds) {

		preds.add(v);
		if (!nodeToPreds.containsKey(v))
			nodeToPreds.put(v, preds);
		else
			nodeToPreds.get(v).addAll(preds);
		if (!nodeToRetPreds.containsKey(v))
			nodeToRetPreds.put(v, retPreds);
		else
			nodeToRetPreds.get(v).addAll(retPreds);

		Iterator<MyEdge> it = v.getInEdges();
		while (it.hasNext()) {
			MyNode p = it.next().getSource();
			HashSet<MyNode> predsClone = (HashSet<MyNode>) preds.clone();
			// predsClone.add(v);
			HashSet<MyNode> retPredsClone = (HashSet<MyNode>) retPreds.clone();
			if (v.getInDegree() > 1)
				retPredsClone.add(v);
			cmpPredecessorsRec(p, predsClone, retPredsClone);
		}

	}

	private boolean isRootOfCluster(MyNode v) {
		HashSet<MyNode> preds = nodeToPreds.get(v);
		HashSet<MyNode> retPreds = nodeToRetPreds.get(v);
		for (MyNode r : retPreds) {
			Iterator<MyEdge> it = r.getInEdges();
			while (it.hasNext()) {
				MyNode p = it.next().getSource();
				if (!preds.contains(p))
					return false;
			}
		}
		return true;
	}

}
