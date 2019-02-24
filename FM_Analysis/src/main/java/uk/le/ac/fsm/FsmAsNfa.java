package uk.le.ac.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class FsmAsNfa {

	private FastNFA<String> nfa;
	private Map<FastNFAState, Integer>  nfa2fsm;
	private Map<Integer, FastNFAState>  fsm2nfa;

	private Map<CompactMealyTransition<Word<String>>, FastNFAState>  fsm2nfa_tr;
	private Map<FastNFAState, CompactMealyTransition<Word<String>>>  nfa2fsm_tr;

	public FsmAsNfa(CompactMealy<String, Word<String>> fsm1) {
		fsm2nfa = new HashMap<>();
		nfa2fsm = new HashMap<>();
		nfa2fsm_tr = new HashMap<>();
		fsm2nfa_tr = new HashMap<>();
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
}
