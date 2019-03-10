package uk.le.ac;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.IConfigurableFSM;
import uk.le.ac.ffsm.ProductMealy;
import uk.le.ac.ffsm.SimplifiedTransition;

public class LearnFFSM {

	private static final String FM = "fm";
	private static final String UPDT = "updt";
	private static final String FREF = "fref";
	private static final String MREF = "mref";
	private static final String HELP = "h";
	private static final String K_VALUE = "k";
	private static final String OUT = "out";
	private static final String CLEAN = "clean";

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

			if(line.hasOption(HELP)){
				formatter.printHelp( "LearnFFSM", options );
				System.exit(0);
			}
			
			File f_fm = new File(line.getOptionValue(FM));
			
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			
			IConfigurableFSM<String, Word<String>> ref  = null;
			IConfigurableFSM<String, Word<String>> updt = null;
			FeaturedMealy<String, Word<String>> ffsm = null;
			File f_ref = null, f_upd = null;
			
			if(line.hasOption(CLEAN)) {
				f_ref  = new File(line.getOptionValue(CLEAN));
				ffsm  = FeaturedMealyUtils.getInstance().loadFeaturedMealy  (f_ref,fm);
				FeaturedMealyUtils.getInstance().cleanFeaturedMealy(ffsm,fm);
				
				FeaturedMealyUtils.getInstance().saveFFSM_kiss(ffsm, new File(f_ref.getParent(),f_ref.getName()+".clean"),false);
				FeaturedMealyUtils.getInstance().saveFFSM(ffsm, new File(f_ref.getParent(),f_ref.getName()+".clean"+".dot"),false);
				System.exit(0);
			}
			
			if(line.hasOption(FREF)){
				f_ref = new File(line.getOptionValue(FREF));
				f_upd = new File(line.getOptionValue(UPDT));
				ref  = FeaturedMealyUtils.getInstance().loadFeaturedMealy (f_ref,fm);
				updt = FeaturedMealyUtils.getInstance().loadProductMachine(f_upd,fm);
			}

			if(line.hasOption(MREF)){
				f_ref  = new File(line.getOptionValue(MREF));
				f_upd = new File(line.getOptionValue(UPDT));
				ref  = FeaturedMealyUtils.getInstance().loadProductMachine(f_ref,fm);
				updt = FeaturedMealyUtils.getInstance().loadProductMachine(f_upd,fm);
			}
			
			double K = Double.valueOf(line.getOptionValue(K_VALUE,"0.50"));
			
			RealVector pairsToScore = computeScores(ref,updt,K);
			Set<List<Integer>> kPairs = identifyLandmaks(pairsToScore,ref,updt);
			Set<List<Integer>> nPairs = surr(kPairs, ref,updt);
			Map<Integer,Set<Integer>> checked = new HashMap<>();
			checked.put(0, new HashSet<>());
			checked.get(0).add(0);
			
			checked.put(1, new HashSet<>());
			checked.get(1).add(0);
			
			removeConflicts(nPairs,checked);
			
			
			while (!nPairs.isEmpty()) {
				while (!nPairs.isEmpty()) {
					List<Integer> A_B = pickHighest(nPairs,pairsToScore,ref,updt);
					kPairs.add(A_B);
					checked.get(0).add(A_B.get(0));
					checked.get(1).add(A_B.get(1));
					removeConflicts(nPairs,checked);
				}
				nPairs = surr(kPairs,ref,updt);
				removeConflicts(nPairs,checked);
			}
			
			System.out.println("Common states found!");
			kPairs.forEach(pair ->System.out.println(pair.get(0)+","+pair.get(1)));			
			
			if(line.hasOption(FREF)){
				ffsm = makeFFSM((FeaturedMealy<String, Word<String>>)ref,(ProductMealy<String, Word<String>>)updt,kPairs,fm);
			}else {
				ffsm = makeFFSM((ProductMealy<String, Word<String>>)ref,(ProductMealy<String, Word<String>>)updt,kPairs,fm);
			}
			
			FeaturedMealyUtils.getInstance().saveFFSM_kiss(ffsm, new File(line.getOptionValue(OUT)));
			FeaturedMealyUtils.getInstance().saveFFSM(ffsm, new File(line.getOptionValue(OUT)+".dot"));
			
