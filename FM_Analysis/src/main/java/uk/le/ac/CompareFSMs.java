package uk.le.ac;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.fsm.FsmAsNfa;
import uk.le.ac.fsm.MealyUtils;

public class CompareFSMs {

	public static void main(String[] args) {
		try {
			File f_fsm1 = new File("./Benchmark_SPL/agm/fsm/fsm_agm_1.txt");
			CompactMealy<String, Word<String>> fsm1 = MealyUtils.getInstance().loadMealyMachine(f_fsm1);
			File f_fsm2 = new File("./Benchmark_SPL/agm/fsm/fsm_agm_2.txt");
			CompactMealy<String, Word<String>> fsm2 = MealyUtils.getInstance().loadMealyMachine(f_fsm2);
			
			FsmAsNfa nfa1 = new FsmAsNfa(fsm1);

			//Visualization.visualize(fsm1, fsm1.getInputAlphabet());
			//Visualization.visualize(nfa1, nfa1.getInputAlphabet());
			
			FastNFA nfa2 = MealyToNFA(fsm2);

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static FastNFA MealyToNFA(CompactMealy<String, Word<String>> model) {
		FastNFA<String> nfa = new FastNFA<String>(makeAlphabetIO(model));
		Map<Integer, FastNFAState> states_fsm2nfa = new HashMap<>();
		Map<FastNFAState, Integer> states_nfa2fsm = new HashMap<>();
		
		for (Integer stateId : model.getStates()) {
			if(!states_fsm2nfa.containsKey(stateId))  {
				states_fsm2nfa.putIfAbsent(stateId, nfa.addState());
				states_nfa2fsm.putIfAbsent(states_fsm2nfa.get(stateId),stateId);
			}
			FastNFAState s1 = states_fsm2nfa.get(stateId);
			for (String inputIdx : model.getInputAlphabet()) {
				FastNFAState s2 = nfa.addState();
				nfa.addTransition(s1, inputIdx, s2);
				
				CompactMealyTransition<Word<String>> tr = model.getTransition(stateId, inputIdx);
				if(!states_fsm2nfa.containsKey(tr.getSuccId()))  {
					states_fsm2nfa.putIfAbsent(tr.getSuccId(), nfa.addState());
					states_nfa2fsm.putIfAbsent(states_fsm2nfa.get(tr.getSuccId()),tr.getSuccId());
				}
				FastNFAState s3 = states_fsm2nfa.get(tr.getSuccId());
				nfa.addTransition(s2, tr.getOutput().toString(), s3);
			}
		}
		return nfa;
	}

	private static Alphabet<String> makeAlphabetIO(CompactMealy<String, Word<String>> model) {
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
	
}
