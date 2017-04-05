import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.RandomVariable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by danielsaltz on 4/4/17.
 */
public class ApproxInference {
    private BayesianNetwork bn;
    private RandomVariable queryVar;
    private Assignment evidence;

    public ApproxInference(BayesianNetwork bn, RandomVariable queryVar, Assignment evidence){
        this.bn = bn;
        this.queryVar = queryVar;
        this.evidence = evidence;
    }

    protected Distribution rejectionSampling(int limit){
        Random random = new Random();
        List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
        Map<String,Integer> counts = getSampleCounts(random,vars,limit);
        Distribution dist = getDistributionOfQueryVar(counts);
        return dist;
    }

    private Map<String,Integer> getSampleCounts(Random random, List<RandomVariable> vars, int limit) {
        Map<String,Integer> counts = new HashMap<>(vars.size());
        for (int count = 0; count < limit; count++){
            Assignment assignment = copy(evidence);
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
            // increment count if valid is true
            if (valid == true){
                updateCounts(assignment,counts);
            }
        }
        return counts;
    }

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
     * This method is necessary in order to overwrite any evidence variables
     * without adding duplicate entries of the same variable to the Assignment
     */
    private void set(Assignment assignment, RandomVariable test, Boolean result) {
        for (RandomVariable rv : assignment.variableSet()){
            if (rv.getName().equals(test.getName())){
                assignment.set(rv,result);
                return;
            }
        }

        // it's not in the assignment, so add a new entry
        assignment.set(test,result);
    }

    private Distribution getDistributionOfQueryVar(Map<String, Integer> counts) {
        Distribution dist = new Distribution();
        double evidenceCount = getCountOfEvidence(counts);
        double queryCount = getCountOfQuery(counts);
        double T = queryCount/evidenceCount;
        double F = 1-T;
        dist.put(queryVar.getName() +" true",round(T,3));
        dist.put(queryVar.getName() +" false",round(F,3));
        return dist;
    }

    private Double getCountOfQuery(Map<String, Integer> counts) {
        for (Map.Entry<String,Integer> entry : counts.entrySet()){
            if (queryVar.getName().equals(entry.getKey())){
                return new Double(entry.getValue());
            }
        }
        return null;
    }

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

    private Boolean inEvidence(String test) {
        for (RandomVariable rv : evidence.variableSet()){
            if (test.equals(rv.getName())){
                return true;
            }
        }
        return false;
    }

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

    /**
     * Returns a shallow copy of this Assignment instance: the
     * values themselves are not cloned.
     */
    private Assignment copy(Assignment old) {
        Assignment newCopy = new Assignment();
        for (Map.Entry<RandomVariable, Object> entry : old.entrySet()){
            newCopy.put(entry.getKey(),entry.getValue());
        }
        return newCopy;
    }

    private boolean getRandResult(double probability, Random random) {
        double randNum = random.nextDouble();
        return randNum < probability;
    }

    private Distribution normalize(Distribution dist) {
        dist.normalize();
        Distribution newDist = new Distribution();
        for (Map.Entry<Object, Double> entry : dist.entrySet()){
            newDist.put(entry.getKey(),round(entry.getValue(),3));
        }
        return newDist;
    }

    private double round(double num, int places){
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
