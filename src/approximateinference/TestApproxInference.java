package approximateinference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class TestApproxInference {

    public static void main(String[] args){

        System.out.println("Begin testing\n");

        // make sure there are the right number of arguments
        if (args.length < 4 || args.length % 2 != 0){
            System.err.println("You did not enter the correct number of command line arguments.");
            System.err.println("Please execute this program in the following format: " +
                    "java exactinference.TestExactInference <example.xml> <Query variable> <Evidence variable> <evidence value>...");
            System.exit(0);
        }

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
        Assignment evidence = getEvidence(args,booleanDomain);
        RandomVariable queryVar = new RandomVariable(queryVarName,booleanDomain);

        // run algorithm
        int limit = 100000;
        ApproxInference approxInference = new ApproxInference(bn,queryVar,evidence);
        Distribution result = approxInference.rejectionSampling(limit);

        // output results
        for (Map.Entry<Object,Double> entry : result.entrySet()){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("\ndone");
    }

    private static Assignment getEvidence(String[] args, Domain booleanDomain) {
        Assignment evidence = new Assignment();
        for (int i = 2; i < args.length; i+= 2){
            try {
                String name = args[i];
                RandomVariable rv = new RandomVariable(name,booleanDomain);
                evidence.set(rv,Boolean.valueOf(args[i+1]));
            }
            catch (NumberFormatException | IndexOutOfBoundsException e){
                System.err.println("You did not enter valid arguments");
                System.exit(0);
            }
        }
        return evidence;
    }

    private static BayesianNetwork getBayesianNetworkFromFile(String testFile) {
        BayesianNetwork bn = null;
        String path = "src/bn/examples/";
        try {
            if (testFile.endsWith(".bif")){
                BIFParser parser = new BIFParser(new FileInputStream(path+testFile));
                bn = parser.parseNetwork();
            } else if (testFile.endsWith(".xml")){
                XMLBIFParser parser = new XMLBIFParser();
                bn = parser.readNetworkFromFile(path+testFile);
            } else {
                System.err.println("Please enter a .xml or .bif input file");
                System.exit(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return bn;
    }

}
