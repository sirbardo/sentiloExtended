package it.cnr.istc.stlab.ktools.sentilo.senti;

import java.util.Vector;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class FrequencyBasedWSD {

    private Location location;
    public FrequencyBasedWSD(String datasetLocation) {
        location = new Location(datasetLocation);
    }
    
    public Vector<WSDResult> doDisambiguation(String term){
	
        Vector<WSDResult> vectorOut = new Vector<WSDResult>();
        
        String sparql = "PREFIX schema: <http://www.w3.org/2006/03/wn/wn30/schema/> " +
	    "PREFIX own2dul: <http://www.ontologydesignpatterns.org/ont/wn/own2dul.owl#> " + 
	    "PREFIX xsd: <" + com.hp.hpl.jena.vocabulary.XSD.getURI() + "> " +
	    "select distinct ?word ?syn ?synset_id ?gloss ?ss ?tc" + System.getProperty("line.separator") +
	    "where { " + System.getProperty("line.separator") +
	    "?ws schema:word ?word . " + System.getProperty("line.separator") +
	    "?word schema:lexicalForm \"" + term + "\"@en-US . " + System.getProperty("line.separator") +
	    "?syn schema:containsWordSense ?ws . " + System.getProperty("line.separator") +
	    "?ws schema:tagCount ?tc . " + System.getProperty("line.separator") +
	    "?syn schema:gloss ?gloss . " + System.getProperty("line.separator") +
	    "?syn schema:synsetId ?synset_id . " + System.getProperty("line.separator") +
	    "?syn schema:lexname ?ss . " + System.getProperty("line.separator") +
	    "} " + 
	    "ORDER BY DESC(xsd:integer(?tc)) " + 
	    "LIMIT 3";
	
	/*
	  "{" +
	  "SELECT (MAX(xsd:integer(?tc)) AS ?max_tc)" +
	  "WHERE{" +
	  "?ws schema:word ?word . " + System.getProperty("line.separator") +
	  "?word schema:lexicalForm \"" + term + "\"@en-US . " + System.getProperty("line.separator") +
	  "?ws schema:tagCount ?tc . " + System.getProperty("line.separator") +
	  "}" +
	  "}" +
	  "FILTER(STR(?tc) = STR(?max_tc))" +
	  "}";
	*/
	//+ System.getProperty("line.separator") +
	//"having (?tc = max(?tc))";
	
        //System.out.println(sparql);
        
        Dataset dataset = TDBFactory.createDataset(location);
	
        Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
        //queryExecution.getContext().set(TDB.symUnionDefaultGraph, true) ;
        com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();
                
        
        
        while(resultSet.hasNext()){
            QuerySolution querySolution = resultSet.next();
            
            Resource wordResource = querySolution.getResource("word");
            Resource synResource = querySolution.getResource("syn");
            Resource ssResource = querySolution.getResource("ss");
            
            Literal synsetIDLiteral = querySolution.getLiteral("?synset_id");
            Literal glossLiteral = querySolution.getLiteral("gloss");
            Literal tcLiteral = querySolution.getLiteral("tc");
	    
            WSDResult wsdResult = new WSDResult(synResource.getURI(), synsetIDLiteral.getLexicalForm(), ssResource.getURI(), null, glossLiteral.getLexicalForm(), wordResource.getURI(), tcLiteral.getInt());
            
            vectorOut.add(wsdResult);
            
            
        }
        
        return vectorOut;
    }
    
    
    public static void main(String[] args){
        FrequencyBasedWSD frequencyBasedWSD = new FrequencyBasedWSD("/Users/andrea/tipalo/fred.launchers.standalone/target/sling/felix/bundle27/data/tdb-data/mgraph/ownAll_040612_a");
        Vector<WSDResult> results = frequencyBasedWSD.doDisambiguation("sex");
        
        for(WSDResult result : results){
            String synset = result.getSynset();
            String synsetID = result.getSynsetID();
            int frequency = result.getFrequency();
            System.out.println(synset + " with ID " + synsetID + " and tagCount " + frequency);
        }
        
    }
}
