package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.List;

public class SelectOperator extends Operator {

    private Operator child;
    private List<Condition> conditions = new ArrayList<>();
//    private List<Triple<String, Integer, Integer>> conditions = new ArrayList<>();
    // each Triple contains one condition,
    // with first element as the comparison operator, the last two element as the index of operator in the relation.
    // e.g. R(x,4,y) and 'x=y' can be interpreted as: < '=', 0, 2 >

    private class Condition {
        private String op;
        private Term term1 = null;
        private int term1Idx;
        private Term term2 = null;
        private int term2Idx;

        public Condition(RelationalAtom baseQueryAtom, ComparisonAtom compAtom) {
            this.op = compAtom.getOp().toString();
            if (compAtom.getTerm1() instanceof Variable) {
                this.term1Idx = baseQueryAtom.getTerms().indexOf(compAtom.getTerm1());
                System.out.println("Term 1 is Variable, at relation index: " + this.term1Idx);
            } else {
                this.term1 = compAtom.getTerm1();
                System.out.println("Term 1 is Constant: " + this.term1);
            }
            if (compAtom.getTerm2() instanceof Variable) {
                this.term2Idx = baseQueryAtom.getTerms().indexOf(compAtom.getTerm2());
                System.out.println("Term 2 is Variable, at relation index: " + this.term2Idx);
            } else {
                this.term2 = compAtom.getTerm2();
                System.out.println("Term 2 is Constant: " + this.term2);
            }
        }

        public boolean check(Tuple tuple) {
            Term operand1 = this.term1 == null ? tuple.getTerms().get(term1Idx) : this.term1;
            Term operand2 = this.term2 == null ? tuple.getTerms().get(term2Idx) : this.term2;
//            System.out.println("--------------- Checking Tuple: " + tuple + ", with " + operand1 + this.op +operand2);
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

    /**
     *
     * @param baseQueryAtom the RelationalAtom on which the selection is applied,
     *                 this atom helps match variables in ComparisonAtom to positions in Tuple
     * @param compAtomList a List of ComparisonAtom, each represent one predicate condition on the base relation
     */
    public SelectOperator(RelationalAtom baseQueryAtom, List<ComparisonAtom> compAtomList) {
        List<Term> baseTerms = baseQueryAtom.getTerms();
        for (ComparisonAtom comparisonAtom : compAtomList) {
            this.conditions.add(new Condition(baseQueryAtom, comparisonAtom));
        }
    }

    public void setChild(Operator child) {
        this.child = child;
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
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = this.child.getNextTuple();
        while (nextTuple != null) {
            boolean pass = true;
            for (Condition condition : this.conditions) {
                if (!condition.check(nextTuple)) {
                    pass = false;
                    break;
                }
            }
            if (pass)
                return nextTuple;
            else
                nextTuple = this.child.getNextTuple();
        }
        return null;
    }

    public static void main(String[] args) {
        DBCatalog dbc = DBCatalog.getInstance();
        dbc.init("data/evaluation/db");
        ScanOperator scanOp = new ScanOperator("R");

        List<Term> queryAtomTerms = new ArrayList<>();
        queryAtomTerms.add( new IntegerConstant(9));
        queryAtomTerms.add( new Variable("x"));
        queryAtomTerms.add( new Variable("y"));
        RelationalAtom queryAtom = new RelationalAtom("R", queryAtomTerms); // R:(9, x, y)
        System.out.println("Query relational atom: " + queryAtom);

        List<ComparisonAtom> compAtomList = new ArrayList<>();
        ComparisonAtom compAtom1 = new ComparisonAtom(
                new Variable("x"), new IntegerConstant(5), ComparisonOperator.fromString("<")); // x < 5
        compAtomList.add(compAtom1);
        ComparisonAtom compAtom2 = new ComparisonAtom(
                new Variable("y"), new StringConstant("mlpr"), ComparisonOperator.fromString(">=")); // y > "mlpr"
        compAtomList.add(compAtom2);
        System.out.println("Query comparison atom: " + compAtom2);


        SelectOperator seleOp = new SelectOperator(queryAtom, compAtomList);
        seleOp.setChild(scanOp);
        seleOp.dump();
    }

}
