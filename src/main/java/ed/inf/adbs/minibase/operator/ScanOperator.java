package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScanOperator extends Operator {

    private final String relationName;
    private Scanner relationScanner;
    private final List<String> relationSchema;

    public ScanOperator(RelationalAtom baseQueryAtom) {
//        for (int i = 0; i < baseQueryAtom.getTerms().size(); i++) {
//            if (baseQueryAtom.getTerms().get(i) instanceof Variable)
//                this.variableMask.put(baseQueryAtom.getTerms().get(i).toString(), i);
//        }
        for (Term term : baseQueryAtom.getTerms()) {
            if (term instanceof Variable)
                this.variableMask.add(((Variable) term).getName());
            else
                this.variableMask.add(null);
        }

        this.relationName = baseQueryAtom.getName();
        DBCatalog dbc = DBCatalog.getInstance();
        this.relationSchema = dbc.getSchema(relationName);
        this.reset();
    }

    @Override
    public void dump() {
        while (this.relationScanner.hasNextLine()) {
            System.out.println(this.getNextTuple());
        }
    }

    @Override
    public void reset() {
        DBCatalog dbc = DBCatalog.getInstance();
        try {
            this.relationScanner = new Scanner(new File(dbc.getRelationPath(relationName)));
        } catch (FileNotFoundException e) {
            System.out.println("Relation data file not found: " + dbc.getRelationPath(relationName));
            e.printStackTrace();
        }
    }

    @Override
    public Tuple getNextTuple() {
        if (this.relationScanner.hasNextLine()) {
            String line = this.relationScanner.nextLine();
            String[] raw_data = line.split("[^a-zA-Z0-9]+");
            ArrayList<Term> terms = new ArrayList<>();
            for (int i = 0; i < raw_data.length; i++) {
                if (this.relationSchema.get(i).equals("int")) {
                    terms.add(new IntegerConstant(Integer.parseInt(raw_data[i])));
                } else {
                    terms.add(new StringConstant(raw_data[i]));
                }
            }
            return new Tuple(this.relationName, terms);
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        DBCatalog dbc = DBCatalog.getInstance();
        dbc.init("data/evaluation/db");

        List<Term> queryAtomTerms = new ArrayList<>();
        queryAtomTerms.add( new IntegerConstant(9));
        queryAtomTerms.add( new Variable("x"));
        queryAtomTerms.add( new Variable("y"));
        RelationalAtom queryAtom = new RelationalAtom("R", queryAtomTerms); // R:(9, x, y)
        ScanOperator scanOp = new ScanOperator(queryAtom);
        System.out.println(scanOp.getVariableMask());

        scanOp.dump();

        System.out.println("---------");
        System.out.println(scanOp.getNextTuple());
        scanOp.reset();
        System.out.println(scanOp.getNextTuple());
        System.out.println(scanOp.getNextTuple());
        scanOp.reset();
        System.out.println(scanOp.getNextTuple());

        System.out.println("---------");
        scanOp.dump();

    }

}