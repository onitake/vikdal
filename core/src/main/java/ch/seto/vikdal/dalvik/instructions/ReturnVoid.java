package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import japa.parser.ast.Node;
import japa.parser.ast.stmt.ReturnStmt;

public class ReturnVoid extends AbstractInstruction {
	
	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new ReturnVoid(); } };
	}
	
	@Override
	public int getOpcode() {
		return 0x0e;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_10x;
	}
	
	@Override
	public boolean breaksProgramFlow() {
		return true;
	}

	@Override
	public String toString() {
		return "return";
	}

	@Override
	public Node toAST() {
		Node ret = new ReturnStmt();
		ret.setData(this);
		return ret;
	}

}