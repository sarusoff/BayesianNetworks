package bn.inference;

import bn.core.*;

import java.util.ArrayList;
import java.util.List;


public class ExactInferencer extends Inferencer {

    /**
     * Parses the command line arguments to perform a test
     */
    public static void main(String[] args){

        ensureEnoughArgs(args);

        // read command line arguments
        String testFile = args[0];
        String queryVarName =  args[1];

        // get BayesianNetwork from file
        BayesianNetwork bn = getBayesianNetworkFromFile(testFile);

        // create boolean domain
        Domain booleanDomain = new Domain();
        booleanDomain.add("true");
        booleanDomain.add("false");

        // get evidence and query variable
        Assignment e = getEvidenceFromArgs(args,booleanDomain,2);
        RandomVariable X = new RandomVariable(queryVarName,booleanDomain);

        // run algorithm
        ExactInferencer exactInference = new ExactInferencer();
        Distribution result = exactInference.ask(bn,X,e);

        // output distribution
        printResults(result);

    }

    /**
     * Checks to make sure there are a valid number of arguments.
     * Exits the program if an illogical number of arguments is found.
     */
    protected static void ensureEnoughArgs(String[] args){
        if (args.length < 4 || args.length % 2 != 0){
            System.err.println("You did not enter the correct number of command line arguments.");
            System.err.println("Please execute this program in the following format: " +
                    "java bn.inference.TestExactInference <samples> <example.xml> <Query variable> <Evidence variable> <evidence value>...");
            System.exit(0);
        }
    }


    @Override
    protected Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e) {
        Distribution dist = new Distribution();
        for (int i = 0; i < X.getDomain().size(); i++){
            Object domain = X.getDomain().get(i);
            Assignment combined = e.copy();
            combined.set(X, domain);
            List<RandomVariable> topSorted = bn.getVariableListTopologicallySorted();
            double probability = enumerate(bn,topSorted,combined);
            dist.put(domain, probability);
        }
        dist.normalize();
        return dist;
    }

    /**
     * Returns the calculated probability of the given assignment,
     * by summing together the products of conditional probabilities.
     * Marginalizes over unobserved variables in the else statement.
     */
    private double enumerate(BayesianNetwork bn, List<RandomVariable> vars, Assignment e) {

        // base case
        if (vars.isEmpty()){
            return 1.0;
        }

        RandomVariable y = vars.get(0); // get first variable

        if (inEvidence(y.getName(),e)){
            double probability = bn.getProb(y,e);
            double returnVal = enumerate(bn, remove(y,vars),e);
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
                double returnVal = enumerate(bn, newVars,newE);

                sum += probability * returnVal;
            }
            return sum;
        }
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

    /**
     * Returns vars with all RandomVariables except the variable passed in.
     */
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
