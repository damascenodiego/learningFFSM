package uk.le.ac.ffsm;

import net.automatalib.commons.util.nid.MutableNumericID;

public class ScorePair<Item> implements Comparable<ScorePair>{
	
	private Double score;
	private Item statei;
	private Item statej;
	private String as_str;
	
	public ScorePair(double sc, Item si, Item sj) {
		this.score = sc;
		this.statei = si;
		this.statej = sj;
		as_str = String.format("%s,%s", statei.toString(),statej.toString());	
	}
	
	public Double getScore() {
		return score;
	}
	
	public Item getStatei() {
		return statei;
	}
	
	public Item getStatej() {
		return statej;
	}

	@Override
	public int compareTo(ScorePair o) {
		int comp = Double.compare(this.score, o.score);
		if(comp != 0) return comp;
		if(this.statei instanceof MutableNumericID) {
			comp = Integer.compare(((MutableNumericID) this.statei).getId(), ((MutableNumericID)o.getStatei()).getId());
			if(comp != 0) return comp;
			comp = Integer.compare(((MutableNumericID) this.statej).getId(), ((MutableNumericID)o.getStatej()).getId());
			if(comp != 0) return comp;
		}
		return comp;
	}
	
	@Override
	public int hashCode() {
		return as_str.hashCode();
	}
	@Override
	public String toString() {
		return as_str;
	}
}