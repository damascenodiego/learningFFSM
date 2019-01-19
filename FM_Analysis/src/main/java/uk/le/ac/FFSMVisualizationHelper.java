package uk.le.ac;

import java.util.Map;

import org.prop4j.Node;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.automata.visualization.AutomatonVisualizationHelper;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;

public class FFSMVisualizationHelper<I,O> 
			extends AutomatonVisualizationHelper<
				ConditionalState<ConditionalTransition<I,O>>, 
				I, 
				ConditionalTransition<I,O>, 
				MutableAutomaton<ConditionalState<ConditionalTransition<I,O>>, I, ConditionalTransition<I,O>, Node, O>> {

	private boolean plotSelfloops;

	public FFSMVisualizationHelper(
			MutableAutomaton<ConditionalState<ConditionalTransition<I, O>>, I, ConditionalTransition<I, O>, Node, O> automaton) {
		super(automaton);
		 this.plotSelfloops = true;
	}
	
	
	public void setPlotSelfloops(boolean plotSelfloops) {
		this.plotSelfloops = plotSelfloops;
	}
	

	@Override
	public boolean getEdgeProperties(ConditionalState<ConditionalTransition<I, O>> src, TransitionEdge<I, ConditionalTransition<I,O>> edge, ConditionalState<ConditionalTransition<I, O>> tgt, Map<String, String> properties) {
		if (!super.getEdgeProperties(src, edge, tgt, properties)) {
			return false;
		}
		
		if(src.equals(tgt)) {
			return false;
		}

		final StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(String.valueOf(edge.getInput()));
		labelBuilder.append("@");
		labelBuilder.append("[");
		labelBuilder.append(edge.getTransition().getCondition().toString());
		labelBuilder.append("]");
		labelBuilder.append("/");
		O output = edge.getTransition().getOutput();
		if (output != null) {
			labelBuilder.append(String.valueOf(output));
		}
		properties.put(EdgeAttrs.LABEL, labelBuilder.toString());
		return true;
	}
	
	@Override
	public boolean getNodeProperties(ConditionalState<ConditionalTransition<I, O>> node,
			Map<String, String> properties) {
		if (!super.getNodeProperties(node, properties)) {
			return false;
		}
		final StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(String.valueOf(node.getId()));
		labelBuilder.append("@");
		labelBuilder.append("[");
		labelBuilder.append(node.getCondition().toString());
		labelBuilder.append("]");
		properties.put(NodeAttrs.LABEL, labelBuilder.toString());
		return super.getNodeProperties(node, properties);
	}

}
