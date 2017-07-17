package smt;

import com.microsoft.z3.*;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTToken;
import intermediateModel.structure.expression.*;
import slicing.model.*;
import slicing.model.interfaces.Stm;
import smt.exception.ModelNotCorrect;
import smt.exception.VarNotFoundException;
import smt.exception.VariableNotCorrect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 13/07/2017.
 */
public class TranslateReducedModel {

    private StackTraceElement[] cause = Thread.currentThread().getStackTrace();
    private ModelCreator modelCreator;
    private Context ctx;
    private List<String> pushModel;
    private List<VariableNotCorrect> errors;

    int _fresh = 0;

    public TranslateReducedModel() {
    }

    public List<String> getPushModel() {
        return pushModel;
    }

    public Optimize convert(Method m){
        modelCreator = new ModelCreator();
        pushModel = new ArrayList<>();
        errors = new ArrayList<>();
        ctx = modelCreator.getCtx();
        for(Stm s : m.getBody()) {
            convert(s);
        }
        return modelCreator.getOpt();
    }
    public List<VariableNotCorrect> check(Method m){
        convert(m);
        return errors;
    }

    private void convert(Stm s) {
        if(s instanceof Assignment){
            handleAssignment((Assignment) s);
        } else if(s instanceof Expression){
            handleExpression((Expression) s);
        } else if(s instanceof If){
            handleIf((If) s);
        } else if(s instanceof MethodCall){
            handleMethodCall((MethodCall) s);
        } else if(s instanceof While){
            handleWhile((While) s);
        } else {
            notYet(s);
        }
    }

    private Expr convert(IASTRE r){
        if(r instanceof ASTArrayInitializer){
            return handleArrayInitializer((ASTArrayInitializer)r);
        } else if(r instanceof ASTAssignment){
            return handleAssignmentExpression((ASTAssignment)r);
        } else if(r instanceof ASTAttributeAccess){
            return handleAttributeAccess((ASTAttributeAccess)r);
        } else if(r instanceof ASTBinary){
            return handleBinary((ASTBinary) r);
        } else if(r instanceof ASTCast){
            return handleCast((ASTCast) r);
        } else if(r instanceof ASTConditional){
            return handleConditional((ASTConditional) r);
        } else if(r instanceof ASTLiteral){
            return handleLiteral((ASTLiteral) r);
        } else if(r instanceof ASTMethodCall){
            return handleMethodCallExpression((ASTMethodCall) r);
        } else if(r instanceof ASTNewObject){
            return handleNewObject( (ASTNewObject) r);
        } else if(r instanceof ASTPostOp){
            return handlePostOp( (ASTPostOp) r);
        } else if(r instanceof ASTPreOp){
            return handlePreOp( (ASTPreOp) r);
        } else if(r instanceof ASTUnary){
            return handleUnary( (ASTUnary) r);
        } else if(r instanceof ASTVariableDeclaration){
            return handleVariableDec( (ASTVariableDeclaration) r);
        } else if(r instanceof ASTVariableMultipleDeclaration){
            return handleMultipleVarDec( (ASTVariableMultipleDeclaration) r);
        } else {
            notYet(r);
        }
        return ctx.mkInt("-100");
    }

    /***************************
     *       Expression        *
     ***************************/
    private Expr handleMultipleVarDec(ASTVariableMultipleDeclaration r) {
        for(IASTRE rr : r.getVars()) {
            convert(rr);
        }
        return ctx.mkBool(true);
    }

    private Expr handleVariableDec(ASTVariableDeclaration r) {
        String varName = r.getNameString();
        IntExpr var = modelCreator.createVariable(varName);
        if(r.getExpr() != null) {
            BoolExpr b = ctx.mkEq(var, convert(r.getExpr()));
            modelCreator.addConstraint(b);
            return b;
        }
        return var;
    }

    private BoolExpr handleUnary(ASTUnary r) {
        BoolExpr e = null;
        switch (r.getOp()) {
            case not:
                e = ctx.mkNot((BoolExpr) convert(r.getExpr()));
                modelCreator.addConstraint(e);
                break;
        }
        return e;
    }

    private BoolExpr handlePreOp(ASTPreOp r) {
        IntExpr var = modelCreator.createVariable(r.getVar().print());
        Expr tmp;
        if(r.getType() == IASTRE.ADDDEC.increment) {
            tmp = ctx.mkAdd(var, ctx.mkInt(1));
        } else {
            tmp = ctx.mkSub(var, ctx.mkInt(1));
        }
        BoolExpr b = ctx.mkEq(var, tmp);
        modelCreator.addConstraint(b);
        return b;
    }

    private BoolExpr handlePostOp(ASTPostOp r) {
        IntExpr var = modelCreator.createVariable(r.getVar().print());
        Expr tmp;
        if(r.getType() == IASTRE.ADDDEC.increment) {
            tmp = ctx.mkAdd(var, ctx.mkInt(1));
        } else {
            tmp = ctx.mkSub(var, ctx.mkInt(1));
        }
        BoolExpr b = ctx.mkEq(var, tmp);
        modelCreator.addConstraint(b);
        return b;
    }

