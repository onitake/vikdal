package ch.seto.vikdal.java.code;

import java.util.*;

import ch.seto.vikdal.java.Modifier;
import ch.seto.vikdal.java.SymbolTable;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;

public class Method {

	public static class Argument {
		public String type;
		public String name;
		public Set<Modifier> modifiers;
		public Argument(String type, String name) {
			this.type = type;
			this.name = name;
			modifiers = EnumSet.noneOf(Modifier.class);
		}
	}
	
	public String name;
	public String type;
	public List<Argument> arguments;
	public Block block;
	public Set<Modifier> modifiers;
	
	public Method(String name) {
		this.name = name;
		this.arguments = new ArrayList<Method.Argument>();
		block = new Block();
		modifiers = EnumSet.noneOf(Modifier.class);
	}
	
	public boolean isConstructor() {
		return type == null;
	}
	
	public BodyDeclaration getASTBody(SymbolTable table) {
		int mods = 0;
		for (Modifier mod : modifiers) {
			mods |= mod.javaModifier;
		}
		List<AnnotationExpr> annotations = null;
        List<TypeParameter> typeParameters = null;
		List<Parameter> params = new ArrayList<Parameter>();
		for (Argument arg : arguments) {
			int argMods = 0;
			for (Modifier mod : arg.modifiers) {
				argMods |= mod.javaModifier;
			}
			Type typ = new ClassOrInterfaceType(arg.type);
			VariableDeclaratorId varDec = new VariableDeclaratorId(arg.name);
			params.add(new Parameter(argMods, typ, varDec));
		}
		List<NameExpr> exceptions = null;
		BodyDeclaration decl;
		if (isConstructor()) {
			decl = new ConstructorDeclaration(mods, annotations, typeParameters, name, params, exceptions, (BlockStmt) block.toASTStatement(table));
		} else {
			Type typ = new ClassOrInterfaceType(type);
			int arrayDimensions = 0;
			decl = new MethodDeclaration(mods, annotations, typeParameters, typ, name, params, arrayDimensions, exceptions, (BlockStmt) block.toASTStatement(table));
		}
		return decl;
	}
	
}
