package exactinference;

import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.RandomVariable;
import bn.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ExactInference {
    private BayesianNetwork bn;
    private RandomVariable queryVar;
    private Assignment evidence;

    public ExactInference(BayesianNetwork bn, RandomVariable queryVar, Assignment evidence){
        this.bn = bn;
        this.queryVar = queryVar;
        this.evidence = evidence;
    }

    protected Distribution run() {
        Distribution dist = new Distribution();
        for (int i = 0; i < queryVar.getDomain().size(); i++){
            Object domain = queryVar.getDomain().get(i);
            Assignment combined = evidence.copy();
            combined.set(queryVar, domain);
            List<RandomVariable> topSorted = bn.getVariableListTopologicallySorted();
            double probability = enumerate(topSorted,combined);
            dist.put(domain, probability);
        }
        return normalize(dist);
    }

    private double enumerate(List<RandomVariable> vars, Assignment e) {

        // base case
        if (vars.isEmpty()){
            return 1.0;
        }

        RandomVariable y = vars.get(0); // get first variable

        if (inEvidence(y,e)){
            double probability = bn.getProb(y,e);
            double returnVal = enumerate(remove(y,vars),e);
            return probability * returnVal;
        }
        else {
            double sum = 0.0;
            for (Object domain : y.getDomain()){ // marginalize

                List<RandomVariable> newVars = shallowCopy(vars);
                newVars = remove(y,newVars);

                Assignment newE = e.copy();
                newE.set(y,domain);

                double probability = bn.getProb(y,newE);
                double returnVal = enumerate(newVars,newE);

                sum += probability * returnVal;
            }
            return sum;
        }
    }

    private Distribution normalize(Distribution dist) {
        dist.normalize();
        Distribution newDist = new Distribution();
        for (Map.Entry<Object, Double> entry : dist.entrySet()){
            newDist.put(entry.getKey(), Utils.round(entry.getValue(),3));
        }
        return newDist;
    }

    /**
     * Returns a shallow copy of this List<RandomVariable> instance: the
     * values themselves are not cloned.
     */
    private List<RandomVariable> shallowCopy(List<RandomVariable> old) {
        List<RandomVariable> newCopy = new ArrayList<>();
        for (RandomVariable rv : old){
            newCopy.add(rv);
        }
        return newCopy;
    }

    private Boolean inEvidence(RandomVariable y, Assignment e) {
        for (RandomVariable rv : e.variableSet()){
            if (y.getName().equals(rv.getName())){
                return true;
            }
        }
        return false;
    }

    private List<RandomVariable> remove(RandomVariable y, List<RandomVariable> vars) {
        for (int i = 0; i < vars.size(); i++){
            if (vars.get(i).getName().equals(y.getName())){
                vars.remove(i);
                return vars;
            }
        }
        return vars; // this is empty now
    }


}
