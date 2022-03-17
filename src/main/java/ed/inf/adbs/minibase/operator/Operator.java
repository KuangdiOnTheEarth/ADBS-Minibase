package ed.inf.adbs.minibase.operator;

import java.util.ArrayList;
import java.util.List;

public abstract class Operator {

    protected List<String> variableMask = new ArrayList<>();

    public abstract void dump();

    public abstract void reset();

    public abstract Tuple getNextTuple();

    public List<String> getVariableMask() {
        return this.variableMask;
    }

}
