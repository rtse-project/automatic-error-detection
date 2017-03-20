package intermediateModelHelper.heuristic.definition;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;

/**
 * The heuristic searches for snippet of code in a guard section of the following type:
 * <pre>
 * {@code  var - x < y | var < y }
 * </pre>
 * Where var is a variable that has a type time related. Regarding the x and y, they can either be constant
 * values or be time related variables.
 * The search is not limited to the <i>&lt;</i> operator, it searches for all the order operators.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class TimeoutResources extends SearchTimeConstraint {

	@Override
	public void setup(ASTClass c) {

	}

	@Override
	public void next(ASTRE stm, Env env) {
		//works only on ASTRE
		IASTRE expr = stm.getExpression();
		if(expr == null){
			return;
		}
		CheckExpression.checkRE(stm,env);

		//search for A {<,<=,>,>=} C
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTbinary(ASTBinary elm) {
				switch (elm.getOp()){
					case less:
					case lessEqual:
					case greater:
					case greaterEqual:
					case equality:
						if(CheckExpression.checkIt(elm, env)){
							stm.setTimeCritical(true);
							TimeoutResources.super.addConstraint("timeoutResource", elm);
						}
				}
			}
		});

	}

	@Override
	public void nextMethod(ASTMethod method, Env env) {

	}

	@Override
	public void nextConstructor(ASTConstructor method, Env env) {

	}

	/*
	private boolean checkIt(ASTBinary expr, Env env){
		final boolean[] r = {false};
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTLiteral(ASTLiteral elm) {
				if(env.existVarName(elm.getValue()))
					r[0] = true;
			}

			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(env.existMethod(elm)){
					r[0] = true;
				}
			}

			@Override
			public void enterASTMultipleMethodCall(ASTMultipleMethodCall elm) {
				if(elm.getVariable() != null && elm.getVariable() instanceof ASTLiteral){
					if( env.existVarName(((ASTLiteral) elm.getVariable()).getValue()) ){
						r[0] = true;
					}
				}
			}
		});
		return r[0];
	}
	*/
}
