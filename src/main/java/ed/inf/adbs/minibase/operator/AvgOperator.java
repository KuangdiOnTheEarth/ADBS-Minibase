package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements a project operation with an AVG term at the end of the query head term list.
 * The main logic of processing the project is similar as {@link ProjectOperator}.
 * To accumulation of the sum and tuple count for the aggregation are processed by {@link AggBuffer}.
 */
public class AvgOperator extends Operator {
    private Operator child;
    private String projectionName;
    private int aggIndex;
    private String aggVariable;

    private List<Integer> projectIndices = new ArrayList<>();
    // a mapping from columns after projection to columns before projection,
    // indicates where to find the projection column in child tuple

    List<AggBuffer> outputBuffer = new ArrayList<>();
    // Store the AggBuffer instances, each of which represent an output tuple of this operator,
    // the accumulation of aggregation term will be processed within the AggBuffer

    HashMap<String, Integer> tuple2BufferIndex = new HashMap<>();
    // map a tuple in string format to its index in outputBuffer.
    // used to check whether a tuple without aggregation term has been observed,
    // if it has been observed, the aggregation term will be accumulated to existing record in outputBuffer
    // otherwise a new record in outputBuffer will be inserted into outputBuffer

    /**
     * Initialise the operator. Process the last aggregation term and other normal terms separately.
     * This main logic is similar as {@link ProjectOperator}: generating a mapping relation of columns for the projection.
     * @param childOperator
     * @param queryHead
     */
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

    /**
     * Reset child operator, buffer states and the map to buffer.
     */
    @Override
    public void reset() {
        this.child.reset();
        this.tuple2BufferIndex = new HashMap<>();
        this.outputBuffer = new ArrayList<>();
    }

    /**
     * Get the next tuple from child operator, extract the term list and remove the aggregation term.
     * The rest of term list will be converted to a string as a key in {@code tuple2BufferIndex}.
     * If a tuple without aggregation term has already been recorded, a GROUP operation is required,
     * and the new tuple will be merged into the existing record, i.e. the new aggregation term will be accumulated on the existing record.
     * After a blocking operation travel through all the child output tuples,
     * each call of this method will remove and return the first output tuple from the buffer.
     * Notice: the aggregation operation and output tuple construction is implemented in {@link AggBuffer}.
     * @return a tuple after projection and aggregation.
     */
    @Override
    public Tuple getNextTuple() {
        Tuple childOutput = this.child.getNextTuple();
        while (childOutput != null) {
            // extract the term list and remove the aggregation term
            List<Term> termList = new ArrayList<>();
            for (int pi : this.projectIndices) {
                termList.add(childOutput.getTerms().get(pi));
            }
            Tuple newTuple = new Tuple(this.projectionName, termList);
            IntegerConstant aggTerm = (IntegerConstant) newTuple.getTerms().remove(this.aggIndex);

            // convert the term list (without aggregation term) into string, acting as a key for hashmap
            String bufferKey = newTuple.getTerms().toString();
            if (this.tuple2BufferIndex.containsKey(bufferKey)) {
                // GROUP operation, accumulate the aggregation term
                int bufferIndex = this.tuple2BufferIndex.get(bufferKey);
                this.outputBuffer.get(bufferIndex).addSum(aggTerm.getValue());
            } else {
                // new tuple, create a new buffer record for it
                AggBuffer aggBuffer = new AggBuffer(newTuple.getTerms(), this.aggIndex, this.aggVariable);
                aggBuffer.addSum(aggTerm.getValue());
                this.outputBuffer.add(aggBuffer);
                this.tuple2BufferIndex.put(bufferKey, this.outputBuffer.size()-1);
            }
            childOutput = this.child.getNextTuple();
        }
        // after all the output tuples from child operator are processed,
        // return the top tuple in buffer for each call of this method.
        if (this.outputBuffer.size() > 0) {
            // add the aggregation term into term list, return the generated Tuple
            return this.outputBuffer.remove(0).getAvgTuple();
        } else {
            return null;
        }
    }
}