    private BoolExpr handleNewObject(ASTNewObject r) {
        return ctx.mkBool(true);
    }

    private IntExpr handleMethodCallExpression(ASTMethodCall r) {
        if(r.isMaxMin()) {
            return convertMaxMin(r);
        } else if(r.isTimeCritical()){
            return modelCreator.getTimeCall();
        }
        return modelCreator.createFunction(r.getMethodName());
    }

    private IntExpr convertMaxMin(ASTMethodCall r) {
        ArithExpr t0 = (ArithExpr) convert(r.getParameters().get(0));
        ArithExpr t1 = (ArithExpr) convert(r.getParameters().get(1));
        if(r.getMethodName().equals("min")){
            return (IntExpr) ModelCreator.min2(ctx, t0, t1);
        }
        return (IntExpr) ModelCreator.max2(ctx, t0, t1);
    }

    private IntExpr handleLiteral(ASTLiteral r) {
        IntExpr e = modelCreator.createVariable(r.getValue());
        return e;
    }

    private Expr handleConditional(ASTConditional r) {
        return ctx.mkBool(true);
    }

    private Expr handleCast(ASTCast r) {
        return ctx.mkBool(true);
    }

    private Expr handleBinary(ASTBinary r) {
        Expr e = null;
        switch (r.getOp()){
            case plus:
                e = ctx.mkAdd(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case minus:
                e = ctx.mkSub(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case less:
                e = ctx.mkLt(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case lessEqual:
                e = ctx.mkLe(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case greater:
                e = ctx.mkGt(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case greaterEqual:
                e = ctx.mkGe(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case mul:
                e = ctx.mkMul(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
            case div:
                e = ctx.mkDiv(
                        (ArithExpr)convert(r.getLeft()),
                        (ArithExpr) convert(r.getRight())
                );
                break;
        }
        return e;
    }

    private Expr handleAttributeAccess(ASTAttributeAccess r) {
        return modelCreator.createVariable(r.getVariableName().print());
    }

    private Expr handleAssignmentExpression(ASTAssignment r) {
        return modelCreator.createVariable(r.getLeft().print());
    }

    private Expr handleArrayInitializer(ASTArrayInitializer r) {
        return ctx.mkBool(true);
    }


    /***************************
     *          MODEL          *
     ***************************/



    private void handleWhile(While s) {
        for(String v : s.getTimeVars()){
            try {
                modelCreator.verifyVariable(v);
            } catch (ModelNotCorrect e) {
                errors.add( new VariableNotCorrect(v, s)  );
                //System.err.println("@" + s.getLine() + " " + e.getMessage());
            } catch (VarNotFoundException e) {
                //this is not a problem
            }
        }
        push();
        if(s.getExpr() != null){
            convert(s.getExpr());
        }
        for(Stm ss : s.getWhileBody()){
            convert(ss);
        }
        pop();
        Expr b = convert(s.getExpr().getExpr().negate());
        modelCreator.addConstraint((BoolExpr) b);
    }

    private void handleIf(If s) {
        push();
        if(s.getIfBody().size() > 0) {
            if(s.getExpr() != null)
                convert(s.getExpr());
            for(Stm ss : s.getIfBody()){
                convert(ss);
            }
        }
        pop();
        if(s.getElseBody().size() > 0){
            push();
            if(s.getExpr() != null)
                convert(s.getExpr().getExpr().negate());
            for(Stm ss : s.getIfBody()){
                convert(ss);
            }
            pop();
        }

    }

    private void handleExpression(Expression s) {
        BoolExpr e = (BoolExpr) convert(s.getExpr());
        modelCreator.addConstraint(e);
    }

    private void handleMethodCall(MethodCall s) {
        for(String v : s.getVariables()){
            try {
                modelCreator.verifyVariable(v);
            } catch (ModelNotCorrect e) {
                errors.add( new VariableNotCorrect(v, s)  );
                //System.err.println("@" + s.getLine() + " " + e.getMessage());
            } catch (VarNotFoundException e) {
                //this is not a problem
            }
        }
        convert(s.getMethodCall());
    }

    private void handleAssignment(Assignment s) {
        IntExpr v = modelCreator.createVariable(s.getLeft());
        Expr e = convert(s.getRight());
        BoolExpr b = ctx.mkEq(v, e);
        modelCreator.addConstraint(b);
    }

    private void push(){
        modelCreator.getOpt().Push();
    }

    private void pop(){
        pushModel.add(modelCreator.getOpt().toString());
        modelCreator.getOpt().Pop();
    }

    private void notYet(IASTToken s){
        System.err.println("Not Yet Implemented :: [" + s.getStart() +"]" + s.getCode() + " @ " + s.getLine() + "--" + cause[2]);
    }

}