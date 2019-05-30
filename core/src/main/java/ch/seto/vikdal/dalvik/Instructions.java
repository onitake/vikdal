package ch.seto.vikdal.dalvik;

import java.util.*;
import ch.seto.vikdal.dalvik.instructions.*;

/**
 * Helper class that encapsulates the instruction set.<br/>
 * Call {@link Instructions#fromBytecode(short[], int)} to decode a bytecode sequence.
 * {@link Instruction#getFormat()}.getInstructionSize() will tell by how much to forward the bytecode pointer.
 */
public final class Instructions {
	
	/**
	 * A map of all bytecodes, indexed by their respective opcode.<br/>
	 * Note that you need to write special handling for 16 bit opcodes like
	 * <ul>
	 * <li>0x0100 - {@link PackedSwitchPayload}</li>
	 * <li>0x0200 - {@link SparseSwitchPayload}</li>
	 * <li>0x0300 - {@link FillArrayDataPayload}</li>
	 * </ul>
	 * <i>nop</i> shares its opcode with these and will undergo special handling as well.
	 */
	private final static InstructionFactory[] INSTRUCTION_MAP = {
		/*0x00*/ null,
		/*0x01*/ Move.factory(Move.Operation.move),
		/*0x02*/ Move.factory(Move.Operation.move_from16),
		/*0x03*/ Move.factory(Move.Operation.move16),
		/*0x04*/ Move.factory(Move.Operation.move_wide),
		/*0x05*/ Move.factory(Move.Operation.move_wide_from16),
		/*0x06*/ Move.factory(Move.Operation.move_wide16),
		/*0x07*/ Move.factory(Move.Operation.move_object),
		/*0x08*/ Move.factory(Move.Operation.move_object_from16),
		/*0x09*/ Move.factory(Move.Operation.move_object16),
		/*0x0a*/ MoveResult.factory(MoveResult.Operation.move_result),
		/*0x0b*/ MoveResult.factory(MoveResult.Operation.move_result_wide),
		/*0x0c*/ MoveResult.factory(MoveResult.Operation.move_result_object),
		/*0x0d*/ MoveResult.factory(MoveResult.Operation.move_exception),
		/*0x0e*/ ReturnVoid.factory(),
		/*0x0f*/ Return.factory(Return.Operation.return_),
		/*0x10*/ Return.factory(Return.Operation.return_wide),
		/*0x11*/ Return.factory(Return.Operation.return_object),
		/*0x12*/ Const.factory(Const.Operation.const4),
		/*0x13*/ Const.factory(Const.Operation.const16),
		/*0x14*/ Const.factory(Const.Operation.const_),
		/*0x15*/ Const.factory(Const.Operation.const_high16),
		/*0x16*/ ConstWide.factory(ConstWide.Operation.const_wide16),
		/*0x17*/ ConstWide.factory(ConstWide.Operation.const_wide32),
		/*0x18*/ ConstWide.factory(ConstWide.Operation.const_wide),
		/*0x19*/ ConstWide.factory(ConstWide.Operation.const_wide_high16),
		/*0x1a*/ ConstObject.factory(ConstObject.Operation.const_string),
		/*0x1b*/ ConstObject.factory(ConstObject.Operation.const_string_jumbo),
		/*0x1c*/ ConstObject.factory(ConstObject.Operation.const_class),
		/*0x1d*/ Monitor.factory(Monitor.Operation.monitor_enter),
		/*0x1e*/ Monitor.factory(Monitor.Operation.monitor_exit),
		/*0x1f*/ CheckCast.factory(),
		/*0x20*/ InstanceOf.factory(),
		/*0x21*/ ArrayLength.factory(),
		/*0x22*/ NewInstance.factory(),
		/*0x23*/ NewArray.factory(),
		/*0x24*/ FilledNewArray.factory(),
		/*0x25*/ FilledNewArrayRange.factory(),
		/*0x26*/ FillArrayData.factory(),
		/*0x27*/ Throw.factory(),
		/*0x28*/ Goto.factory(Goto.Operation.goto_),
		/*0x29*/ Goto.factory(Goto.Operation.goto16),
		/*0x2a*/ Goto.factory(Goto.Operation.goto32),
		/*0x2b*/ Switch.factory(Switch.Operation.packed_switch),
		/*0x2c*/ Switch.factory(Switch.Operation.sparse_switch),
		/*0x2d*/ Cmp.factory(Cmp.Operation.cmpl_float),
		/*0x2e*/ Cmp.factory(Cmp.Operation.cmpg_float),
		/*0x2f*/ Cmp.factory(Cmp.Operation.cmpl_double),
		/*0x30*/ Cmp.factory(Cmp.Operation.cmpg_double),
		/*0x31*/ Cmp.factory(Cmp.Operation.cmp_long),
		/*0x32*/ IfTest.factory(IfTest.Operation.if_eq),
		/*0x33*/ IfTest.factory(IfTest.Operation.if_ne),
		/*0x34*/ IfTest.factory(IfTest.Operation.if_lt),
		/*0x35*/ IfTest.factory(IfTest.Operation.if_ge),
		/*0x36*/ IfTest.factory(IfTest.Operation.if_gt),
		/*0x37*/ IfTest.factory(IfTest.Operation.if_le),
		/*0x38*/ IfTestZ.factory(IfTestZ.Operation.if_eqz),
		/*0x39*/ IfTestZ.factory(IfTestZ.Operation.if_nez),
		/*0x3a*/ IfTestZ.factory(IfTestZ.Operation.if_ltz),
		/*0x3b*/ IfTestZ.factory(IfTestZ.Operation.if_gez),
		/*0x3c*/ IfTestZ.factory(IfTestZ.Operation.if_gtz),
		/*0x3d*/ IfTestZ.factory(IfTestZ.Operation.if_lez),
		/*0x3e*/ null,
		/*0x3f*/ null,
		/*0x40*/ null,
		/*0x41*/ null,
		/*0x42*/ null,
		/*0x43*/ null,
		/*0x44*/ ArrayOp.factory(ArrayOp.Operation.aget),
		/*0x45*/ ArrayOp.factory(ArrayOp.Operation.aget_wide),
		/*0x46*/ ArrayOp.factory(ArrayOp.Operation.aget_object),
		/*0x47*/ ArrayOp.factory(ArrayOp.Operation.aget_boolean),
		/*0x48*/ ArrayOp.factory(ArrayOp.Operation.aget_byte),
		/*0x49*/ ArrayOp.factory(ArrayOp.Operation.aget_char),
		/*0x4a*/ ArrayOp.factory(ArrayOp.Operation.aget_short),
		/*0x4b*/ ArrayOp.factory(ArrayOp.Operation.aput),
		/*0x4c*/ ArrayOp.factory(ArrayOp.Operation.aput_wide),
		/*0x4d*/ ArrayOp.factory(ArrayOp.Operation.aput_object),
		/*0x4e*/ ArrayOp.factory(ArrayOp.Operation.aput_boolean),
		/*0x4f*/ ArrayOp.factory(ArrayOp.Operation.aput_byte),
		/*0x50*/ ArrayOp.factory(ArrayOp.Operation.aput_char),
		/*0x51*/ ArrayOp.factory(ArrayOp.Operation.aput_short),
		/*0x52*/ InstanceOp.factory(InstanceOp.Operation.iget),
		/*0x53*/ InstanceOp.factory(InstanceOp.Operation.iget_wide),
		/*0x54*/ InstanceOp.factory(InstanceOp.Operation.iget_object),
		/*0x55*/ InstanceOp.factory(InstanceOp.Operation.iget_boolean),
		/*0x56*/ InstanceOp.factory(InstanceOp.Operation.iget_byte),
		/*0x57*/ InstanceOp.factory(InstanceOp.Operation.iget_char),
		/*0x58*/ InstanceOp.factory(InstanceOp.Operation.iget_short),
		/*0x59*/ InstanceOp.factory(InstanceOp.Operation.iput),
		/*0x5a*/ InstanceOp.factory(InstanceOp.Operation.iput_wide),
		/*0x5b*/ InstanceOp.factory(InstanceOp.Operation.iput_object),
		/*0x5c*/ InstanceOp.factory(InstanceOp.Operation.iput_boolean),
		/*0x5d*/ InstanceOp.factory(InstanceOp.Operation.iput_byte),
		/*0x5e*/ InstanceOp.factory(InstanceOp.Operation.iput_char),
		/*0x5f*/ InstanceOp.factory(InstanceOp.Operation.iput_short),
		/*0x60*/ StaticOp.factory(StaticOp.Operation.sget),
		/*0x61*/ StaticOp.factory(StaticOp.Operation.sget_wide),
		/*0x62*/ StaticOp.factory(StaticOp.Operation.sget_object),
		/*0x63*/ StaticOp.factory(StaticOp.Operation.sget_boolean),
		/*0x64*/ StaticOp.factory(StaticOp.Operation.sget_byte),
		/*0x65*/ StaticOp.factory(StaticOp.Operation.sget_char),
		/*0x66*/ StaticOp.factory(StaticOp.Operation.sget_short),
		/*0x67*/ StaticOp.factory(StaticOp.Operation.sput),
		/*0x68*/ StaticOp.factory(StaticOp.Operation.sput_wide),
		/*0x69*/ StaticOp.factory(StaticOp.Operation.sput_object),
		/*0x6a*/ StaticOp.factory(StaticOp.Operation.sput_boolean),
		/*0x6b*/ StaticOp.factory(StaticOp.Operation.sput_byte),
		/*0x6c*/ StaticOp.factory(StaticOp.Operation.sput_char),
		/*0x6d*/ StaticOp.factory(StaticOp.Operation.sput_short),
		/*0x6e*/ Invoke.factory(Invoke.Operation.invoke_virtual),
		/*0x6f*/ Invoke.factory(Invoke.Operation.invoke_super),
		/*0x70*/ Invoke.factory(Invoke.Operation.invoke_direct),
		/*0x71*/ Invoke.factory(Invoke.Operation.invoke_static),
		/*0x72*/ Invoke.factory(Invoke.Operation.invoke_interface),
		/*0x73*/ null,
		/*0x74*/ InvokeRange.factory(InvokeRange.Operation.invoke_virtual),
		/*0x75*/ InvokeRange.factory(InvokeRange.Operation.invoke_super),
		/*0x76*/ InvokeRange.factory(InvokeRange.Operation.invoke_direct),
		/*0x77*/ InvokeRange.factory(InvokeRange.Operation.invoke_static),
		/*0x78*/ InvokeRange.factory(InvokeRange.Operation.invoke_interface),
		/*0x79*/ null,
		/*0x7a*/ null,
		/*0x7b*/ UnOp.factory(UnOp.Operation.neg_int),
		/*0x7c*/ UnOp.factory(UnOp.Operation.not_int),
		/*0x7d*/ UnOp.factory(UnOp.Operation.neg_long),
		/*0x7e*/ UnOp.factory(UnOp.Operation.not_long),
		/*0x7f*/ UnOp.factory(UnOp.Operation.neg_float),
		/*0x80*/ UnOp.factory(UnOp.Operation.neg_double),
		/*0x81*/ UnOp.factory(UnOp.Operation.int_to_long),
		/*0x82*/ UnOp.factory(UnOp.Operation.int_to_float),
		/*0x83*/ UnOp.factory(UnOp.Operation.int_to_double),
		/*0x84*/ UnOp.factory(UnOp.Operation.long_to_int),
		/*0x85*/ UnOp.factory(UnOp.Operation.long_to_float),
		/*0x86*/ UnOp.factory(UnOp.Operation.long_to_double),
		/*0x87*/ UnOp.factory(UnOp.Operation.float_to_int),
		/*0x88*/ UnOp.factory(UnOp.Operation.float_to_long),
		/*0x89*/ UnOp.factory(UnOp.Operation.float_to_double),
		/*0x8a*/ UnOp.factory(UnOp.Operation.double_to_int),
		/*0x8b*/ UnOp.factory(UnOp.Operation.double_to_long),
		/*0x8c*/ UnOp.factory(UnOp.Operation.double_to_float),
		/*0x8d*/ UnOp.factory(UnOp.Operation.int_to_byte),
		/*0x8e*/ UnOp.factory(UnOp.Operation.int_to_char),
		/*0x8f*/ UnOp.factory(UnOp.Operation.int_to_short),
		/*0x90*/ BinOp.factory(BinOp.Operation.add_int),
		/*0x91*/ BinOp.factory(BinOp.Operation.sub_int),
		/*0x92*/ BinOp.factory(BinOp.Operation.mul_int),
		/*0x93*/ BinOp.factory(BinOp.Operation.div_int),
		/*0x94*/ BinOp.factory(BinOp.Operation.rem_int),
		/*0x95*/ BinOp.factory(BinOp.Operation.and_int),
		/*0x96*/ BinOp.factory(BinOp.Operation.or_int),
		/*0x97*/ BinOp.factory(BinOp.Operation.xor_int),
		/*0x98*/ BinOp.factory(BinOp.Operation.shl_int),
		/*0x99*/ BinOp.factory(BinOp.Operation.shr_int),
		/*0x9a*/ BinOp.factory(BinOp.Operation.ushr_int),
		/*0x9b*/ BinOp.factory(BinOp.Operation.add_long),
		/*0x9c*/ BinOp.factory(BinOp.Operation.sub_long),
		/*0x9d*/ BinOp.factory(BinOp.Operation.mul_long),
		/*0x9e*/ BinOp.factory(BinOp.Operation.div_long),
		/*0x9f*/ BinOp.factory(BinOp.Operation.rem_long),
		/*0xa0*/ BinOp.factory(BinOp.Operation.and_long),
		/*0xa1*/ BinOp.factory(BinOp.Operation.or_long),
		/*0xa2*/ BinOp.factory(BinOp.Operation.xor_long),
		/*0xa3*/ BinOp.factory(BinOp.Operation.shl_long),
		/*0xa4*/ BinOp.factory(BinOp.Operation.shr_long),
		/*0xa5*/ BinOp.factory(BinOp.Operation.ushr_long),
		/*0xa6*/ BinOp.factory(BinOp.Operation.add_float),
		/*0xa7*/ BinOp.factory(BinOp.Operation.sub_float),
		/*0xa8*/ BinOp.factory(BinOp.Operation.mul_float),
		/*0xa9*/ BinOp.factory(BinOp.Operation.div_float),
		/*0xaa*/ BinOp.factory(BinOp.Operation.rem_float),
		/*0xab*/ BinOp.factory(BinOp.Operation.add_double),
		/*0xac*/ BinOp.factory(BinOp.Operation.sub_double),
		/*0xad*/ BinOp.factory(BinOp.Operation.mul_double),
		/*0xae*/ BinOp.factory(BinOp.Operation.div_double),
		/*0xaf*/ BinOp.factory(BinOp.Operation.rem_double),
		/*0xb0*/ BinOp2Addr.factory(BinOp2Addr.Operation.add_int),
		/*0xb1*/ BinOp2Addr.factory(BinOp2Addr.Operation.sub_int),
		/*0xb2*/ BinOp2Addr.factory(BinOp2Addr.Operation.mul_int),
		/*0xb3*/ BinOp2Addr.factory(BinOp2Addr.Operation.div_int),
		/*0xb4*/ BinOp2Addr.factory(BinOp2Addr.Operation.rem_int),
		/*0xb5*/ BinOp2Addr.factory(BinOp2Addr.Operation.and_int),
		/*0xb6*/ BinOp2Addr.factory(BinOp2Addr.Operation.or_int),
		/*0xb7*/ BinOp2Addr.factory(BinOp2Addr.Operation.xor_int),
		/*0xb8*/ BinOp2Addr.factory(BinOp2Addr.Operation.shl_int),
		/*0xb9*/ BinOp2Addr.factory(BinOp2Addr.Operation.shr_int),
		/*0xba*/ BinOp2Addr.factory(BinOp2Addr.Operation.ushr_int),
		/*0xbb*/ BinOp2Addr.factory(BinOp2Addr.Operation.add_long),
		/*0xbc*/ BinOp2Addr.factory(BinOp2Addr.Operation.sub_long),
		/*0xbd*/ BinOp2Addr.factory(BinOp2Addr.Operation.mul_long),
		/*0xbe*/ BinOp2Addr.factory(BinOp2Addr.Operation.div_long),
		/*0xbf*/ BinOp2Addr.factory(BinOp2Addr.Operation.rem_long),
		/*0xc0*/ BinOp2Addr.factory(BinOp2Addr.Operation.and_long),
		/*0xc1*/ BinOp2Addr.factory(BinOp2Addr.Operation.or_long),
		/*0xc2*/ BinOp2Addr.factory(BinOp2Addr.Operation.xor_long),
		/*0xc3*/ BinOp2Addr.factory(BinOp2Addr.Operation.shl_long),
		/*0xc4*/ BinOp2Addr.factory(BinOp2Addr.Operation.shr_long),
		/*0xc5*/ BinOp2Addr.factory(BinOp2Addr.Operation.ushr_long),
		/*0xc6*/ BinOp2Addr.factory(BinOp2Addr.Operation.add_float),
		/*0xc7*/ BinOp2Addr.factory(BinOp2Addr.Operation.sub_float),
		/*0xc8*/ BinOp2Addr.factory(BinOp2Addr.Operation.mul_float),
		/*0xc9*/ BinOp2Addr.factory(BinOp2Addr.Operation.div_float),
		/*0xca*/ BinOp2Addr.factory(BinOp2Addr.Operation.rem_float),
		/*0xcb*/ BinOp2Addr.factory(BinOp2Addr.Operation.add_double),
		/*0xcc*/ BinOp2Addr.factory(BinOp2Addr.Operation.sub_double),
		/*0xcd*/ BinOp2Addr.factory(BinOp2Addr.Operation.mul_double),
		/*0xce*/ BinOp2Addr.factory(BinOp2Addr.Operation.div_double),
		/*0xcf*/ BinOp2Addr.factory(BinOp2Addr.Operation.rem_double),
		/*0xd0*/ BinOpLit.factory(BinOpLit.Operation.add_int16),
		/*0xd1*/ BinOpLit.factory(BinOpLit.Operation.rsub_int16),
		/*0xd2*/ BinOpLit.factory(BinOpLit.Operation.mul_int16),
		/*0xd3*/ BinOpLit.factory(BinOpLit.Operation.div_int16),
		/*0xd4*/ BinOpLit.factory(BinOpLit.Operation.rem_int16),
		/*0xd5*/ BinOpLit.factory(BinOpLit.Operation.and_int16),
		/*0xd6*/ BinOpLit.factory(BinOpLit.Operation.or_int16),
		/*0xd7*/ BinOpLit.factory(BinOpLit.Operation.xor_int16),
		/*0xd8*/ BinOpLit.factory(BinOpLit.Operation.add_int8),
		/*0xd9*/ BinOpLit.factory(BinOpLit.Operation.rsub_int8),
		/*0xda*/ BinOpLit.factory(BinOpLit.Operation.mul_int8),
		/*0xdb*/ BinOpLit.factory(BinOpLit.Operation.div_int8),
		/*0xdc*/ BinOpLit.factory(BinOpLit.Operation.rem_int8),
		/*0xdd*/ BinOpLit.factory(BinOpLit.Operation.and_int8),
		/*0xde*/ BinOpLit.factory(BinOpLit.Operation.or_int8),
		/*0xdf*/ BinOpLit.factory(BinOpLit.Operation.xor_int8),
		/*0xe0*/ BinOpLit.factory(BinOpLit.Operation.shl_int8),
		/*0xe1*/ BinOpLit.factory(BinOpLit.Operation.shr_int8),
		/*0xe2*/ BinOpLit.factory(BinOpLit.Operation.ushr_int8),
		/*0xe3*/ null,
		/*0xe4*/ null,
		/*0xe5*/ null,
		/*0xe6*/ null,
		/*0xe7*/ null,
		/*0xe8*/ null,
		/*0xe9*/ null,
		/*0xea*/ null,
		/*0xeb*/ null,
		/*0xec*/ null,
		/*0xed*/ null,
		/*0xee*/ null,
		/*0xef*/ null,
		/*0xf0*/ null,
		/*0xf1*/ null,
		/*0xf2*/ null,
		/*0xf3*/ null,
		/*0xf4*/ null,
		/*0xf5*/ null,
		/*0xf6*/ null,
		/*0xf7*/ null,
		/*0xf8*/ null,
		/*0xf9*/ null,
		/*0xfa*/ null,
		/*0xfb*/ null,
		/*0xfc*/ null,
		/*0xfd*/ null,
		/*0xfe*/ null,
		/*0xff*/ null,
	};
	
