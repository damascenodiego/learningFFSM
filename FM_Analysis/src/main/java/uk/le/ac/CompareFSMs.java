package uk.le.ac;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.serialization.dot.DOTVisualizationHelper;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.fsm.ModelAsNfa;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.ProductMealy;
import uk.le.ac.ffsm.SimplifiedTransition;

public class CompareFSMs {

	public static void main(String[] args) {
		try {
			String spl_name = "agm";
			File f_fm = new File("Benchmark_SPL/"+spl_name+"/feature_models/example_"+spl_name+".xml");
			
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			
			File f_fsm1 = new File("./Benchmark_SPL/"+spl_name+"/fsm/fsm_"+spl_name+"_1.txt");
			ProductMealy<String, Word<String>> fsm1 = FeaturedMealyUtils.getInstance().loadProductMachine(f_fsm1,fm);
			File f_fsm2 = new File("./Benchmark_SPL/"+spl_name+"/fsm/fsm_"+spl_name+"_5.txt");
			ProductMealy<String, Word<String>> fsm2 = FeaturedMealyUtils.getInstance().loadProductMachine(f_fsm2,fm);
			
			//File f_ffsm1 = new File("./Benchmark_SPL/"+spl_name+"/ffsms/ffsm_"+spl_name+".txt");
			//FeaturedMealy<String, String> ffsm_orig = FeaturedMealyUtils.getInstance().loadFeaturedMealy(f_ffsm1,fm);
			//f = new File("ffsm_orig.dot");
			//FeaturedMealyUtils.getInstance().saveFFSM(ffsm_orig, f);
			
			double K = 0.50;
			//double K = 1;
			
			RealVector pairsToScore = computeScores(fsm1,fsm2,K);
			Set<List<Integer>> kPairs = identifyLandmaks(pairsToScore,fsm1,fsm2);
			Set<List<Integer>> nPairs = surr(kPairs, fsm1,fsm2);
			Map<Integer,Set<Integer>> checked = new HashMap<>();
			checked.put(0, new HashSet<>());
			checked.get(0).add(0);
			
			checked.put(1, new HashSet<>());
			checked.get(1).add(0);
			
			removeConflicts(nPairs,checked);
			
			
			while (!nPairs.isEmpty()) {
				while (!nPairs.isEmpty()) {
					List<Integer> A_B = pickHighest(nPairs,pairsToScore, fsm1,fsm2);
					kPairs.add(A_B);
					checked.get(0).add(A_B.get(0));
					checked.get(1).add(A_B.get(1));
					removeConflicts(nPairs,checked);
				}
				nPairs = surr(kPairs, fsm1,fsm2);
				removeConflicts(nPairs,checked);
			}
			
			kPairs.forEach(pair ->System.out.println(pair.get(0)+","+pair.get(1)));
			
			FeaturedMealy<String, String> ffsm = makeFFSM(fsm1,fsm2,kPairs,fm);

			
			File f = null;
			BufferedWriter bw = null;
			
			String dirName = f_fsm1.getName().replaceAll("\\.[a-z]+$", "-")+f_fsm2.getName().replaceAll("\\.[a-z]+$", "");
			File outDir = new File(new File(spl_name),dirName);
			outDir.mkdirs();
			
			f = new File(outDir,f_fsm1.getName()+".dot");
			bw = new BufferedWriter(new FileWriter(f));
			GraphDOT.write(fsm1,fsm1.getInputAlphabet(),bw);
			
			f = new File(outDir,f_fsm2.getName()+".dot");
			bw = new BufferedWriter(new FileWriter(f));
			GraphDOT.write(fsm2,fsm2.getInputAlphabet(),bw);
			
			f = new File(outDir,"merged.dot");
			FeaturedMealyUtils.getInstance().saveFFSM(ffsm, f);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static <I> FeaturedMealy<String, String> makeFFSM(ProductMealy<String, Word<String>> fsm1, ProductMealy<String, Word<String>> fsm2, Set<List<Integer>> kPairs, IFeatureModel fm) {
		Map<Integer,Integer> kPairs_nfa0to1 = new HashMap<>();
		
		for (List<Integer> pair : kPairs) {
			kPairs_nfa0to1.put(pair.get(0), pair.get(1));
		}
		
		Map<Integer,ConditionalState<ConditionalTransition<String, String>>> nfa0ToFFSM = new HashMap<>();
		Map<Integer,ConditionalState<ConditionalTransition<String, String>>> nfa1ToFFSM = new HashMap<>();
		
		Alphabet<String> alphabet = Alphabets.fromCollection(createAlphabet(fsm1,fsm2));
		FeaturedMealy<String, String> ffsm = new FeaturedMealy<>(alphabet,fm);
		ConditionalState<ConditionalTransition<String, String>> s0 = ffsm.addInitialState();

		nfa0ToFFSM.put(fsm1.getInitialState(), s0);
		nfa1ToFFSM.put(fsm2.getInitialState(), s0);
		
		Set<Node> feat_common = new LinkedHashSet<>(fsm1.getConfiguration());
		feat_common.retainAll(fsm2.getConfiguration());
		
		makeInitialCondition(s0,fsm1,fsm2);
		
		for (Integer si : fsm1.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = fsm1.getSimplifiedTransitions(si);
			if(!nfa0ToFFSM.containsKey(si)){
				ConditionalState<ConditionalTransition<String, String>> newState = ffsm.addState();
				nfa0ToFFSM.put(si, newState);
				if(kPairs_nfa0to1.containsKey(si)) {
					nfa1ToFFSM.put(kPairs_nfa0to1.get(si), newState);
				}
			}
			ConditionalState<ConditionalTransition<String, String>> ffsm_si = nfa0ToFFSM.get(si);
			updateCondition(ffsm_si,fsm1,fsm2);
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa0ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, String>> newState = ffsm.addState();
						nfa0ToFFSM.put(sj, newState);
						if(kPairs_nfa0to1.containsKey(sj)) {
							nfa1ToFFSM.put(kPairs_nfa0to1.get(sj), newState);
						}
					}
					String ffsm_in  = simpleTr.getIn();
					String ffsm_out = simpleTr.getOut().toString();
					ConditionalState<ConditionalTransition<String, String>> ffsm_sj = nfa0ToFFSM.get(sj);
					updateCondition(ffsm_sj,fsm1,fsm2);
					
					ConditionalTransition<String, String> newTr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out);
					updateCondition(newTr,fsm1,fsm2);
					
				}
			}
		}
		
		for (Integer si : fsm2.getStates()) {
			Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = fsm2.getSimplifiedTransitions(si);
			if(!nfa1ToFFSM.containsKey(si)){
				ConditionalState<ConditionalTransition<String, String>> newState = ffsm.addState();
				nfa1ToFFSM.put(si, newState);
			}
			ConditionalState<ConditionalTransition<String, String>> ffsm_si = nfa1ToFFSM.get(si);
			updateCondition(ffsm_si,fsm2,fsm1);
			for (String input : transitions.keySet()) {
				for (SimplifiedTransition<String, Word<String>> simpleTr : transitions.get(input)) {
					Integer sj = simpleTr.getSj();
					if(!nfa1ToFFSM.containsKey(sj)){
						ConditionalState<ConditionalTransition<String, String>> newState = ffsm.addState();
						nfa1ToFFSM.put(sj, newState);
					}
					String ffsm_in  = simpleTr.getIn();
					String ffsm_out = simpleTr.getOut().toString();
					ConditionalState<ConditionalTransition<String, String>> ffsm_sj = nfa1ToFFSM.get(sj);
					updateCondition(ffsm_sj,fsm2,fsm1);
					
					
					ConditionalTransition<String, String> aTr = null; 
					Map<String, List<SimplifiedTransition<String, String>>> trs_matching = ffsm.getSimplifiedTransitions(ffsm_si.getId(), ffsm_in, ffsm_out, ffsm_sj.getId());
					if(trs_matching.isEmpty()) {
						aTr = ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out);
					}else {
						SimplifiedTransition<String, String> tr = new ArrayList<>(trs_matching.values()).get(0).get(0);
						aTr = (ConditionalTransition<String, String>) tr.getTransition();
					}
					updateCondition(aTr,fsm2,fsm1);
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

	private static void makeInitialCondition(ConditionalState<ConditionalTransition<String, String>> s0,
			ProductMealy<String, Word<String>> fsmRef, ProductMealy<String, Word<String>> fsmUpdt) {
		
		Set<Node> featCommon = new LinkedHashSet<>(fsmRef.getConfiguration());
		featCommon.retainAll(fsmUpdt.getConfiguration());
		
		s0.setCondition(new Or(featCommon));
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

	private static Set<List<Integer>> identifyLandmaks(RealVector pairsToScore, ProductMealy<String, Word<String>> fsm1, ProductMealy<String, Word<String>> fsm2) {
		Set<List<Integer>> outPairs = new LinkedHashSet<>();
		List<Integer> kPairs = new ArrayList<>();
		kPairs.add(fsm1.getIntInitialState());
		kPairs.add(fsm2.getIntInitialState());
		outPairs.add(kPairs);
		return outPairs;
	}

	private static Set<List<Integer>> surr(Set<List<Integer>> kPairs, ProductMealy<String, Word<String>> fsm0, ProductMealy<String, Word<String>> fsm1) {
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

	private static RealVector computeScores(ProductMealy<String, Word<String>> fsm1, ProductMealy<String, Word<String>> fsm2, double K) {
		
		List<Integer> lst_nfa1 = new ArrayList<>(fsm1.getStates());
		List<Integer> lst_nfa2 = new ArrayList<>(fsm2.getStates());
		
		
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
	
	private static List<Integer> pickHighest(Set<List<Integer>> nPairs, RealVector solution, ProductMealy<String, Word<String>> fsm1, ProductMealy<String, Word<String>> fsm2) {
		List<Integer> lst_B = new ArrayList<>(fsm2.getStates());
		
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

}
