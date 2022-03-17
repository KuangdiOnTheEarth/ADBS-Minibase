package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JoinOperator extends Operator {

    private Operator leftChild;
    private Operator rightChild;

    private HashMap<Integer, Integer> joinConditionIndices = new HashMap<>();
    private List<Integer> rightChildJoinColumns = new ArrayList<>();
    // the columns in right child to be removed (since it duplicates with columns in left child)

    public JoinOperator(Operator leftChild, Operator rightChild) {
        this.leftChild = leftChild;
        List<String> leftVariableMask = leftChild.getVariableMask();
        this.rightChild = rightChild;
        List<String> rightVariableMask = rightChild.getVariableMask();

        for (String leftVar : leftVariableMask) {
            this.variableMask.add(leftVar);
            if (rightVariableMask.contains(leftVar)) {
                this.joinConditionIndices.put(leftVariableMask.indexOf(leftVar), rightVariableMask.indexOf(leftVar));
                this.rightChildJoinColumns.add(rightVariableMask.indexOf(leftVar));
            }
        }
        for (String rightVar : rightVariableMask) {
            if (rightVar == null) {
                this.variableMask.add(null);
            } else {
                if (!this.variableMask.contains(rightVar))
                    this.variableMask.add(rightVar);
            }
        }
    }

    @Override
    public void dump() {
        
    }

    @Override
    public void reset() {
        this.leftChild.reset();
        this.rightChild.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple leftTuple = this.leftChild.getNextTuple();
        while (leftTuple != null) {

            Tuple rightTuple = this.rightChild.getNextTuple();
            while (rightTuple != null) {

                // check the join conditions
                boolean pass = true;
                for (Integer leftIndex : this.joinConditionIndices.keySet()) {
                    int rightIndex = this.joinConditionIndices.get(leftIndex);
                    if (!leftTuple.getTerms().get(leftIndex).equals(rightTuple.getTerms().get(rightIndex))) {
                        pass = false;
                        break;
                    }
                }

                // if all conditions are satisfied, construct a new Tuple instance as join result
                if (pass) {
                    List<Term> joinTermList = new ArrayList<>();
                    // the join result contains all columns in left tuple, and the non-duplicate columns in right tuple
                    for (Term leftTerm : leftTuple.getTerms())
                        joinTermList.add(leftTerm);
                    for (int i = 0; i < rightTuple.getTerms().size(); i++) {
                        if (!this.rightChildJoinColumns.contains(i)) {
                            joinTermList.add(rightTuple.getTerms().get(i));
                        }
                    }
                    return new Tuple("Join", joinTermList);
                }

                // otherwise, check the next right tuple
                rightTuple = this.rightChild.getNextTuple();
            }
            this.rightChild.reset();

            leftTuple = this.leftChild.getNextTuple();
        }
        return null;
    }
}
