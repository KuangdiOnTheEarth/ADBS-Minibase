package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.List;

public class JoinCondition {
    private String op;
    private boolean reverseOrder = false;
    // assume the ComparisonAtom represents a predicate: term1 op term2
    // if (term1 in leftTuple) && (term2 in rightTuple), the tuple order matches operand order, and reverseOrder=false
    // if (term1 in rightTuple) && (term2 in leftTuple), the tuple order reverses operand order, and reverseOrder=true

    private int operand1Idx; // the index of operand1 in corresponding tuple (either left or right tuple depends on reverseOrder)
    private int operand2Idx; // the index of operand2 in corresponding tuple

    public JoinCondition(ComparisonAtom compAtom, List<String> leftVariableMask, List<String> rightVariableMask) {
        this.op = compAtom.getOp().toString();
        if ( leftVariableMask.contains(((Variable) compAtom.getTerm1()).getName()) ) {
            this.operand1Idx = leftVariableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.operand2Idx = rightVariableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
        } else {
            this.reverseOrder = true;
            this.operand1Idx = rightVariableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.operand2Idx = leftVariableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
        }
    }

    public boolean check(Tuple leftTuple, Tuple rightTuple) {
        Term operand1;
        Term operand2;
        if (!reverseOrder) {
            operand1 = leftTuple.getTerms().get(this.operand1Idx);
            operand2 = rightTuple.getTerms().get(this.operand2Idx);
        } else {
            operand1 = rightTuple.getTerms().get(this.operand1Idx);
            operand2 = leftTuple.getTerms().get(this.operand2Idx);
        }

        if (this.op.equals("=")) {
            return operand1.equals(operand2);
        } else if (this.op.equals("!=")) {
            return (!operand1.equals(operand2));
        } else if (this.op.equals(">")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() > ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) > 0;
        } else if (this.op.equals(">=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() >= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) >= 0;
        } else if (this.op.equals("<")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() < ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) < 0;
        } else if (this.op.equals("<=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() <= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) <= 0;
        } else {
            System.out.println("!!!! None of the if-branches is evoked in the Selection Operator !!!!");
            return false;
        }
    }
}
