package uk.le.ac.ffsm;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.concepts.MutableProbabilistic;
import net.automatalib.automata.concepts.MutableTransitionOutput;
import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.automata.transout.impl.FastProbMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.automata.transout.impl.ProbMealyTransition;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.automata.transout.probabilistic.MutableProbabilisticMealy;
import net.automatalib.automata.transout.probabilistic.ProbabilisticOutput;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.ts.transout.DeterministicTransitionOutputTS;
import net.automatalib.words.Alphabet;

public class FeaturedMealy<I,O> 
				extends 	AbstractFastMutableNondet<ConditionalState<ConditionalTransition<O>>, I, ConditionalTransition<O>, Node, O>
				implements 	MutableTransitionOutput<ConditionalTransition<O>, O>,
                			MutableAutomaton<ConditionalState<ConditionalTransition<O>>, I, ConditionalTransition<O>, Node, O> {

	private IFeatureModel featureModel;

	public FeaturedMealy(Alphabet<I> inputAlphabet, IFeatureModel fm) {
		this(inputAlphabet);
		this.featureModel = fm;
	}
	
	public FeaturedMealy(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}

	public IFeatureModel getFeatureModel() {
		return featureModel;
	}
	
    @Override
    public ConditionalState<ConditionalTransition<O>> getSuccessor(ConditionalTransition<O> transition) {
        return transition.getSuccessor();
    }

    @Override
    public O getTransitionOutput(ConditionalTransition<O> transition) {
        return transition.getOutput();
    }

    @Override
    public Node getStateProperty(ConditionalState<ConditionalTransition<O>> state) {
        return state.getConstraint();
    }

    @Override
	public void setStateProperty(ConditionalState<ConditionalTransition<O>> state, Node constraint) {
		state.setConstraint(constraint);
	}

    @Override
    public O getTransitionProperty(ConditionalTransition<O> transition) {
    	return transition.getOutput();
    }
    
    @Override
    public void setTransitionProperty(ConditionalTransition<O> transition, O output) {
    	transition.setOutput(output);
    }

    
    @Override
    public void setTransitionOutput(ConditionalTransition<O> transition, O output) {
    	setTransitionProperty(transition, output);
    }
    
    @Override
    protected ConditionalState<ConditionalTransition<O>> createState(Node constr) {
        return new ConditionalState<ConditionalTransition<O>>(inputAlphabet.size()*2, constr);
    }

    public ConditionalTransition<O> addTransition(
    		ConditionalState<ConditionalTransition<O>> state, I input,
    		ConditionalState<ConditionalTransition<O>> successor, O output, Node cond) {
    	ConditionalTransition<O> tr = new ConditionalTransition<O>(successor, output, cond);
    	addTransition(state, input, tr);
    	return tr;
    }
    
    @Override
    public ConditionalTransition<O> addTransition(
    		ConditionalState<ConditionalTransition<O>> state, I input,
    		ConditionalState<ConditionalTransition<O>> successor, O output) {
    	ConditionalTransition<O> tr = new ConditionalTransition<O>(successor, output);
    	addTransition(state, input, tr);
    	return tr;
    }
    
    @Override
    public void addTransition(ConditionalState<ConditionalTransition<O>> state, 
    		I input, ConditionalTransition<O> tr) {
    	ArrayList<ConditionalTransition<O>> tmp_transitions = new ArrayList<>(getTransitions(state, input));
    	tmp_transitions.add(tr);
    	addTransitions(state, input, tmp_transitions);
    }
    
    @Override
    public void addTransitions(ConditionalState<ConditionalTransition<O>> state, I input,
    		Collection<? extends ConditionalTransition<O>> transitions) {
    	setTransitions(state, input, transitions);
    }

	@Override
	public ConditionalTransition<O> createTransition(ConditionalState<ConditionalTransition<O>> successor,
			O output) {
		return new ConditionalTransition<O>(successor, output);
	}

}
