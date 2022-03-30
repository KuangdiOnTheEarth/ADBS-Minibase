package ed.inf.adbs.minibase.operator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class Operator {

    protected List<String> variableMask = new ArrayList<>();

    public void dump(String outputFile) {
        try {
            PrintWriter writer = null;
            if (outputFile != null && !outputFile.equals("")) {
                writer = new PrintWriter(outputFile);
            }

            Tuple nextTuple = this.getNextTuple();
            while (nextTuple != null) {
                if (writer == null)
                    System.out.println(nextTuple);
                else
                    writer.println(nextTuple.toString());
                nextTuple = this.getNextTuple();
            }

            if (writer!=null)
                writer.close();

        } catch (Exception e) {
            System.err.println("Exception occurred during dump operation");
            e.printStackTrace();
        }
    }

    public abstract void reset();

    public abstract Tuple getNextTuple();

    public List<String> getVariableMask() {
        return this.variableMask;
    }

}
