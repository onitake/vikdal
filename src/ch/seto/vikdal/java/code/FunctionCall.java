package ch.seto.vikdal.java.code;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class FunctionCall implements Expression, Statement {

	public static class Argument {
		public String type;
		public String name;
	}
	
	private final static Pattern QUALIFIED_NAME_SPLIT = Pattern.compile("^(.*)\\.(.*?)$");
	
	public String name;
	public String assignTo;
	public String receiver;
	public List<String> arguments;
	
	public FunctionCall(String receiver, String name) {
		this.name = name;
		this.receiver = receiver;
		assignTo = null;
		arguments = new ArrayList<String>();
	}

	public boolean isAssignment() {
		return assignTo != null;
	}
	
	@Override
	public japa.parser.ast.stmt.Statement toASTStatement() {
		return new ExpressionStmt(toASTExpression());
	}

	@Override
	public japa.parser.ast.expr.Expression toASTExpression() {
		japa.parser.ast.expr.Expression ret;
		NameExpr receiverExpr;
		Matcher match = QUALIFIED_NAME_SPLIT.matcher(receiver);
		if (match.matches()) {
			receiverExpr = new QualifiedNameExpr(new NameExpr(match.group(1)), match.group(2));
		} else {
			receiverExpr = new NameExpr(receiver);
		}
		List<japa.parser.ast.expr.Expression> argsExpr = new ArrayList<japa.parser.ast.expr.Expression>();
		for (String arg : arguments) {
			argsExpr.add(new NameExpr(arg));
		}
		MethodCallExpr callExpr = new MethodCallExpr(receiverExpr, name, argsExpr);
		if (isAssignment()) {
			NameExpr assignVarExpr = new NameExpr(assignTo);
			AssignExpr assignExpr = new AssignExpr(assignVarExpr, callExpr, AssignExpr.Operator.assign);
			ret = assignExpr;
		} else {
			ret = callExpr;
		}
		return ret;
	}

}
