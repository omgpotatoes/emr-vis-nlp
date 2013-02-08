package annotator.explor;

import annotator.explor.SimScoreMatchTuple;

/**
 * 
 * Simple tuple class representing a text sentence's best match 
 * in the stance structure.
 * 
 * @author alexander.p.conrad@gmail.com
 *
 */
public class SimScoreMatchTuple implements Comparable<SimScoreMatchTuple> {
	
	protected double simScore;
	protected String textPara;
	protected String stanceStructPara;
	protected boolean matchesArg;
	protected boolean matchesCat;
	protected boolean somethingMatchesArg;
	protected boolean somethingMatchesCat;
	
	public SimScoreMatchTuple(double simScore, String textPara,
			String stanceStructPara, boolean matchesArg, boolean matchesCat,
			boolean somethingMatchesArg, boolean somethingMatchesCat) {
		super();
		this.simScore = simScore;
		this.textPara = textPara;
		this.stanceStructPara = stanceStructPara;
		this.matchesArg = matchesArg;
		this.matchesCat = matchesCat;
		this.somethingMatchesArg = somethingMatchesArg;
		this.somethingMatchesCat = somethingMatchesCat;
	}

	public double getSimScore() {
		return simScore;
	}

	public void setSimScore(double simScore) {
		this.simScore = simScore;
	}

	public String getTextPara() {
		return textPara;
	}

	public void setTextPara(String textPara) {
		this.textPara = textPara;
	}

	public String getStanceStructPara() {
		return stanceStructPara;
	}

	public void setStanceStructPara(String stanceStructPara) {
		this.stanceStructPara = stanceStructPara;
	}

	public boolean getMatchesArg() {
		return matchesArg;
	}

	public void setMatchesArg(boolean matchesArg) {
		this.matchesArg = matchesArg;
	}

	public boolean getMatchesCat() {
		return matchesCat;
	}

	public void setMatchesCat(boolean matchesCat) {
		this.matchesCat = matchesCat;
	}

	public boolean getSomethingMatchesArg() {
		return somethingMatchesArg;
	}

	public void setSomethingMatchesArg(boolean somethingMatchesArg) {
		this.somethingMatchesArg = somethingMatchesArg;
	}

	public boolean getSomethingMatchesCat() {
		return somethingMatchesCat;
	}

	public void setSomethingMatchesCat(boolean somethingMatchesCat) {
		this.somethingMatchesCat = somethingMatchesCat;
	}
	
	/**
	 * sorts tuples based on simScore
	 * 
	 * 
	 */
	public int compareTo(SimScoreMatchTuple tuple2) {
		
		if (simScore < tuple2.getSimScore()) {
			return 1;
		} else if (simScore == tuple2.getSimScore()) {
			return 0;
		}
		return -1;
		
	}

	@Override
	public String toString() {
		return "SimScoreMatchTuple [simScore=" + simScore + ", textPara="
				+ textPara + ", stanceStructPara=" + stanceStructPara
				+ ", matchesArg=" + matchesArg + ", matchesCat=" + matchesCat
				+ ", somethingMatchesArg=" + somethingMatchesArg
				+ ", somethingMatchesCat=" + somethingMatchesCat + "]";
	}
	
	

}
