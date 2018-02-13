package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

public class SentiHolder implements SentiElement {
    
    public org.apache.clerezza.rdf.core.Resource holder;
    public org.apache.clerezza.rdf.core.Resource holdertype;
    public org.apache.clerezza.rdf.core.Resource holder_quality;
	public org.apache.clerezza.rdf.core.Resource holder_linking;
	public org.apache.clerezza.rdf.core.Resource holder_truth_value;

    
    public Vector getTypeVector() {
	return null;
    }
    
    public org.apache.clerezza.rdf.core.Resource getValue() {
	return holder;
    }

    public org.apache.clerezza.rdf.core.Resource getType() {
	return holdertype;
    }

    public org.apache.clerezza.rdf.core.Resource getQuality() {
	return holder_quality;
    }
    
    public org.apache.clerezza.rdf.core.Resource getLink() {
	return holder_linking;
    }

    public org.apache.clerezza.rdf.core.Resource getMod() {
    	return null;
    }
    
    public org.apache.clerezza.rdf.core.Resource getTruthValue() {
		return holder_truth_value;
    }
    
    public org.apache.clerezza.rdf.core.Resource getAssociated() {
	return null;
    }
    
    public SentiHolder(org.apache.clerezza.rdf.core.Resource holder, org.apache.clerezza.rdf.core.Resource holdertype, org.apache.clerezza.rdf.core.Resource holder_quality, org.apache.clerezza.rdf.core.Resource holder_truth_value, org.apache.clerezza.rdf.core.Resource holder_linking) {
	this.holder = holder;
	this.holdertype = holdertype;
	this.holder_quality = holder_quality;
	this.holder_linking = holder_linking;
    }
    
    public String toString() {
	return "holder:"+(String)this.holder.toString()+" type:"+(String)this.holdertype.toString()+" qual:"+(String)this.holder_quality.toString();
    }
    
    public boolean equals(Object m) {
	SentiHolder m1 = (SentiHolder)m;
	String t1 = "";
	if(holder!=null)
	    t1 = holder.toString();
	String t2 = "";
	if(holdertype!=null)
	    t2 = holdertype.toString();
	String t3 = "";
	if(holder_quality!=null)
	    t3 = holder_quality.toString();
	String t4 = "";
	if(holder_linking!=null)
	    t4 = holder_linking.toString();

	String l1 = "";
	if(m1.holder!=null)
	    l1 = m1.holder.toString();
	String l2 = "";
	if(m1.holdertype!=null)
	    l2 = m1.holdertype.toString();
	String l3 = "";
	if(m1.holder_quality!=null)
	    l3 = m1.holder_quality.toString();
	String l4 = "";
	if(m1.holder_linking!=null)
	    l4 = m1.holder_linking.toString();

	if(t1.equals(l1) && t2.equals(l2) && t3.equals(l3) && t4.equals(l4) )
	    return true;
	return false;
    }
    
}
