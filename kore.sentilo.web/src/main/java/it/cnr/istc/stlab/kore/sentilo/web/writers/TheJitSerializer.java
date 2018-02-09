package it.cnr.istc.stlab.kore.sentilo.web.writers;

import it.cnr.istc.stlab.tipalo.api.impl.RDFUtils;
import it.cnr.istc.stlab.tipalo.api.jena.JenaSparqlProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class TheJitSerializer {

    public static final String MIME_TYPE = "application/json+jit";
    
    public static JSONArray adapt(TripleCollection tc){
        
        String sparql = "SELECT ?class WHERE{" +
                "{?ind a ?class}" +
                "UNION" +
                "{?x " + org.apache.clerezza.rdf.ontologies.RDFS.subClassOf + " ?class}" +
                "UNION" +
                "{?class " + org.apache.clerezza.rdf.ontologies.RDFS.subClassOf + "?y}" +
                "UNION" +
                "{?v " + OWL.equivalentClass + " ?class}" +
                "UNION" +
                "{?class " + RDF.type + " " + OWL.Class + "}" +
                "}";

        ResultSet resultSet = (ResultSet) JenaSparqlProvider.execute(new TcManager(), tc, sparql);
        
        
        List<String> classes = new ArrayList<String>();
        while(resultSet.hasNext()){
            SolutionMapping solutionMapping = resultSet.next();
            classes.add(RDFUtils.getString(solutionMapping.get("class")));
        }
        
        Map<NonLiteral,List<Triple>> rdfGraphMap = new HashMap<NonLiteral,List<Triple>>();
        
        Set<UriRef> objects = new HashSet<UriRef>();
        Iterator<Triple> it = tc.iterator();
        while(it.hasNext()){
            Triple triple = it.next();
            NonLiteral subject = triple.getSubject();
            Resource object = triple.getObject();
            if(object instanceof UriRef)
                objects.add((UriRef)object);
            
            List<Triple> triples = rdfGraphMap.get(subject);
            if(triples == null){
                triples = new ArrayList<Triple>();
                rdfGraphMap.put(subject, triples);
            }
            triples.add(triple);
        }
        
        JSONArray jsonArray = new JSONArray();
        Set<NonLiteral> subjects = rdfGraphMap.keySet();
        
        Set<NonLiteral> addedNodes = new HashSet<NonLiteral>();
        
        for(NonLiteral subject : subjects){
            if(!addedNodes.contains(subject)){
                List<Triple> triplesList = rdfGraphMap.get(subject);
                
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", RDFUtils.getString(subject));
                    jsonObject.put("name", getId(RDFUtils.getString(subject)));
                    
                    JSONObject nodeData = new JSONObject();
                    nodeData.put("isClass", classes.contains(RDFUtils.getString(subject)));
                    if(classes.contains(RDFUtils.getString(subject))){
                        nodeData.put("$color", "#E19F1B");
                        nodeData.put("$type", "circle");
                        nodeData.put("$dim", 10);
                    }
                    else{
                        nodeData.put("$color", "#83548B");
                        nodeData.put("$type", "diamond");
                        nodeData.put("$dim", 10);
                    }
                    
                    jsonObject.put("data", nodeData);
                    
                    JSONArray adjacences = new JSONArray();
                    for(Triple triple : triplesList){
                        JSONObject adjacence = new JSONObject();
                        Resource object = triple.getObject();
                        if(object instanceof UriRef){
                            adjacence.put("nodeTo", RDFUtils.getString((UriRef)object));
                            JSONObject data = new JSONObject();
                            data.put("property", RDFUtils.getString(triple.getPredicate()));
                            data.put("subject", RDFUtils.getString(triple.getSubject()));
                            data.put("object", RDFUtils.getString((UriRef)triple.getObject()));
                            adjacence.put("data", data);
                        }
                        adjacences.put(adjacence);
                    }
                    
                    jsonObject.put("adjacencies", adjacences);
                    jsonArray.put(jsonObject);
                    
                    addedNodes.add(subject);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }
        
        for(UriRef object : objects){
            if(!addedNodes.contains(object)){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", RDFUtils.getString(object));
                    jsonObject.put("name", getId(RDFUtils.getString(object)));
                    
                    JSONObject nodeData = new JSONObject();
                    nodeData.put("isClass", classes.contains(RDFUtils.getString(object)));
                    if(classes.contains(RDFUtils.getString(object))){
                        nodeData.put("$color", "#E19F1B");
                        nodeData.put("$type", "circle");
                        nodeData.put("$dim", 10);
                    }
                    else{
                        nodeData.put("$color", "#83548B");
                        nodeData.put("$type", "diamond");
                        nodeData.put("$dim", 10);
                    }
                    
                    jsonObject.put("data", nodeData);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                jsonArray.put(jsonObject);
                addedNodes.add(object);
            }
        }
        
        return jsonArray;
    }
    
    private static String getId(String uri){
        uri = uri.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        uri = uri.replace("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#", "dul:"); 
        uri = uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
        uri = uri.replace("http://www.w3.org/2002/07/owl#", "owl:");
        uri = uri.replace("http://schema.org/", "schemaorg:");
        uri = uri.replace("http://dbpedia.org/ontology/", "dbpedia-owl:");
        uri = uri.replace("http://dbpedia.org/resource/", "dbpedia:");
        uri = uri.replace("http://www.ontologydesignpatterns.org/ont/fred/domain.owl#", "fred:");
        uri = uri.replace("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#", "boxing:");
        return uri;
    }
    
    
}
