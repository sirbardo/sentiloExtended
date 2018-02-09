package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

public interface SentiElement {
    
    public org.apache.clerezza.rdf.core.Resource getValue();
    
    public org.apache.clerezza.rdf.core.Resource getType();
    
    public org.apache.clerezza.rdf.core.Resource getQuality();
    
    public org.apache.clerezza.rdf.core.Resource getLink();

    public org.apache.clerezza.rdf.core.Resource getMod();

    public org.apache.clerezza.rdf.core.Resource getTruthValue();

    public org.apache.clerezza.rdf.core.Resource getAssociated();

    public Vector getTypeVector();
}
