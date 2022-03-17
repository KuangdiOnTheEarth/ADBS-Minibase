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
        HashMap<String, Integer> leftVariableMask = leftChild.getVariableMask();
        this.rightChild = rightChild;
        HashMap<String, Integer> rightVariableMask = rightChild.getVariableMask();

        for (String varName : leftVariableMask.keySet()) {
            this.variableMask.put(varName, leftVariableMask.get(varName));
            if (rightVariableMask.keySet().contains(varName)) {
                this.joinConditionIndices.put(leftVariableMask.get(varName), rightVariableMask.get(varName));
                this.rightChildJoinColumns.add(rightVariableMask.get(varName));
            }
        }
        int leftTupleSize = leftChild.getNextTuple().getTerms().size();
        leftChild.reset();
        for (String rightVar : rightVariableMask.keySet()) {
            if (!this.variableMask.keySet().contains(rightVar)) {
                int rightIndex = 
                this.variableMask.put(rightVar, (leftTupleSize-1)+rightIndex );
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
