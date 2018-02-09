package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

public class SentiSubtopic implements SentiElement {
    
    public org.apache.clerezza.rdf.core.Resource subtopic;
    public org.apache.clerezza.rdf.core.Resource subtopictype;
    public org.apache.clerezza.rdf.core.Resource subtopic_quality;
    public org.apache.clerezza.rdf.core.Resource subtopic_associated;
    public org.apache.clerezza.rdf.core.Resource subtopiclinking;
    public org.apache.clerezza.rdf.core.Resource subtopic_mod;
    public org.apache.clerezza.rdf.core.Resource subtopic_truth_value;
    
    public Vector getTypeVector() {
	return null;
    }

    public org.apache.clerezza.rdf.core.Resource getValue() {
	return subtopic;
    }

    public org.apache.clerezza.rdf.core.Resource getType() {
	return subtopictype;
    }

    public org.apache.clerezza.rdf.core.Resource getQuality() {
	return subtopic_quality;
    }
    
    public org.apache.clerezza.rdf.core.Resource getLink() {
	return subtopiclinking;
    }
    public org.apache.clerezza.rdf.core.Resource getMod() {
	return subtopic_mod;
    }
    
    public org.apache.clerezza.rdf.core.Resource getTruthValue() {
	return subtopic_truth_value;
    }
    
    public org.apache.clerezza.rdf.core.Resource getAssociated() {
	return subtopic_associated;
    }
    
    public SentiSubtopic(org.apache.clerezza.rdf.core.Resource subtopic, org.apache.clerezza.rdf.core.Resource subtopictype, org.apache.clerezza.rdf.core.Resource subtopic_quality, org.apache.clerezza.rdf.core.Resource subtopic_associated, org.apache.clerezza.rdf.core.Resource subtopiclinking,org.apache.clerezza.rdf.core.Resource subtopic_mod, org.apache.clerezza.rdf.core.Resource subtopic_truth_value) {
	this.subtopic = subtopic;
	this.subtopictype = subtopictype;
	this.subtopic_quality = subtopic_quality;
	this.subtopic_associated = subtopic_associated;
	this.subtopiclinking = subtopiclinking;
	this.subtopic_mod = subtopic_mod;
	this.subtopic_truth_value = subtopic_truth_value;
    }
    
    public String toString() {
	return "subtopic:"+(String)subtopic.toString();
    }
    
    public boolean equals(Object m) {
	SentiSubtopic m1 = (SentiSubtopic)m;
	String t1 = "";
	if(subtopic!=null)
	    t1 = subtopic.toString();
	String t2 = "";
	if(subtopictype!=null)
	    t2 = subtopictype.toString();
	String t3 = "";
	if(subtopic_quality!=null)
	    t3 = subtopic_quality.toString();
	String t4 = "";
	if(subtopic_associated!=null)
	    t4 = subtopic_associated.toString();
	String t5 = "";
	if(subtopiclinking!=null)
	    t5 = subtopiclinking.toString();
	String t6 = "";
	if(subtopic_mod!=null)
	    t6 = subtopic_mod.toString();
	String t7 = "";
	if(subtopic_truth_value!=null)
	    t7 = subtopic_truth_value.toString();
	
	
	String l1 = "";
	if(m1.subtopic!=null)
	    l1 = m1.subtopic.toString();
	String l2 = "";
	if(m1.subtopictype!=null)
	    l2 = m1.subtopictype.toString();
	String l3 = "";
	if(m1.subtopic_quality!=null)
	    l3 = m1.subtopic_quality.toString();
	String l4 = "";
	if(m1.subtopic_associated!=null)
	    l4 = m1.subtopic_associated.toString();
	String l5 = "";
	if(m1.subtopiclinking!=null)
	    l5 = m1.subtopiclinking.toString();
	String l6 = "";
	if(m1.subtopic_mod!=null)
	    l6 = m1.subtopic_mod.toString();
	String l7 = "";
	if(m1.subtopic_truth_value!=null)
	    l7 = m1.subtopic_truth_value.toString();
	
	if(t1.equals(l1) && t2.equals(l2) && t3.equals(l3) && t4.equals(l4) && t5.equals(l5) && t6.equals(l6) && t7.equals(l7) )
	    return true;
	
	return false;
    }
    
}
