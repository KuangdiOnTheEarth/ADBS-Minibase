package ed.inf.adbs.minibase.base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StringConstant)) return false;
        return (this.value).equals(((StringConstant) obj).getValue());
    }
}