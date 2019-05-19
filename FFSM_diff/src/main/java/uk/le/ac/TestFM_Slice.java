package uk.le.ac;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Or;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.Constraint;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator.CNFType;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator.ModelType;
import de.ovgu.featureide.fm.core.editing.FeatureModelToNodeTraceModel;
import de.ovgu.featureide.fm.core.explanations.fm.FeatureModelExplanationCreator;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.SliceFeatureModelJob;
import de.ovgu.featureide.fm.core.job.SliceFeatureModelJob.Arguments;
import de.ovgu.featureide.fm.core.job.monitor.NullMonitor;
import de.ovgu.featureide.fm.core.job.util.JobArguments;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class TestFM_Slice {
	
	public static void main(String[] args) {
		
		try {
			File f_fm = new File("/home/cdnd1/git/fm_analysis/FM_Analysis/Benchmark_SPL/agm/model.xml");
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			
			File f_ref  = new File("/home/cdnd1/git/fm_analysis/FM_Analysis/Benchmark_SPL/agm/learnt/ffsm_agm_1_2_3_4_5_6.txt");
			FeaturedMealy<String, Word<String>> ffsm = FeaturedMealyUtils.getInstance().loadFeaturedMealy (f_ref,fm);
			
			
//			List<String> excludedFeatures = new ArrayList<>();
//			for (IFeature feat : fm.getAnalyser().getCoreFeatures()) {
//				excludedFeatures.add(feat.getName());
//			}
//			
//			AdvancedNodeCreator nc = new AdvancedNodeCreator(fm,excludedFeatures);
//			nc.setCnfType(CNFType.Regular);
//			nc.setIncludeBooleanValues(false);
//			nc.setOptionalRoot(false);
//			Node node = nc.createNodes();
			
			List<String> listOfFeatures = new ArrayList<>();
			listOfFeatures.add("B");
			listOfFeatures.add("S");

//			Arguments arguments = new SliceFeatureModelJob.Arguments(f_fm.toPath(), fm, listOfFeatures, true);
//			final SliceFeatureModelJob slice = new SliceFeatureModelJob(arguments);
//			final IFeatureModel slicedModel = slice.sliceModel(fm, listOfFeatures, new NullMonitor()).clone(); // returns new feature model
//			
//			List<String> excludedFeatures = new ArrayList<>();
//			excludedFeatures.add("__root__");
//			for (IFeature feat : slicedModel.getFeatures()) {
//				if(feat.getName().startsWith("_Abstract")) excludedFeatures.add(feat.getName());
//			}
			
			AdvancedNodeCreator nc = new AdvancedNodeCreator(fm);
			Node fm2 = nc.createConstraintNode(new Constraint(fm, new And(listOfFeatures)));
			nc.setCnfType(CNFType.Regular);
			nc.setModelType(ModelType.OnlyStructure);
			nc.setIncludeBooleanValues(false);
			Node node = nc.createNodes();
			
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
