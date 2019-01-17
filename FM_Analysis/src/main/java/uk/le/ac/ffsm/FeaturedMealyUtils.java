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

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.NodeWriter;
import org.prop4j.NodeWriter.Notation;

import org.prop4j.Not;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.FeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class FeaturedMealyUtils {
	
	private static final long MAX_CONFIGURATIONS = 100000;
	private static final long MIN_CONFIGURATIONS = 10;
	public static IFeatureModelFactory fact = FMFactoryManager.getDefaultFactory();
	
	// https://github.com/vhfragal/ConFTGen-tool/blob/450dd0a0e408be6b42e223d41154eab2269427f3/work_neon_ubu/br.icmc.ffsm.ui.base/src/br/usp/icmc/feature/logic/FFSMProperties.java#L2384
	public static <I, O> boolean isDeterministic(FeaturedMealy<I,O> ffsm){
		FeatureModel fm = (FeatureModel) ffsm.getFeatureModel().clone();

		Alphabet<I> alphabet = ffsm.getInputAlphabet();

		for (ConditionalState<ConditionalTransition<I,O>> cState : ffsm.getStates()) {
			for (I inputIdx : alphabet) {
				Collection<ConditionalTransition<I,O>> outTrs = ffsm.getTransitions(cState,inputIdx);
				for (ConditionalTransition<I,O> tr1 : outTrs) {
					List<Node> ands_l = new ArrayList<>();
					for (ConditionalTransition<I,O> tr2 : outTrs) {
						if(tr1.equals(tr2)) continue;
						ands_l.clear();
						ands_l.add(new And(
								new And(tr1.getPredecessor().getCondition(),tr1.getCondition(),tr1.getSuccessor().getCondition()),
								new And(tr2.getPredecessor().getCondition(),tr2.getCondition(),tr2.getSuccessor().getCondition())
								));
						IConstraint ands = fact.createConstraint(fm, new And(ands_l));
						fm.addConstraint(ands);
						Configuration conf = new Configuration(fm);
						if(conf.number(MIN_CONFIGURATIONS)!=0) {
							fm.reset();
							return false;
						}
						fm.removeConstraint(ands);
					}
				}
				
			}
		}
		fm.reset();
		return true;
	}

	public static <I, O> boolean isComplete(FeaturedMealy<I,O> ffsm){
		FeatureModel fm = (FeatureModel) ffsm.getFeatureModel().clone();

		Alphabet<I> alphabet = ffsm.getInputAlphabet();

		for (ConditionalState<ConditionalTransition<I,O>> cState : ffsm.getStates()) {

			IConstraint stateAnd = fact.createConstraint(fm, cState.getCondition());
			fm.addConstraint(stateAnd); // check origin condition
			List<Node> notAnds = new ArrayList<>(); 
			for (I inputIdx : alphabet) {
				Collection<ConditionalTransition<I,O>> outTrs = ffsm.getTransitions(cState,inputIdx);
				if(outTrs.isEmpty()) return false;
				for (ConditionalTransition<I,O> tr : outTrs) {
					notAnds.add(new Not(new And(tr.getCondition(),tr.getSuccessor().getCondition()))); // input and destination conditions
				}				
			}
			IConstraint and_notAnds = fact.createConstraint(fm, new And(notAnds));
			fm.addConstraint(and_notAnds); // check origin condition
			Configuration conf = new Configuration(fm);
			if(conf.number(MIN_CONFIGURATIONS)!=0) {
				fm.reset();
				return false;
			}
			fm.removeConstraint(and_notAnds);
			fm.removeConstraint(stateAnd);
		}
		fm.reset();
		return true;

	}
	
	public static <I, O> boolean isInitiallyConnected(FeaturedMealy<I,O> ffsm){
		ConditionalState<ConditionalTransition<I,O>> s0 = ((ConditionalState<ConditionalTransition<I,O>>) (ffsm.getInitialStates().toArray())[0]);

		Set<ConditionalTransition<I,O>> no_loop_tr = new HashSet<>();
		for (ConditionalState<ConditionalTransition<I,O>> state : ffsm.getStates()) {
			for (I input : ffsm.getInputAlphabet()) {
				Collection<ConditionalTransition<I,O>> trs = ffsm.getTransitions(state, input);
				for (ConditionalTransition<I,O> tr : trs) {
					if(!tr.getPredecessor().equals(tr.getSuccessor()))  
						no_loop_tr.add(tr);
				}
			}			
		}
		
		Set<ConditionalState<ConditionalTransition<I,O>>> found_fc = new HashSet<>();
		Set<ConditionalState<ConditionalTransition<I,O>>> nfound_fc = new HashSet<>(ffsm.getStates());
		if((ffsm.getInitialStates().size() == 1)) {
			nfound_fc.remove(s0);
			found_fc.add(s0);			
		} else return false;

		Map<ConditionalState<ConditionalTransition<I,O>>, List<List<ConditionalTransition<I,O>>>> allPaths = new HashMap<ConditionalState<ConditionalTransition<I,O>>,List<List<ConditionalTransition<I,O>>>>();
		nfound_fc.forEach(cState -> allPaths.put(cState, new ArrayList<>()));
		
		for (I input : ffsm.getInputAlphabet()) {
			Collection<ConditionalTransition<I,O>> trs = ffsm.getTransitions(s0 , input);
			for (ConditionalTransition<I,O> tr : trs) {
				if(no_loop_tr.contains(tr)) {
					if(!found_fc.contains(tr.getSuccessor())){
						nfound_fc.remove(tr.getSuccessor());
						found_fc.add(tr.getSuccessor());
					}
					ArrayList<ConditionalTransition<I,O>> currPath = new ArrayList<>();
					currPath.add(tr);
					allPaths.get(tr.getSuccessor()).add(currPath);
				}
			}
		}

		ArrayList<ConditionalState<ConditionalTransition<I,O>>> covered_fc = new ArrayList<>();
		for(ConditionalState<ConditionalTransition<I,O>> cs : found_fc){
			if(!cs.equals(s0)){
				rec_find_paths(ffsm,cs,covered_fc,no_loop_tr,allPaths);
			}				
		}
		return false;
		
	}
	

	private static <I,O> void rec_find_paths(
			FeaturedMealy<I, O> ffsm, 
			ConditionalState<ConditionalTransition<I,O>> current,
			List<ConditionalState<ConditionalTransition<I,O>>> covered_fc, 
			Set<ConditionalTransition<I,O>> no_loop_tr,
			Map<ConditionalState<ConditionalTransition<I,O>>, List<List<ConditionalTransition<I,O>>>> allPaths) {

		ArrayList<ConditionalTransition<I,O>> listOfTrs = new ArrayList<>();
		for (I input : ffsm.getInputAlphabet())  listOfTrs.addAll(ffsm.getTransitions(current, input));
		
		for(ConditionalTransition<I,O> currTr : listOfTrs){
			if(no_loop_tr.contains(currTr) && !ffsm.getInitialStates().contains(currTr.getSuccessor())){				
				List<List<ConditionalTransition<I,O>>> c_paths = new ArrayList<>(allPaths.get(currTr.getSuccessor()));
				List<List<ConditionalTransition<I,O>>> lc_paths = allPaths.get(current);
				if(check_path_coverage(ffsm,currTr.getSuccessor(), c_paths)){
					if(!covered_fc.contains(currTr.getSuccessor())){
						covered_fc.add(currTr.getSuccessor());
					}
					continue;
				}
				boolean contribute = false;
				prepath: for(List<ConditionalTransition<I,O>> inc_path : lc_paths){					
					//if this c. state was found before in the previous cycle (avoid loops)
					for(ConditionalTransition<I,O> c : inc_path){
						if(c.getSuccessor().equals(currTr.getSuccessor())){
							continue prepath;
						}
					}
					ConditionalState<ConditionalTransition<I,O>> last = inc_path.get(inc_path.size()-1).getPredecessor();
					List<ConditionalTransition<I,O>> new_path = new ArrayList<>();
					if(!last.equals(currTr.getSuccessor()) && c_paths != null){						
						new_path.addAll(inc_path);
						new_path.add(currTr);
						if(!c_paths.contains(new_path) && check_valid_path(ffsm, new_path)){
							c_paths.add(new_path);
							contribute = true;
						}						
					}
				}
				//update paths
				allPaths.put(currTr.getSuccessor(), c_paths);
				if(contribute){
					rec_find_paths(ffsm, currTr.getSuccessor(), covered_fc, no_loop_tr, allPaths);
				}
			}			
		}
	}


	private static <I, O> boolean check_valid_path(FeaturedMealy<I, O> ffsm,
			List<ConditionalTransition<I,O>> new_path) {
		
		FeatureModel fm = (FeatureModel) ffsm.getFeatureModel().clone();
		ArrayList<Node> pathClause = new ArrayList<>();

		IConstraint stateConstr = fact.createConstraint(fm, new_path.get(new_path.size()-1).getSuccessor().getCondition());
		fm.addConstraint(stateConstr); // check origin condition;
		for (ConditionalTransition<I,O> path : new_path){
			pathClause.add(path.getPredecessor().getCondition());
			pathClause.add(path.getCondition());
		}
		IConstraint andPathConstrs = fact.createConstraint(fm, new And(pathClause));
		fm.addConstraint(stateConstr);  fm.addConstraint(andPathConstrs); 
		Configuration conf = new Configuration(fm);
		if(conf.number(MIN_CONFIGURATIONS)==0) {
			return false;
		}
		return true;
	}

	private static <I,O> boolean check_path_coverage(
			FeaturedMealy<I, O> ffsm,
			ConditionalState<ConditionalTransition<I,O>> successor, 
			List<List<ConditionalTransition<I,O>>> list) {
		
		FeatureModel fm = (FeatureModel) ffsm.getFeatureModel().clone();
		List<Node> pathClause = new ArrayList<>();
		IConstraint stateConstr = fact.createConstraint(fm, successor.getCondition());
		for (List<ConditionalTransition<I,O>> path : list){
			List<Node> notAndClauses = new ArrayList<>();
			for (ConditionalTransition<I,O> condTr : path) {
				notAndClauses.add(condTr.getPredecessor().getCondition());
				notAndClauses.add(condTr.getCondition());
			}
			pathClause.add(new Not(new And(notAndClauses)));
		}
		IConstraint pathConstr = fact.createConstraint(fm, new And(pathClause));
		fm.addConstraint(stateConstr); // check origin condition;
		fm.addConstraint(pathConstr); // check origin condition
		Configuration conf = new Configuration(fm);
		if(conf.number(MIN_CONFIGURATIONS)!=0) {
			return false;
		}
		return true;
	}

	public static <I, O> boolean isMinimal(FeaturedMealy<I,O> ffsm){
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
//			Collections.reverse(abc);
			Alphabet<String> alphabet = Alphabets.fromCollection(abc);
			ffsm = new FeaturedMealy<>(alphabet,fm);
			
			ConditionalState<ConditionalTransition<String,String>> s0 = null;
			Map<Integer,ConditionalState<ConditionalTransition<String,String>>> statesMap = new HashMap<>();
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
			/*
			Collection<ConditionalTransition<String>> condTr = null;
			int totTrs = 0; 
			for (ConditionalState<ConditionalTransition<String>> state : ffsm.getStates()) {
				for (String input : ffsm.getInputAlphabet()) {
					condTr = ffsm.getTransitions(state, input);
					totTrs+=condTr.size();
					for (ConditionalTransition<String> tr : condTr) {
						System.out.println(String.format("%d@[%s] -- %s@[%s] / %s -> %d@[%s]", 
								state.getId(),
								formatNode(state.getCondition()),
								input,
								formatNode(tr.getCondition()),
								tr.getOutput(),
								tr.getSuccessor().getId(),
								formatNode(ffsm.getState(tr.getSuccessor().getId()).getCondition())
								));
					}
				}
			}
			System.out.println(condTr); System.out.println(condTr.size()); System.out.println(totTrs);
			*/
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
