package intermediateModel.structure;

import intermediateModel.interfaces.*;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class ASTConstructor extends IASTStm implements IASTMethod, IASTHasStms, IASTVisitor {
	String name;
	List<ASTVariable> parameters;
	List<String> exceptionsThrowed;
	List<IASTStm> stms = new ArrayList<>();

	public ASTConstructor(Token start, Token end, String name, List<ASTVariable> parameters, List<String> exceptionsThrowed) {
		super(start,end);
		this.name = name;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
	}
	public ASTConstructor(int start, int end, String name, List<ASTVariable> parameters, List<String> exceptionsThrowed) {
		super(start,end);
		this.name = name;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
	}

	public List<String> getExceptionsThrowed() {
		return exceptionsThrowed;
	}

	public void setExceptionsThrowed(List<String> exceptionsThrowed) {
		this.exceptionsThrowed = exceptionsThrowed;
	}

	public void addThrows(String eType){
		exceptionsThrowed.add(eType);
	}

	public String getName() {
		return name;
	}


	public List<ASTVariable> getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTConstructor)) return false;
		ASTConstructor astMethod = (ASTConstructor) o;
		if (getName() != null ? !getName().equals(astMethod.getName()) : astMethod.getName() != null) return false;
		if (getParameters() != null ? !getParameters().equals(astMethod.getParameters()) : astMethod.getParameters() != null)
			return false;
		return true;
	}

	public String toString(){
		String out;
		out = "\t" + name + "(";
		for(ASTVariable v: parameters){
			out += v.toString() + ",";
		}
		if(parameters.size() > 0){
			out = out.substring(0,out.length()-1);
		}
		out += ")";
		if(exceptionsThrowed.size() > 0){
			out += " throws ";
			for(String v: exceptionsThrowed){
				out += v.toString() + ",";
			}
			out = out.substring(0,out.length()-1);
		}
		out += "\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		return out;
	}

	public void setStms(List<IASTStm> stms) {
		this.stms = stms;
	}
	public void addStms(IASTStm stm) {
		this.stms.add(stm);
	}

	public List<IASTStm> getStms() {
		return stms;
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTConstructor(this);
		for(ASTVariable p : parameters){
			p.visit(visitor);
		}
		for(IASTStm s : stms){
			s.visit(visitor);
		}
	}
}