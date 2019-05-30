package ch.seto.vikdal.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.SortedMap;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.code.Method;
import ch.seto.vikdal.java.transformers.CodeGraphGenerator;
import ch.seto.vikdal.java.transformers.Function;
import ch.seto.vikdal.java.transformers.ProgramVerificationException;
import japa.parser.ast.Node;

public class AstTest {

	public static void main(String[] args) {
		File dexFile;
		if (args.length > 0) {
			dexFile = new File(args[0]);
		} else {
			try {
				dexFile = new File(AstTest.class.getResource("/classes.dex").toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException("Can't load built-in resource", e);
			}
		}
		int classId = -1;
		if (args.length > 1) {
			classId = Integer.parseInt(args[1]);
		}
		int methodId = -1;
		if (args.length > 2) {
			methodId = Integer.parseInt(args[2]);
		}
		AstTest test = new AstTest(dexFile);
		if (classId != -1) {
			if (methodId != -1) {
				Node ast = test.decompile(classId, methodId);
				System.out.println(ast.toString());
			} else {
				test.printMethods(classId, System.out);
			}
		} else {
			test.printClasses(System.out);
		}
		
	}

	private void printClasses(PrintStream out) {
		for (int i = 0; i < dex.numberOfTypes(); i++) {
			ClassDescriptor klass = dex.lookupClass(i);
			if (klass != null) {
				out.println("" + klass.classid + ": " + klass.toString(dex));
			}
		}
	}

	private void printMethods(int classId, PrintStream out) {
		ClassDescriptor klass = dex.lookupClass(classId);
		if (klass != null) {
			for (ClassMethodDescriptor method : klass.methods) {
				out.println("" + method.methodid + ": " + method.toString(dex));
			}
		} else {
			out.println("No class with id " + classId + " found.");
		}
	}

	private Dex dex;
	private CodeGraphGenerator generator;

	private AstTest(File dexFile) {
		try {
			dex = new Dex(dexFile);
			dex.parse();
			generator = new CodeGraphGenerator(dex);
		} catch (IOException e) {
			throw new RuntimeException("Can't read DEX archive", e);
		} catch (DexFormatException e) {
			throw new RuntimeException("Invalid DEX archive", e);
		}
	}
	
	private Node decompile(int classId, int methodId) {
		ClassDescriptor klass = dex.lookupClass(classId);
		if (klass == null) {
			throw new IndexOutOfBoundsException("Can't find class " + classId + " in DEX");
		}
		ClassMethodDescriptor cm = null;
		for (ClassMethodDescriptor method : klass.methods) {
			if (method.methodid == methodId) {
				cm = method;
			}
		}
		if (cm == null) {
			throw new IndexOutOfBoundsException("Can't find method " + methodId + " in class " + classId);
		}
		SortedMap<Integer, Instruction> code = dex.getCode(methodId);
		try {
			Function fn = generator.transformToPseudoCode(code, cm);
			Function fns = generator.symbolicate(fn);
			Method m = generator.transformToStatements(fns);
			return m.getASTBody();
		} catch (ProgramVerificationException e) {
			throw new RuntimeException("Can't decompile", e);
		}
	}

}
