package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JoinOperator extends Operator {

    private Operator leftChild;
    private Operator rightChild;

    private List<JoinCondition> conditions = new ArrayList<>();

    private HashMap<Integer, Integer> joinConditionIndices = new HashMap<>();
    private List<Integer> rightDuplicateColumns = new ArrayList<>();
    // the columns in right child to be removed (since it duplicates with columns in left child)

    public JoinOperator(Operator leftChild, Operator rightChild, List<ComparisonAtom> comparisonAtoms) {
        this.leftChild = leftChild;
        List<String> leftVariableMask = leftChild.getVariableMask();
        this.rightChild = rightChild;
        List<String> rightVariableMask = rightChild.getVariableMask();

        for (ComparisonAtom compAtom : comparisonAtoms)
            this.conditions.add(new JoinCondition(compAtom, leftVariableMask, rightVariableMask));

        for (String leftVar : leftVariableMask) {
            this.variableMask.add(leftVar);
            if (rightVariableMask.contains(leftVar)) {
                this.joinConditionIndices.put(leftVariableMask.indexOf(leftVar), rightVariableMask.indexOf(leftVar));
                this.rightDuplicateColumns.add(rightVariableMask.indexOf(leftVar));
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
        Tuple nextTuple = this.getNextTuple();
        while (nextTuple != null) {
            System.out.println(nextTuple);
            nextTuple = this.getNextTuple();
        }
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

                boolean pass = true;
                // check the inner join conditions provided by same variable names in two query atoms
                for (Integer leftIndex : this.joinConditionIndices.keySet()) {
                    int rightIndex = this.joinConditionIndices.get(leftIndex);
                    if (!leftTuple.getTerms().get(leftIndex).equals(rightTuple.getTerms().get(rightIndex))) {
                        pass = false;
                        break;
                    }
                }
                // check the join conditions provided by extra ComparisonAtom, and involves different variables
                if (pass) {
                    for (JoinCondition condition : this.conditions) {
                        if (!condition.check(leftTuple, rightTuple)) {
                            pass = false;
                            break;
                        }
                    }
                }

                // if all conditions are satisfied, construct a new Tuple instance as join result
                if (pass) {
                    List<Term> joinTermList = new ArrayList<>();
                    // the join result contains all columns in left tuple, and the non-duplicate columns in right tuple
                    for (Term leftTerm : leftTuple.getTerms())
                        joinTermList.add(leftTerm);
                    for (int i = 0; i < rightTuple.getTerms().size(); i++) {
                        if (!this.rightDuplicateColumns.contains(i)) {
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

    public static void main(String[] args) {

    }
}
