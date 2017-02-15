package IntermediateModelHelper.indexing.structure;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModel.interfaces.IASTVar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexEnv {
	List<IndexParameter> vars = new ArrayList<>();
	public IndexEnv() {
	}

	public IndexEnv(List<IndexParameter> vars) {
		this.vars = vars;
	}

	public IndexEnv(Env e){
		convertEnv(e);
	}

	private void convertEnv(Env e) {
		for(IASTVar v : e.getVarList()){
			vars.add(new IndexParameter(
					v.getType(), v.getName()
			));
		}
		if(e.getPrev() != null){
			convertEnv(e.getPrev());
		}
	}

	public List<IndexParameter> getVars() {
		return vars;
	}

	public void setVars(List<IndexParameter> vars) {
		this.vars = vars;
	}

	public IndexParameter getVar(String name){
		for(IndexParameter p : vars){
			if(p.getName().equals(name)){
				return p;
			}
		}
		return null;
	}
}
