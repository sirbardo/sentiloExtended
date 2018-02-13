package it.cnr.istc.stlab.ktools.sentilo.senti;


public class SensitivenessEls {
    
    public String posFact;
    public String negFact;
    public String sensitiveness;
    public String othertop;
    
    public SensitivenessEls(String posFact,String negFact,String sensitiveness,String othertop) {
	this.posFact = posFact;
	this.negFact = negFact;
	this.sensitiveness = sensitiveness;
	this.othertop = othertop;
    }
    
    public String toString() {
	String ret = "posFact:"+posFact+" negFact:"+negFact+" sensitiveness:"+sensitiveness+" othertop:"+othertop;
	return ret;
    }
    
    public boolean equals(Object m) {
	
	SensitivenessEls m1 = (SensitivenessEls)m;
	String t1 = "";
	if(posFact!=null)
	    t1 = posFact;
	String t2 = "";
	if(negFact!=null)
	    t2 = negFact;
	String t3 = "";
	if(sensitiveness!=null)
	    t3 = sensitiveness;
	String t4 = "";
	if(othertop!=null)
	    t4 = othertop;
	
	String l1 = "";
	if(m1.posFact!=null)
	    l1 = m1.posFact;
	String l2 = "";
	if(m1.negFact!=null)
	    l2 = m1.negFact;
	String l3 = "";
	if(m1.sensitiveness!=null)
	    l3 = m1.sensitiveness;
	String l4 = "";
	if(m1.othertop!=null)
	    l4 = m1.othertop;
	
	if(t1.equals(l1) && t2.equals(l2) && t3.equals(l3) && t4.equals(l4)) 
	    return true;
	return false;
    }
    
}
