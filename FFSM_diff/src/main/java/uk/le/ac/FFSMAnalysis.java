package uk.le.ac;

import java.io.File;
import java.util.Arrays;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.NodeWriter;
import org.prop4j.NodeWriter.Notation;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class FFSMAnalysis {

	public static void main(String[] args) {
		try {
			String spl_name = "bcs2";
			File f_fm = new File("Benchmark_SPL/"+spl_name+"/feature_models/example_"+spl_name+".xml");
			File f_ffsm = new File("./Benchmark_SPL/"+spl_name+ "/ffsms/ffsm_"+spl_name+".txt");
			String checking_str = null;
			
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			FeaturedMealy<String,Word<String>> ffsm = FeaturedMealyUtils.getInstance().loadFeaturedMealy(f_ffsm, fm);

			checking_str = "COMPLETENESS CHECK: ";
			boolean is_complt = FeaturedMealyUtils.getInstance().isComplete(ffsm);
			if(is_complt) {
				System.out.println(checking_str + "OK");
			}else System.err.println(checking_str +"NOK!");

			checking_str = ("DETERMINISTIC CHECK: ");
			boolean is_determ = FeaturedMealyUtils.getInstance().isDeterministic(ffsm);
			if(is_determ){
				System.out.println(checking_str + "OK");
				checking_str = ("INIT. CONNECTED CHECK: ");
				boolean is_iniCon = FeaturedMealyUtils.getInstance().isInitiallyConnected(ffsm);	
				if(is_iniCon){
					System.out.println(checking_str + "OK");
					checking_str = ("MINIMAL CHECK: ");
					boolean is_minmal = FeaturedMealyUtils.getInstance().isMinimal(ffsm);
					if(is_minmal){					
						System.out.println(checking_str + "OK");
					}else System.err.println(checking_str +"NOK!");
				}else System.err.println(checking_str +"NOK!");
			}else System.err.println(checking_str +"NOK!");


//			FFSMVisualizationHelper<String,Word<String>> ffsm_viz = new FFSMVisualizationHelper<>(ffsm);
//			ffsm_viz.setPlotSelfloops(false);
//			Visualization.visualize(ffsm, ffsm.getInputAlphabet(),ffsm_viz);
			
			if(spl_name.equals("agm")) {
				testMethods(fm,ffsm);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private static void testMethods(IFeatureModel fm, FeaturedMealy ffsm) throws TimeoutException {
		IFeatureModelFactory fact = FMFactoryManager.getDefaultFactory();

		System.out.println(fm.toString());

		Feature xx = (Feature) fm.getFeature("AGM");

		System.out.println(xx.getProperty().getDescription());

		IConstraint constraint = fact.createConstraint(fm, new And(new Literal("W"),new Literal("N")));
		//		IConstraint constraint = fact.createConstraint(fm, new Literal("ManPW"));
		fm.addConstraint(constraint);

		NodeWriter nodeWriter = new NodeWriter(constraint.getNode());

		//		nodeWriter.setNotation(Notation.PREFIX);setpl
		nodeWriter.setSymbols(NodeWriter.textualSymbols);

		NodeReader nodeReader = new NodeReader();
		nodeReader.activateTextualSymbols();

		Node node = nodeReader.stringToNode("(W and N)");
		nodeWriter.setNotation(Notation.PREFIX);
		System.out.println(Arrays.toString(NodeWriter.logicalSymbols));
		System.out.println(node.toString());

		nodeWriter.setEnforceBrackets(true);
		nodeWriter.setEnquoteWhitespace(true);

		System.out.println(nodeWriter.nodeToString());

		Configuration conf;

		conf = new Configuration(fm);
		System.out.println(conf.number());
		System.out.println(conf.getSolutions(100));

		fm.removeConstraint(constraint);
		conf = new Configuration(fm);
		System.out.println(conf.number());
		System.out.println(conf.getSolutions(100));

	}
}
