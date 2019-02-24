package uk.le.ac.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.usp.icmc.fsm.common.State;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class FsmAsNfa {

	private CompactMealy<String, Word<String>> fsm;
	private FastNFA<String> nfa;
	private Map<FastNFAState, Integer>  nfa2fsm;
	private Map<Integer, FastNFAState>  fsm2nfa;

	private Map<CompactMealyTransition<Word<String>>, FastNFAState>  fsm2nfa_tr;
	private Map<FastNFAState, CompactMealyTransition<Word<String>>>  nfa2fsm_tr;
	
	private Map<FastNFAState,List<StateSymbolState>> transitionsOut;
	private Map<FastNFAState,List<StateSymbolState>> transitionsIn;

	public FsmAsNfa(CompactMealy<String, Word<String>> the_fsm) {
		fsm = the_fsm;
		fsm2nfa = new LinkedHashMap<>();
		nfa2fsm = new LinkedHashMap<>();
		nfa2fsm_tr = new LinkedHashMap<>();
		fsm2nfa_tr = new LinkedHashMap<>();
		transitionsOut = new LinkedHashMap<>();
		transitionsIn = new LinkedHashMap<>();
		MealyToNFA(the_fsm);
	}

	private FastNFA MealyToNFA(CompactMealy<String, Word<String>> model) {
		nfa = new FastNFA<String>(makeAlphabetIO(model));
		
		for (Integer stateId : model.getStates()) {
			if(!fsm2nfa.containsKey(stateId))  {
				fsm2nfa.putIfAbsent(stateId, nfa.addState());
				nfa2fsm.putIfAbsent(fsm2nfa.get(stateId),stateId);
			}
			FastNFAState s1 = fsm2nfa.get(stateId);
			for (String inputIdx : model.getInputAlphabet()) {
				FastNFAState s2 = nfa.addState();
				nfa.addTransition(s1, inputIdx, s2);
				

				CompactMealyTransition<Word<String>> tr = model.getTransition(stateId, inputIdx);
				if(!fsm2nfa_tr.containsKey(tr)) {
					fsm2nfa_tr.putIfAbsent(tr, s2);
					nfa2fsm_tr.putIfAbsent(fsm2nfa_tr.get(tr),tr);
				}

				if(!fsm2nfa.containsKey(tr.getSuccId()))  {
					fsm2nfa.putIfAbsent(tr.getSuccId(), nfa.addState());
					nfa2fsm.putIfAbsent(fsm2nfa.get(tr.getSuccId()),tr.getSuccId());
				}
				FastNFAState s3 = fsm2nfa.get(tr.getSuccId());
				nfa.addTransition(s2, tr.getOutput().toString(), s3);

				
				transitionsOut.putIfAbsent(s1, new ArrayList<>());
				transitionsOut.putIfAbsent(s2, new ArrayList<>());
				transitionsOut.putIfAbsent(s3, new ArrayList<>());
	
				transitionsIn.putIfAbsent(s1, new ArrayList<>());
				transitionsIn.putIfAbsent(s2, new ArrayList<>());
				transitionsIn.putIfAbsent(s3, new ArrayList<>());

				
				transitionsOut.get(s1).add(new StateSymbolState(s1, inputIdx, 					s2));
				transitionsOut.get(s2).add(new StateSymbolState(s2, tr.getOutput().toString(), 	s3));
				
				transitionsIn.get(s2).add(new StateSymbolState(s1, inputIdx, 					s2));
				transitionsIn.get(s3).add(new StateSymbolState(s2, tr.getOutput().toString(), 	s3));

			}
		}
		return nfa;
	}

	private Alphabet<String> makeAlphabetIO(CompactMealy<String, Word<String>> model) {
		Set<String> set_inputAlphabet = new HashSet<>();
		for (String in : model.getInputAlphabet()) set_inputAlphabet.add(in);
		for (Integer stateId : model.getStates()) {
			for (String inputIdx : model.getInputAlphabet()) {
				set_inputAlphabet.add(model.getTransition(stateId, inputIdx).getOutput().toString());
			}
		}
		Alphabet<String> inpAlphabet = Alphabets.fromCollection(set_inputAlphabet);
		return inpAlphabet;
	}
	
	public Map<Integer, FastNFAState> getFsm2nfa() {
		return fsm2nfa;
	}
	
	public Map<FastNFAState, Integer> getNfa2fsm() {
		return nfa2fsm;
	}
	
	public FastNFA<String> getNfa() {
		return nfa;
	}
	
	public Map<CompactMealyTransition<Word<String>>, FastNFAState> getFsm2nfa_tr() {
		return fsm2nfa_tr;
	}
	
	public Map<FastNFAState, CompactMealyTransition<Word<String>>> getNfa2fsm_tr() {
		return nfa2fsm_tr;
	}
	
	public CompactMealy<String, Word<String>> getFsm() {
		return fsm;
	}
	
	public Map<FastNFAState, List<StateSymbolState>> getTransitionsIn() {
		return transitionsIn;
	}
	
	public Map<FastNFAState, List<StateSymbolState>> getTransitionsOut() {
		return transitionsOut;
	}
	
	class StateSymbolState{
		FastNFAState si;
		String symbol;
		FastNFAState sj;
		
		public StateSymbolState(FastNFAState a, String i, FastNFAState b) {
			si = a;
			symbol = i;
			sj = b;
		}
		public FastNFAState getSi() {
			return si;
		}
		
		public FastNFAState getSj() {
			return sj;
		}
		
		public String getSymbol() {
			return symbol;
		}
		@Override
		public String toString() {
			return si.getId()+"--"+symbol+"->"+sj.getId();
		}
	}
}
