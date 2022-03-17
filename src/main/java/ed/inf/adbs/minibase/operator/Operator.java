package ed.inf.adbs.minibase.operator;

import java.util.HashMap;

public abstract class Operator {

    protected HashMap<String, Integer> variableMask = new HashMap<>();

    public abstract void dump();

    public abstract void reset();

    public abstract Tuple getNextTuple();

    public HashMap<String, Integer> getVariableMask() {
        return this.variableMask;
    }

}
