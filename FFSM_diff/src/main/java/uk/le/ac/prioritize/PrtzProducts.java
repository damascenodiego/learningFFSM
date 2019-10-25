package uk.le.ac.prioritize;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Word;
import uk.le.ac.ScorePair;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.ProductMealy;

public class PrtzProducts {
	
	private static final String FM = "fm";
	private static final String HELP = "h";
	//private static final String TYPE = "t";
	
	
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
			
			//// reading p_fsm
			//boolean is_fMealy = true;
			//if(line.hasOption(TYPE)) is_fMealy = false;
			
			Scanner scanner = new Scanner(System.in);
			
			
			List<ScorePair<ProductMealy<String, Word<String>>>> prodPair_lst = new ArrayList<>();
			List<ProductMealy<String, Word<String>>> prod_lst = new ArrayList<>();
			
			//int totConfig = Integer.valueOf(scanner.nextLine());
			//for (int i = 0; i < totConfig; i++) {
			while(scanner.hasNextLine()) {
				//System.out.println("-->"+scanner.nextLine());
				File a_prod = new File(scanner.nextLine());
				ProductMealy<String, Word<String>> fMealy = FeaturedMealyUtils.getInstance().loadProductMachine(a_prod,fm);
				prod_lst.add(fMealy);
			}
			
			for (int i = 0; i < prod_lst.size()-1; i++) {
				for (int j = i+1; j < prod_lst.size(); j++) {
					ProductMealy<String, Word<String>> fMealy_i = prod_lst.get(i);
					ProductMealy<String, Word<String>> fMealy_j = prod_lst.get(j);
					
					List<String> conf_i = new ArrayList<>();
					List<String> conf_j = new ArrayList<>();
					List<String> allFeatures = new ArrayList<>();
					
					for (Node n: fMealy_i.getConfiguration()) {
						if(n instanceof Literal) conf_i.add(n.toString());
					}
					
					for (Node n: fMealy_j.getConfiguration()) {
						if(n instanceof Literal) conf_j.add(n.toString());
					}
					for (IFeature node : fm.getFeatures()) {
						if(node.getName().equals("TRUE")) continue;
						allFeatures.add(node.getName());
					}
					
					
					double sc = clacDistance(conf_i, conf_j, allFeatures);
					prodPair_lst.add(new ScorePair<ProductMealy<String,Word<String>>>(sc, fMealy_i, fMealy_j));
				}
				
				Collections.sort(prodPair_lst);
				System.out.println(prodPair_lst);
			}
			
			
			
			scanner.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
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
//		options.addOption( TYPE,  false, "Product file type as configuration" );		
		options.addOption( HELP,  false, "Help menu" );
		return options;
	}	

}
