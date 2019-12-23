package ch.seto.vikdal.java.transformers;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.code.Block;
import ch.seto.vikdal.java.code.Statement;

public class InstructionGenerator implements TraversalListener<GraphNode, GraphEdge> {

	private Block block;

	public InstructionGenerator() {
		block = new Block();
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<GraphNode, GraphEdge> e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<GraphNode> e) {
		final Instruction instruction = ((InstructionGraphNode) e.getSource()).getInstruction();
		block.statements.add(new Statement() {
			@Override
			public japa.parser.ast.stmt.Statement toASTStatement(SymbolTable table) {
				return instruction.toAST(table);
			}
		});
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<GraphNode> e) {
		// TODO Auto-generated method stub

	}

	public Block getBlock() {
		return block;
	}

}
