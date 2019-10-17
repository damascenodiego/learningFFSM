package uk.le.ac.fts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.slf4j.LoggerFactory;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.SimpleProjection;
import be.vibes.ts.State;
import be.vibes.ts.Transition;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import be.vibes.ts.io.dot.TransitionSystemDotPrinter;
import be.vibes.ts.io.xml.TransitionSystemHandler;
import be.vibes.ts.io.xml.XmlLoaders;
import be.vibes.ts.io.xml.XmlReader;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.minimizer.hopcroft.HopcroftMinimization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class FsmFromFTS {
	
//	private static final String FM = "fm";
	private static final String FTS = "fts";
	private static final String HELP = "h";
	private static final String CONF = "conf";
	
	
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
			
			if(line.hasOption(HELP)
					|| !line.hasOption(FTS)
					|| !line.hasOption(CONF)){
				formatter.printHelp( "fts2fsm", options );
				System.exit(0);
			}
			
			String s_fts  = 
					//"Benchmark_SPL/minepump/fts/minepump.fts" ;
					line.getOptionValue(FTS);
			String s_conf = 
					//"Benchmark_SPL/minepump/products/00001.config";
					line.getOptionValue(CONF);
			
			// load the fts
			File f_fts = new File(s_fts);
			FeaturedTransitionSystem fts = XmlLoaders.loadFeaturedTransitionSystem(f_fts);
			
			// generate projection using configuration
			SimpleConfiguration product = FtsUtils.getInstance().loadConfiguration(s_conf);
			TransitionSystem lts = SimpleProjection.getInstance().project(fts, product);
			CompactMealy<String, Word<String>> mealy = FtsUtils.getInstance().lts2fsm(lts);
			mealy = HopcroftMinimization.minimizeMealy(mealy);
			
			// save fts.dot
			new FeaturedTransitionSystemDotPrinter(fts,new PrintStream(new File(s_fts.replaceFirst(".fts$", "_fts.dot")))).printDot();
			
			// save lts.dot
			String s_lts  = s_conf.replaceFirst(".config$", "_lts.dot");
			new TransitionSystemDotPrinter(lts, new PrintStream(new File(s_lts))).printDot();
			
			// save fsm.dot
			String s_fsm  = s_conf.replaceFirst(".config$", "_fsm.dot");
			BufferedWriter bw = new BufferedWriter(new FileWriter(s_fsm)); 
			GraphDOT.write(mealy, bw); 
			bw.close();
			
			// save kiss.txt
			String s_kiss = s_conf.replaceFirst(".config$", "_kiss.txt");
			String header = FtsUtils.getInstance().simpleConfigurationToString(product);
			FeaturedMealyUtils.getInstance().saveFSM_kiss(mealy, new File(s_kiss), header);
			
			// check the FSM is initially connected
			List<Word<String>> q_set = Automata.stateCover(mealy, mealy.getInputAlphabet());
			if(q_set.size() != mealy.getStates().size()) {
				throw new Exception(
						String.format(
								"Unreachable states at %s", 
								s_conf
								)
						);
			}
			//System.out.println(String.format("The FTS %s has been projected using %s",s_fts, s_conf));
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption( FTS,    true, "Featured transition system" );
		options.addOption( CONF,    true, "Product-line configuration" );
		options.addOption( HELP,  false, "Help menu" );
		return options;
	}
}
