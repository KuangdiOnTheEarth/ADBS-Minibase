package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.List;

public class AggBuffer {

    private List<Term> termList;
    private int aggIndex;
    private String aggVarName;
    private int aggSum = 0; // the accumulated sum for SUM and AVG
    private int aggCount = 0; // the number of rows in this group, for AVG

    public AggBuffer(List<Term> termList, int aggIndex, String aggVarName) {
        this.termList = new ArrayList<>(termList);
        this.aggIndex = aggIndex;
        this.aggVarName = aggVarName;
    }

    public void addSum(int val) {
        this.aggSum += val;
        this.aggCount += 1;
    }

    public Tuple getSumTuple() {
        List<Term> termList = new ArrayList<>(this.termList);
        termList.add(this.aggIndex, new IntegerConstant(this.aggSum));
        return new Tuple("SUM("+this.aggVarName+")", termList);
    }

    public Tuple getAvgTuple() {
        List<Term> termList = new ArrayList<>(this.termList);
        termList.add(this.aggIndex, new IntegerConstant(this.aggSum/this.aggCount));
        return new Tuple("AVG("+this.aggVarName+")", termList);
    }
}
