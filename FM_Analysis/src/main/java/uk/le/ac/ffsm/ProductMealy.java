package uk.le.ac.ffsm;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.prop4j.Node;
import org.prop4j.NodeReader;

public class ProductMealy<I,O> extends CompactMealy<I, O> {

	private static final long serialVersionUID = -5631683009867401872L;
	
	private IFeatureModel featureModel;
	private static final String TRUE_STRING = "TRUE";
	private List<Node> configuration;
	
	public ProductMealy(Alphabet<I> alphabet) {
		super(alphabet);
		this.configuration = new ArrayList<>();
	}
	
	public ProductMealy(Alphabet<I> alphabet, IFeatureModel fm, Collection<Node> configuration) {
		this(alphabet);
		this.featureModel = fm;
		this.configuration.addAll(configuration);
		addTRUE_feature();
	}
	
	private void addTRUE_feature() {
		IFeature newChild = FMFactoryManager.getFactory(this.featureModel).createFeature(this.featureModel, TRUE_STRING);
		this.featureModel.addFeature(newChild);
		IFeature root = this.featureModel.getStructure().getRoot().getFeature();
		root.getStructure().addChild(newChild.getStructure());
		newChild.getStructure().setMandatory(true);
	}
	public Collection<Node> getConfiguration() {
		return Collections.unmodifiableCollection(configuration);
	}

}
