package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.List;

public class SelectCondition {
    private String op;
    private Term term1 = null;
    private int term1Idx;
    private Term term2 = null;
    private int term2Idx;

    public SelectCondition(ComparisonAtom compAtom, List<String> variableMask) {
        this.op = compAtom.getOp().toString();
        if (compAtom.getTerm1() instanceof Variable) {
//                this.term1Idx = baseQueryAtom.getTerms().indexOf(compAtom.getTerm1());
//            this.term1Idx = variableMask.get(((Variable) compAtom.getTerm1()).getName());
            this.term1Idx = variableMask.indexOf(((Variable) compAtom.getTerm1()).getName());
            System.out.println("Term 1 is Variable, at relation index: " + this.term1Idx);
        } else {
            this.term1 = compAtom.getTerm1();
            System.out.println("Term 1 is Constant: " + this.term1);
        }
        if (compAtom.getTerm2() instanceof Variable) {
//                this.term2Idx = baseQueryAtom.getTerms().indexOf(compAtom.getTerm2());
//            this.term2Idx = variableMask.get(((Variable) compAtom.getTerm2()).getName());
            this.term2Idx = variableMask.indexOf(((Variable) compAtom.getTerm2()).getName());
            System.out.println("Term 2 is Variable, at relation index: " + this.term2Idx);
        } else {
            this.term2 = compAtom.getTerm2();
            System.out.println("Term 2 is Constant: " + this.term2);
        }
    }

    public boolean check(Tuple tuple) {
        Term operand1 = this.term1 == null ? tuple.getTerms().get(term1Idx) : this.term1;
        Term operand2 = this.term2 == null ? tuple.getTerms().get(term2Idx) : this.term2;
//            System.out.println("--------------- Checking Tuple: " + tuple + ", with " + operand1 + this.op +operand2);
        if (this.op.equals("=")) {
            return operand1.equals(operand2);
        } else if (this.op.equals("!=")) {
            return (!operand1.equals(operand2));
        } else if (this.op.equals(">")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() > ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) > 0;
        } else if (this.op.equals(">=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() >= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) >= 0;
        } else if (this.op.equals("<")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() < ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) < 0;
        } else if (this.op.equals("<=")) {
            if (operand1 instanceof IntegerConstant)
                return ((IntegerConstant) operand1).getValue() <= ((IntegerConstant) operand2).getValue();
            return ((StringConstant) operand1).getValue().compareTo(((StringConstant) operand2).getValue()) <= 0;
        } else {
            System.out.println("!!!! None of the if-branches is evoked in the Selection Operator !!!!");
            return false;
        }
    }
}