package ch.seto.vikdal.java.transformers;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;

import ch.seto.vikdal.java.SymbolTable;

public class GraphSymbolicator extends TraversalListenerAdapter<GraphNode, GraphEdge> implements DepthFirstVisitor<GraphNode, GraphEdge> {
	private Map<GraphNode, StateTracker> stack;
	private SymbolTable symbols;
	private StateTracker tracker;
	private boolean success;
	
	public GraphSymbolicator(SymbolTable table, StateTracker initial) {
		success = true;
		symbols = table;
		tracker = initial;
		stack = new HashMap<GraphNode, StateTracker>();
	}
	
	public boolean hasSucceeded() {
		return success;
	}
	
	@Override
	public void vertexTraversed(VertexTraversalEvent<GraphNode> e) {
		GraphNode node = e.getVertex();
		// check if we have visited this node before
		StateTracker previous = stack.get(node);
		if (previous != null) {
			// verify that no register has changed its type from the previous encounter
			for (int i = tracker.getLowerRegisterBoundary(); i < tracker.getUpperRegisterBoundary(); i++) {
				if (tracker.getRegisterType(i) != previous.getRegisterType(i)) {
					success = false;
				}
			}
		}
		// add/replace the current tracker state for this node
		stack.put(node, (StateTracker) tracker.clone());
	}
	
	@Override
	public void vertexFinished(VertexTraversalEvent<GraphNode> e) {
		GraphNode node = e.getVertex();
		// use the initial state tracker instance to symbolicate this node
		StateTracker previous = stack.get(node);
		// update the node's description
		if (previous != null) {
			node.setDescription(node.getInstruction().toString(symbols, previous));
		} else {
			node.setDescription(node.getInstruction().toString(symbols, tracker));
		}
	}

	@Override
	public void visitNode(GraphNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitEdge(GraphEdge edge, GraphNode from, GraphNode to) {
		StateTracker previous = stack.get(to);
		// check if we have visited this node before
		if (previous != null) {
			// verify that no register has changed its type from the previous encounter
			for (int i = tracker.getLowerRegisterBoundary(); i < tracker.getUpperRegisterBoundary(); i++) {
				if (tracker.getRegisterType(i) != previous.getRegisterType(i)) {
					success = false;
				}
			}
		}
		// add/replace the current tracker state for this node
		stack.put(to, (StateTracker) tracker.clone());
	}

	@Override
	public void visitReturnEdge(GraphEdge edge, GraphNode from, GraphNode to) {
		// use the initial state tracker instance to symbolicate this node
		StateTracker previous = stack.get(to);
		// update the node's description
		if (previous != null) {
			to.setDescription(to.getInstruction().toString(symbols, previous));
		} else {
			to.setDescription(to.getInstruction().toString(symbols, tracker));
		}
	}
}
