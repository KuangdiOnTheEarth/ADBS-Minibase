package ed.inf.adbs.minibase.base;

public class Avg extends AggTerm {

    public Avg(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "AVG(" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Avg)) return false;
        return (this.name).equals(((Avg) obj).getVariable());
    }

}