			if(line.hasOption(MREF)){
				Set<String> refFeatures = new HashSet<>();
				Set<String> updtFeatures = new HashSet<>();
				
				for (Node conf : ref.getConfiguration()) {
					refFeatures.add(conf.toString());
				}
				
				for (Node conf : updt.getConfiguration()) {
					updtFeatures.add(conf.toString());
				}
				
				refFeatures.retainAll(updtFeatures);
				
				System.out.println("Reference/Updated/StatesReference/StatesUpdated/StatesFFSM/CommonFeatures:"
					    +f_ref.getName()
					+"/"+f_upd.getName()
					+"/"+ref.getStateIDs().size()
					+"/"+updt.getStateIDs().size()
					+"/"+ffsm.getStateIDs().size()
					+"/"+refFeatures.size());	
			}
			
			if(line.hasOption(FREF)){
				System.out.println("Reference/Updated/StatesReference/StatesUpdated/StatesFFSM:"
					    +f_ref.getName()
					+"/"+f_upd.getName()
					+"/"+ref.getStateIDs().size()
					+"/"+updt.getStateIDs().size()
					+"/"+ffsm.getStateIDs().size());
				
			}

			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static Node removeCoreFeatures(IFeatureModel fm, Or condition) {
		List<IFeature> coreFeatures = new ArrayList<>(fm.getAnalyser().getCoreFeatures());
		for (IFeature iFeat: coreFeatures) {
			if(iFeat.getName().equals("TRUE")) {
				coreFeatures.remove(iFeat);
				break;
			}
			
		}
		Set<String> featNames = new HashSet<>();
		coreFeatures.forEach(aFeat -> featNames.add(aFeat.getName()));
		
		Set<Node> or_set = new LinkedHashSet<>();
		for (Node and_child : condition.getChildren()) {
			if(and_child instanceof And) {
				Set<Node> and_set = removeFeaturesByName(featNames,(And)and_child); 
				or_set.add(new And(and_set));
			}
		}
		return new Or(or_set);
	}
	
	private static Node removeCoreFeatures(IFeatureModel fm, And condition) {
		List<IFeature> coreFeatures = new ArrayList<>(fm.getAnalyser().getCoreFeatures());
		for (IFeature iFeat: coreFeatures) {
			if(iFeat.getName().equals("TRUE")) {
				coreFeatures.remove(iFeat);
				break;
			}	
		}
		Set<String> featNames = new HashSet<>();
		coreFeatures.forEach(aFeat -> featNames.add(aFeat.getName()));
		Set<Node> and_set = removeFeaturesByName(featNames,condition);
		return new And(and_set);
	}
	
	private static Set<Node> removeFeaturesByName(Set<String> featNames, And and_child) {
		Set<Node> and_set =  new LinkedHashSet<>();
		for (Node node : and_child.getChildren()) {
			Node aLiteral;
			if (node instanceof Not) {
				aLiteral = node.getChildren()[0];
				if(!featNames.contains(aLiteral.toString())) {
					and_set.add(new Not(aLiteral));
				}
			}else{
				aLiteral  = node;
				if(!featNames.contains(aLiteral.toString())) {
					and_set.add(aLiteral);
				}
			} 
		}
		return and_set;
	}

