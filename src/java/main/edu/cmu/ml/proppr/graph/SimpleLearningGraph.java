package edu.cmu.ml.proppr.graph;

import java.util.Set;
import java.util.TreeSet;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectProcedure;

/**
 * Straightforward implementation using three hash maps for u -> v -> f -> w.
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class SimpleLearningGraph extends LearningGraph {
	private static final TIntArrayList EMPTY_LIST = new TIntArrayList();
	private static final TObjectDoubleMap<String> EMPTY_MAP = new TObjectDoubleHashMap<String>();
	protected TIntObjectMap<TIntArrayList> near = new TIntObjectHashMap<TIntArrayList>();
	protected TIntObjectMap<TIntObjectMap<TObjectDoubleMap<String>>> phi = 
			new TIntObjectHashMap<TIntObjectMap<TObjectDoubleMap<String>>>();
	protected Set<String> features = new TreeSet<String>();
	protected int edgeSize = 0;

	public static class SLGBuilder extends LearningGraphBuilder {
		@Override
		public LearningGraph create() {
			return new SimpleLearningGraph();
		}

		@Override
		public void addOutlink(LearningGraph g, int u, RWOutlink rwOutlink) {
			((SimpleLearningGraph) g).addOutlink(u, rwOutlink);
		}

		@Override
		public void freeze(LearningGraph g) {
			((SimpleLearningGraph) g).freeze();
		}

		@Override
		public void setGraphSize(LearningGraph g, int nodeSize, int edgeSize) {}
	}
	
	private void ensureNode(int n) {
		if (!near.containsKey(n)) {
			near.put(n, new TIntArrayList());
			phi.put(n, new TIntObjectHashMap<TObjectDoubleMap<String>>());
		}
	}

	public void addOutlink(int u, RWOutlink outlink) {
		ensureNode(u);
		ensureNode(outlink.nodeid);
		// only add v to the near list if it's not already there;
		// cheat by using the feature lookup
		if (!phi.get(u).containsKey(outlink.nodeid)) {
			near.get(u).add(outlink.nodeid);
			phi.get(u).put(outlink.nodeid,outlink.fd);
		} else {
			phi.get(u).get(outlink.nodeid).putAll(outlink.fd);
		}
		for (String f : outlink.fd.keys(new String[0])) features.add(f);
		edgeSize++;
	}


	
	public TIntArrayList near(int u) {
		if (near.containsKey(u)) return near.get(u);
		return EMPTY_LIST;
	}


	
	public TObjectDoubleMap<String> getFeatures(int u, int v) {
		if (phi.containsKey(u)) {
			TIntObjectMap<TObjectDoubleMap<String>> phiU = phi.get(u);
			if (phiU.containsKey(v)) return phiU.get(v);
		}
		return EMPTY_MAP;
	}


	
	public Set<String> getFeatureSet() {
		return this.features;
	}


	@Override
	public int[] getNodes() {
		return near.keys();
	}


	@Override
	public int nodeSize() {
		return near.size();
	}


	@Override
	public int edgeSize() {
		return edgeSize;
	}

	public void freeze() {
		this.near.forEachValue(new TObjectProcedure<TIntArrayList>() {
			@Override
			public boolean execute(TIntArrayList destList) {
				destList.trimToSize();
				return true;
			}
		});
	}
}
