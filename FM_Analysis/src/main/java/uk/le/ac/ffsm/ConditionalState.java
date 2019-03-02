package uk.le.ac.ffsm;

import org.prop4j.Node;

import net.automatalib.automata.base.fast.AbstractFastNondetState;

public class ConditionalState<T> extends AbstractFastNondetState<T> {

	private static final long serialVersionUID = 5544896862451159256L;
	Node condition;

    public ConditionalState(int numInputs) {
        super(numInputs);
    }

    public ConditionalState(int numInputs, Node cond) {
        super(numInputs);
        this.condition = cond;
    }

    public Node getCondition() {
		return condition;
	}
    
    public void setCondition(Node cond) {
		this.condition = cond;
	}

    @Override
	public String toString() {
		Node node = this.condition;
		return this.id+"@["+((node==null)?"null":FeaturedMealyUtils.getInstance().formatNode(node))+"]";
	}
    
    @Override
    public int hashCode() {
    	return this.getId();
    }

}
