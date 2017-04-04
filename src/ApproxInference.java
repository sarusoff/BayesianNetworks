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
            for (RandomVariable rv : vars){
                String name = rv.getName();

                // set the rv to true so that the variable's prior is used
                assignment.set(rv,new Boolean(true));

                double probability = bn.getProb(rv,assignment);
                Boolean result = getRandResult(probability,random);

                // set rv result to what actually was predicted
                assignment.set(rv,result);

                // reject contradicting samples
                if (contradictsEvidence(name,result)){
                    break;
                }

                // increment count
                if (result == true){
                    int c = (counts.containsKey(name)) ? counts.get(name) : 0;
                    counts.put(name,c+1);
                }
            }
        }
        return counts;
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
