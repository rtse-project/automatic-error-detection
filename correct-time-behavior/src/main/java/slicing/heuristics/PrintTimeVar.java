package slicing.heuristics;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.heuristic.definition.SearchTimeConstraint;
import slicing.TimeStatements;

/**
 * The {@link PrintTimeVar} searches for instances of time assignment
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class PrintTimeVar extends SearchTimeConstraint {

	TimeStatements listTimeStms;

	public PrintTimeVar() {
		this.listTimeStms = TimeStatements.getInstance();
	}

	@Override
	public void next(ASTRE stm, Env env) {
		//works only on ASTRE
		IASTRE expr = stm.getExpression();
		if(expr == null){
			return;
		}

		//record time vars
		stm.visit(new DefaultASTVisitor(){
			@Override
			public void enterASTLiteral(ASTLiteral elm) {
				String name = elm.getValue();
				if(env.existVarNameTimeRelevant(name)){
					//System.out.print(name + " ");
					print(stm);
				}
			}
		});


	}

	private void print(IASTStm stm) {
		this.listTimeStms.addStatements(stm);
		//System.out.println(" -- Time var Found @" + stm.getLine());
	}

}
