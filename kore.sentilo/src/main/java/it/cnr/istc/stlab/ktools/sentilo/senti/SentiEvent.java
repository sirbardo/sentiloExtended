package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

public class SentiEvent implements SentiElement {

	public org.apache.clerezza.rdf.core.Resource event;
	public org.apache.clerezza.rdf.core.Resource eventype;
	public org.apache.clerezza.rdf.core.Resource event_quality;
	public org.apache.clerezza.rdf.core.Resource event_truth_value;
	public org.apache.clerezza.rdf.core.Resource event_mod;
	public org.apache.clerezza.rdf.core.Resource eventlinking;

	public Vector getTypeVector() {
		return null;
	}

	public org.apache.clerezza.rdf.core.Resource getValue() {
		return event;
	}

	public org.apache.clerezza.rdf.core.Resource getType() {
		return eventype;
	}

	public org.apache.clerezza.rdf.core.Resource getQuality() {
		return event_quality;
	}

	public org.apache.clerezza.rdf.core.Resource getLink() {
		return eventlinking;
	}

	public org.apache.clerezza.rdf.core.Resource getMod() {
		return event_mod;
	}

	public org.apache.clerezza.rdf.core.Resource getTruthValue() {
		return event_truth_value;
	}

	public org.apache.clerezza.rdf.core.Resource getAssociated() {
		return null;
	}

	public SentiEvent(org.apache.clerezza.rdf.core.Resource event, org.apache.clerezza.rdf.core.Resource eventype,
			org.apache.clerezza.rdf.core.Resource event_quality,
			org.apache.clerezza.rdf.core.Resource event_truth_value, org.apache.clerezza.rdf.core.Resource event_mod,
			org.apache.clerezza.rdf.core.Resource eventlinking) {
		this.event = event;
		this.eventype = eventype;
		this.event_quality = event_quality;
		this.event_truth_value = event_truth_value;
		this.event_mod = event_mod;
		this.eventlinking = eventlinking;
	}

	public String toString() {
		// return "event:"+this.event+" type:"+(String)this.eventype+"
		// qual:"+(String)this.event_quality+"
		// truth:"+(String)this.event_truth_value+"
		// mod:"+(String)this.event_mod;
		return "";
	}

	public boolean equals(Object m) {
		SentiEvent m1 = (SentiEvent) m;
		String t1 = "";
		if (event != null)
			t1 = event.toString();
		String t2 = "";
		if (eventype != null)
			t2 = eventype.toString();
		String t3 = "";
		if (event_quality != null)
			t3 = event_quality.toString();
		String t4 = "";
		if (event_truth_value != null)
			t4 = event_truth_value.toString();
		String t5 = "";
		if (event_mod != null)
			t5 = event_mod.toString();
		String t6 = "";
		if (eventlinking != null)
			t6 = eventlinking.toString();

		String l1 = "";
		if (m1.event != null)
			l1 = m1.event.toString();
		String l2 = "";
		if (m1.eventype != null)
			l2 = m1.eventype.toString();
		String l3 = "";
		if (m1.event_quality != null)
			l3 = m1.event_quality.toString();
		String l4 = "";
		if (m1.event_truth_value != null)
			l4 = m1.event_truth_value.toString();
		String l5 = "";
		if (m1.event_mod != null)
			l5 = m1.event_mod.toString();
		String l6 = "";
		if (m1.eventlinking != null)
			l6 = m1.eventlinking.toString();

		if (t1.equals(l1) && t2.equals(l2) && t3.equals(l3) && t4.equals(l4) && t5.equals(l5) && t6.equals(l6))
			return true;

		return false;
	}

}
