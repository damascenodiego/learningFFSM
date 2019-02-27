package uk.le.ac;

import java.io.File;
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

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.DefaultDOTVisualizationHelper;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Word;
import uk.le.ac.fsm.ModelAsNfa;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.ProductMealy;
import uk.le.ac.fsm.MealyUtils;

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
			
			ModelAsNfa m_nfa1 = new ModelAsNfa(fsm1);
			ModelAsNfa m_nfa2 = new ModelAsNfa(fsm2);
			
			//Visualization.visualize(fsm1,fsm1.getInputAlphabet());
			//Visualization.visualize(m_nfa1.getNfa(),m_nfa1.getNfa().getInputAlphabet());
			//Visualization.visualize(fsm2,fsm2.getInputAlphabet());
			//Visualization.visualize(m_nfa2.getNfa(),m_nfa2.getNfa().getInputAlphabet());
			
			double K = 0.5;
			//double K = 1;
			
			RealVector pairsToScore = computeScores(m_nfa1,m_nfa2,K);
			Set<List<FastNFAState>> kPairs = identifyLandmaks(pairsToScore,m_nfa1,m_nfa2);
			Set<List<FastNFAState>> nPairs = surr(kPairs, m_nfa1, m_nfa2);
			
			while (!nPairs.isEmpty()) {
				while (!nPairs.isEmpty()) {
					List<FastNFAState> A_B = pickHighest(nPairs,pairsToScore, m_nfa1, m_nfa2);
					kPairs.add(A_B);
					removeConflicts(nPairs,A_B);
				}
				nPairs = surr(kPairs, m_nfa1, m_nfa2);
			}
			
			kPairs.forEach(pair ->System.out.println(pair.get(0).getId()+","+pair.get(1).getId()));
			
			FeaturedMealy<String, String> ffsm = makeFFSM(m_nfa1,m_nfa2,kPairs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static FeaturedMealy<String, String> makeFFSM(ModelAsNfa nfa0, ModelAsNfa nfa1,
			Set<List<FastNFAState>> kPairs) {
		List<List<Integer>> toVisit = new ArrayList<>();
		toVisit.add(new ArrayList<>());
		toVisit.get(0).add(0);
		toVisit.get(0).add(0);
		
		Set<List<Integer>> visited = new HashSet<>();
		
		while (!toVisit.isEmpty()) {
			List<Integer> pair = toVisit.remove(0);
			visited.add(pair);

			Integer model_s0 = pair.get(0);
			Integer model_s1 = pair.get(1);

			
			Set<List<String>> tr_s0 = getTransitions(model_s0,nfa0.getModel(),nfa0);
			Set<List<String>> tr_s1 = getTransitions(model_s1,nfa1.getModel(),nfa1);
			
			Set<List<String>> added   = getAdded(tr_s0,tr_s1,nfa0,nfa1,kPairs);
			Set<List<String>> removed = getRemoved(tr_s0,tr_s1,nfa0,nfa1,kPairs);
			Set<List<String>> matched = getMatched(tr_s0,tr_s1,nfa0,nfa1,kPairs);
			System.out.println("");
			
		}
		
		return null;
	}

	private static Set<List<String>> getRemoved(Set<List<String>> tr_s0, Set<List<String>> tr_s1,
			ModelAsNfa nfa0, ModelAsNfa nfa1, Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsRemoved = new LinkedHashSet<>();
		
		List<FastNFAState> toSearch = new ArrayList<>();
		toSearch.add(null);toSearch.add(null);
		
		while (!toCheck_s0.isEmpty()) {
			List<String> item_s0 = toCheck_s0.remove(0);
			for (List<String> item_s1 : toCheck_s1) {

				toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0))));
				toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0))));
				if(kPairs.contains(toSearch)) {
					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
					if(kPairs.contains(toSearch)) {
						toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4))));
						toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4))));
						if(kPairs.contains(toSearch)) {
							if(!(item_s0.get(1).equals(item_s1.get(1)) || !item_s0.get(3).equals(item_s1.get(3)))) {
								trsRemoved.add(item_s0);
								toCheck_s1.remove(item_s1);
								continue;
							}
						}
					}
				}
			}	
		}
		
		
		return trsRemoved;
	}


	private static Set<List<String>> getAdded(Set<List<String>> tr_s1, Set<List<String>> tr_s0,
			ModelAsNfa nfa1, ModelAsNfa nfa0, Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsAdded = new LinkedHashSet<>();
		
		List<FastNFAState> toSearch = new ArrayList<>();
		toSearch.add(null);toSearch.add(null);
		
		while (!toCheck_s0.isEmpty()) {
			List<String> item_s0 = toCheck_s0.remove(0);
			for (List<String> item_s1 : toCheck_s1) {

				toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0))));
				toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0))));
				if(kPairs.contains(toSearch)) {
					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
					if(kPairs.contains(toSearch)) {
						toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4))));
						toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4))));
						if(kPairs.contains(toSearch)) {
							if(!(item_s0.get(1).equals(item_s1.get(1)) || !item_s0.get(3).equals(item_s1.get(3)))) {
								trsAdded.add(item_s0);
								toCheck_s1.remove(item_s1);
								continue;
							}
						}
					}
				}
			}	
		}
		
		return trsAdded;
	}
	
	private static Set<List<String>> getMatched(Set<List<String>> tr_s0, Set<List<String>> tr_s1,
			ModelAsNfa nfa0, ModelAsNfa nfa1, Set<List<FastNFAState>> kPairs) {
		List<List<String>> toCheck_s0 = new ArrayList<>(tr_s0);
		List<List<String>> toCheck_s1 = new ArrayList<>(tr_s1);
		
		Set<List<String>> trsMatched = new LinkedHashSet<>();
		
		List<FastNFAState> toSearch = new ArrayList<>();
		toSearch.add(null);toSearch.add(null);
		
		while (!toCheck_s0.isEmpty()) {
			List<String> item_s0 = toCheck_s0.remove(0);
			for (List<String> item_s1 : toCheck_s1) {
				if(!(item_s0.get(1).equals(item_s1.get(1)) && item_s0.get(3).equals(item_s1.get(3)))) continue;

				toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(0))));
				toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(0))));
				if(kPairs.contains(toSearch)) {
					//toSearch.set(0, nfa0.getNfa().getState(item_s0.get(2)));
					//toSearch.set(1, nfa1.getNfa().getState(item_s1.get(2)));
					if(kPairs.contains(toSearch)) {
						toSearch.set(0, nfa0.getNfa().getState(Integer.valueOf(item_s0.get(4))));
						toSearch.set(1, nfa1.getNfa().getState(Integer.valueOf(item_s1.get(4))));
						if(kPairs.contains(toSearch)) {
							toCheck_s1.remove(item_s1);
							trsMatched.add(item_s0);
							break;
						}
					}
				}
			}	
		}
		
		return trsMatched;
	}

	private static Set<List<String>> getTransitions(Integer si, ProductMealy<String, String> mealy, ModelAsNfa model_as_nfa) {
		Set<List<String>> trs = new LinkedHashSet<>();
		
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
						List<String> a_tr = new ArrayList<>();
						a_tr.add(Integer.toString(st_ini.getId()));
						a_tr.add(input);
						a_tr.add(Integer.toString(st_mid.getId()));
						a_tr.add(output);
						a_tr.add(Integer.toString(st_end.getId()));
						trs.add(a_tr);
					}
				}	
			}
		}
		return trs;
	}
	
	private static Set<List<String>> getTransitions(Integer model_s0, FeaturedMealy<String, String> mealy, ModelAsNfa nfa) {
		Set<List<String>> trs = new LinkedHashSet<>();
		
		return trs;
	}
	
	private static Set<List<String>> getTransitions(Integer s0, MutableAutomaton model, ModelAsNfa nfa) {
		if(model instanceof FeaturedMealy) return getTransitions(s0,(FeaturedMealy<String,String> )model,nfa);
		return getTransitions(s0,(ProductMealy<String,String> )model,nfa);
	}

	private static void removeConflicts(Set<List<FastNFAState>> nPairs, List<FastNFAState> a_B) {
		Set<List<FastNFAState>> toRemove = new HashSet<>();
		for (List<FastNFAState> pair : nPairs) {
			if (a_B.contains(pair.get(0)) || a_B.contains(pair.get(1))) {
				toRemove.add(pair);
			}
		}
		nPairs.removeAll(toRemove);
		
	}

	private static Set<List<FastNFAState>> identifyLandmaks(RealVector pairsToScore, ModelAsNfa nfa1, ModelAsNfa nfa2) {
		Set<List<FastNFAState>> outPairs = new LinkedHashSet<>();
		List<FastNFAState> kPairs = new ArrayList<>();
		kPairs.add(nfa1.getNfa().getState(0));
		kPairs.add(nfa2.getNfa().getState(0));
		outPairs.add(kPairs);
		return outPairs;
	}

	private static Set<List<FastNFAState>> surr(Set<List<FastNFAState>> kPairs, ModelAsNfa nfa1, ModelAsNfa nfa2) {
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

	private static RealVector computeScores(ModelAsNfa nfa1, ModelAsNfa nfa2, double K) {
		
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
