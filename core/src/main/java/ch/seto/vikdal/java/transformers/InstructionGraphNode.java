package ch.seto.vikdal.java.transformers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ch.seto.vikdal.dalvik.IllegalInstructionException;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.Instructions;

// Serializable, to stop mxGraph from complaining when moving nodes
@SuppressWarnings("serial")
public class InstructionGraphNode implements GraphNode, Serializable {
	private int address;
	private Instruction instruction;
	private String description;
	
	public InstructionGraphNode(int addr, Instruction inst) {
		address = addr;
		instruction = inst;
		description = instruction.toString();
	}

	public int getAddress() {
		return address;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(address);
		short[] bytecodes = instruction.getBytecode();
		if (bytecodes == null) {
			throw new RuntimeException("Got no bytecodes from " + instruction.getClass().getName());
		} else {
			out.writeInt(bytecodes.length);
			for (short bytecode : bytecodes) {
				out.writeShort(bytecode);
			}
			out.writeObject(description);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		address = in.readInt();
		int length = in.readInt();
		short[] bytecodes = new short[length];
		for (int i = 0; i < length; i++) {
			bytecodes[i] = in.readShort();
		}
		try {
			instruction = Instructions.fromBytecode(bytecodes);
		} catch (IllegalInstructionException e) {
			throw new ClassNotFoundException("Cannot deserialize invalid bytecode", e);
		}
		description = (String) in.readObject();
	}
	
	@Override
	public String toString() {
		return description;
	}

	@Override
	public String edgeFromDescription() {
		if (address != -1) {
			return String.valueOf(address);
		}
		return null;
	}

	@Override
	public String edgeToDescription() {
		if (address != -1) {
			return String.valueOf(address);
		}
		return null;
	}

	@Override
	public String nodeDescription() {
		return description;
	}

	/*@Override
	public int hashCode() {
		return address;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return address == ((GraphNode) obj).address;
	}*/
	
}
