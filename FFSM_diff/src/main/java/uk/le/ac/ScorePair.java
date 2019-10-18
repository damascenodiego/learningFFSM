package uk.le.ac;

public class ScorePair implements Comparable<ScorePair>{
	
	private Double score;
	private Integer statei;
	private Integer statej;
	private String as_str;
	
	public ScorePair(double sc, int si, int sj) {
		this.score = sc;
		this.statei = si;
		this.statej = sj;
		as_str = String.format("%d,%d", statei,statej);
	}
	
	public Double getScore() {
		return score;
	}
	
	public Integer getStatei() {
		return statei;
	}
	
	public Integer getStatej() {
		return statej;
	}

	@Override
	public int compareTo(ScorePair o) {
		return Double.compare(o.score, this.score);
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