package it.cnr.istc.stlab.ktools.sentilo.utils;

import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Jena2ClerezzaRdfTermConverter {

	public static Resource convert(RDFNode term){
		org.apache.clerezza.rdf.core.Resource termResource = null;
	    if(term.isLiteral()){
	    	Literal objectLiteral = term.asLiteral();
	    	String lexicalForm = objectLiteral.getLexicalForm();
	    	String dataypeUri = objectLiteral.getDatatypeURI();
	    	String language = objectLiteral.getLanguage();
	    	
	    	if(dataypeUri != null) termResource = new TypedLiteralImpl(lexicalForm, new UriRef(dataypeUri));	
	    	else if(language != null) termResource = new PlainLiteralImpl(lexicalForm, new Language(language));
	    	else termResource = new PlainLiteralImpl(lexicalForm);
	    }
	    else termResource = new UriRef(term.asResource().getURI());
	    
	    return termResource;
	}
}
