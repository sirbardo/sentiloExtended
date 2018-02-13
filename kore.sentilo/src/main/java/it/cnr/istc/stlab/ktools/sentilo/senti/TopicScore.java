package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;


public class TopicScore {
    
    String topic;
    Vector els = null;
    
    public TopicScore(String topic, String posFact, String negFact, String sensitiveness, String othertop) {
	this.topic = topic;
	els = new Vector();
	SensitivenessEls se = new SensitivenessEls(posFact,negFact,sensitiveness,othertop);
	els.add(se);
    }
    
    public void update(String posFact, String negFact, String sensitiveness,String othertop) {
	SensitivenessEls se = new SensitivenessEls(posFact,negFact,sensitiveness,othertop);
	if(els.contains(se)==false) {
	    els.add(se);
	}
    }
    
    public String toString() {
	String ret = "topic:"+topic+" els:"+els.toString();
	return ret;
    }
    
    public String getTopic() {
	return topic;
    }
    
    public Vector participatesIn() {
	Vector ret = new Vector();
	for(int i=0;i<els.size();i++) {
	    SensitivenessEls se = (SensitivenessEls)els.get(i);
	    if(se.othertop!=null)
		ret.add(se.othertop);
	}
	return ret;
    }
    
    public boolean hasSensitiveness() {
	for(int i=0;i<els.size();i++) {
	    SensitivenessEls se = (SensitivenessEls)els.get(i);
	    if(se.sensitiveness!=null)
		return true;
	}
	return false;
    }
}
