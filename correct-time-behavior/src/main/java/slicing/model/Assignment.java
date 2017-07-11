package slicing.model;

import intermediateModel.interfaces.IASTRE;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

/**
 * Created by giovanni on 11/07/2017.
 */
public class Assignment extends Stm {

    String left = "";
    IASTRE right = null;

    public Assignment(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(IASTRE right) {
        this.right = right;
    }

    public String getLeft() {
        return left;
    }

    public IASTRE getRight() {
        return right;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterAssignmet(this);
        if(right != null)
            right.visit(visitor);
        visitor.exitAssignment(this);
    }
}
