package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScanOperator extends Operator{

    private final String relationName;
    private Scanner relationScanner;
    private final List<String> relationSchema;

    public ScanOperator(String relationName) {
        this.relationName = relationName;
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
        ScanOperator scanOp = new ScanOperator("R");

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