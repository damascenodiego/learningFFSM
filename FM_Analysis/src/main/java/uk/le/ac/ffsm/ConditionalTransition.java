package uk.le.ac.ffsm;

import org.prop4j.Node;
import org.prop4j.NodeReader;

import net.automatalib.automata.transout.impl.MealyTransition;

public class ConditionalTransition<O> extends MealyTransition<ConditionalState<ConditionalTransition<O>>, O> {

	NodeReader nodeReader = new NodeReader();
    private Node condition;

    public ConditionalTransition(ConditionalState<ConditionalTransition<O>> successor, O output, Node cond) {
        super(successor, output);
        this.condition = cond;
    }
    
    public ConditionalTransition(ConditionalState<ConditionalTransition<O>> successor, O output) {
        super(successor, output);
		nodeReader.activateTextualSymbols();
		setCondition(nodeReader.stringToNode("(TRUE)"));
    }

    public Node getCondition() {
		return condition;
	}
    
    public void setCondition(Node condition) {
		this.condition = condition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((getOutput() == null) ? 0 : getOutput().hashCode());;
		result = prime * result + ((getSuccessor() == null) ? 0 : getSuccessor().hashCode());;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConditionalTransition other = (ConditionalTransition) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (getOutput() == null) {
			if (other.getOutput() != null)
				return false;
		} else if (!getOutput().equals(other.getOutput()))
			return false;
		
		if (getSuccessor() == null) {
			if (other.getSuccessor() != null)
				return false;
		} else if (!getSuccessor().equals(other.getSuccessor()))
			return false;
		return true;
	}
}
