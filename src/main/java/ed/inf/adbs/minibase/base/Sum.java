package ed.inf.adbs.minibase.base;

public class Sum extends AggTerm {

    public Sum(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "SUM(" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Sum)) return false;
        return (this.name).equals(((Sum) obj).getVariable());
    }

}