	private static <I> FeaturedMealy<String, Word<String>> makeFFSM(ProductMealy<String, Word<String>> fsm1, ProductMealy<String, Word<String>> fsm2, Set<List<Integer>> kPairs, IFeatureModel fm) {
		Map<Integer,Integer> kPairs_nfa0to1 = new HashMap<>();
		
		for (List<Integer> pair : kPairs) {
			kPairs_nfa0to1.put(pair.get(0), pair.get(1));
		}
		
		Map<Integer,ConditionalState<ConditionalTransition<String, Word<String>>>> nfa0ToFFSM = new HashMap<>();
		Map<Integer,ConditionalState<ConditionalTransition<String, Word<String>>>> nfa1ToFFSM = new HashMap<>();
		
		Alphabet<String> alphabet = Alphabets.fromCollection(createAlphabet(fsm1,fsm2));
		FeaturedMealy<String, Word<String>> ffsm = new FeaturedMealy<>(alphabet,fm);
		ConditionalState<ConditionalTransition<String, Word<String>>> s0 = ffsm.addInitialState();

		nfa0ToFFSM.put(fsm1.getInitialState(), s0);
		nfa1ToFFSM.put(fsm2.getInitialState(), s0);
		
		Set<Node> feat0 = new LinkedHashSet<>(fsm1.getConfiguration());
		Set<Node> feat1 = new LinkedHashSet<>(fsm2.getConfiguration());
		
		feat0.removeAll(fsm2.getConfiguration());
		feat1.removeAll(fsm1.getConfiguration());
		
		Set<Node> feat_common = new LinkedHashSet<>(fsm1.getConfiguration());
		feat_common.retainAll(fsm2.getConfiguration());
		
		s0.setCondition(new Or( 
				removeCoreFeatures(fm, new And(fsm1.getConfiguration())),
				removeCoreFeatures(fm, new And(fsm2.getConfiguration()))
				));
		//s0.setCondition(removeCoreFeatures(fm, FeaturedMealyUtils.getInstance().makeConditionAsOr(s0.getCondition())));
		
		
		for (Integer si : fsm1.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = fsm1.getSimplifiedTransitions(si);
			if(!nfa0ToFFSM.containsKey(si)){
				ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
				newState.setCondition(new Or(removeCoreFeatures(fm, new And(fsm1.getConfiguration()))));
				nfa0ToFFSM.put(si, newState);
				if(kPairs_nfa0to1.containsKey(si)) {
					nfa1ToFFSM.put(kPairs_nfa0to1.get(si), newState);
				}
			}
			ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_si = nfa0ToFFSM.get(si);
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa0ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
						newState.setCondition(new Or(removeCoreFeatures(fm, new And(fsm1.getConfiguration()))));
						nfa0ToFFSM.put(sj, newState);
						if(kPairs_nfa0to1.containsKey(sj)) {
							nfa1ToFFSM.put(kPairs_nfa0to1.get(sj), newState);
						}
					}
					String ffsm_in  = simpleTr.getIn();
					Word<String> ffsm_out = simpleTr.getOut();
					ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_sj = nfa0ToFFSM.get(sj);
					
					Node cond = new Or(removeCoreFeatures(fm, new And(fsm1.getConfiguration())));
					ConditionalTransition<String, Word<String>> newTr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, cond);
					//ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, new And("A"));
					
				}
			}
		}
		
		for (Integer si : fsm2.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = fsm2.getSimplifiedTransitions(si);
			if(!nfa1ToFFSM.containsKey(si)){
				ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
				newState.setCondition(new Or(removeCoreFeatures(fm, new And(fsm2.getConfiguration()))));
				nfa1ToFFSM.put(si, newState);
			}
			ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_si = nfa1ToFFSM.get(si);
			
			Node a_cons = removeCoreFeatures(fm, new And(fsm2.getConfiguration()));
			Set<Node> the_set = new HashSet<>();
			for (Node node : ffsm_si.getCondition().getChildren())  the_set.add(node);
			if(!the_set.contains(a_cons)) {
				the_set.add(a_cons);
				ffsm_si.setCondition(new Or(the_set));
			}
			
			
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa1ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
						newState.setCondition(removeCoreFeatures(fm, new Or(new And(fsm2.getConfiguration()))));
						nfa1ToFFSM.put(sj, newState);
					}
					String ffsm_in  = simpleTr.getIn();
					Word<String> ffsm_out = simpleTr.getOut();
					ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_sj = nfa1ToFFSM.get(sj);
					
					a_cons = removeCoreFeatures(fm, new And(fsm2.getConfiguration()));
					the_set = new HashSet<>();
					for (Node node : ffsm_sj.getCondition().getChildren()) the_set.add(node);
					if(!the_set.contains(a_cons)) {
						the_set.add(a_cons);
						ffsm_sj.setCondition(new Or(the_set));
					}
					
					Map<String, List<SimplifiedTransition<String, Word<String>>>> trs_matching = ffsm.getSimplifiedTransitions(ffsm_si.getId(), ffsm_in, ffsm_out, ffsm_sj.getId());
					ConditionalTransition<String, Word<String>> a_tr = null;
					if(trs_matching.isEmpty()) {
						Node cond = new Or(removeCoreFeatures(fm, new And(fsm2.getConfiguration())));
						a_tr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, cond);
					}else {
						SimplifiedTransition<String, Word<String>> tr = new ArrayList<>(trs_matching.values()).get(0).get(0);
						a_tr = (ConditionalTransition<String, Word<String>>) tr.getTransition();
						a_cons = removeCoreFeatures(fm, new And(fsm2.getConfiguration()));
						the_set = new HashSet<>();
						for (Node node : a_tr.getCondition().getChildren()) the_set.add(node);
						if(!the_set.contains(a_cons)) {
							the_set.add(a_cons);
							a_tr.setCondition(new Or(the_set));
						}
						
					}
					
				}
			}
		}
		
		return ffsm;
	}
	


	private static <I> FeaturedMealy<String, Word<String>> makeFFSM(FeaturedMealy<String, Word<String>> pffsm, ProductMealy<String, Word<String>> fsm, Set<List<Integer>> kPairs, IFeatureModel fm) {
		Map<Integer,Integer> kPairs_nfa0to1 = new HashMap<>();
		
		for (List<Integer> pair : kPairs) {
			kPairs_nfa0to1.put(pair.get(0), pair.get(1));
		}
		
		Map<Integer,ConditionalState<ConditionalTransition<String, Word<String>>>> nfa0ToFFSM = new HashMap<>();
		Map<Integer,ConditionalState<ConditionalTransition<String, Word<String>>>> nfa1ToFFSM = new HashMap<>();
		
		Set<String> abcSet = new LinkedHashSet<>();
		
		abcSet.addAll(pffsm.getInputAlphabet());
		abcSet.addAll(fsm.getInputAlphabet());
		
		List<String> abc = new ArrayList<>(abcSet);
		Collections.sort(abc);
		
		Alphabet<String> alphabet = Alphabets.fromCollection(abc);
		FeaturedMealy<String, Word<String>> ffsm = new FeaturedMealy<>(alphabet,fm);
		ConditionalState<ConditionalTransition<String, Word<String>>> s0 = ffsm.addInitialState();

		nfa0ToFFSM.put(pffsm.getInitialStateIndex(), s0);
		nfa1ToFFSM.put(fsm.getInitialState(), s0);

		Node newConfig = removeCoreFeatures(fm, new And(fsm.getConfiguration()));
		
		Set<Node> ands = new LinkedHashSet<>(FeaturedMealyUtils.getInstance().getAllAnds(pffsm.getInitialState().getCondition()));
		
		if(!ands.contains(newConfig)) {
			ands.add(newConfig);
		}
		s0.setCondition(removeCoreFeatures(fm, (Or) new Or(ands)));
		
		for (ConditionalState<ConditionalTransition<String, Word<String>>> si : pffsm.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = pffsm.getSimplifiedTransitions(si.getId());
			if(!nfa0ToFFSM.containsKey(si.getId())){
				ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
				newState.setCondition(removeCoreFeatures(fm, new Or(FeaturedMealyUtils.getInstance().getAllAnds(si.getCondition()))));
				nfa0ToFFSM.put(si.getId(), newState);
				if(kPairs_nfa0to1.containsKey(si.getId())) {
					nfa1ToFFSM.put(kPairs_nfa0to1.get(si.getId()), newState);
				}
			}
			ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_si = nfa0ToFFSM.get(si.getId());
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa0ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
						newState.setCondition(removeCoreFeatures(fm, new Or(FeaturedMealyUtils.getInstance().getAllAnds(pffsm.getState(sj).getCondition()))));
						nfa0ToFFSM.put(sj, newState);
						if(kPairs_nfa0to1.containsKey(sj)) {
							nfa1ToFFSM.put(kPairs_nfa0to1.get(sj), newState);
						}
					}
					String ffsm_in  = simpleTr.getIn();
					Word<String> ffsm_out = simpleTr.getOut();
					ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_sj = nfa0ToFFSM.get(sj);

					ConditionalTransition<String, Word<String>> a_tr = (ConditionalTransition<String, Word<String>>)simpleTr.getTransition();
					Node cond = removeCoreFeatures(fm, new Or(FeaturedMealyUtils.getInstance().getAllAnds(a_tr.getCondition())));
					ConditionalTransition<String, Word<String>> newTr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, cond);
					//ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, new And("A"));
					
				}
			}
		}
		
		for (Integer si : fsm.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = fsm.getSimplifiedTransitions(si);
			if(!nfa1ToFFSM.containsKey(si)){
				ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
				newState.setCondition(new Or(removeCoreFeatures(fm, new And(fsm.getConfiguration()))));
				nfa1ToFFSM.put(si, newState);
			}
			ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_si = nfa1ToFFSM.get(si);
			
			Node a_cons = removeCoreFeatures(fm, new And(fsm.getConfiguration()));
			Set<Node> the_set = new LinkedHashSet<>(FeaturedMealyUtils.getInstance().getAllAnds(ffsm_si.getCondition()));
			if(!the_set.contains(a_cons)) {
				the_set.add(a_cons);
				ffsm_si.setCondition(new Or(the_set));
			}
			
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa1ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, Word<String>>> newState = ffsm.addState();
						newState.setCondition(new Or(removeCoreFeatures(fm, new And(fsm.getConfiguration()))));
						nfa1ToFFSM.put(sj, newState);
					}
					String ffsm_in  = simpleTr.getIn();
					Word<String> ffsm_out = simpleTr.getOut();
					ConditionalState<ConditionalTransition<String, Word<String>>> ffsm_sj = nfa1ToFFSM.get(sj);
					
					a_cons = removeCoreFeatures(fm, new And(fsm.getConfiguration()));
					the_set = new LinkedHashSet<>(FeaturedMealyUtils.getInstance().getAllAnds(ffsm_sj.getCondition()));
					if(!the_set.contains(a_cons)) {
						the_set.add(a_cons);
						ffsm_sj.setCondition(new Or(the_set));
					}
					
					Map<String, List<SimplifiedTransition<String, Word<String>>>> trs_matching = ffsm.getSimplifiedTransitions(ffsm_si.getId(), ffsm_in, ffsm_out, ffsm_sj.getId());
					ConditionalTransition<String, Word<String>> a_tr = null;
					if(trs_matching.isEmpty()) {
						Node cond = new Or(removeCoreFeatures(fm, new And(fsm.getConfiguration())));
						a_tr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, cond);
					}else {
						SimplifiedTransition<String, Word<String>> tr = new ArrayList<>(trs_matching.values()).get(0).get(0);
						a_tr = (ConditionalTransition<String, Word<String>>) tr.getTransition();
						a_cons = removeCoreFeatures(fm, new And(fsm.getConfiguration()));
						the_set = new LinkedHashSet<>(FeaturedMealyUtils.getInstance().getAllAnds(a_tr.getCondition()));
						if(!the_set.contains(a_cons)) {
							the_set.add(a_cons);
							a_tr.setCondition(new Or(the_set));
						}
						
					}
					
				}
			}
		}
		
		return ffsm;
	}



	private static void updateCondition(ConditionalTransition<String, String> tr,
			ProductMealy<String, Word<String>> fsmRef, ProductMealy<String, Word<String>> fsmUpdt) {
		Set<Node> featRefOnly = new LinkedHashSet<>(fsmRef.getConfiguration());
		featRefOnly.removeAll(fsmUpdt.getConfiguration());
		
		Set<Node> featUpdtOnly = new LinkedHashSet<>(fsmUpdt.getConfiguration());
		featUpdtOnly.removeAll(fsmRef.getConfiguration());
		
		if(tr.getCondition()==null) {
			tr.setCondition(new And(featRefOnly));
		}else {
			tr.setCondition(new Or(tr.getCondition(),new And(featRefOnly)));
		}
	}

	private static void updateCondition(ConditionalState<ConditionalTransition<String, String>> newState,
			ProductMealy<String, Word<String>> fsmRef, ProductMealy<String, Word<String>> fsmUpdt) {
		Set<Node> featRefOnly = new LinkedHashSet<>(fsmRef.getConfiguration());
		featRefOnly.removeAll(fsmUpdt.getConfiguration());
		
		Set<Node> featUpdtOnly = new LinkedHashSet<>(fsmUpdt.getConfiguration());
		featUpdtOnly.removeAll(fsmRef.getConfiguration());
		
		if(newState.getCondition()==null) {
			newState.setCondition(new And(featRefOnly));
		}else {
			newState.setCondition(new Or(newState.getCondition(),new And(featRefOnly)));
		}
	}

	private static Collection<String> createAlphabet(
			ProductMealy<String, Word<String>> fsm1, 
			ProductMealy<String, Word<String>> fsm2) {
		Set<String> abcSet = new HashSet<>();
		
		abcSet.addAll(fsm1.getInputAlphabet());
		abcSet.addAll(fsm2.getInputAlphabet());
		
		List<String> abc = new ArrayList<>(abcSet);
		Collections.sort(abc);
		return abc;
	}

	
	private static void removeConflicts(Set<List<Integer>> nPairs, Map<Integer, Set<Integer>> checked) {
		Set<List<Integer>> toRemove = new HashSet<>();
		for (List<Integer> pair : nPairs) {
			if (checked.get(0).contains(pair.get(0)) || checked.get(1).contains(pair.get(1))) {
				toRemove.add(pair);
			}
		}
		nPairs.removeAll(toRemove);
		
	}

	private static Set<List<Integer>> identifyLandmaks(RealVector pairsToScore, IConfigurableFSM<String, Word<String>> fsm1, IConfigurableFSM<String, Word<String>> fsm2) {
		Set<List<Integer>> outPairs = new LinkedHashSet<>();
		List<Integer> kPairs = new ArrayList<>();
		kPairs.add(fsm1.getInitialStateIndex());
		kPairs.add(fsm2.getInitialStateIndex());
		outPairs.add(kPairs);
		return outPairs;
	}

	private static Set<List<Integer>> surr(Set<List<Integer>> kPairs, IConfigurableFSM<String, Word<String>> fsm0, IConfigurableFSM<String, Word<String>> fsm1) {
		Set<List<Integer>> outPairs = new LinkedHashSet<>();
		Set<List<Integer>> outPairsIn = new LinkedHashSet<>();
		Set<List<Integer>> outPairsOut = new LinkedHashSet<>();
		
		for (List<Integer> pair : kPairs) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trs0 = fsm0.getSimplifiedTransitions(pair.get(0));
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trs1 = fsm1.getSimplifiedTransitions(pair.get(1));
			Set<String> commonInputs = new LinkedHashSet<>(trs0.keySet());
			commonInputs.retainAll(trs1.keySet());
			for (String input : commonInputs) {
				for (SimplifiedTransition<String, Word<String>> s0 : trs0.get(input)) {
					for (SimplifiedTransition<String, Word<String>> s1 : trs1.get(input)) {
						if(!s0.getOut().equals(s1.getOut())) continue;
						List<Integer> nPairs = new ArrayList<>();
						nPairs.add(s0.getSj());
						nPairs.add(s1.getSj());
						outPairsOut.add(nPairs);
					}	
				}
			}
		}
		for (List<Integer> pair : kPairs) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trs0 = fsm0.getSimplifiedTransitionsIn(pair.get(0));
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trs1 = fsm1.getSimplifiedTransitionsIn(pair.get(1));
			Set<String> commonInputs = new LinkedHashSet<>(trs0.keySet());
			commonInputs.retainAll(trs1.keySet());
			for (String input : commonInputs) {
				for (SimplifiedTransition<String, Word<String>> s0 : trs0.get(input)) {
					for (SimplifiedTransition<String, Word<String>> s1 : trs1.get(input)) {
						if(!s0.getOut().equals(s1.getOut())) continue;
						List<Integer> nPairs = new ArrayList<>();
						nPairs.add(s0.getSi());
						nPairs.add(s1.getSi());
						outPairsIn.add(nPairs);
					}	
				}
			}
		}

		outPairs.addAll(outPairsIn);
		outPairs.addAll(outPairsOut);
		outPairs.removeAll(kPairs);
		return outPairs;
	}

	private static RealVector computeScores(IConfigurableFSM<String, Word<String>> fsm1, IConfigurableFSM<String, Word<String>> fsm2, double K) {
		
		List<Integer> lst_nfa1 = new ArrayList<>(fsm1.getStateIDs());
		List<Integer> lst_nfa2 = new ArrayList<>(fsm2.getStateIDs());
		
		
		//////////////////////////////////////////////////////
		// Solving linear equation for outgoing transitions //
		//////////////////////////////////////////////////////
		RealMatrix coeffOut = new Array2DRowRealMatrix(lst_nfa1.size()*lst_nfa2.size(),lst_nfa1.size()*lst_nfa2.size());
		RealVector constOut = new ArrayRealVector(lst_nfa1.size()*lst_nfa2.size());
		for (int i1 = 0; i1 < lst_nfa1.size(); i1++) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trsIn1 = fsm1.getSimplifiedTransitions(lst_nfa1.get(i1));
			Map<String,List<SimplifiedTransition<String, Word<String>>>> sigma_i1 = new LinkedHashMap<>();
			trsIn1.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i1.putIfAbsent(sympTr.getIn()+"\t/\t"+sympTr.getOut(),new ArrayList<>())));
			trsIn1.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i1.get(sympTr.getIn()+"\t/\t"+sympTr.getOut()).add(sympTr)));
			
			for (int i2 = 0; i2 < lst_nfa2.size(); i2++) {
				int rowIdx = lst_nfa1.get(i1)*lst_nfa2.size()+lst_nfa2.get(i2);
				
				Map<String,List<SimplifiedTransition<String, Word<String>>>> sigma_i2 = new LinkedHashMap<>();
				Map<String, List<SimplifiedTransition<String, Word<String>>>> trsIn2 = fsm2.getSimplifiedTransitions(lst_nfa2.get(i2));
				trsIn2.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i2.putIfAbsent(sympTr.getIn()+"\t/\t"+sympTr.getOut(),new ArrayList<>())));
				trsIn2.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i2.get(sympTr.getIn()+"\t/\t"+sympTr.getOut()).add(sympTr)));
				
				Set<String> sigma_i1_min_i2 = new HashSet<>(sigma_i1.keySet()); sigma_i1_min_i2.removeAll(sigma_i2.keySet());
				Set<String> sigma_i2_min_i1 = new HashSet<>(sigma_i2.keySet()); sigma_i2_min_i1.removeAll(sigma_i1.keySet());
				Set<String> sigma_intersec = new HashSet<>(sigma_i1.keySet()); sigma_intersec.retainAll(sigma_i2.keySet());
				
				int succ_i1_i2 = 0;
				for (String inputSymbol : sigma_intersec) {
					for (SimplifiedTransition<String, Word<String>> dState1 : sigma_i1.get(inputSymbol)) {
						for (SimplifiedTransition<String, Word<String>> dState2 : sigma_i2.get(inputSymbol)) {
							int colIdx = lst_nfa1.indexOf(dState1.getSj())*lst_nfa2.size()+lst_nfa2.indexOf(dState2.getSj());
							coeffOut.setEntry(rowIdx, colIdx, coeffOut.getEntry(rowIdx, colIdx)-K);
							constOut.setEntry(rowIdx, constOut.getEntry(rowIdx)+1);
						}
					}
					
					succ_i1_i2+=sigma_i1.get(inputSymbol).size()*sigma_i2.get(inputSymbol).size();
				}
				
				double sG = 2*(sigma_i1_min_i2.size()+sigma_i2_min_i1.size()+succ_i1_i2);
				sG = (sG==0)?1:sG;
				int colIdx = lst_nfa1.get(i1)*lst_nfa2.size()+lst_nfa2.get(i2);
				coeffOut.setEntry(rowIdx, colIdx, coeffOut.getEntry(rowIdx, colIdx)+sG);
				
			}
		}
		
		DecompositionSolver solverOut = new LUDecomposition(coeffOut).getSolver();
		RealVector solutionOut = solverOut.solve(constOut);

		//////////////////////////////////////////////////////
		// Solving linear equation for incoming transitions //
		//////////////////////////////////////////////////////
		RealMatrix coeffIn = new Array2DRowRealMatrix(lst_nfa1.size()*lst_nfa2.size(),lst_nfa1.size()*lst_nfa2.size());
		RealVector constIn = new ArrayRealVector(lst_nfa1.size()*lst_nfa2.size());
		for (int i1 = 0; i1 < lst_nfa1.size(); i1++) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> trsIn1 = fsm1.getSimplifiedTransitionsIn(lst_nfa1.get(i1));
			Map<String,List<SimplifiedTransition<String, Word<String>>>> sigma_i1 = new LinkedHashMap<>();
			trsIn1.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i1.putIfAbsent(sympTr.getIn()+"\t/\t"+sympTr.getOut(),new ArrayList<>())));
			trsIn1.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i1.get(sympTr.getIn()+"\t/\t"+sympTr.getOut()).add(sympTr)));
			for (int i2 = 0; i2 < lst_nfa2.size(); i2++) {
				int rowIdx = lst_nfa1.get(i1)*lst_nfa2.size()+lst_nfa2.get(i2);
				
				Map<String,List<SimplifiedTransition<String, Word<String>>>> sigma_i2 = new LinkedHashMap<>();
				Map<String, List<SimplifiedTransition<String, Word<String>>>> trsIn2 = fsm2.getSimplifiedTransitionsIn(lst_nfa2.get(i2));
				trsIn2.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i2.putIfAbsent(sympTr.getIn()+"\t/\t"+sympTr.getOut(),new ArrayList<>())));
				trsIn2.values().forEach(a_lst -> a_lst.forEach(sympTr -> sigma_i2.get(sympTr.getIn()+"\t/\t"+sympTr.getOut()).add(sympTr)));
				
				Set<String> sigma_i1_min_i2 = new HashSet<>(sigma_i1.keySet()); sigma_i1_min_i2.removeAll(sigma_i2.keySet());
				Set<String> sigma_i2_min_i1 = new HashSet<>(sigma_i2.keySet()); sigma_i2_min_i1.removeAll(sigma_i1.keySet());
				Set<String> sigma_intersec = new HashSet<>(sigma_i1.keySet()); sigma_intersec.retainAll(sigma_i2.keySet());
				
				int succ_i1_i2 = 0;
				for (String inputSymbol : sigma_intersec) {
					for (SimplifiedTransition<String, Word<String>> dState1 : sigma_i1.get(inputSymbol)) {
						for (SimplifiedTransition<String, Word<String>> dState2 : sigma_i2.get(inputSymbol)) {
							int colIdx = lst_nfa1.indexOf(dState1.getSj())*lst_nfa2.size()+lst_nfa2.indexOf(dState2.getSj());
							coeffIn.setEntry(rowIdx, colIdx, coeffIn.getEntry(rowIdx, colIdx)-K);
							constIn.setEntry(rowIdx, constIn.getEntry(rowIdx)+1);
						}
					}
					
					succ_i1_i2+=sigma_i1.get(inputSymbol).size()*sigma_i2.get(inputSymbol).size();
				}
				
				double sG = 2*(sigma_i1_min_i2.size()+sigma_i2_min_i1.size()+succ_i1_i2);
				sG = (sG==0)?1:sG;
				int colIdx = lst_nfa1.get(i1)*lst_nfa2.size()+lst_nfa2.get(i2);
				coeffIn.setEntry(rowIdx, colIdx, coeffIn.getEntry(rowIdx, colIdx)+sG);
				
			}
		}
		
		DecompositionSolver solverIn = new LUDecomposition(coeffIn).getSolver();
		RealVector solutionIn = solverIn.solve(constIn);
		
		RealVector solutionJoined = solutionOut.copy().add(solutionIn);
		solutionJoined.mapDivideToSelf(2);
		return solutionJoined;
	}
	
	private static List<Integer> pickHighest(Set<List<Integer>> nPairs, RealVector solution, IConfigurableFSM<String, Word<String>> fsm1, IConfigurableFSM<String, Word<String>> fsm2) {
		List<Integer> lst_B = new ArrayList<>(fsm2.getStateIDs());
		
		List<Integer> max = new ArrayList<>();
		max.add(null); max.add(null);
		double maxSim = Double.MIN_VALUE;
		for (List<Integer> pair : nPairs) {
			Integer A = pair.get(0);
			Integer B = pair.get(1);
			int coordIdx = (A) * lst_B.size() + (B);
			if(maxSim<solution.getEntry(coordIdx)) {
				max.set(0, A);
				max.set(1, B);
				maxSim = solution.getEntry(coordIdx);
			}
			
		}
		return max;
		
	}
	
	private static Options createOptions() {
		Options options = new Options();
		options.addOption( CLEAN,  true, "Simplify FFSM labels" );
		options.addOption( MREF,  true, "Mealy reference" );
		options.addOption( FREF,  true, "FFSM reference" );
		options.addOption( UPDT,  true, "Mealy update" );
		options.addOption( FM,    true, "Feature model" );
		options.addOption( OUT,    true, "Output file" );
		options.addOption( HELP,  false, "Help menu" );
		return options;
	}

}
