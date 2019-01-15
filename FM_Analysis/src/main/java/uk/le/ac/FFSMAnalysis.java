package uk.le.ac;

import java.io.File;
import java.util.Arrays;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.NodeWriter;
import org.prop4j.NodeWriter.Notation;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class FFSMAnalysis {

	public static void main(String[] args) {
			try {
				File f_fm = new File("Benchmark_SPL/agm/feature_models/example_agm.xml");
				File f_ffsm = new File("./Benchmark_SPL/agm/ffsms/ffsm_agm.txt");
				
				IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
				FeaturedMealy ffsm = FeaturedMealyUtils.readFeaturedMealy(f_ffsm, fm);


				IFeatureModelFactory fact = FMFactoryManager.getDefaultFactory();
				
				System.out.println(fm.toString());
				
				Feature xx = (Feature) fm.getFeature("AGM");
				
				System.out.println(xx.getProperty().getDescription());
				
				IConstraint constraint = fact.createConstraint(fm, new And(new Literal("W"),new Literal("N")));
//				IConstraint constraint = fact.createConstraint(fm, new Literal("ManPW"));
				fm.addConstraint(constraint);

				NodeWriter nodeWriter = new NodeWriter(constraint.getNode());

//				nodeWriter.setNotation(Notation.PREFIX);
				nodeWriter.setSymbols(NodeWriter.textualSymbols);
				
				NodeReader nodeReader = new NodeReader();
				nodeReader.activateTextualSymbols();
				
				Node node = nodeReader.stringToNode("(W and N)");
				nodeWriter.setNotation(Notation.PREFIX);
				System.out.println(Arrays.toString(NodeWriter.logicalSymbols));
				
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
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
	}
