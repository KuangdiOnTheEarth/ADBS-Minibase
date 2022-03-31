package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.List;

/**
 * This class is used in {@link JoinOperator}.
 * Similar as {@link SelectCondition}, this class converts a {@link ComparisonAtom} into condition instance
 * and provides methods for checking whether two input tuples satisfy a join condition.
 *
 * The two operands of input {@link ComparisonAtom} to this class are permitted to be both {@link Variable} instances.
 * (If a comparison atom only contain one variable,
 * it must be a select condition that should be processed in {@link SelectOperator} instead of {@link JoinOperator})
 */
public class JoinCondition {
    private String op;
    private boolean reverseOrder = false;
    // assume the ComparisonAtom represents a predicate: term1 op term2
    // if (term1 in leftTuple) && (term2 in rightTuple), the tuple order matches operand order, and reverseOrder=false
    // if (term1 in rightTuple) && (term2 in leftTuple), the tuple order reverses operand order, and reverseOrder=true

    private int operand1Idx; // the index of operand1 in corresponding tuple (either left or right tuple depends on reverseOrder)
    private int operand2Idx; // the index of operand2 in corresponding tuple

    /**
     * Extract the indices of two variable operands from the corresponding tuple.
     * Notice that the order between operands and between the tuples they appeared may be opposite,
     * in which case a {@code this.reverseOrder} flag will be set
     * to guid the {@link #check(Tuple, Tuple)} to process the input tuples in correct order.
     * @param compAtom
     * @param leftVariableMask
     * @param rightVariableMask
     */
    public JoinCondition(ComparisonAtom compAtom, List<String> leftVariableMask, List<String> rightVariableMask) {
        this.op = compAtom.getOp().toString();
        if ( leftVariableMask.contains(((Variable) compAtom.getTerm1()).getName()) ) {
            // if the left relation contains the first operand, the order are the same
            this.operand1Idx = leftVariableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.operand2Idx = rightVariableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
        } else {
            // otherwise, the orders are reversed
            this.reverseOrder = true;
            this.operand1Idx = rightVariableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
            this.operand2Idx = leftVariableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
        }
    }

    /**
     * Check whether two input tuples satisfy the join condition.
     * First the operand will be extracted from the input tuples by their indices,
     * depending on the state of {@code this.reverseOrder} flag, the order of these two operand may be reversed.
     * Then the two operand will be checked on the join condition.
     * @param leftTuple a tuple from the left child operator of {@link JoinOperator}
     * @param rightTuple a tuple from the right child operator of {@link JoinOperator}
     * @return {@code true} if join condition is satisfied on these two tuples; {@code false} otherwise
     */
    public boolean check(Tuple leftTuple, Tuple rightTuple) {
        Term operand1;
        Term operand2;
        // extract operand from input tuples, order of operands depends on the reverseOrder flag
        if (!reverseOrder) {
            operand1 = leftTuple.getTerms().get(this.operand1Idx);
            operand2 = rightTuple.getTerms().get(this.operand2Idx);
        } else {
            operand1 = rightTuple.getTerms().get(this.operand1Idx);
            operand2 = leftTuple.getTerms().get(this.operand2Idx);
        }

        // check the join condition on extracted operands
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
