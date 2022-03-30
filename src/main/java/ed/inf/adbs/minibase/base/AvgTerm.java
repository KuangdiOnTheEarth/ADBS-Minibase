package ed.inf.adbs.minibase.base;

public class AvgTerm extends Term {

    private String name;

    public AvgTerm(String name) {
        this.name = name;
    }

    public String getVariable() {
        return name;
    }

    @Override
    public String toString() {
        return "AVG(" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AvgTerm)) return false;
        return (this.name).equals(((AvgTerm) obj).getVariable());
    }

}