	static {
		// FIXME Sanity check, this should be a unit test instead
		for (int opcode = 0; opcode < INSTRUCTION_MAP.length; opcode++) {
			if (INSTRUCTION_MAP[opcode] != null) {
				if (INSTRUCTION_MAP[opcode].newInstance().getOpcode() != opcode) {
					// Looks like we are insane
					throw new RuntimeException("Invalid instruction class, the opcode differs (map=" + opcode + " class=" + INSTRUCTION_MAP[opcode].newInstance().getOpcode() + ")");
				}
			}
		}
	}
	
	/**
	 * Decodes a bytecode sequence into a dalvik instruction.
	 * Use {@link Instruction#getInstructionSize()} to determine the start of the next instruction in the sequence.
	 * @param bytecode an array of bytecodes (dalvik machine code)
	 * @return a decoded instruction
	 * @throws IllegalInstructionException if the bytecode sequence did not represent a dalvik instruction,
	 * was too short, or had an invalid parameter encoding
	 */
	public static Instruction fromBytecode(short[] bytecode) throws IllegalInstructionException {
		return fromBytecode(bytecode, 0);
	}
	
	/**
	 * Decodes a bytecode sequence into a dalvik instruction.<br/>
	 * Use {@link Instruction#getInstructionSize()} to determine the start of the next instruction in the sequence.
	 * @param bytecode an array of bytecodes (dalvik machine code)
	 * @param off the index of the first bytecode to examine
	 * @return a decoded instruction
	 * @throws IllegalInstructionException if the bytecode sequence did not represent a dalvik instruction,
	 * was too short, or had an invalid parameter encoding
	 */
	public static Instruction fromBytecode(short[] bytecode, int off) throws IllegalInstructionException {
		int opcode = bytecode[off] & 0xff;
		if (opcode == 0x00) {
			// special case for payload data and nop
			opcode = bytecode[off] & 0xffff;
			switch (opcode) {
			case 0x0000:
				return new Nop();
			case 0x0100:
				return new PackedSwitchPayload(bytecode, off);
			case 0x0200:
				return new SparseSwitchPayload(bytecode, off);
			case 0x0300:
				return new FillArrayDataPayload(bytecode, off);
			}
			throw new IllegalInstructionException(String.format("Undefined payload instruction 0x%04x", opcode));
		} else {
			InstructionFactory factory = INSTRUCTION_MAP[opcode];
			if (factory != null) {
				Instruction ret = (Instruction) factory.newInstance();
				ret.setArguments(bytecode, off);
				return ret;
			}
			throw new IllegalInstructionException(String.format("Undefined opcode 0x%02x", opcode));
		}
	}

