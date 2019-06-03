package ch.seto.vikdal.java.transformers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//import org.jgrapht.EdgeFactory;

import ch.seto.vikdal.java.EdgeTag;

public class GraphEdge {
//	public final static EdgeFactory<GraphNode, GraphEdge> FACTORY = new EdgeFactory<GraphNode, GraphEdge>() {
//		@Override
//		public GraphEdge createEdge(GraphNode source, GraphNode target) {
//			return new GraphEdge(source, target);
//		}
//	};
	
	public final GraphNode source, target;
	private EdgeTag tag;
	
	public GraphEdge(GraphNode s, GraphNode t) {
		this(s, t, EdgeTag.DEFAULT);
	}

	public GraphEdge(GraphNode s, GraphNode t, EdgeTag tg) {
		source = s;
		target = t;
		tag = tg;
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
		if (source.getAddress() != -1) {
			ret.append(source.getAddress());
		}
		if (source.getAddress() != -1 || target.getAddress() != -1) {
			// right arrow
			ret.append(" \u2192 ");
		}
		if (target.getAddress() != -1) {
			ret.append(target.getAddress());
		}
		if (getTag() != EdgeTag.DEFAULT) {
			ret.append(" [");
			ret.append(getTag());
			ret.append("]");
		}
		return ret.toString();
	}
	
	public static Collection<GraphEdge> edgesWithTag(Collection<GraphEdge> edges, EdgeTag tag) {
		Set<GraphEdge> ret = new HashSet<GraphEdge>();
		for (GraphEdge edge : edges) {
			if (edge.getTag() == tag) {
				ret.add(edge);
			}
		}
		return ret;
	}
	
	public static Collection<GraphEdge> edgesWithoutTag(Collection<GraphEdge> edges, EdgeTag tag) {
		Set<GraphEdge> ret = new HashSet<GraphEdge>();
		for (GraphEdge edge : edges) {
			if (edge.getTag() != tag) {
				ret.add(edge);
			}
		}
		return ret;
	}
	
}