package dendroscope.hybroscale.util.graph;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import dendroscope.hybroscale.util.newick.NewickParser;

public class MyPhyloTree extends MyGraph {
	
	private String info;
	private Integer timeDegree, addTaxaDegree, level;
	private String name;
	private MyNode root;

	public MyPhyloTree() {
		root = new MyNode(this, null);
	}

	public MyPhyloTree(MyNode root) {
		this.root = root;
		root.setOwner(this);
	}
	
	public static MyPhyloTree copy(MyPhyloTree t) {
		return new MyPhyloTree(t);
	}
	
	public MyPhyloTree(MyPhyloTree t) {
		MyNode v = t.getRoot();
		root = new MyNode(this, v.getLabel());
		root.setLabel(v.getLabel());
		copyTreeRec(t, v, root, new Hashtable<MyNode, MyNode>());
		name = t.getName();
		info = t.getInfo();
		timeDegree = t.getTimeDegree();
		addTaxaDegree = t.getAddTaxaDegree();
		level = t.getLevel();
	}

	private void copyTreeRec(MyPhyloTree t, MyNode v, MyNode vCopy, Hashtable<MyNode, MyNode> visited) {
		visited.put(v, vCopy);
		Iterator<MyEdge> it = v.getOutEdges();
		while (it.hasNext()) {
			MyEdge e = it.next();
			MyNode c = e.getTarget();
			MyEdge eCopy;
			if (visited.containsKey(c)) {
				MyNode cCopy = visited.get(c);
				eCopy = newEdge(vCopy, cCopy);
				cCopy.setSolid(c.isSolid());
			} else {
				MyNode cCopy = newNode(c);
				eCopy = newEdge(vCopy, cCopy);
				if (c.getOutDegree() != 0)
					copyTreeRec(t, c, cCopy, visited);
				cCopy.setSolid(c.isSolid());
			}
			eCopy.setInfo(e.getInfo());
			eCopy.setLabel(e.getLabel());
			eCopy.setCritical(e.isCritical());
		}
	}

	public Iterator<MyEdge> edgeIterator() {
		return getEdges().iterator();
	}

	public Iterator<MyNode> nodeIterator() {
		return getNodes().iterator();
	}

	public Vector<MyNode> getNodes() {
		Vector<MyNode> nodes = new Vector<MyNode>();
		getNodesRec(root, nodes);
		return nodes;
	}

	private void getNodesRec(MyNode v, Vector<MyNode> nodes) {
		if (!nodes.contains(v)) {
			nodes.add(v);
			Iterator<MyEdge> it = v.getOutEdges();
			while (it.hasNext())
				getNodesRec(it.next().getTarget(), nodes);
		}
	}

	public int getNumberOfNodes() {
		return getNodes().size();
	}
	
	private void getLeavesRec(MyNode v, Vector<MyNode> nodes) {
		if (v.getOutDegree() != 0) {
			Iterator<MyEdge> it = v.getOutEdges();
			while(it.hasNext())
				getLeavesRec(it.next().getTarget(), nodes);
		}else
			nodes.add(v);
	}
	
	public Vector<MyNode> getLeaves() {
		Vector<MyNode> leaves = new Vector<MyNode>();
		getLeavesRec(root, leaves);
		return leaves;
	}

	public int getNumberOfLeaves() {
		return getLeaves().size();
	}

	public int getInDegree(MyNode v) {
		return v.getInDegree();
	}

	public int getOutDegree(MyNode v) {
		return v.getOutDegree();
	}

	public MyNode getSource(MyEdge e) {
		return e.getSource();
	}

	public MyNode getTarget(MyEdge e) {
		return e.getTarget();
	}

	public int getDegree(MyNode v) {
		return v.getInDegree() + v.getOutDegree();
	}

	public String getLabel(MyNode v) {
		return v.getLabel();
	}

	public String getLabel(MyEdge e) {
		return e.getLabel();
	}

	public Vector<MyEdge> getEdges() {
		Vector<MyEdge> edges = new Vector<MyEdge>();
		getEdgesRec(root, edges);
		return edges;
	}

	private void getEdgesRec(MyNode v, Vector<MyEdge> edges) {
		Iterator<MyEdge> it = v.getOutEdges();
		while (it.hasNext()) {
			MyEdge e = it.next();
			if (!edges.contains(e)) {
				edges.add(e);
				getEdgesRec(e.getTarget(), edges);
			}
		}
	}

	public Iterator<MyEdge> getInEdges(MyNode v) {
		// if (v.getOwner() != this)
		return v.getInEdges();
		// else
		// throw new RuntimeException("Wrong Owner Exception");
	}

	public Iterator<MyEdge> getOutEdges(MyNode v) {
		// if (v.getOwner() != this)
		return v.getOutEdges();
		// else
		// throw new RuntimeException("Wrong Owner Exception");
	}

	public int getNumberOfEdges() {
		return getEdges().size();
	}

	public MyNode getRoot() {
		return root;
	}

	public void setRoot(MyNode v) {
		// if (v.getOwner() != this)
		root = v;
		// else
		// throw new RuntimeException("Wrong Owner Exception");
	}

	public String toMyBracketString() {
		return root.toMyNewick("", new Vector<MyNode>(), new Hashtable<MyNode, String>(), null) + ";";
	}

	public String toBracketString() {
		return root.toNewick("", new Vector<MyNode>(), new Hashtable<MyNode, String>()) + ";";
	}

	public String toString() {
		return root.toNewick("", new Vector<MyNode>(), new Hashtable<MyNode, String>());
	}
	
	public void parseBracketNotation(String newickString) {
		MyPhyloTree t = new NewickParser().run(newickString);
		root = t.getRoot();
	}

	public void setWeight(MyEdge e, double d) {
		e.setWeight(d);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isTree() {
		for (MyNode v : getNodes()) {
			if (v.getInDegree() > 1)
				return false;
		}
		return true;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Integer getTimeDegree() {
		return timeDegree;
	}
	
	public Integer getLevel() {
		return level;
	}

	public void setTimeDegree(Integer timeDegree) {
		this.timeDegree = timeDegree;
	}
	
	public Integer getAddTaxaDegree() {
		return addTaxaDegree;
	}
	
	public void setAddTaxaDegree(int addTaxaDegree) {
		this.addTaxaDegree = addTaxaDegree;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

}
