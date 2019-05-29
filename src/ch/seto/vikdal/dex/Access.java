package ch.seto.vikdal.dex;

import java.util.EnumSet;

import ch.seto.vikdal.java.Modifier;

public enum Access {
	ACC_PUBLIC(0x00000001, "public"),
	ACC_PRIVATE(0x00000002, "private"),
	ACC_PROTECTED(0x00000004, "protected"),
	ACC_STATIC(0x00000008, "static"),
	ACC_FINAL(0x00000010, "final"),
	ACC_SYNCHRONIZED(0x00000020, "synchronized"),
	ACC_VOLATILE(0x00000040, "volatile"),
	ACC_TRANSIENT(0x00000080, "transient"),
	ACC_VARARGS(0x00000080, "varargs"),
	ACC_NATIVE(0x00000100, "native"),
	ACC_INTERFACE(0x00000200, "interface"),
	ACC_ABSTRACT(0x00000400, "abstract"),
	ACC_STRICT(0x00000800, "strict"),
	ACC_SYNTHETIC(0x00001000, "synthetic"),
	ACC_ANNOTATION(0x00002000, "annotation"),
	ACC_ENUM(0x00004000, "enum"),
	ACC_CONSTRUCTOR(0x00010000, "constructor"),
	ACC_DECLARED_SYNCHRONIZED(0x00020000, "declared_synchronized");
	
	private final long flag;
	private final String description;
	
	private Access(long f, String d) {
		flag = f;
		description = d;
	}
	
	public long getFlag() {
		return flag;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return getDescription();
	}
	
	public static EnumSet<Access> fromFlags(long flags) {
		EnumSet<Access> ret = EnumSet.noneOf(Access.class);
		for (Access access : Access.values()) {
			if ((flags & access.getFlag()) != 0) {
				ret.add(access);
			}
		}
		return ret;
	}
	
	public static long toFlags(EnumSet<Access> flags) {
		long ret = 0;
		for (Access access : flags) {
			ret |= access.getFlag();
		}
		return ret;
	}
	
	public static EnumSet<Modifier> toJavaModifier(EnumSet<Access> flags) {
		EnumSet<Modifier> ret = EnumSet.noneOf(Modifier.class);
		for (Access access : flags) {
			switch (access) {
			case ACC_PUBLIC:
				ret.add(Modifier.PUBLIC);
				break;
			case ACC_PRIVATE:
				ret.add(Modifier.PRIVATE);
				break;
			case ACC_PROTECTED:
				ret.add(Modifier.PROTECTED);
				break;
			case ACC_STATIC:
				ret.add(Modifier.STATIC);
				break;
			case ACC_FINAL:
				ret.add(Modifier.FINAL);
				break;
			case ACC_SYNCHRONIZED:
				ret.add(Modifier.SYNCHRONIZED);
				break;
			case ACC_VOLATILE:
				ret.add(Modifier.VOLATILE);
				break;
			case ACC_TRANSIENT:
				ret.add(Modifier.TRANSIENT);
				break;
			case ACC_NATIVE:
				ret.add(Modifier.NATIVE);
				break;
			case ACC_ABSTRACT:
				ret.add(Modifier.ABSTRACT);
				break;
			default:
				// warn about unsupported modifiers
				break;
			}
		}
		return ret;
	}
	
}