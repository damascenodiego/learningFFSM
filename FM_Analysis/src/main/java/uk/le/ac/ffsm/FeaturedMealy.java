package uk.le.ac.ffsm;


import java.util.ArrayList;
import java.util.Collection;

import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FMFactoryManager;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.concepts.MutableTransitionOutput;
import net.automatalib.words.Alphabet;

public class FeaturedMealy<I,O> 
				extends 	AbstractFastMutableNondet<ConditionalState<ConditionalTransition<I,O>>, I, ConditionalTransition<I,O>, Node, O>
				implements 	MutableTransitionOutput<ConditionalTransition<I,O>, O>,
                			MutableAutomaton<ConditionalState<ConditionalTransition<I,O>>, I, ConditionalTransition<I,O>, Node, O> {

	private static final String TRUE_STRING = "TRUE";
	private IFeatureModel featureModel;

	public FeaturedMealy(Alphabet<I> inputAlphabet, IFeatureModel fm) {
		this(inputAlphabet);
		this.featureModel = fm;
		addTRUE_feature();
	}
	
	private void addTRUE_feature() {
		IFeature newChild = FMFactoryManager.getFactory(this.featureModel).createFeature(this.featureModel, TRUE_STRING);
		this.featureModel.addFeature(newChild);
		IFeature root = this.featureModel.getStructure().getRoot().getFeature();
		root.getStructure().addChild(newChild.getStructure());
		newChild.getStructure().setMandatory(true);
	}

	public FeaturedMealy(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}

	public IFeatureModel getFeatureModel() {
		return featureModel;
	}
	
    @Override
    public ConditionalState<ConditionalTransition<I,O>> getSuccessor(ConditionalTransition<I,O> transition) {
        return transition.getSuccessor();
    }

    @Override
    public O getTransitionOutput(ConditionalTransition<I,O> transition) {
        return transition.getOutput();
    }

    @Override
    public Node getStateProperty(ConditionalState<ConditionalTransition<I,O>> state) {
        return state.getCondition();
    }

    @Override
	public void setStateProperty(ConditionalState<ConditionalTransition<I,O>> state, Node cond) {
		state.setCondition(cond);
	}

    @Override
    public O getTransitionProperty(ConditionalTransition<I,O> transition) {
    	return transition.getOutput();
    }
    
    @Override
    public void setTransitionProperty(ConditionalTransition<I,O> transition, O output) {
    	transition.setOutput(output);
    }

    
    @Override
    public void setTransitionOutput(ConditionalTransition<I,O> transition, O output) {
    	setTransitionProperty(transition, output);
    }
    
    @Override
    protected ConditionalState<ConditionalTransition<I,O>> createState(Node constr) {
        return new ConditionalState<ConditionalTransition<I,O>>(inputAlphabet.size()*2, constr);
    }

    public ConditionalTransition<I,O> addTransition(
    		ConditionalState<ConditionalTransition<I,O>> state, I input,
    		ConditionalState<ConditionalTransition<I,O>> successor, O output, Node cond) {
    	ConditionalTransition<I,O> tr = new ConditionalTransition<>(state,input,successor, output, cond);
    	addTransition(state, input, tr);
    	return tr;
    }
    
    @Override
    public ConditionalTransition<I,O> addTransition(
    		ConditionalState<ConditionalTransition<I,O>> state, I input,
    		ConditionalState<ConditionalTransition<I,O>> successor, O output) {
    	ConditionalTransition<I,O> tr = new ConditionalTransition<I,O>(successor, output);
    	addTransition(state, input, tr);
    	return tr;
    }
    
    @Override
    public void addTransition(ConditionalState<ConditionalTransition<I,O>> state, 
    		I input, ConditionalTransition<I,O> tr) {
    	ArrayList<ConditionalTransition<I,O>> tmp_transitions = new ArrayList<>(getTransitions(state, input));
    	tmp_transitions.add(tr);
    	addTransitions(state, input, tmp_transitions);
    }
    
    @Override
    public void addTransitions(ConditionalState<ConditionalTransition<I,O>> state, I input,
    		Collection<? extends ConditionalTransition<I,O>> transitions) {
    	setTransitions(state, input, transitions);
    }

	@Override
	public ConditionalTransition<I,O> createTransition(ConditionalState<ConditionalTransition<I,O>> successor,
			O output) {
		return new ConditionalTransition<I,O>(successor, output);
	}

}
