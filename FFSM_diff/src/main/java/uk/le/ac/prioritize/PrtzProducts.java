package uk.le.ac.prioritize;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Word;
import uk.le.ac.ScorePair;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.ProductMealy;

public class PrtzProducts {
	
	private static final String FM = "fm";
	private static final String HELP = "h";
	private static final String REVERSE = "r";
	private static final String SHUFFLE = "shuffle";
	private static final String GMDP = null;
	
	
	public static void main(String[] args) {
		
		
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();
			// create the Options
			Options options = createOptions();
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse( options, args);

			if(line.hasOption(HELP) || !line.hasOption(FM)){
				formatter.printHelp( "LearnFFSM", options );
				System.exit(0);
			}
			
			// load the feature model
			File f_fm = new File(line.getOptionValue(FM));
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			
			List<ScorePair<ProductMealy<String, Word<String>>>> prodPair_lst = new ArrayList<>();
			List<ProductMealy<String, Word<String>>> prod_lst = new ArrayList<>();
			
			Scanner scanner = new Scanner(System.in);
			while(scanner.hasNextLine()) {
				//System.out.println("-->"+scanner.nextLine());
				File a_prod = new File(scanner.nextLine());
				ProductMealy<String, Word<String>> fMealy = FeaturedMealyUtils.getInstance().loadProductMachine(a_prod,fm);
				fMealy.getInfo().setProperty("abspath", a_prod.getAbsolutePath());
				fMealy.getInfo().setProperty("filename", a_prod.getName());
				prod_lst.add(fMealy);
			}
			scanner.close();
			
			List<String> allFeatures = new ArrayList<>();
			for (IFeature node : fm.getFeatures()) {
				if(node.getName().equals("TRUE")) continue;
				allFeatures.add(node.getName());
			}
			
			for (int i = 0; i < prod_lst.size()-1; i++) {
				ProductMealy<String, Word<String>> fMealy_i = prod_lst.get(i);
				List<String> conf_i = new ArrayList<>();
				for (Node n: fMealy_i.getConfiguration()) {
					if(n instanceof Literal) conf_i.add(n.toString());
				}
				
				for (int j = i+1; j < prod_lst.size(); j++) {
					ProductMealy<String, Word<String>> fMealy_j = prod_lst.get(j);
					List<String> conf_j = new ArrayList<>();
					for (Node n: fMealy_j.getConfiguration()) {
						if(n instanceof Literal) conf_j.add(n.toString());
					}
					
					double sc = clacDistance(conf_i, conf_j, allFeatures);
					prodPair_lst.add(new ScorePair<ProductMealy<String,Word<String>>>(sc, fMealy_i, fMealy_j));
				}				
			}
			
			if(line.hasOption(SHUFFLE)) Collections.shuffle(prodPair_lst);
			boolean reverse_mode = line.hasOption(REVERSE);
			
//			if(line.hasOption(GMDP)) {
//				prod_lst = prioritizeGlobalMaxDistance(prod_lst,prodPair_lst,reverse_mode);
//			}else {
				prod_lst = prioritizeLocalMaxDistance (prod_lst,prodPair_lst,reverse_mode);
//			}
			
			for (ProductMealy<String, Word<String>> productMealy : prod_lst) {
				System.out.println(productMealy.getInfo().get("abspath"));
			}
			
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	private static List<ProductMealy<String, Word<String>>> prioritizeLocalMaxDistance(
			List<ProductMealy<String, Word<String>>> prod_lst,
			List<ScorePair<ProductMealy<String, Word<String>>>> prodPair_lst, 
			boolean reverse) {
		
		int pairTot = prod_lst.size();
		List<ScorePair<ProductMealy<String, Word<String>>>> prodPair_copy = new ArrayList<>(prodPair_lst);
		
		Collections.sort(prodPair_copy);
		if(reverse) Collections.reverse(prodPair_copy);
		
		Iterator<ScorePair<ProductMealy<String, Word<String>>>> iter = prodPair_copy.iterator();
		List<ProductMealy<String, Word<String>>> result_lst = new ArrayList<>();
		Set<ProductMealy<String, Word<String>>> pairSet = new HashSet<>();
		
		while (pairTot>0){
			ScorePair<ProductMealy<String, Word<String>>> pair = iter.next();
			
			ProductMealy<String, Word<String>> ci = pair.getStatei();
			ProductMealy<String, Word<String>> cj = pair.getStatej();
			
			if(!pairSet.contains(ci) && !pairSet.contains(cj)){
				System.out.println(pair.getScore());
				result_lst.add(ci); pairSet.add(ci); --pairTot;
				result_lst.add(cj); pairSet.add(cj); --pairTot;
			}
			if(pairTot==1){
				for (ProductMealy<String, Word<String>> a_conf : prod_lst) {
					if(!pairSet.contains(a_conf)) {
						result_lst.add(a_conf); pairSet.add(a_conf); --pairTot;
					}
				}
			}
		}
		
		
		return result_lst;
	}

	private static List<ProductMealy<String, Word<String>>> prioritizeGlobalMaxDistance(
			List<ProductMealy<String, Word<String>>> prod_lst,
			List<ScorePair<ProductMealy<String, Word<String>>>> prodPair_lst, 
			boolean reverse) {
		// TODO Auto-generated method stub
		return prod_lst;
	}

	private static double clacDistance(List<String> x, List<String> y, List<String> allFeatures) {
		final Collection<String> similar = new HashSet<String>(x);
		final Collection<String> different = new HashSet<String>();

		different.addAll(x);
		different.addAll(y);
		similar.retainAll(y);

		different.removeAll(similar);

		final double s = similar.size();
		final double d = different.size();
		final double t = allFeatures.size();

		return (s + (t - (s + d))) / t;
	}
	
	private static Options createOptions() {
		Options options = new Options();
		options.addOption( FM,    true, "Feature model" );
		options.addOption( SHUFFLE,    false, "Shuffle" );
		options.addOption( REVERSE,  false, "Sort by dissimilarity" );		
		options.addOption( HELP,  false, "Help menu" );
		return options;
	}	

}
