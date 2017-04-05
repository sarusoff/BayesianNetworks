package approximateinference;

import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.RandomVariable;
import bn.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ApproxInference {
    private BayesianNetwork bn;
    private RandomVariable queryVar;
    private Assignment evidence;

    public ApproxInference(BayesianNetwork bn, RandomVariable queryVar, Assignment evidence){
        this.bn = bn;
        this.queryVar = queryVar;
        this.evidence = evidence;
    }

    /**
     * Calls the necessary private methods to return a normalized probability
     * distribution of the query variable given the evidence. Performs the number
     * of samples that is passed in as a parameter.
     */

    protected Distribution rejectionSampling(int limit){
        Random random = new Random();
        List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
        Map<String,Integer> counts = getSampleCounts(random,vars,limit);
        Distribution dist = getDistributionOfQueryVar(counts);
        dist.normalize();
        return dist;
    }

    /**
     * Returns a Map containing each RandomVariable's String value mapping
     * to the number of times that the variable was true given the evidence.
     */
    private Map<String,Integer> getSampleCounts(Random random, List<RandomVariable> vars, int limit) {
        Map<String,Integer> counts = new HashMap<>(vars.size());
        for (int count = 0; count < limit; count++){
            Assignment assignment = shallowCopy(evidence);
            boolean valid = true;
            for (RandomVariable rv : vars){
                String name = rv.getName();

                // set the rv to be true so that the variable's prior is used
                set(assignment,rv,true);

                double probability = bn.getProb(rv,assignment);
                Boolean result = getRandResult(probability,random);

                // set rv result to what actually was predicted
                set(assignment,rv,result);

                // reject contradicting samples
                if (contradictsEvidence(name,result)){
                    valid = false;
                    break;
                }
            }
            // increment count if the sample is valid (not rejected)
            if (valid == true){
                updateCounts(assignment,counts);
            }
        }
        return counts;
    }

    /**
     * Updates the counts Map so that variables that are true in the assignment
     * are incremented.
     */
    private void updateCounts(Assignment assignment, Map<String, Integer> counts) {
        for (Map.Entry<RandomVariable, Object> entry : assignment.entrySet()){
            boolean value = Boolean.parseBoolean(entry.getValue().toString());

            if (value == true){
                String name = entry.getKey().getName();
                int c = (counts.containsKey(name)) ? counts.get(name) : 0;
                counts.put(name,c+1);
            }
        }
    }

    /**
     * Assign the given variable to the given Boolean value. If the
     * variable already exists in the Assignment then overwrite it
     * with the new Boolean value.
     */
    private void set(Assignment assignment, RandomVariable var, Boolean value) {
        for (RandomVariable rv : assignment.variableSet()){
            if (rv.getName().equals(var.getName())){
                assignment.set(rv,value);
                return;
            }
        }

        // it's not in the assignment, so add a new entry
        assignment.set(var,value);
    }


    private Distribution getDistributionOfQueryVar(Map<String, Integer> counts) {
        Distribution dist = new Distribution();
        double evidenceCount = getCountOfEvidence(counts);
        double queryCount = getCountOfQueryVariable(counts);
        double T = queryCount/evidenceCount;
        double F = 1-T;
        dist.put(queryVar.getName() +" true", Utils.round(T,3));
        dist.put(queryVar.getName() +" false",Utils.round(F,3));
        return dist;
    }


    /**
     * Returns the count associated with the query variable
     */
    private Double getCountOfQueryVariable(Map<String, Integer> counts) {
        for (Map.Entry<String,Integer> entry : counts.entrySet()){
            if (queryVar.getName().equals(entry.getKey())){
                return new Double(entry.getValue());
            }
        }
        System.err.println("An error occurred in method getCountOfQueryVariable()");
        return null;
    }

    /**
     * Returns the smallest count of any evidence variable. Intuitively,
     * this represents the probability that all evidence variables are true
     */
    private double getCountOfEvidence(Map<String, Integer> counts) {
        double min = Double.MAX_VALUE;
        for (Map.Entry<String,Integer> entry : counts.entrySet()){
            double val = entry.getValue();
            String name = entry.getKey();
            if (inEvidence(name)){
                if (val < min){
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Returns true if the given String representing a variable
     * exists in the evidence instance
     */
    private Boolean inEvidence(String test) {
        for (RandomVariable rv : evidence.variableSet()){
            if (test.equals(rv.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the given variable name and its corresponding
     * Boolean result contradict that variable's assignment in the evidence
     */
    private boolean contradictsEvidence(String name, Boolean result) {
        for (Map.Entry<RandomVariable, Object> entry : evidence.entrySet()){
            if (entry.getKey().getName().equals(name)){ // if the name is in the evidence
                if (result != Boolean.valueOf(entry.getValue().toString())){ // if it isn't equal to the evidence value
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getRandResult(double probability, Random random) {
        double randNum = random.nextDouble();
        return randNum < probability;
    }

    /**
     * Returns a shallow copy of the given Assignment. The
     * values themselves are not cloned.
     */
    public Assignment shallowCopy(Assignment old) {
        Assignment newCopy = new Assignment();
        for (Map.Entry<RandomVariable, Object> entry : old.entrySet()){
            newCopy.put(entry.getKey(),entry.getValue());
        }
        return newCopy;
    }



}
