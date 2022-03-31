package ed.inf.adbs.minibase.base;

/**
 * Represents the terms with aggregation operations in query head.
 */
public class AggTerm extends Term {

    protected String name;

    public AggTerm(String name) {
        this.name = name;
    }

    public String getVariable() {
        return name;
    }

}
