package IntermediateModelHelper.indexing;

import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexEnv;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import IntermediateModelHelper.indexing.structure.IndexSyncCall;
import IntermediateModelHelper.types.ResolveTypes;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTImport;
import intermediateModel.structure.ASTSynchronized;
import intermediateModel.structure.ASTVariable;
import intermediateModel.visitors.interfaces.ParseIM;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class created the {@link IndexSyncCall} for a given {@link ASTClass}.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingSyncBlock extends ParseIM {

	MongoConnector mongo;
	ASTClass _c = null;
	String lastMethodName = "";
	List<String> signatureLastMethodName = new ArrayList<>();
	List<IndexSyncBlock> output = new ArrayList<>();
	List<IndexData> imports = new ArrayList<>();

	public IndexingSyncBlock() {
		String dbname = MongoOptions.getInstance().getDbName();
		mongo = MongoConnector.getInstance(dbname);
	}

	public IndexingSyncBlock(MongoConnector mongo) {
		this.mongo = mongo;
	}

	/**
	 * Start the indexing the calls of synchronized method of a {@link ASTClass}.
	 * It force to delete the index structure from the DB and recreate it.
	 *
	 * @param c	Class to analyse.
	 * @return	The list of indexes of sync calls of the class.
	 */
	public List<IndexSyncBlock> index(ASTClass c){
		return index(c, false);
	}
	/**
	 * Start the indexing the calls of synchronized method of a {@link ASTClass}.
	 * It force to delete the index structure from the DB and to recreate it if the <i>forceReindex</i> flag is set to true.
	 * @param c	Class to analyze
     * @param forceReindex flag to force the recreation of the index
	 * @return	The list of indexes of sync calls of the class.
	 */
	public List<IndexSyncBlock> index(ASTClass c, boolean forceReindex) {
		this._c = c;
		if(mongo.existSyncCallIndex(c)){
			if(forceReindex){
				mongo.deleteSyncBlock(c);
			} else {
				List<IndexSyncBlock> out = mongo.getSyncBlockIndex(c);
				return out;
			}
		}
		//collect sync blocks
		processImports(this._c);
		createBaseEnv(c);
		for(IndexSyncBlock s : output){
			mongo.add(s);
		}
		return output;
	}

	/**
	 * It connects to the mongodb and prepare the data in order to resolve the from which
	 * objects the method calls are coming from.
	 */
	private void processImports(ASTClass c) {
		for(ASTImport imp : c.getImports()){
			String pkg = imp.getPackagename();
			List<IndexData> d = mongo.getFromImport(pkg);
			if(d.size() > 0) imports.addAll(d);
		}
		//add myself as well
		String pkg = c.getPackageName() + "." + c.getName();
		List<IndexData> d = mongo.getFromImport(pkg);
		if(d.size() > 0) imports.addAll(d);
		//and my subs
		d = mongo.getFromImport(pkg + ".*");
		if(d.size() > 0) imports.addAll(d);
		if(c.getParent() != null){
			processImports(c.getParent());
		}
	}

	/**
	 * The following method creates the basic environment for a class.
	 * @param c Class to analyze
	 */
	@Override
	protected Env createBaseEnv(ASTClass c){
		super.createBaseEnv(c);
		//check method
		for (IASTMethod m : c.getMethods()) {
			lastMethodName = m.getName();
			signatureLastMethodName = new ArrayList<>();
			for(ASTVariable p : m.getParameters()){
				signatureLastMethodName.add(p.getType());
			}
			Env eMethod = new Env(base_env);
			eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
			super.analyze(m.getStms(), eMethod);
		}
		return base_env;
	}

	@Override
	protected void analyzeASTSynchronized(ASTSynchronized elm, Env env) {
		Pair<String,String> exprType = ResolveTypes.getSynchronizedExprType(imports, elm.getExpr().getExpression(), this._c, env);
		//System.out.println(exprType.getValue0() + " : " + exprType.getValue1());
		IndexSyncBlock sync = new IndexSyncBlock();
		sync.setPackageName(this._c.getPackageName());
		sync.setName(this._c.getName());
		sync.setMethodName(lastMethodName);
		sync.setExpr(elm.getExpr().getCode());
		sync.setStart(elm.getStart());
		sync.setEnd(elm.getEnd());
		sync.setLine(elm.getLine());
		IndexEnv e_index = new IndexEnv(env);
		sync.setSyncVar( e_index.getVar(sync.getExpr()) );
		sync.setSignature(signatureLastMethodName);
		sync.setExprPkg(exprType.getValue0());
		sync.setExprType(exprType.getValue1());
		output.add(sync);
	}
}