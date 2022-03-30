package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AvgOperator extends Operator {
    private Operator child;
    private String projectionName;
    private int aggIndex;
    private String aggVariable;

    private List<Integer> projectIndices = new ArrayList<>(); // where to find the projection column in child tuple
//    private List<String> reportBuffer = new ArrayList<>();

    List<AggBuffer> outputBuffer = new ArrayList<>();
    HashMap<String, Integer> tuple2BufferIndex = new HashMap<>();


    public AvgOperator(Operator childOperator, RelationalAtom queryHead) {
        this.child = childOperator;
        List<String> childVariableMask = childOperator.getVariableMask(); // the variableMask before projection
        this.projectionName = queryHead.getName();
        for (int i = 0; i < queryHead.getTerms().size() - 1; i++) {
            String varName = ((Variable) queryHead.getTerms().get(i)).getName();
            int idx = childVariableMask.indexOf(varName);
            this.projectIndices.add(idx);
            this.variableMask.add(varName); // this.variableMask will record the variable positions after projection
        }
        // process the last aggregation term:
        this.aggIndex = queryHead.getTerms().size()-1;
        Avg avgTerm = ((Avg) queryHead.getTerms().get(this.aggIndex));
        this.aggVariable = avgTerm.getVariable();
        String aggVar = avgTerm.getVariable();
        int idx = childVariableMask.indexOf(aggVar);
        this.projectIndices.add(idx);
        this.variableMask.add(avgTerm.toString()); // this.variableMask will record the variable positions after projection

        System.out.println(childVariableMask + "- -> " + this.variableMask + "(" + this.projectIndices + ")");
    }

    @Override
    public void reset() {
        this.child.reset();
        this.tuple2BufferIndex = new HashMap<>();
        this.outputBuffer = new ArrayList<>();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple childOutput = this.child.getNextTuple();
        while (childOutput != null) {
            List<Term> termList = new ArrayList<>();
            for (int pi : this.projectIndices) {
                termList.add(childOutput.getTerms().get(pi));
            }
            Tuple newTuple = new Tuple(this.projectionName, termList);
            IntegerConstant aggTerm = (IntegerConstant) newTuple.getTerms().remove(this.aggIndex);

            String bufferKey = newTuple.getTerms().toString();
            if (this.tuple2BufferIndex.containsKey(bufferKey)) {
                int bufferIndex = this.tuple2BufferIndex.get(bufferKey);
                this.outputBuffer.get(bufferIndex).addSum(aggTerm.getValue());
            } else {
                AggBuffer aggBuffer = new AggBuffer(newTuple.getTerms(), this.aggIndex, this.aggVariable);
                aggBuffer.addSum(aggTerm.getValue());
                this.outputBuffer.add(aggBuffer);
                this.tuple2BufferIndex.put(bufferKey, this.outputBuffer.size()-1);
            }
            childOutput = this.child.getNextTuple();
        }
        if (this.outputBuffer.size() > 0) {
            // add the aggregation term into term list, return the generated Tuple
            return this.outputBuffer.remove(0).getAvgTuple();
        } else {
            return null;
        }
    }
}
