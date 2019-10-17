package uk.le.ac.fts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.prop4j.NodeReader;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.prop4j.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.State;
import be.vibes.ts.Transition;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.xml.XmlLoaders;
import be.vibes.ts.io.xml.XmlReader;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import de.ovgu.featureide.fm.core.base.impl.FeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class FtsUtils {

	public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");

	private static final long MAX_CONFIGURATIONS = 100000;
	private static final long MIN_CONFIGURATIONS = 10;
	public static IFeatureModelFactory fact = FMFactoryManager.getDefaultFactory();

	private static FtsUtils instance;

	private FtsUtils() { }

	public static FtsUtils getInstance() {
		if(instance == null){
			FtsUtils.instance = new FtsUtils();
		}
		return instance;
	}
	
	public FeaturedTransitionSystem loadFts(File f_fm, File f_fts) throws TransitionSystemDefinitionException{		
		FeaturedTransitionSystem an_fts = XmlLoaders.loadFeaturedTransitionSystem(f_fts);
		return an_fts;
	}
	
	public String simpleConfigurationToString(SimpleConfiguration product) {
		List<String> sconf_str = new ArrayList<>();
		for(Feature feat: product.getFeatures()) {
			sconf_str.add(feat.getName());
		}
		return String.join("\t", sconf_str);
	}

	public SimpleConfiguration loadConfiguration(String config_filename) throws IOException {
		
		// load the config file (FeatureIDE format)
		File file = new File(config_filename);
		BufferedReader br = new BufferedReader(new FileReader(file));

		// HashSet with features from the config file
		Set<Feature> feat_set = new HashSet<>();
		
		while (br.ready()) {
			String line = br.readLine();
			if(line.length()>0) {
				feat_set.add(new Feature(line));
			}
			
		}
		SimpleConfiguration simple_conf = new SimpleConfiguration(feat_set);
		
		return simple_conf;
	}

	public CompactMealy<String, Word<String>> lts2fsm(TransitionSystem lts) {
		Set<String> abc_s = new HashSet<>();
		Map<String,Integer> state_h = new HashMap<>();
		List<String[]> transitions = new ArrayList<>();			
		
		for (Iterator<State> it_s = lts.states(); it_s.hasNext();) {
			State s = it_s.next();
			for (Iterator<Transition> it_t = lts.getOutgoing(s); it_t.hasNext();) {
				Transition tr = it_t.next();
				String[] atr = new String[3];
				atr[0] = tr.getSource().getName();
				atr[1] = tr.getAction().getName();
				atr[2] = tr.getTarget().getName();
				transitions.add(atr);
				abc_s.add(atr[1]);
			}
		}
		
		Alphabet<String> alphabet = Alphabets.fromCollection(abc_s);
		CompactMealy<String, Word<String>> mealy = new CompactMealy<>(alphabet); 
		for (String[] a_tr : transitions) {
			if(!state_h.containsKey(a_tr[0])) state_h.put(a_tr[0], mealy.addState());
			if(!state_h.containsKey(a_tr[2])) state_h.put(a_tr[2], mealy.addState());
			Word word = Word.epsilon().append("0");
			mealy.addTransition(state_h.get(a_tr[0]), a_tr[1], state_h.get(a_tr[2]), word);	
		}
		mealy.setInitial(state_h.get(lts.getInitialState().getName()), true);
		return mealy;
	}
}
