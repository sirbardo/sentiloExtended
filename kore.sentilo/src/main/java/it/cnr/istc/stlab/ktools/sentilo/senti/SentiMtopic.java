package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

public class SentiMtopic implements SentiElement {

	public org.apache.clerezza.rdf.core.Resource mtopic;
	public org.apache.clerezza.rdf.core.Resource mtopic_quality;
	public org.apache.clerezza.rdf.core.Resource mtopic_truth_value;
	public org.apache.clerezza.rdf.core.Resource mtopic_mod;
	public org.apache.clerezza.rdf.core.Resource mtopiclinking;
	public org.apache.clerezza.rdf.core.Resource eventmtopictype;
	public Vector mtopictype;

	public Vector getTypeVector() {
		return mtopictype;
	}

	public org.apache.clerezza.rdf.core.Resource getValue() {
		return mtopic;
	}

	public org.apache.clerezza.rdf.core.Resource getType() {
		return eventmtopictype;
	}

	public org.apache.clerezza.rdf.core.Resource getQuality() {
		return mtopic_quality;
	}

	public org.apache.clerezza.rdf.core.Resource getLink() {
		return mtopiclinking;
	}

	public org.apache.clerezza.rdf.core.Resource getMod() {
		return mtopic_mod;
	}

	public org.apache.clerezza.rdf.core.Resource getTruthValue() {
		return mtopic_truth_value;
	}

	public org.apache.clerezza.rdf.core.Resource getAssociated() {
		return null;
	}

	public SentiMtopic(org.apache.clerezza.rdf.core.Resource mtopic,
			org.apache.clerezza.rdf.core.Resource mtopic_quality,
			org.apache.clerezza.rdf.core.Resource mtopic_truth_value, org.apache.clerezza.rdf.core.Resource mtopic_mod,
			org.apache.clerezza.rdf.core.Resource mtopiclinking, Vector mtopictype,
			org.apache.clerezza.rdf.core.Resource eventmtopictype) {
		this.mtopic = mtopic;
		this.mtopictype = mtopictype;
		this.mtopic_quality = mtopic_quality;
		this.mtopic_truth_value = mtopic_truth_value;
		this.mtopic_mod = mtopic_mod;
		this.mtopiclinking = mtopiclinking;
		this.eventmtopictype = eventmtopictype;
	}

	public String toString() {
		String ret = "";
		if (mtopic != null)
			ret += "mtopic:" + (String) mtopic.toString();
		if (mtopictype != null)
			ret += " mtopictype:" + (String) mtopictype.toString();
		if (mtopic_quality != null)
			ret += " mtopic_quality:" + (String) mtopic_quality.toString();
		if (mtopic_truth_value != null)
			ret += " mtopic_truth_value:" + (String) mtopic_truth_value.toString();
		return ret;
	}

	public boolean equals(Object m) {
		SentiMtopic m1 = (SentiMtopic) m;
		String t1 = "";
		if (mtopic != null)
			t1 = mtopic.toString();
		String t2 = "";
		if (mtopictype != null) {
			for (int i = 0; i < mtopictype.size(); i++)
				t2 += ((org.apache.clerezza.rdf.core.Resource) mtopictype.get(i)).toString() + " ";
		}
		String t3 = "";
		if (mtopic_quality != null)
			t3 = mtopic_quality.toString();
		String t4 = "";
		if (mtopic_truth_value != null)
			t4 = mtopic_truth_value.toString();
		String t5 = "";
		if (mtopic_mod != null)
			t5 = mtopic_mod.toString();
		String t6 = "";
		if (mtopiclinking != null)
			t6 = mtopiclinking.toString();
		String t7 = "";
		if (eventmtopictype != null)
			t7 = eventmtopictype.toString();

		String l1 = "";
		if (m1.mtopic != null)
			l1 = m1.mtopic.toString();
		String l2 = "";
		if (m1.mtopictype != null) {
			for (int i = 0; i < m1.mtopictype.size(); i++)
				l2 += ((org.apache.clerezza.rdf.core.Resource) m1.mtopictype.get(i)).toString() + " ";
		}
		String l3 = "";
		if (m1.mtopic_quality != null)
			l3 = m1.mtopic_quality.toString();
		String l4 = "";
		if (m1.mtopic_truth_value != null)
			l4 = m1.mtopic_truth_value.toString();
		String l5 = "";
		if (m1.mtopic_mod != null)
			l5 = m1.mtopic_mod.toString();
		String l6 = "";
		if (m1.mtopiclinking != null)
			l6 = m1.mtopiclinking.toString();
		String l7 = "";
		if (m1.eventmtopictype != null)
			l7 = m1.eventmtopictype.toString();

		if (t1.equals(l1) && t2.equals(l2) && t3.equals(l3) && t4.equals(l4) && t5.equals(l5) && t6.equals(l6)
				&& t7.equals(l7))
			return true;

		return false;
	}

}
