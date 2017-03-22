package PCFG.creation.helper;

import PCFG.structure.CFG;
import PCFG.structure.IHasCFG;
import PCFG.structure.PCFG;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.AnonymEdge;
import PCFG.structure.edge.Edge;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.interfaces.ConvertIM;
import org.javatuples.KeyValue;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CreatePCFG extends ConvertIM {

	private List<KeyValue<IASTMethod,ASTClass>> classes = new ArrayList<>();

	private CFG  lastCfg;
	private Node lastNode;
	private SyncNode syncNode;
	private String lastLabel = "";
	private boolean isMultiLabel = false;
	private List<Node> multiLabel = new ArrayList<>();

	private IHasCFG lastPCFG = null;
	private String lastClass = "";
	List<ASTHiddenClass> visitedHidden = new ArrayList<>();
	private List<Triplet<String, IASTStm, Class>> constraints = new ArrayList<>();

	private PCFG pcfg;


	public void addMethod(KeyValue<IASTMethod,ASTClass> k){
		classes.add(k);
	}
	public void addMethod(IASTMethod m, ASTClass c){
		classes.add(new KeyValue<>(m,c));
	}

	public List<KeyValue<IASTMethod, ASTClass>> getClasses() {
		return classes;
	}

	public PCFG getPCFG() {
		return pcfg;
	}

	public List<Triplet<String, IASTStm, Class>> getConstraints() {
		return constraints;
	}

	public PCFG convert(){
		pcfg = new PCFG();
		lastPCFG = pcfg;

		//add all the states to the PCFG
		for(KeyValue<IASTMethod,ASTClass> c : classes){
			//reset structure
			visitedHidden.clear();
			//Node._ID = 0;

			//consider also hidden methods
			c.getValue().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTMethod(ASTMethod m) {
					if(m.equalsBySignature( c.getKey())) {
						addSingleClassStates(c.getValue(), m);
					}
				}

				@Override
				public void enterASTConstructor(ASTConstructor m) {
					if(m.equalsBySignature(c.getKey())) {
						addSingleClassStates(c.getValue(), m);
					}
				}
			});
		}
		return pcfg;
	}

	private void addSingleClassStates(ASTClass c, IASTMethod m){
		lastNode = null;
		lastCfg = new CFG(c.getPackageName() + "." +  c.getName() + "::" + m.getName(), c.hashCode());
		lastPCFG.addCFG(lastCfg);
		lastClass = c.getName();
		dispatchMethod(m, c);
	}

	private void addSingleClassStates(ASTHiddenClass c, IASTMethod m){

		//init
		lastNode = null;
		lastCfg = new CFG("anonymous" + "::" + m.getName(), lastCfg.getHashcode());
		lastCfg.setLine(m.getLine());
		lastPCFG.addCFG(lastCfg);
		lastClass = "anonymous";
		dispachStm(m.getStms());

	}


	protected void convertForeach(ASTForEach stm) {

		Node has_next = new Node("hasNext", "", Node.TYPE.FOREACH, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(has_next);
		ASTVariable var = stm.getVar();
		addState(new Node(
				var.getName() + "_takes_" + stm.getExpr().getExpressionName(),
				var.getCode() + " : " + stm.getExpr().getCode(),
				Node.TYPE.NORMAL,
				var.getStart(), var.getEnd(), var.getLine()
		));
		this.lastLabel = "True";
		super.dispachStm(stm.getStms());

		//back to the expr
		Edge eBack = new Edge(this.lastNode, has_next);
		this.lastCfg.addEdge(eBack);

		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(has_next, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
		this.lastNode = end_for;
	}

	protected void convertFor(ASTFor stm) {
		Node init_for = new Node("_init_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_for);
		if(stm.getInit().size() > 0){
			stm.getInit().forEach(this::convertRE);
		}
		Node expr_for = new Node("_expr_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(expr_for);
		if(stm.getExpr() != null){
			convertRE(stm.getExpr());
		}
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		this.lastLabel = "";
		Node post_for = new Node("_post_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(post_for);
		if(stm.getPost().size() > 0){
			stm.getPost().forEach(this::convertRE);
		}
		//to expr again
		Edge eBack = new Edge(this.lastNode, expr_for);
		this.lastCfg.addEdge(eBack);
		//close the for
		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(expr_for, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
		this.lastNode = end_for;
	}

	protected void convertDoWhile(ASTDoWhile stm) {
		Node init_do_while = new Node("_init_do_while", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_do_while);
		dispachStm(stm.getStms());
		ASTRE ex = stm.getExpr();
		Node expr = new Node(ex.getExpressionName(), ex.getCode(), Node.TYPE.NORMAL, ex.getStart(), ex.getEnd(), ex.getLine() );
		addState( expr );
		Edge e = new Edge( expr, init_do_while );
		e.setLabel("True");
		this.lastCfg.addEdge(e);
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	protected void convertWhile(ASTWhile stm) {
		Node expr = new Node(stm.getExpr().getExpressionName(), stm.getExpr().getCode(), Node.TYPE.NORMAL, stm.getStart(), stm.getEnd(), stm.getLine());
		addState( expr );
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		//back to the expr
		Edge e = new Edge( this.lastNode , expr );
		this.lastCfg.addEdge(e);
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	protected void convertSynchronized(ASTSynchronized stm) {
		syncNode = new SyncNode(stm.getExpr().getCode(), stm.getStart(), stm.getEnd(), stm.getLine(), this.lastClass );
		this.lastCfg.addNode(syncNode);
		dispachStm(stm.getStms());
		syncNode = null;
	}

	protected void convertIf(ASTIf stm) {
		ASTRE ex = stm.getGuard();
		Node expr = new Node(ex.getExpressionName(), ex.getCode(), Node.TYPE.IF_EXPR, ex.getStart(), ex.getEnd(), ex.getLine() );
		Node end_if = new Node("_end_if_", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState( expr );
		this.lastLabel = "True";
		super.dispachStm(stm.getIfBranch().getStms());
		//jump to the end
		Edge eEnd = new Edge(this.lastNode, end_if);
		this.lastCfg.addEdge(eEnd);

		this.lastLabel = "False";
		this.lastNode = expr;
		if(stm.getElseBranch() != null){
			dispachStm(stm.getElseBranch().getStms());
		}
		addState(end_if);
	}

	protected void convertTry(ASTTry stm) {

		Node init_try = new Node("try", "", Node.TYPE.TRY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_try);
		if(this.syncNode != null){
			this.syncNode.add(finally_try);
		}
		this.lastCfg.addNode(finally_try);


		//this.lastCfg.addNode(end_try);



		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
			Edge e = new Edge(init_try, catch_try);
			if(this.syncNode != null){
				this.syncNode.add(catch_try);
			}
			this.lastCfg.addNode(catch_try);

			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
				);
				Edge eInstance = new Edge(catch_try, instance);
				if(this.syncNode != null){
					this.syncNode.add(instance);
				}
				this.lastCfg.addNode(instance);
				this.lastCfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.lastCfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(instance, finally_try);
				toEnd.setLabel("False");
				this.lastCfg.addEdge(toEnd);
			}
		}

		if(stm.getFinallyBranch() != null){
			this.lastNode = finally_try;
			super.dispachStm(stm.getFinallyBranch().getStms());
			Edge toEnd = new Edge(this.lastNode, end_try);
			this.lastCfg.addEdge(toEnd);
		} else {
			Edge finalEdge = new Edge(finally_try, end_try);
			this.lastCfg.addEdge(finalEdge);
		}

		this.lastNode = end_try;
		this.lastCfg.addNode(end_try);
		if(this.syncNode != null){
			this.syncNode.add(end_try);
		}
	}

	protected void convertTryResource(ASTTryResources stm) {
		Node init_try = new Node("try", "", Node.TYPE.TRY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_try);
		this.lastCfg.addNode(finally_try);

		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
			Edge e = new Edge(init_try, catch_try);
			this.lastCfg.addNode(catch_try);
			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
				);
				Edge eInstance = new Edge(catch_try, instance);
				this.lastCfg.addNode(instance);
				this.lastCfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.lastCfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(instance, finally_try);
				toEnd.setLabel("False");
				this.lastCfg.addEdge(toEnd);
			}
		}

		this.lastNode = finally_try;
		if(stm.getFinallyBranch() != null){
			super.dispachStm(stm.getFinallyBranch().getStms());
		}
		//convert the resources
		for(ASTRE r : stm.getResources()){
			Node resource = new Node(
					"close_" + r.getExpressionName(),
					r.getCode(),
					Node.TYPE.NORMAL,
					r.getStart(), r.getEnd(), r.getLine()
			);
			addState(resource);
		}

		Edge toEnd = new Edge(this.lastNode, end_try);
		this.lastCfg.addEdge(toEnd);

		this.lastNode = end_try;
		this.lastCfg.addNode(end_try);
	}

	protected void convertASTSwitch(ASTSwitch stm) {

		Node init_switch = new Node("switch", "", Node.TYPE.SWITCH, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_switch = new Node("endSwitch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());

		this.lastCfg.addNode(end_switch);
		addState(init_switch);

		Node check = new Node(
				"check_" + stm.getExpr().getExpressionName(),
				stm.getExpr().getCode(),
				Node.TYPE.NORMAL,
				stm.getExpr().getStart(), stm.getExpr().getEnd(), stm.getExpr().getLine()
		);
		addState(check);

		for(ASTSwitch.ASTCase c : stm.getCases()){
			for(String label : c.getLabels()){
				Node labelSwitch = new Node(
						"equals_" + label,
						label,
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
				);
				Edge e = new Edge(check, labelSwitch);
				this.lastCfg.addNode(labelSwitch);
				this.lastCfg.addEdge(e);
				this.multiLabel.add(labelSwitch);
			}
			this.isMultiLabel = true;
			super.dispachStm(c.getStms());
			Edge toEnd = new Edge(this.lastNode, end_switch);
			this.lastCfg.addEdge(toEnd);
		}
		this.lastLabel = "";
		this.lastNode = end_switch;
	}


	protected void convertReturn(ASTReturn stm) {
		int start, end, line;
		start = stm.getExpr() != null ? stm.getExpr().getStart() : stm.getStart();
		end   = stm.getExpr() != null ? stm.getExpr().getEnd()   : stm.getEnd();
		line  = stm.getExpr() != null ? stm.getExpr().getLine()  : stm.getLine();
		addState(
				new Node(
						"return" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.RETURN,
						start, end, line
				)
		);
		ASTRE r = stm.getExpr();
		if(r != null && r.getExpression() != null) {
			r.getExpression().visit(new DefaultASTVisitor() {
				@Override
				public void enterASTNewObject(ASTNewObject elm) {
					convertASTNewObject(elm);
				}
			});
		}

	}

	protected void convertThrow(ASTThrow stm) {
		int start, end, line;
		start = stm.getExpr() != null ? stm.getExpr().getStart() : stm.getStart();
		end   = stm.getExpr() != null ? stm.getExpr().getEnd()   : stm.getEnd();
		line  = stm.getExpr() != null ? stm.getExpr().getLine()  : stm.getLine();
		addState(
				new Node(
						"throw" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.THROW,
						start, end, line
				)
		);
	}

	protected void convertContinue(ASTContinue stm) {

		addState(
				new Node("continue", stm.getCode(), Node.TYPE.CONTINUE, stm.getStart(), stm.getEnd(), stm.getLine())
		);
	}

	protected void convertBreak(ASTBreak stm) {
		addState(
				new Node("break", stm.getCode(), Node.TYPE.BREAK, stm.getStart(), stm.getEnd(), stm.getLine())
		);
	}

	protected void convertRE(ASTRE r) {
		Triplet<String, IASTStm, Class> c = getConstraint(r);
		if(c != null){
			this.lastLabel = this.lastLabel + "[" + c.getValue0() + "]";
		}
		addState(
				new Node(r.getExpressionName(), r.getCode(), Node.TYPE.NORMAL, r.getStart(), r.getEnd(), r.getLine())
		);
		if(r != null && r.getExpression() != null) {
			r.getExpression().visit(new DefaultASTVisitor() {
				@Override
				public void enterASTNewObject(ASTNewObject elm) {
					convertASTNewObject(elm);
				}
			});
		}
	}


	protected void convertASTNewObject(ASTNewObject stm) {
		ASTHiddenClass hc = stm.getHiddenClass();
		if(hc != null && !visitedHidden.contains(hc)){
			visitedHidden.add(hc);
			convertASTHiddenClass(hc);
		}
	}

	protected void convertASTHiddenClass(ASTHiddenClass stm) {
		Node bckNode = lastNode;
		CFG bck = lastCfg;
		String bckLastClass = lastClass;
		IHasCFG bckLastPCFG = lastPCFG;
		//new anonym
		AnonymClass anon = new AnonymClass();
		lastCfg.addNode(anon);
		AnonymEdge anonEdge = new AnonymEdge(lastNode,anon);
		lastCfg.addEdge(anonEdge);
		lastPCFG = anon;
		for(IASTMethod m : stm.getMethods()){
			addSingleClassStates(stm, m);
		}
		lastCfg = bck;
		lastNode = bckNode;
		lastClass = bckLastClass;
		lastPCFG = bckLastPCFG;
	}

	private void addState(Node node) {
		this.lastCfg.addNode(node);
		if(this.lastNode != null && !this.isMultiLabel){
			Edge e = new Edge(this.lastNode, node);
			e.setLabel(lastLabel);
			this.lastCfg.addEdge(e);
		}
		if(this.isMultiLabel){
			for(Node n : this.multiLabel){
				Edge e = new Edge(n, node);
				e.setLabel("True");
				this.lastCfg.addEdge(e);
			}
			this.isMultiLabel = false;
			this.multiLabel.clear();
		}
		if(this.syncNode != null){
			this.syncNode.add(node);
		}
		this.lastNode = node;
		this.lastLabel = "";
	}

	private Triplet<String, IASTStm, Class> getConstraint(ASTRE r){
		for(Triplet<String, IASTStm, Class> c : this.constraints){
			if(c.getValue1().equals(r)){
				return c;
			}
		}
		return null;
	}
}