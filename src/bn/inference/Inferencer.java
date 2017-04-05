package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Defines an abstract class meant to be the parent
 * class of inference algorithms for Bayesian Networks.
 */
public abstract class Inferencer {
	
	/**
	 * Returns the Distribution of the query RandomVariable X
	 * given evidence Assignment e, using the distribution encoded
	 * by the BayesianNetwork bn. Each subclass of Inferencer
	 * will implement this method in its own way.
	 */
	protected abstract Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e);

	/**
	 * Prints the probability distribution of the query variable,
	 * that is ultimately returned by the ask() method
	 */
	protected static void printResults(Distribution result){
		int count = 0;
		System.out.print("{");
		for (Map.Entry<Object,Double> entry : result.entrySet()){
			System.out.print(entry.getKey() + "=" + round(entry.getValue()));
			count++;
			if (count != result.size()){
				System.out.print(", ");
			}
		}
		System.out.println("}");
	}

	/**
	 * Rounds the given number to 10 digits
	 */
	private static double round(double num){
		BigDecimal bd = new BigDecimal(num);
		return bd.setScale(10, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Returns a BayesianNetwork given the name of a test file
	 */
	protected static BayesianNetwork getBayesianNetworkFromFile(String testFile) {
		BayesianNetwork bn = null;
		try {
			if (testFile.endsWith(".bif")){
				BIFParser parser = new BIFParser(new FileInputStream(testFile));
				bn = parser.parseNetwork();
			} else if (testFile.endsWith(".xml")){
				XMLBIFParser parser = new XMLBIFParser();
				bn = parser.readNetworkFromFile(testFile);
			} else {
				System.err.println("Please enter a .xml or .bif input file");
				System.exit(0);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return bn;
	}

	/**
	 * Returns an Assignment object representing the evidence of a query.
	 *
	 * Takes an int start as a parameter to determine where in args to
	 * start checking
	 */
	protected static Assignment getEvidenceFromArgs(String[] args, Domain booleanDomain, int start) {
		Assignment evidence = new Assignment();
		for (int i = start; i < args.length; i+= 2){
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

	/**
	 * Returns true if the given String representing a variable
	 * exists in the evidence
	 */
	protected Boolean inEvidence(String var, Assignment e) {
		for (RandomVariable rv : e.variableSet()){
			if (var.equals(rv.getName())){
				return true;
			}
		}
		return false;
	}



}
