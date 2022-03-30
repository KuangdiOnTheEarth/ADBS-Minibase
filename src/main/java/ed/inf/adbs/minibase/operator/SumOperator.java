package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.Sum;
import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SumOperator extends Operator {

    private Operator child;
    private String aggVariable;
    private int aggIndex;

    List<AggBuffer> outputBuffer = new ArrayList<>();
    HashMap<String, Integer> tuple2BufferIndex = new HashMap<>();
//    HashMap<String, AggBuffer> outputBuffer = new HashMap<>();
    // map a string of tuple (with the group-by variables only) to its index in output buffer

    public SumOperator(Operator childOperator, Sum aggTerm) {
        this.child = childOperator;
        this.aggVariable = aggTerm.getVariable();
        List<String> childVariableMask = childOperator.getVariableMask(); // the variableMask before aggregation
        this.aggIndex = childVariableMask.indexOf(this.aggVariable);
        // update the variable mask (this variableMask wouldn't be further used, just to keep the interface unified)
        for (String childVar : childVariableMask) {
            if (!childVar.equals(aggVariable))
                this.variableMask.add(childVar);
            else
                this.variableMask.add(aggTerm.toString());
        }
        System.out.println(childVariableMask + " --> " + "SUM over " + this.aggVariable);
    }

    @Override
    public void dump() {
        Tuple nextTuple = this.getNextTuple();
        while (nextTuple != null) {
            System.out.println(nextTuple);
            nextTuple = this.getNextTuple();
        }
    }

    @Override
    public void reset() {
        this.child.reset();
        this.tuple2BufferIndex = new HashMap<>();
        this.outputBuffer = new ArrayList<>();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple childTuple = this.child.getNextTuple();
        while (childTuple != null) {
            List<Term> termList = new ArrayList<>(childTuple.getTerms());
            Term aggTerm = termList.remove(this.aggIndex); // the group-by variable list, used to group aggregation
            String bufferKey = termList.toString();
//            System.out.println("childTuple: " + bufferKey + "; agg term: " + aggTerm);
            if (this.tuple2BufferIndex.containsKey(bufferKey)) {
//                System.out.println("Key in buffer");
                // this combination of group-by variables exists in output buffer
                // only need to update the aggregation term
                int bufferIndex = this.tuple2BufferIndex.get(bufferKey);
                this.outputBuffer.get(bufferIndex).addSum(((IntegerConstant) aggTerm).getValue());
            } else {
//                System.out.println("Key not in buffer");
                // add the new group-by variable combination into buffer
                // add the whole tuple into output buffer
                AggBuffer aggBuffer = new AggBuffer(termList, this.aggIndex, this.aggVariable);
                aggBuffer.addSum(((IntegerConstant) aggTerm).getValue());
                this.outputBuffer.add(aggBuffer);
                this.tuple2BufferIndex.put(bufferKey, this.outputBuffer.size()-1);
            }
            childTuple = this.child.getNextTuple();
        }
//        System.out.println(this.outputBuffer.size());
        if (this.outputBuffer.size() > 0) {
            // add the aggregation term into term list, return the generated Tuple
            return this.outputBuffer.remove(0).getSumTuple();
        } else {
            return null;
        }
    }
}
