package ch.seto.vikdal.java.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

import ch.seto.vikdal.java.SymbolTable;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;

public class BodyGenerator implements TraversalListener<GraphNode, GraphEdge> {
	
	private interface StatementProducer {
		public void addChild(Statement statement);
		public Statement getStatement();
	}
	private static class BlockStatementProducer implements StatementProducer {
		private final BlockStmt block = new BlockStmt(new ArrayList<Statement>());
		@Override
		public void addChild(Statement statement) {
			block.getStmts().add(statement);
		}
		@Override
		public Statement getStatement() {
			return block;
		}
	}

	private final SymbolTable table;
	private final List<Statement> statements;
	private final Map<GraphNode, Statement> nodes;
	private final Stack<GraphNode> stack;
	private final Stack<StatementProducer> producers;

	public BodyGenerator(SymbolTable table) {
		this.table = table;
		statements = new ArrayList<>();
		nodes = new HashMap<>();
		stack = new Stack<>();
		producers = new Stack<>();
		producers.add(new BlockStatementProducer());
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<GraphNode, GraphEdge> e) {
		GraphNode vs = e.getEdge().source;
		Statement ss = getOrMake(vs);
		GraphNode vt = e.getEdge().target;
		Statement st = getOrMake(vt);
		switch (ss.getData().getClass().getName()) {
		default:
			
		}
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<GraphNode> e) {
		GraphNode v = e.getVertex();
		stack.push(v);
		Statement s = getOrMake(v);
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<GraphNode> e) {
		GraphNode v = e.getVertex();
		GraphNode v2 = stack.pop();
		if (v != v2) {
			throw new RuntimeException("Did not finish with the same node as started");
		}
		Statement s = getOrMake(v);
	}

	public BlockStmt getBody() {
		return new BlockStmt(statements);
	}

	private Statement getOrMake(GraphNode n) {
		Statement s = nodes.get(n);
		if (s == null) {
			InstructionGraphNode iv = (InstructionGraphNode) n;
			s = iv.getInstruction().toAST(table);
			nodes.put(n,  s);
		}
		return s;
	}
}
