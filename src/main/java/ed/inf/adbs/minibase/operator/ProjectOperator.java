package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectOperator extends Operator {

    private Operator child;
    private String projectionName;

    private List<Integer> projectIndices = new ArrayList<>();
    private List<String> reportBuffer = new ArrayList<>();


    public ProjectOperator(Operator childOperator, RelationalAtom queryHead) {
        this.child = childOperator;
        HashMap<String, Integer> childVariableMask = childOperator.getVariableMask(); // the variableMask before projection
        this.projectionName = queryHead.getName();
        for (int i = 0; i < queryHead.getTerms().size(); i++) {
            String varName = ((Variable) queryHead.getTerms().get(i)).getName();
            int idx = childVariableMask.get(varName);
            this.projectIndices.add(idx);
            this.variableMask.put(varName, i); // this.variableMask will record the variable positions after projection
        }
        System.out.println(childVariableMask + "- -> " + this.variableMask + "(" + this.projectIndices + ")");
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
        Tuple childOutput = this.child.getNextTuple();
        while (childOutput != null) {
            List<Term> termList = new ArrayList<>();
            for (int pi : this.projectIndices) {
                termList.add(childOutput.getTerms().get(pi));
            }
            Tuple newTuple = new Tuple(this.projectionName, termList);
            if (!this.reportBuffer.contains(newTuple.toString())) {
                this.reportBuffer.add(newTuple.toString());
                return newTuple;
            }
            childOutput = this.child.getNextTuple();
        }
        return null;
    }

    public static void main(String[] args) {
        DBCatalog dbc = DBCatalog.getInstance();
        dbc.init("data/evaluation/db");

        List<Term> queryAtomTerms = new ArrayList<>();
        queryAtomTerms.add( new Variable("x"));
        queryAtomTerms.add( new Variable("y"));
        queryAtomTerms.add( new Variable("z"));
        RelationalAtom queryBodyAtom = new RelationalAtom("R", queryAtomTerms); // R:(x, y, z)
        System.out.println("Query relational atom: " + queryBodyAtom);

        ScanOperator scanOp = new ScanOperator(queryBodyAtom);

        List<ComparisonAtom> compAtomList = new ArrayList<>();
        ComparisonAtom compAtom1 = new ComparisonAtom(
                new Variable("x"), new IntegerConstant(5), ComparisonOperator.fromString(">=")); // x >= 5
        compAtomList.add(compAtom1);
        ComparisonAtom compAtom2 = new ComparisonAtom(
                new Variable("z"), new StringConstant("mlpr"), ComparisonOperator.fromString(">=")); // z >= "mlpr"
        compAtomList.add(compAtom2);
        System.out.println("Query comparison atom: " + compAtom2);

        SelectOperator seleOp = new SelectOperator(scanOp, compAtomList);
        seleOp.dump();
        seleOp.reset();
        System.out.println("------------------------------");

        List<Term> queryHeadTerms = new ArrayList<>();
        queryHeadTerms.add( new Variable("y"));
        queryHeadTerms.add( new Variable("x"));
        RelationalAtom queryHeadAtom = new RelationalAtom("Q", queryHeadTerms);
        System.out.println(queryHeadAtom);

        ProjectOperator projOp = new ProjectOperator(seleOp, queryHeadAtom);
        projOp.dump();

    }
}
