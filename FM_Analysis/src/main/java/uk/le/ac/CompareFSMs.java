package uk.le.ac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.prop4j.Node;
import org.prop4j.NodeReader;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.graphs.TransitionEdge;
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
			
			ModelAsNfa<String,Word<String>> m_nfa1 = new ModelAsNfa(fsm1);
			ModelAsNfa<String,Word<String>> m_nfa2 = new ModelAsNfa(fsm2);
			
			//plotModels(m_nfa1,m_nfa2);
			
			double K = 0.5;
			//double K = 1;
			
			RealVector pairsToScore = computeScores(m_nfa1,m_nfa2,K);
			Set<List<FastNFAState>> kPairs = identifyLandmaks(pairsToScore,m_nfa1,m_nfa2);
			Set<List<FastNFAState>> nPairs = surr(kPairs, m_nfa1, m_nfa2);
			
			Set<FastNFAState> checked = new HashSet<>();
			while (!nPairs.isEmpty()) {
				while (!nPairs.isEmpty()) {
					List<FastNFAState> A_B = pickHighest(nPairs,pairsToScore, m_nfa1, m_nfa2);
					kPairs.add(A_B); 
					checked.addAll(A_B);
					removeConflicts(nPairs,checked);
				}
				nPairs = surr(kPairs, m_nfa1, m_nfa2);
				removeConflicts(nPairs,checked);
			}
			
			kPairs.forEach(pair ->System.out.println(pair.get(0).getId()+","+pair.get(1).getId()));
			
			//FeaturedMealy<String, String> ffsm = makeFFSM(m_nfa1,m_nfa2,kPairs,fm);
			//Visualization.visualize(ffsm,ffsm.getInputAlphabet(), new FFSMVisualizationHelper<>(ffsm));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void plotModels(ModelAsNfa<String,Word<String>> m_nfa1, ModelAsNfa<String,Word<String>> m_nfa2) throws IOException {
		DOTVisualizationHelper helper = new NFAVisualizationHelper();
		Visualization.visualize(m_nfa1.getNfa(),m_nfa1.getNfa().getInputAlphabet(),helper);
		GraphDOT.write(m_nfa1.getNfa(),m_nfa1.getNfa().getInputAlphabet(), System.out, helper);
		
		Visualization.visualize(m_nfa2.getNfa(),m_nfa2.getNfa().getInputAlphabet(),helper);
		GraphDOT.write(m_nfa2.getNfa(),m_nfa2.getNfa().getInputAlphabet(), System.out, helper);
	}

