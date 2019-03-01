package uk.le.ac.ffsm;

import org.prop4j.Node;
import org.prop4j.NodeReader;

public class SimplifiedTransition<I,O> {
	
	
	private static final String TRUE_STRING = "TRUE";
	
	Integer si;
	I in;
	O out;
	Node condition;
	Integer sj;
	
	Object transition;
	
	
	
	public SimplifiedTransition(Integer si, I in, O out, Node condition, Integer sj) {
		this.si = si;
		this.in = in;
		this.out = out;
		this.condition = condition;
		this.sj = sj;
	}
	
	public SimplifiedTransition(Integer si, I in, O out, Integer sj) {
		this(si, in, out, null, sj);
		NodeReader nodeReader = new NodeReader();
		nodeReader.activateTextualSymbols();
		this.condition = nodeReader.stringToNode(TRUE_STRING);
	}

	public Integer getSi() {
		return si;
	}
	
	public Integer getSj() {
		return sj;
	}
	
	public I getIn() {
		return in;
	}
	
	public O getOut() {
		return out;
	}
	
	public Node getCondition() {
		return condition;
	}
	
	public Object getTransition() {
		return transition;
	}
	
	public void setTransition(Object transition) {
		this.transition = transition;
	}

}