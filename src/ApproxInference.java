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
    private int validCount;

    public ApproxInference(BayesianNetwork bn, RandomVariable queryVar, Assignment evidence){
        this.bn = bn;
        this.queryVar = queryVar;
        this.evidence = evidence;
        this.validCount = 0;
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
//            boolean valid = true;
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
//                    valid = false;
                    break;
                }

                // increment count
                if (result == true){
                    int c = (counts.containsKey(name)) ? counts.get(name) : 0;
                    counts.put(name,c+1);
                }
            }
//            if (valid){
//                validCount++;
//            }
        }
        return counts;
    }

    private Distribution getDistributionOfQueryVar(Map<String, Integer> counts) {
        Distribution dist = new Distribution();
        double evidenceCount = getCountOfEvidence(counts);
        double queryCount = getCountOfQuery(counts);

        double T = (double)Integer.valueOf(entry.getValue())/(double)validCount;
        double F = 1-T;
        dist.put(queryVar.getName() +" true",round(T,3));
        dist.put(queryVar.getName() +" false",round(F,3));


//        for (Map.Entry<String,Integer> entry : counts.entrySet()){
//            String name = entry.getKey();
//            if (name.equals(queryVar.getName())){  // only put queryVar in distribution
//                double T = (double)Integer.valueOf(entry.getValue())/(double)validCount;
//                double F = 1-T;
//                dist.put(queryVar.getName() +" true",round(T,3));
//                dist.put(queryVar.getName() +" false",round(F,3));
//            }
//        }
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
        double sum = 0;
        for (Map.Entry<String,Integer> entry : counts.entrySet()){
            String name = entry.getKey();
            if (inEvidence(name)){
                sum += entry.getValue();
            }
        }
        return sum;
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


//    private double getProb(RandomVariable rv, Assignment assignment) {

//        BayesianNetwork.Node node = bn.getNodeForVariable(rv);
//
//        if (node.parents.isEmpty()){  // if it's a root node, use the prior
//            String priorStr = node.cpt.toString();
//            priorStr = priorStr.substring(7,priorStr.length());
//            double prior = Double.parseDouble(priorStr.split("\n")[0]);
//            return prior;
//
//        } else {                      // otherwise use CPTs given values of parent nodes
//
//            double probability = bn.getProb(rv,assignment);
//
//
//
//            System.out.println(node);
//            return -1;
//        }

//    }


}