	/**
	 * Generate a hex string from a sequence of bytecodes
	 * @param bytecode an array of 16bit bytecodes
	 * @return a string of the form "0123 4567 89ab cdef"
	 */
	public static String byteCodeToHexString(short[] bytecode) {
		StringBuilder ret = new StringBuilder();
		boolean notfirst = false;
		for (short code : bytecode) {
			if (notfirst) {
				ret.append(' ');
			} else {
				notfirst = true;
			}
			ret.append(String.format("%04x", code));
		}
		return ret.toString();
	}
	
	/**
	 * Parse a bytecode sequence into a chain of instructions.
	 * @param bytecode a list of bytecodes
	 * @return an ordered mapping from addresses to instructions
	 * @throws IllegalInstructionException
	 */
	public static SortedMap<Integer, Instruction> parse(short[] bytecode) throws IllegalInstructionException {
		SortedMap<Integer, Instruction> code = new TreeMap<Integer, Instruction>();
		for (int i = 0; i < bytecode.length;) {
			try {
				int address = i;
				Instruction instruction = fromBytecode(bytecode, address);
				i += instruction.getInstructionSize();
				// FIXME Move this into a unit test
				try {
					short[] original = Arrays.copyOfRange(bytecode, address, i);
					short[] encoded = instruction.getBytecode();
					if (!Arrays.equals(encoded, original)) {
						throw new RuntimeException("Instruction did not reencode into the same bytecode (original=" + Instructions.byteCodeToHexString(original) + " reencoded=" + Instructions.byteCodeToHexString(encoded) + ")");
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new RuntimeException("Reencoded instruction length mismatch");
				}
				code.put(address, instruction);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new IllegalInstructionException("Truncated instruction", e);
			}
		}
		return code;
	}
	
	/**
	 *  Don't instantiate utility classes
	 */
	private Instructions() { }

}
