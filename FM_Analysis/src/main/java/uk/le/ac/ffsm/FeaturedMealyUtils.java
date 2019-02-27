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
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.prop4j.NodeWriter;
import org.prop4j.NodeWriter.Notation;

import org.prop4j.Not;
import org.prop4j.Or;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.FeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.fsm.MealyUtils;

public class FeaturedMealyUtils {
	
	public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");
	
	private static final long MAX_CONFIGURATIONS = 100000;
	private static final long MIN_CONFIGURATIONS = 10;
	public static IFeatureModelFactory fact = FMFactoryManager.getDefaultFactory();
	
	private static FeaturedMealyUtils instance;
	
	private FeaturedMealyUtils() { }
	
	public static FeaturedMealyUtils getInstance() {
		if(instance == null){
			FeaturedMealyUtils.instance = new FeaturedMealyUtils();
		}
		return instance;
	}
	
	
	public FeaturedMealy<String,String> readFeaturedMealy(File f_ffsm, IFeatureModel fm) throws IOException{
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
				String line = null;
				Map<String, Node> cInps = mapConditionalInputs(fm);
				List<String> abc = new ArrayList<>(cInps.keySet());
				Collections.sort(abc);
				Alphabet<String> alphabet = Alphabets.fromCollection(abc);
				ffsm = new FeaturedMealy<>(alphabet,fm,cInps);
				
				ConditionalState<ConditionalTransition<String,String>> s0 = null;
				Map<Integer,ConditionalState<ConditionalTransition<String,String>>> statesMap = new HashMap<>();
				Map<String,Integer> statesId = new HashMap<>();
				int stateId = 0;
				while(br.ready()){
					line = br.readLine();
					Matcher m = kissLine.matcher(line);
					if(m.matches()){
						String[] tr = new String[7];
						IntStream.range(1, tr.length+1).forEach(idx-> tr[idx-1] = m.group(idx));
						
						/* Conditional state origin */
						if(!statesId.containsKey(tr[0])) statesId.put(tr[0],stateId++);
						
						Integer si = statesId.get(tr[0]); 
						Node si_c = nodeReader.stringToNode(tr[1]);
						if(!statesMap.containsKey(si)) {
							statesMap.put(si,ffsm.addState(si_c));
							if(s0==null) {
								s0 = statesMap.get(si);
							}
						}
						
						/* Conditional Input */
						String in = tr[2];
						Node in_c = nodeReader.stringToNode(tr[3]);
						
						/* Output */
						String out = tr[4];
						
						/* Conditional state destination */
						if(!statesId.containsKey(tr[5])) statesId.put(tr[5],stateId++);
						Integer sj = statesId.get(tr[5]);
						Node sj_c = nodeReader.stringToNode(tr[6]);
						if(!statesMap.containsKey(sj)) {
							statesMap.put(sj,ffsm.addState(sj_c));
						}
						
						ffsm.addTransition(statesMap.get(si), in, statesMap.get(sj), out,in_c);
					}
				}
				
				ffsm.setInitialState(s0);
			}
			
			br.close();
	
