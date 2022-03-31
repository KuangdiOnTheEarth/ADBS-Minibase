package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used in {@link AggregateOperator} to support the aggregation operation for both SUM and AVG.
 * The output tuple of aggregation operation is split into two parts:
 *      (1) the name aggregation term are stored in {@code aggVarName}.
 *          they need to be updated when new tuples are grouped to existing tuples:
 *          for every new tuple that grouped to existing one:
 *          the aggregation value will be added to {@code aggSum},
 *          and the {@code aggCount} will be incremented by 1 for preparing for the AVG calculation
 *      (2) the other terms are stored in {@code termList}.
 *  After the aggregation process is completed,
 *  each instance of this class will be converted into an output tuple of {@link AggregateOperator}
 *  by calling {@link #getAvgTuple()} or {@link #getSumTuple()} depending on the use case.
 */
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

    /**
     * Support a GROUP operation, accumulate the value by adding the aggregation term in new tuple to {@code aggSum}.
     * The {@code aggCount} is incremented to track the number of aggregation terms, supports the AVG operation.
     * @param val the value to be accumulated.
     */
    public void addSum(int val) {
        this.aggSum += val;
        this.aggCount += 1;
    }

    /**
     * Used by {@link SumOperator}
     * Convert the aggregation sum value into a IntegerConstant and add it to the tail of term list,
     * then construct an output tuple and return.
     * @return the output tuple after aggregation.
     */
    public Tuple getSumTuple() {
        List<Term> termList = new ArrayList<>(this.termList);
        termList.add(this.aggIndex, new IntegerConstant(this.aggSum));
        return new Tuple("SUM("+this.aggVarName+")", termList);
    }

    /**
     * Used by {@link AvgOperator}
     * Divide the aggregation {@code aggSum} by {@code aggCount},
     * convert to IntegerConstant and add it to the tail of term list,
     * then construct an output tuple and return.
     * @return the output tuple after aggregation.
     */
    public Tuple getAvgTuple() {
        List<Term> termList = new ArrayList<>(this.termList);
        termList.add(new IntegerConstant(this.aggSum/this.aggCount));
        return new Tuple("AVG("+this.aggVarName+")", termList);
    }
}
