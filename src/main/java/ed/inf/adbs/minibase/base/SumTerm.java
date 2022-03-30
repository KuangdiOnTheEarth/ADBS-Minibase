package ed.inf.adbs.minibase.base;

public class SumTerm extends Term {

    private String name;

    public SumTerm(String name) {
        this.name = name;
    }

    public String getVariable() {
        return name;
    }

    @Override
    public String toString() {
        return "SUM(" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SumTerm)) return false;
        return (this.name).equals(((SumTerm) obj).getVariable());
    }

}
