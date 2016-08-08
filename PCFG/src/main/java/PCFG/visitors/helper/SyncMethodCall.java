package PCFG.visitors.helper;

import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.ASTVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SyncMethodCall {
	private String _packageName;
	private String _className;
	private String _methodName;
	private List<String> paramsType = new ArrayList<>();
	private ASTRE node;

	public SyncMethodCall(String _packageName, String _className, String _methodName, ASTRE node, List<String> paramsType) {
		this._packageName = _packageName;
		this._className = _className;
		this._methodName = _methodName;
		this.node = node;
		this.paramsType = paramsType;
	}

	public List<String> getParamsType() {
		return paramsType;
	}

	public String get_packageName() {
		return _packageName;
	}

	public String get_className() {
		return _className;
	}

	public String get_methodName() {
		return _methodName;
	}

	@Override
	public String toString() {
		return _packageName + "." + _className + "#" + _methodName;
	}

	public ASTRE getNode() {
		return node;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SyncMethodCall)) return false;

		SyncMethodCall that = (SyncMethodCall) o;

		if (get_packageName() != null ? !get_packageName().equals(that.get_packageName()) : that.get_packageName() != null)
			return false;
		if (get_className() != null ? !get_className().equals(that.get_className()) : that.get_className() != null)
			return false;
		return get_methodName() != null ? get_methodName().equals(that.get_methodName()) : that.get_methodName() == null;
	}

	public boolean equalsBySignature(SyncMethodCall o){
		if(!this._methodName.equals(o.get_methodName())) return false; //not same method name
		if(this.paramsType.size() != o.getParamsType().size()) return false; //not same number of parameters
		for(int i = 0, max = this.paramsType.size(); i < max; i++){ //not same type for each par
			String t1 = this.paramsType.get(i);
			String t2 = o.getParamsType().get(i);
			if(!t1.equals(t2))	return false;
		}
		return true;
	}
}
