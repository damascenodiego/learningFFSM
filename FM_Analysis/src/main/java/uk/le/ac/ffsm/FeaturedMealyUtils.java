package uk.le.ac.ffsm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.NodeWriter;
import org.prop4j.NodeWriter.Notation;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

public class FeaturedMealyUtils {
	
	// https://github.com/vhfragal/ConFTGen-tool/blob/450dd0a0e408be6b42e223d41154eab2269427f3/work_neon_ubu/br.icmc.ffsm.ui.base/src/br/usp/icmc/feature/logic/FFSMProperties.java#L2384
	public static <I, O> boolean isDeterministic(FeaturedMealy ffsm){
		return false;
		
	}
	
	public static <I, O> boolean isComplete(FeaturedMealy ffsm){
		return false;
		
	}
	
	public static <I, O> boolean isInitiallyConnected(FeaturedMealy ffsm){
		return false;
		
	}
	
	public static <I, O> boolean isMinimal(FeaturedMealy ffsm){
		return false;
		
	}

	public static FeaturedMealy<String,String> readFeaturedMealy(File f_ffsm, IFeatureModel fm) throws IOException{
		Pattern kissLine = Pattern.compile(
				"\\s*"
				+ "(\\S+)" + "@" + "\\[([^\\]]+)\\]"
				+ "\\s+--\\s+"+
				"\\s*"
				+ "(\\S+)" + "@" + "\\[([^\\]]+)\\]"
				+ "\\s*/\\s*"
				+ "(\\S+)"
				+ "\\s+->\\s+"
				+ "(\\S+)" + "@" + "\\[([^\\]]+)\\]"
				);

		BufferedReader br = new BufferedReader(new FileReader(f_ffsm));

		NodeReader nodeReader = new NodeReader();
		nodeReader.activateTextualSymbols();
		FeaturedMealy<String, String> ffsm = null;
		
		if(br.ready()){
			String line = br.readLine();
			String[] inputSymbols = line.split(";");
			HashSet<String> abcSet = new HashSet<>();
			List<String> abc = new ArrayList<>();
			for (String ic : inputSymbols) {
				String[] ic_split = ic.split("@");
				if(abcSet.add(ic_split[0])) {
					abc.add(ic_split[0]);
				}
 			}
			Collections.sort(abc);
			Alphabet<String> alphabet = Alphabets.fromCollection(abc);
			ffsm = new FeaturedMealy<>(alphabet,fm);
			
			ConditionalState<ConditionalTransition<String>> s0 = null;
			Map<Integer,ConditionalState<ConditionalTransition<String>>> statesMap = new HashMap<>();
			while(br.ready()){
				line = br.readLine();
				Matcher m = kissLine.matcher(line);
				if(m.matches()){
					String[] tr = new String[7];
					IntStream.range(1, tr.length+1).forEach(idx-> tr[idx-1] = m.group(idx));
					
					/* Conditional state origin */
					Integer si = Integer.valueOf(tr[0]); 
					Node si_c = nodeReader.stringToNode(tr[1]);
					if(!statesMap.containsKey(si)) {
						statesMap.put(si,ffsm.addState(si_c));
						if(s0==null) s0 = statesMap.get(si);
					}
					
					/* Conditional Input */
					String in = tr[2];
					Node in_c = nodeReader.stringToNode(tr[3]);
					
					/* Output */
					String out = tr[4];
					
					/* Conditional state destination */
					Integer sj = Integer.valueOf(tr[5]);
					Node sj_c = nodeReader.stringToNode(tr[6]);
					if(!statesMap.containsKey(sj)) {
						statesMap.put(sj,ffsm.addState(sj_c));
					}
					
					ffsm.addTransition(statesMap.get(si), in, statesMap.get(sj), out,in_c);
				}
			}
			
			ffsm.setInitial(s0,true);
			Collection<ConditionalTransition<String>> condTr = null;
			int totTrs = 0; 
			for (ConditionalState<ConditionalTransition<String>> state : ffsm.getStates()) {
				for (String input : ffsm.getInputAlphabet()) {
					condTr = ffsm.getTransitions(state, input);
					totTrs+=condTr.size();
					for (ConditionalTransition<String> tr : condTr) {
						System.out.println(String.format("%d@[%s] -- %s@[%s] / %s -> %d@[%s]", 
								state.getId(),
								formatNode(state.getConstraint()),
								input,
								formatNode(tr.getCondition()),
								tr.getOutput(),
								tr.getSuccessor().getId(),
								formatNode(ffsm.getState(tr.getSuccessor().getId()).getConstraint())
								));
					}
				}
			}
			System.out.println(condTr);
			System.out.println(condTr.size());
			System.out.println(totTrs);
		}
		
		br.close();

		return ffsm;
	}
	public static String formatNode(Node n) {
		NodeWriter nw = new NodeWriter(n);
//		nw.setNotation(Notation.INFIX);
		nw.setNotation(Notation.PREFIX);
		nw.setSymbols(NodeWriter.textualSymbols);
		nw.setEnforceBrackets(true);
		return nw.nodeToString();
	}
	
}