//	private static FeaturedMealy<String, String> makeFFSM(ModelAsNfa<String,Word<String>>nfa0, ModelAsNfa<String,Word<String>> nfa1,
//			Set<List<FastNFAState>> kPairs, IFeatureModel fm) {
//		
//		Map<FastNFAState,FastNFAState> kPairs_nfa0 = new HashMap<>();
//		Map<FastNFAState,FastNFAState> kPairs_nfa1 = new HashMap<>();
//		
//		for (List<FastNFAState> pair : kPairs) {
//			kPairs_nfa0.put(pair.get(0), pair.get(1));
//			kPairs_nfa1.put(pair.get(1), pair.get(0));
//		}
//		
//		Alphabet<String> alphabet = Alphabets.fromCollection(createAlphabet(nfa0,nfa1));
//		FeaturedMealy<String, String> ffsm = new FeaturedMealy<>(alphabet,fm);
//		ConditionalState<ConditionalTransition<String, String>> s0 = ffsm.addInitialState();
//		
//		s0.setCondition(createInitialCondition(nfa0,nfa1));
//		
//		Map<FastNFAState,ConditionalState<ConditionalTransition<String, String>>> nfa0ToFFSM = new HashMap<>();
//		nfa0ToFFSM.put(nfa0.getNfa().getState(0), s0);
//
//		Map<FastNFAState,ConditionalState<ConditionalTransition<String, String>>> nfa1ToFFSM = new HashMap<>();
//		nfa1ToFFSM.put(nfa1.getNfa().getState(0), s0);
//		
//		for (FastNFAState state : nfa0.getNfa2model().keySet()) {
//			Set<List<Integer>> transitions = getTransitions(state.getId(),nfa0.getModel(),nfa0);
//			for (List<Integer> transition : transitions) {
//				FastNFAState si = nfa0.getNfa().getState(transition.get(0));
//				if(!nfa0ToFFSM.containsKey(si)){
//					nfa0ToFFSM.put(si, ffsm.addState());
//					nfa1ToFFSM.put(kPairs_nfa0.get(si), nfa0ToFFSM.get(si));
//				}
//				
//				FastNFAState sj = nfa0.getNfa().getState(transition.get(4));
//				if(!nfa0ToFFSM.containsKey(sj)){
//					nfa0ToFFSM.put(sj, ffsm.addState());
//					nfa1ToFFSM.put(kPairs_nfa0.get(sj), nfa0ToFFSM.get(sj));
//				}
//				
//				
//				ConditionalState<ConditionalTransition<String, String>> ffsm_si = nfa0ToFFSM.get(si);
//				ConditionalState<ConditionalTransition<String, String>> ffsm_sj = nfa0ToFFSM.get(sj);
//				
//				String ffsm_in = nfa0.getNfa().getInputAlphabet().getSymbol(transition.get(1));
//				String ffsm_out = nfa0.getNfa().getInputAlphabet().getSymbol(transition.get(3));
//				
//				NodeReader nodeReader = new NodeReader();
//				nodeReader.activateTextualSymbols();
//				
//				ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, nodeReader.stringToNode("A"));
//			}
//		}
//
//		
//		for (FastNFAState state : nfa1.getNfa2model().keySet()) {
//			Set<List<Integer>> transitions = getTransitions(state.getId(),nfa1.getModel(),nfa1);
//			for (List<Integer> transition : transitions) {
//				FastNFAState si = nfa1.getNfa().getState(transition.get(0));
//				if(!nfa1ToFFSM.containsKey(si)){
//					nfa1ToFFSM.put(si, ffsm.addState());
//					nfa0ToFFSM.put(kPairs_nfa1.get(si), nfa1ToFFSM.get(si));
//				}
//				
//				FastNFAState sj = nfa1.getNfa().getState(transition.get(4));
//				if(!nfa1ToFFSM.containsKey(sj)){
//					nfa1ToFFSM.put(sj, ffsm.addState());
//					nfa0ToFFSM.put(kPairs_nfa1.get(sj), nfa1ToFFSM.get(sj));
//				}
//				
//				
//				ConditionalState<ConditionalTransition<String, String>> ffsm_si = nfa1ToFFSM.get(si);
//				ConditionalState<ConditionalTransition<String, String>> ffsm_sj = nfa1ToFFSM.get(sj);
//				
//				String ffsm_in = nfa1.getNfa().getInputAlphabet().getSymbol(transition.get(1));
//				String ffsm_out = nfa1.getNfa().getInputAlphabet().getSymbol(transition.get(3));
//				
//				NodeReader nodeReader = new NodeReader();
//				nodeReader.activateTextualSymbols();
//				
//				ffsm.addTransition(ffsm_si, ffsm_in, ffsm_sj, ffsm_out, nodeReader.stringToNode("B"));
//			}
//		}
//
//		return ffsm;
//	}

	private static Node createInitialCondition(ModelAsNfa<String,Word<String>> nfa0, ModelAsNfa<String,Word<String>> nfa1) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Collection<String> createAlphabet(
			ModelAsNfa<String,Word<String>> nfa0, 
			ModelAsNfa<String,Word<String>> nfa1) {
		Set<String> abcSet = new HashSet<>();
		
		abcSet.addAll(nfa0.getModel().getInputAlphabet());
		abcSet.addAll(nfa1.getModel().getInputAlphabet());
		
		List<String> abc = new ArrayList<>(abcSet);
		Collections.sort(abc);
		return abc;
	}

	private static Set<List<String>> getRemoved( Set<List<String>> tr_s0, Set<List<String>> tr_s1,
			ModelAsNfa<String,Word<String>> nfa0, ModelAsNfa<String,Word<String>> nfa1, 
			Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsRemoved = new LinkedHashSet<>();
		
//		List<FastNFAState> toSearch = new ArrayList<>();
//		toSearch.add(null);toSearch.add(null);
//		
//		while (!toCheck_s0.isEmpty()) {
//			List<String> item_s0 = toCheck_s0.remove(0);
//			for (List<String> item_s1 : toCheck_s1) {
//
//				toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0))));
//				toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0))));
//				if(kPairs.contains(toSearch)) {
//					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
//					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
//					if(kPairs.contains(toSearch)) {
//						toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4))));
//						toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4))));
//						if(kPairs.contains(toSearch)) {
//							if(!(item_s0.get(1).equals(item_s1.get(1)) || !item_s0.get(3).equals(item_s1.get(3)))) {
//								trsRemoved.add(item_s0);
//								toCheck_s1.remove(item_s1);
//								continue;
//							}
//						}
//					}
//				}
//			}	
//		}
		
		
		return trsRemoved;
	}


	private static Set<List<String>> getAdded(Set<List<String>> tr_s1, Set<List<String>> tr_s0,
			ModelAsNfa<String,Word<String>> nfa1, ModelAsNfa<String,Word<String>> nfa0, Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsAdded = new LinkedHashSet<>();
		
//		List<FastNFAState> toSearch = new ArrayList<>();
//		toSearch.add(null);toSearch.add(null);
//		
//		FastNFAState item2Search = null;
//		while (!toCheck_s0.isEmpty()) {
//			List<String> item_s0 = toCheck_s0.remove(0);
//			for (List<String> item_s1 : toCheck_s1) {
//				
//				item2Search = nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0)));
//				toSearch.set(0, item2Search);
//				
//				item2Search = nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0)));
//				toSearch.set(1, item2Search);
//				if(kPairs.contains(toSearch)) {
//					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
//					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
//					if(kPairs.contains(toSearch)) {
//						item2Search = nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4)));
//						toSearch.set(0, item2Search);
//						item2Search = nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4)));
//						toSearch.set(1, item2Search);
//						if(kPairs.contains(toSearch)) {
//							if(!(item_s0.get(1).equals(item_s1.get(1)) || !item_s0.get(3).equals(item_s1.get(3)))) {
//								trsAdded.add(item_s0);
//								toCheck_s1.remove(item_s1);
//								continue;
//							}
//						}
//					}
//				}
//			}	
//		}
		
		return trsAdded;
	}
	
	private static Set<List<String>> getMatched(Set<List<String>> tr_s0, Set<List<String>> tr_s1,
			ModelAsNfa<String,Word<String>> nfa0, ModelAsNfa<String,Word<String>> nfa1, Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsMatched = new LinkedHashSet<>();
		
//		List<FastNFAState> toSearch = new ArrayList<>();
//		toSearch.add(null);toSearch.add(null);
//		
//		while (!toCheck_s0.isEmpty()) {
//			List<String> item_s0 = toCheck_s0.remove(0);
//			for (List<String> item_s1 : toCheck_s1) {
//				if(!(item_s0.get(1).equals(item_s1.get(1)) && item_s0.get(3).equals(item_s1.get(3)))) continue;
//
//				toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0))));
//				toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0))));
//				if(kPairs.contains(toSearch)) {
//					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
//					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
//					if(kPairs.contains(toSearch)) {
//						toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4))));
//						toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4))));
//						if(kPairs.contains(toSearch)) {
//							toCheck_s1.remove(item_s1);
//							trsMatched.add(item_s0);
//							break;
//						}
//					}
//				}
//			}	
//		}
		
		return trsMatched;
	}

	private static Set<List<Integer>> getTransitions(Integer si, ProductMealy<String, String> mealy, ModelAsNfa model_as_nfa) {
		Set<List<Integer>> trs = new LinkedHashSet<>();
		
		FastNFA<String> nfa = model_as_nfa.getNfa();
		FastNFAState st_ini = nfa.getState(si);
		
		for (String input : nfa.getInputAlphabet()) {
			int inputIdx = nfa.getInputAlphabet().getSymbolIndex(input);
			if(st_ini.getTransitions(inputIdx)==null) continue;
			for (FastNFAState st_mid : st_ini.getTransitions(inputIdx)) {
				for (String output : nfa.getInputAlphabet()) {
					int outputIdx = nfa.getInputAlphabet().getSymbolIndex(output);
					if(st_mid.getTransitions(outputIdx)==null) continue;
					for (FastNFAState st_end : st_mid.getTransitions(outputIdx)) {
						List<Integer> a_tr = new ArrayList<>();
						a_tr.add((st_ini.getId()));
						a_tr.add(inputIdx);
						a_tr.add((st_mid.getId()));
						a_tr.add(outputIdx);
						a_tr.add((st_end.getId()));
						trs.add(a_tr);
					}
				}	
			}
		}
		return trs;
	}
	
	
	private static Set<List<Integer>> getTransitions(Integer model_s0, FeaturedMealy<String, String> mealy, ModelAsNfa nfa) {
		Set<List<Integer>> trs = new LinkedHashSet<>();
		
		return trs;
	}
	
	private static Set<List<Integer>> getTransitions(Integer s0, MutableAutomaton model, ModelAsNfa nfa) {
		if(model instanceof FeaturedMealy) return getTransitions(s0,(FeaturedMealy<String,String> )model,nfa);
		return getTransitions(s0,(ProductMealy<String,String> )model,nfa);
	}

	private static void removeConflicts(Set<List<FastNFAState>> nPairs, Set<FastNFAState> a_B) {
		Set<List<FastNFAState>> toRemove = new HashSet<>();
		for (List<FastNFAState> pair : nPairs) {
			if (a_B.contains(pair.get(0)) || a_B.contains(pair.get(1))) {
				toRemove.add(pair);
			}
		}
		nPairs.removeAll(toRemove);
		
	}

	private static Set<List<FastNFAState>> identifyLandmaks(RealVector pairsToScore, ModelAsNfa<String,Word<String>> nfa1, ModelAsNfa<String,Word<String>> nfa2) {
		Set<List<FastNFAState>> outPairs = new LinkedHashSet<>();
		List<FastNFAState> kPairs = new ArrayList<>();
		kPairs.add(nfa1.getNfa().getState(0));
		kPairs.add(nfa2.getNfa().getState(0));
		outPairs.add(kPairs);
		return outPairs;
	}

	private static Set<List<FastNFAState>> surr(Set<List<FastNFAState>> kPairs, ModelAsNfa<String,Word<String>> nfa1, ModelAsNfa<String,Word<String>> nfa2) {
		Set<List<FastNFAState>> outPairs = new LinkedHashSet<>();
		
		for (List<FastNFAState> pair : kPairs) {
			Set<String> commonInputs = new LinkedHashSet<>(nfa1.getTransitionsOut().get(pair.get(0)).keySet());
			commonInputs.retainAll(nfa2.getTransitionsOut().get(pair.get(1)).keySet());
			for (String input : commonInputs) {
				for (FastNFAState s0 : nfa1.getTransitionsOut().get(pair.get(0)).get(input)) {
					for (FastNFAState s1 : nfa2.getTransitionsOut().get(pair.get(1)).get(input)) {
						List<FastNFAState> nPairs = new ArrayList<>();
						nPairs.add(s0);
						nPairs.add(s1);
						outPairs.add(nPairs);
					}	
				}
			}
		}

		for (List<FastNFAState> pair : kPairs) {
			Set<String> commonInputs = new LinkedHashSet<>(nfa1.getTransitionsIn().get(pair.get(0)).keySet());
			commonInputs.retainAll(nfa2.getTransitionsIn().get(pair.get(1)).keySet());
			for (String input : commonInputs) {
				for (FastNFAState s0 : nfa1.getTransitionsIn().get(pair.get(0)).get(input)) {
					for (FastNFAState s1 : nfa2.getTransitionsIn().get(pair.get(1)).get(input)) {
						List<FastNFAState> nPairs = new ArrayList<>();
						nPairs.add(s0);
						nPairs.add(s1);
						outPairs.add(nPairs);
					}	
				}
			}
		}

		outPairs.removeAll(kPairs);
		return outPairs;
	}

	private static RealVector computeScores(ModelAsNfa<String,Word<String>> nfa1, ModelAsNfa<String,Word<String>> nfa2, double K) {
		
		List<FastNFAState> lst_nfa1 = new ArrayList<>(nfa1.getNfa().getStates());
		List<FastNFAState> lst_nfa2 = new ArrayList<>(nfa2.getNfa().getStates());
		
		
		//////////////////////////////////////////////////////
		// Solving linear equation for outgoing transitions //
		//////////////////////////////////////////////////////
		RealMatrix coeffOut = new Array2DRowRealMatrix(lst_nfa1.size()*lst_nfa2.size(),lst_nfa1.size()*lst_nfa2.size());
		RealVector constOut = new ArrayRealVector(lst_nfa1.size()*lst_nfa2.size());
		for (int i1 = 0; i1 < lst_nfa1.size(); i1++) {
			for (int i2 = 0; i2 < lst_nfa2.size(); i2++) {
				int rowIdx = i1*lst_nfa2.size()+i2;

				Set<String> sigma_i1 = nfa1.getTransitionsOut().get(lst_nfa1.get(i1)).keySet(); 
				Set<String> sigma_i2 = nfa2.getTransitionsOut().get(lst_nfa2.get(i2)).keySet();
				
				Set<String> sigma_i1_min_i2 = new HashSet<>(sigma_i1); sigma_i1_min_i2.removeAll(sigma_i2);
				Set<String> sigma_i2_min_i1 = new HashSet<>(sigma_i2); sigma_i2_min_i1.removeAll(sigma_i1);
				Set<String> sigma_intersec = new HashSet<>(sigma_i1); sigma_intersec.retainAll(sigma_i2);
				
				for (String inputSymbol : sigma_intersec) {
					
					for (FastNFAState dState1 : nfa1.getTransitionsOut().get(lst_nfa1.get(i1)).get(inputSymbol)) {
						for (FastNFAState dState2 : nfa2.getTransitionsOut().get(lst_nfa2.get(i2)).get(inputSymbol)) {
							int colIdx = lst_nfa1.indexOf(dState1)*lst_nfa2.size()+lst_nfa2.indexOf(dState2);
							coeffOut.setEntry(rowIdx, colIdx, coeffOut.getEntry(rowIdx, colIdx)-K);
							constOut.setEntry(rowIdx, constOut.getEntry(rowIdx)+1);
						}
					}
					
				}
				
				int succ_i1_i2 = 0; 
				for (String symb  : sigma_intersec) {
					succ_i1_i2+=nfa1.getTransitionsOut().get(lst_nfa1.get(i1)).get(symb).size()*nfa2.getTransitionsOut().get(lst_nfa2.get(i2)).get(symb).size();
				}
				
				double sG = 2*(sigma_i1_min_i2.size()+sigma_i2_min_i1.size()+succ_i1_i2);
				sG = (sG==0)?1:sG;
				int colIdx = i1*lst_nfa2.size()+i2;
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
			for (int i2 = 0; i2 < lst_nfa2.size(); i2++) {
				int rowIdx = i1*lst_nfa2.size()+i2;

				Set<String> sigma_i1 = nfa1.getTransitionsIn().get(lst_nfa1.get(i1)).keySet(); 
				Set<String> sigma_i2 = nfa2.getTransitionsIn().get(lst_nfa2.get(i2)).keySet();
				
				Set<String> sigma_i1_min_i2 = new HashSet<>(sigma_i1); sigma_i1_min_i2.removeAll(sigma_i2);
				Set<String> sigma_i2_min_i1 = new HashSet<>(sigma_i2); sigma_i2_min_i1.removeAll(sigma_i1);
				Set<String> sigma_intersec = new HashSet<>(sigma_i1); sigma_intersec.retainAll(sigma_i2);
				
				for (String inputSymbol : sigma_intersec) {
					
					for (FastNFAState dState1 : nfa1.getTransitionsIn().get(lst_nfa1.get(i1)).get(inputSymbol)) {
						for (FastNFAState dState2 : nfa2.getTransitionsIn().get(lst_nfa2.get(i2)).get(inputSymbol)) {
							int colIdx = lst_nfa1.indexOf(dState1)*lst_nfa2.size()+lst_nfa2.indexOf(dState2);
							coeffIn.setEntry(rowIdx, colIdx, coeffIn.getEntry(rowIdx, colIdx)-K);
							constIn.setEntry(rowIdx, constIn.getEntry(rowIdx)+1);
						}
					}
					
				}
				
				int prev_i1_i2 = 0; 
				for (String symb  : sigma_intersec) {
					prev_i1_i2+=nfa1.getTransitionsIn().get(lst_nfa1.get(i1)).get(symb).size()*nfa2.getTransitionsIn().get(lst_nfa2.get(i2)).get(symb).size();
				}
				double pG = 2*(sigma_i1_min_i2.size()+sigma_i2_min_i1.size()+prev_i1_i2);
				pG = (pG==0)?1:pG;
				int colIdx = i1*lst_nfa2.size()+i2;
				coeffIn.setEntry(rowIdx, colIdx, coeffIn.getEntry(rowIdx, colIdx)+pG);
				
			}
		}
		
		DecompositionSolver solverIn = new LUDecomposition(coeffIn).getSolver();
		RealVector solutionIn = solverIn.solve(constIn);
		
		RealVector solutionJoined = solutionOut.copy().add(solutionIn);
		solutionJoined.mapDivideToSelf(2);
		return solutionJoined;
	}
	
	private static List<FastNFAState> pickHighest(Set<List<FastNFAState>> nPairs, RealVector solution, ModelAsNfa nfa1, ModelAsNfa nfa2) {
		List<FastNFAState> lst_A = new ArrayList<>(nfa1.getNfa().getStates());
		List<FastNFAState> lst_B = new ArrayList<>(nfa2.getNfa().getStates());
		
		List<FastNFAState> max = new ArrayList<>();
		max.add(null); max.add(null);
		double maxSim = Double.MIN_VALUE;
		for (List<FastNFAState> pair : nPairs) {
			FastNFAState A = pair.get(0);
			FastNFAState B = pair.get(1);
			int coordIdx = lst_A.indexOf(A) * lst_B.size() + lst_B.indexOf(B);
			if(maxSim<solution.getEntry(coordIdx)) {
				max.set(0, A);
				max.set(1, B);
				maxSim = solution.getEntry(coordIdx);
			}
			
		}
		return max;
		
	}

}
