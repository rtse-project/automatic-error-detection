package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTVariableMultipleDeclaration extends IASTStm implements IASTRE {

	String type;
	List<IASTRE> vars = new ArrayList<>();

	public ASTVariableMultipleDeclaration(int start, int end, String type, List<IASTRE> vars) {
		super(start, end);
		this.type = type;
		this.vars = vars;
	}

	public void addVar(ASTVariableDeclaration v){
		vars.add(v);
	}

	public String getType() {
		return type;
	}

	public List<IASTRE> getVars() {
		return vars;
	}

	public void setVars(List<IASTRE> vars) {
		this.vars = vars;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		//Default RE
		visitor.enterAll(this);
		visitor.enterASTVariableMultipleDeclaration(this);
		for(IASTRE v : vars){
			v.visit(visitor);
		}
		visitor.exitASTVariableMultipleDeclaration(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visit((ASTREVisitor) visitor);
	}

}
