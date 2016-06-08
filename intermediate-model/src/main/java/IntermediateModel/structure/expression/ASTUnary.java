package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.ASTREVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTUnary extends IASTStm implements IASTRE {

	private OPERATOR op;
	private IASTRE expr;

	public ASTUnary(Token start, Token end, OPERATOR op, IASTRE expr) {
		super(start, end);
		this.op = op;
		this.expr = expr;
	}

	public ASTUnary(int start, int end, OPERATOR op, IASTRE expr) {
		super(start, end);
		this.op = op;
		this.expr = expr;
	}


	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterASTUnary(this);
		expr.visit(visitor);
	}
}
