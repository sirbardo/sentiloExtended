package it.cnr.istc.stlab.ktools.sentilo.api;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;

public interface TopicIndex {

    void addTopic(UriRef id, TripleCollection tripleCollection);
    
    TripleCollection getTopic(UriRef id);
    
    TripleCollection getTopic(String name);
    
}
