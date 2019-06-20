package ch.seto.vikdal.java.transformers;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;

import ch.seto.vikdal.java.EdgeTag;

public class GraphEdge {
	public final static EdgeFactory<GraphNode, GraphEdge> FACTORY = new EdgeFactory<GraphNode, GraphEdge>() {
		@Override
		public GraphEdge createEdge(GraphNode source, GraphNode target) {
			return new GraphEdge(source, target);
		}
	};
	
	public final GraphNode source, target;
	private EdgeTag tag;
	
	public GraphEdge(GraphNode s, GraphNode t) {
		source = s;
		target = t;
		setTag(EdgeTag.DEFAULT);
	}
	
	public EdgeTag getTag() {
		return tag;
	}

	public void setTag(EdgeTag t) {
		tag = t;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		String from = source.edgeFromDescription();
		String to = target.edgeToDescription();
		if (from != null) {
			ret.append(from);
		}
		if (from != null || to != null) {
			// right arrow
			ret.append(" \u2192 ");
		}
		if (to != null) {
			ret.append(to);
		}
		if (getTag() != EdgeTag.DEFAULT) {
			ret.append(" [");
			ret.append(getTag());
			ret.append("]");
		}
		return ret.toString();
	}
	
	public static Set<GraphEdge> edgesWithTag(Set<GraphEdge> edges, EdgeTag tag) {
		Set<GraphEdge> ret = new HashSet<GraphEdge>();
		for (GraphEdge edge : edges) {
			if (edge.getTag() == tag) {
				ret.add(edge);
			}
		}
		return ret;
	}
	
	public static Set<GraphEdge> edgesWithoutTag(Set<GraphEdge> edges, EdgeTag tag) {
		Set<GraphEdge> ret = new HashSet<GraphEdge>();
		for (GraphEdge edge : edges) {
			if (edge.getTag() != tag) {
				ret.add(edge);
			}
		}
		return ret;
	}
	
}