			return ffsm;
		}
	
	public ProductMealy<String, Word<String>> loadProductMachine(File f, IFeatureModel fm) throws Exception {

		Pattern kissLine = Pattern.compile("\\s*(\\S+)\\s+--\\s+(\\S+)\\s*/\\s*(\\S+)\\s+->\\s+(\\S+)\\s*");

		BufferedReader br = new BufferedReader(new FileReader(f));

		List<String[]> trs = new ArrayList<String[]>();

		HashSet<String> abcSet = new HashSet<>();
		List<String> abc = new ArrayList<>();

		//		int count = 0;
		String configuration = br.readLine();
		String[] configurations_split = configuration.split(" ");

		while(br.ready()){
			String line = br.readLine();
			Matcher m = kissLine.matcher(line);
			if(m.matches()){
				//				System.out.println(m.group(0));
				//				System.out.println(m.group(1));
				//				System.out.println(m.group(2));
				//				System.out.println(m.group(3));
				//				System.out.println(m.group(4));

				String[] tr = new String[4];
				tr[0] = m.group(1);
				tr[1] = m.group(2); 
				if(!abcSet.contains(tr[1])){
					abcSet.add(tr[1]);
					abc.add(tr[1]);					
				}
				tr[2] = m.group(3);
				tr[3] = m.group(4);
				trs.add(tr);
			}
			//			count++;
		}

		br.close();
		
		NodeReader nodeReader = new NodeReader();
		nodeReader.activateTextualSymbols();
		
		List<Node> configuration_list = new ArrayList<>();
		for (String string : configurations_split) {
			configuration_list.add(nodeReader.stringToNode(string));
		}

		Collections.sort(abc);
		Alphabet<String> alphabet = Alphabets.fromCollection(abc);
		ProductMealy<String, Word<String>> mealym = new ProductMealy<String, Word<String>>(alphabet,fm,configuration_list);
 
		Map<String,Integer> states = new HashMap<String,Integer>();
		Integer si=null,sf=null;

		Map<String,Word<String>> words = new HashMap<String,Word<String>>();		


		WordBuilder<String> aux = new WordBuilder<>();

		aux.clear();
		aux.append(OMEGA_SYMBOL);
		words.put(OMEGA_SYMBOL.toString(), aux.toWord());

		Integer s0 = null;

		for (String[] tr : trs) {
			if(!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
			if(!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

			si = states.get(tr[0]);
			if(s0==null) s0 = si;
			sf = states.get(tr[3]);

			if(!words.containsKey(tr[1])){
				aux.clear();
				aux.add(tr[1]);
				words.put(tr[1], aux.toWord());
			}
			if(!words.containsKey(tr[2])){
				aux.clear();
				aux.add(tr[2]);
				words.put(tr[2], aux.toWord());
			}
			mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
		}

		for (Integer st : mealym.getStates()) {
			for (String in : alphabet) {
				//				System.out.println(mealym.getTransition(st, in));
				if(mealym.getTransition(st, in)==null){
					mealym.addTransition(st, in, st, OMEGA_SYMBOL);
				}
			}
		}


		mealym.setInitialState(s0);

		return mealym;
	}

	// https://github.com/vhfragal/ConFTGen-tool/blob/450dd0a0e408be6b42e223d41154eab2269427f3/work_neon_ubu/br.icmc.ffsm.ui.base/src/br/usp/icmc/feature/logic/FFSMProperties.java#L2384
	public  <I, O> boolean isDeterministic(FeaturedMealy<I,O> ffsm){
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

	public  <I, O> boolean isComplete(FeaturedMealy<I,O> ffsm){
		FeatureModel fm = (FeatureModel) ffsm.getFeatureModel().clone();

		Alphabet<I> alphabet = ffsm.getInputAlphabet();

		for (ConditionalState<ConditionalTransition<I,O>> cState : ffsm.getStates()) {

			IConstraint stateAnd = fact.createConstraint(fm, cState.getCondition());
			fm.addConstraint(stateAnd); // check origin condition
			List<Node> notAnds = new ArrayList<>(); 
			for (I inputIdx : alphabet) {
				Collection<ConditionalTransition<I,O>> outTrs = ffsm.getTransitions(cState,inputIdx);
				if(outTrs.isEmpty()) {
					return false;
				}
				notAnds.add(ffsm.getConditionalInputs().get(inputIdx));
				for (ConditionalTransition<I,O> tr : outTrs) {
					notAnds.add(new Not(new And(tr.getSuccessor().getCondition()))); // input and destination conditions
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
	
	public  <I,O> boolean isInitiallyConnected(FeaturedMealy<I,O> ffsm){
		Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid = null;
		allValid = getAllValidPaths(ffsm);
		
		for(ConditionalState<ConditionalTransition<I, O>> cstate : ffsm.getStates()){
			if(!ffsm.getInitialStates().contains(cstate)){
				if(allValid.get(cstate) == null || allValid.get(cstate).size() <= 0){
					return false; //there is no path for this state 
				} 
			}
		}
		
		//remove invalid paths
		boolean epath = check_valid_paths(ffsm,allValid);			
		// if a state has no path that reach it
		if(!epath){
			return false;
		}
		
		//check reachability of products
		Set<ConditionalState<ConditionalTransition<I, O>>> uncovStates = null;
		uncovStates = check_product_coverage(ffsm.getFeatureModel(),allValid);
		if(uncovStates.size()!=0){				
			return false;
		}
		
		//reduce redundant paths
		// TODO check why reduce_redundant_paths is buggy?
		//reduce_redundant_paths(ffsm,allValid); 
		
		//reduce set of paths
		for(ConditionalState<ConditionalTransition<I, O>> s: allValid.keySet()){
			if(!ffsm.getInitialState().equals(s)){
				reduce_state_cover(s,allValid);
			}
		}	
		
		//check reachability of products 2
		uncovStates = check_product_coverage(ffsm.getFeatureModel(),allValid);
		if(uncovStates.size()!=0){				
			return false;
		}
					
		//generate table with conditional inputs
		Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> condQSet =  null; 
		condQSet = create_state_cover_set(ffsm,allValid);
		
		return true;
	}
	
	private  <I,O> void 
			reduce_state_cover(
				ConditionalState<ConditionalTransition<I, O>> s,
				Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid) {
		// TODO Auto-generated method stub
		
	}

	private  <I,O> Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> 
		create_state_cover_set(
			FeaturedMealy<I, O> ffsm,
			Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid) {
		// TODO Auto-generated method stub
		return null;
	}

	private  <I,O> void 
		reduce_redundant_paths(
				FeaturedMealy<I, O> ffsm,
				Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid) {
		
		IFeatureModel fm = ffsm.getFeatureModel().clone();
		
		new_state:for(ConditionalState<ConditionalTransition<I, O>> s: allValid.keySet()){
			if(!ffsm.getInitialState().equals(s)){
				List<List<ConditionalTransition<I, O>>> checked_paths = new ArrayList<>();
				List<List<ConditionalTransition<I, O>>> valid_paths = new ArrayList<>();
				List<List<ConditionalTransition<I, O>>> original_paths = new ArrayList<>(allValid.get(s));
				List<Node> checked_cond = new ArrayList<>();
				
				new_path:for(List<ConditionalTransition<I, O>> path : original_paths){
					List<Node> path_nodes = new ArrayList<>();
					for(ConditionalTransition<I, O>ft: path){
//						path_cond.add(new And(ft.getPredecessor().getCondition(),ft.getCondition(),ft.getSuccessor()));
						path_nodes.add(ffsm.getConditionalInputs().get(ft.getInput()));
						path_nodes.add(ft.getPredecessor().getCondition());
						path_nodes.add(ft.getCondition());
						path_nodes.add(ft.getSuccessor().getCondition());
					}
					Node path_cond = new And(path_nodes);
					//check if this path has a cond prefix of another path
					int i=0;
					for(Node ccond : checked_cond){
						if(check_cond_prefix(fm,path_cond, ccond)){
							allValid.get(s).remove(path);
							continue new_path;
						}
						if(check_cond_prefix(fm,ccond, path_cond)){
							allValid.get(s).remove(checked_paths.get(i));
							valid_paths.remove(checked_paths.get(i));
						}						
						i++;
					}					
					checked_cond.add(path_cond);
					checked_paths.add(path);
					valid_paths.add(path);
					if(check_path_coverage(fm,s, valid_paths)){
						// remove the rest
						allValid.get(s).clear();
						allValid.get(s).addAll(valid_paths);						
						continue new_state;
					}
				}			
			}
		}
	}

	private  boolean check_cond_prefix(IFeatureModel featModel, Node cond_prefix, Node cond_seq) {
		IFeatureModel fm = featModel.clone();
		Configuration conf =  null;
		
		IConstraint andC1C2 = fact.createConstraint(fm, new And(cond_prefix,cond_seq));
		fm.addConstraint(andC1C2);
		
		conf = new Configuration(fm);
		boolean sat_andC1C2 = (conf.number(MIN_CONFIGURATIONS)!=0);
		
		IConstraint andC1notC2 = fact.createConstraint(fm, new And(cond_prefix, new Not(cond_seq)));
		fm.addConstraint(andC1notC2);
		
		conf = new Configuration(fm);
		boolean sat_andC1notC2 = (conf.number(MIN_CONFIGURATIONS)!=0);
		
		if(sat_andC1C2 && !sat_andC1notC2){
			return true;
		}
		return false;	
	}

	private  <I,O> Map<ConditionalState<ConditionalTransition<I,O>>, List<List<ConditionalTransition<I,O>>>> 
			getAllValidPaths(FeaturedMealy<I,O> ffsm){
		Map<ConditionalState<ConditionalTransition<I,O>>, List<List<ConditionalTransition<I,O>>>> allPaths = new HashMap<ConditionalState<ConditionalTransition<I,O>>,List<List<ConditionalTransition<I,O>>>>();

		ConditionalState<ConditionalTransition<I,O>> s0 = ffsm.getInitialState();
		
		Set<ConditionalTransition<I,O>> no_loop_tr = new HashSet<>();
		for (ConditionalState<ConditionalTransition<I,O>> state : ffsm.getStates()) {
			for (I input : ffsm.getInputAlphabet()) {
				for (ConditionalTransition<I,O> tr : ffsm.getTransitions(state, input)) {
					if(!tr.getPredecessor().equals(tr.getSuccessor())) no_loop_tr.add(tr);
				}
			}			
		}
		
		Set<ConditionalState<ConditionalTransition<I,O>>> found_fc = new HashSet<>();
		Set<ConditionalState<ConditionalTransition<I,O>>> nfound_fc = new HashSet<>(ffsm.getStates());
		if((ffsm.getInitialStates().size() == 1)) {
			nfound_fc.remove(s0);
			found_fc.add(s0);			
		} else {
			return allPaths;
		}

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
		return allPaths;
	}
	

	private  <I,O> void rec_find_paths(
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
				if(check_path_coverage(ffsm.getFeatureModel(),currTr.getSuccessor(), c_paths)){
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


	private  <I,O> boolean check_valid_paths(
			FeaturedMealy<I, O> ffsm,
//			IFeatureModel fm,
			Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid) {
		
		for(ConditionalState<ConditionalTransition<I, O>> state: allValid.keySet()){
			List<List<ConditionalTransition<I, O>>> aux_paths = new ArrayList<>(allValid.get(state));
			for(List<ConditionalTransition<I, O>> path : aux_paths){					
				if(!check_valid_path(ffsm, path)) {
					allValid.get(state).remove(path);
				}
			}				
		}
		
		for(ConditionalState<ConditionalTransition<I, O>> state: allValid.keySet()){
			if(allValid.get(state).size() < 1){
				return false;
			}
		}
		return true;
	}

	private  <I, O> boolean check_valid_path(
			FeaturedMealy<I, O> ffsm,
//			IFeatureModel featModel,
			List<ConditionalTransition<I,O>> new_path) {
		
//		IFeatureModel fm = featModel.clone();
		IFeatureModel fm = ffsm.getFeatureModel().clone();
		ArrayList<Node> pathClause = new ArrayList<>();

		IConstraint stateConstr = fact.createConstraint(fm, new_path.get(new_path.size()-1).getSuccessor().getCondition());
		for (ConditionalTransition<I,O> path : new_path){
			pathClause.add(path.getPredecessor().getCondition());
			pathClause.add(path.getPredecessor().getCondition());
			pathClause.add(path.getCondition());
		}
		IConstraint andPathConstrs = fact.createConstraint(fm, new And(pathClause));
		fm.addConstraint(stateConstr);
		fm.addConstraint(andPathConstrs); 
		Configuration conf = new Configuration(fm);
		if(conf.number(MIN_CONFIGURATIONS)==0) {
			return false;
		}
		return true;
	}

	private  <I,O> boolean check_path_coverage(
			IFeatureModel fm_par,
			ConditionalState<ConditionalTransition<I,O>> state, 
			List<List<ConditionalTransition<I,O>>> list) {
		
		FeatureModel fm = (FeatureModel) fm_par.clone();
		List<Node> pathClause = new ArrayList<>();
		IConstraint stateConstr = fact.createConstraint(fm, state.getCondition());
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

	private  <I,O> Set<ConditionalState<ConditionalTransition<I, O>>> 
			check_product_coverage(
					//FeaturedMealy<I, O> ffsm,
					IFeatureModel featModel,
					Map<ConditionalState<ConditionalTransition<I, O>>, List<List<ConditionalTransition<I, O>>>> allValid) {
		// TODO Auto-generated method stub
		Set<ConditionalState<ConditionalTransition<I, O>>> uncovStates = new HashSet<>() ;
		
		//check_path_coverage
		for(ConditionalState<ConditionalTransition<I, O>> state: allValid.keySet()){
			if(allValid.get(state) != null){
				if(!check_path_coverage(featModel, state, allValid.get(state))) {
					uncovStates.add(state);
				}
			}			
		}
		return uncovStates;
	}

	public  <I, O> boolean isMinimal(FeaturedMealy<I,O> ffsm){
		return false;

	}

	public  List<String> getAlphabetFromFeature(IFeature feat) {
		List<String> abc = new ArrayList<>();
		String descr = feat.getProperty().getDescription();
		descr=descr.replaceFirst("^Inputs=\\{", "").replaceFirst("\\}$", "");
		if(descr.length()==0) return abc;
		String[] inputs = descr.split(";");
		for (String in : inputs) {
			abc.add(in);
		}
		return abc;
	}
	
	public  Map<String, Node> mapConditionalInputs(IFeatureModel featModel) {
		NodeReader nodeReader = new NodeReader();
		nodeReader.activateTextualSymbols();
		Map<String,Node> conditionalInputs = new HashMap<>();
		Map<String,Set<Node>> inputCondSet = new HashMap<>();
		List<String> inputs = getAlphabetFromFeature(featModel.getStructure().getRoot().getFeature());
		for (String key : inputs) {
			String[] in_cond = key.split("@");
			in_cond[1] = in_cond[1].replaceAll("^\\[", "").replaceAll("\\]$", "");
			inputCondSet.putIfAbsent(in_cond[0], new HashSet<>());
			
			inputCondSet.get(in_cond[0]).add(nodeReader.stringToNode(in_cond[1]));
		}
		for (String key : inputCondSet.keySet()) {
			conditionalInputs.put(key, new Or(inputCondSet.get(key)));			
		}
		
		return conditionalInputs;
	}
	public  String formatNode(Node n) {
		NodeWriter nw = new NodeWriter(n);
//		nw.setNotation(Notation.INFIX);
		nw.setNotation(Notation.PREFIX);
		nw.setSymbols(NodeWriter.textualSymbols);
		nw.setEnforceBrackets(true);
		return nw.nodeToString();
	}
	
}
