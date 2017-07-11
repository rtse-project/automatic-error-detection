package slicing.model.visitor;

import intermediateModel.visitors.DefualtASTREVisitor;
import slicing.model.Assignment;
import slicing.model.If;
import slicing.model.MethodCall;
import slicing.model.While;

/**
 * Created by giovanni on 11/07/2017.
 */
public class DefaultReducedVisitor extends DefualtASTREVisitor implements ReducedVisitor {

    @Override
    public void enterAssignmet(Assignment elm) {

    }

    @Override
    public void enterIf(If elm) {

    }

    @Override
    public void enterMethodCall(MethodCall elm) {

    }

    @Override
    public void enterWhile(While elm) {

    }

    @Override
    public void exitAssignment(Assignment elm) {

    }

    @Override
    public void exitIf(If elm) {

    }

    @Override
    public void exitMethodCall(MethodCall elm) {

    }

    @Override
    public void exitWhile(While elm) {

    }
}