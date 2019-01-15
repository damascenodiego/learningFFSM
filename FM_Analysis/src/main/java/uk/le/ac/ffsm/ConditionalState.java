package uk.le.ac.ffsm;

import org.prop4j.Node;

import net.automatalib.automata.base.fast.AbstractFastNondetState;
import net.automatalib.automata.transout.impl.MealyTransition;

public class ConditionalState<T> extends AbstractFastNondetState<T> {

	private static final long serialVersionUID = 5544896862451159256L;
	
	Node constraint;

    public ConditionalState(int numInputs) {
        super(numInputs);
    }

    public ConditionalState(int numInputs, Node constr) {
        super(numInputs);
        this.constraint = constr;
    }

    public Node getConstraint() {
		return constraint;
	}
    
    public void setConstraint(Node constraint) {
		this.constraint = constraint;
	}

    @Override
	public String toString() {
		return this.id+"@["+this.constraint.toString()+"]";
	}
    
    @Override
    public int hashCode() {
    	return this.getId();
    }

}
