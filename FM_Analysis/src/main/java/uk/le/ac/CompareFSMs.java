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
			
			FsmAsNfa nfa2 = new FsmAsNfa(fsm2);
			
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
