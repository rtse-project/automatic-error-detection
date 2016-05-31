package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTContinue extends IASTStm {

	public ASTContinue(Token start, Token end) {
		super(start.getStartIndex(), end.getStopIndex());
	}
	public ASTContinue(int start, int end) {
		super(start, end);
	}
	@Override
	public String toString() {
		return "---> CONTINUE <--- ";
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
