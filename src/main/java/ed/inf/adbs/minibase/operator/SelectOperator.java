package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.List;

public class SelectOperator extends Operator {

    private Operator child;
    private List<Condition> conditions = new ArrayList<>();

//    /**
//     *
//     * @param baseQueryAtom the RelationalAtom on which the selection is applied,
//     *                 this atom helps match variables in ComparisonAtom to positions in Tuple
//     * @param compAtomList a List of ComparisonAtom, each represent one predicate condition on the base relation
//     */
//    public SelectOperator(RelationalAtom baseQueryAtom, List<ComparisonAtom> compAtomList) {
//        List<Term> baseTerms = baseQueryAtom.getTerms();
//        for (ComparisonAtom comparisonAtom : compAtomList) {
//            this.conditions.add(new Condition(baseQueryAtom, comparisonAtom, this.variableMask));
//        }
//    }

    public SelectOperator(Operator child, List<ComparisonAtom> compAtomList) {
        this.child = child;
        this.variableMask = this.child.getVariableMask();

        for (ComparisonAtom comparisonAtom : compAtomList) {
            this.conditions.add(new Condition(comparisonAtom, this.variableMask));
        }
    }

//    public void setChild(Operator child) {
//        this.child = child;
//        this.variableMask = this.child.getVariableMask();
//    }

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

        List<Term> queryAtomTerms = new ArrayList<>();
        queryAtomTerms.add( new IntegerConstant(9));
        queryAtomTerms.add( new Variable("x"));
        queryAtomTerms.add( new Variable("y"));
        RelationalAtom queryAtom = new RelationalAtom("R", queryAtomTerms); // R:(9, x, y)
        System.out.println("Query relational atom: " + queryAtom);

        ScanOperator scanOp = new ScanOperator(queryAtom);

        List<ComparisonAtom> compAtomList = new ArrayList<>();
        ComparisonAtom compAtom1 = new ComparisonAtom(
                new Variable("x"), new IntegerConstant(5), ComparisonOperator.fromString("<")); // x < 5
        compAtomList.add(compAtom1);
        ComparisonAtom compAtom2 = new ComparisonAtom(
                new Variable("y"), new StringConstant("mlpr"), ComparisonOperator.fromString(">=")); // y > "mlpr"
        compAtomList.add(compAtom2);
        System.out.println("Query comparison atom: " + compAtom2);


        SelectOperator seleOp = new SelectOperator(scanOp, compAtomList);
        seleOp.dump();
    }

}
