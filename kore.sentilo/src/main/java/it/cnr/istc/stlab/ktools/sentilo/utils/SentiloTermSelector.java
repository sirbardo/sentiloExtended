package it.cnr.istc.stlab.ktools.sentilo.utils;

import it.cnr.istc.stlab.tipalo.api.impl.RDFUtils;
import it.cnr.istc.stlab.tipalo.api.jena.JenaSparqlProvider;

import java.util.HashSet;
import java.util.Set;

import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;

public class SentiloTermSelector {
    

    private static final String TYPE_SELECTION_QUERY = 
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
	"PREFIX pos: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> " + 
	"SELECT DISTINCT ?term " +
	"WHERE { " +
	"   ?term pos:pos pos:n . " +
	"   FILTER(REGEX(STR(?term), \"^NS\"))  . " + 
	"   {?term ?prop1 ?value} " +
	"   UNION " +
	"   {?subj ?prop2 ?term} "  + 
	"}";
    
    private String ns;
    private TcManager tcManager;
    
    public SentiloTermSelector(String ns, TcManager tcManager) {
        this.ns = ns;
        this.tcManager = tcManager;
    }
    
    
    public Set<String> select(TripleCollection tripleCollection){
        String sparql = TYPE_SELECTION_QUERY.replaceAll("NS", ns);
	
	//System.out.println(JenaToClerezzaConverter.printTripleCollection(tripleCollection, "application/rdf+xml"));
	//System.out.println("SPAR:"+sparql);
        ResultSet resultSet = (ResultSet) JenaSparqlProvider.execute(tcManager, tripleCollection, sparql);
        
        Set<String> types = new HashSet<String>();
        
        while(resultSet.hasNext()){
            SolutionMapping solutionMapping = resultSet.next();
            Resource classResource = solutionMapping.get("term");
            
            String type = RDFUtils.getLocalname(classResource);
            
            
            if(!types.contains(type)){
		
                char[] typeChars = type.toCharArray();
                
                type = "";
                
                boolean upperCase = true;
                for(char typeChar : typeChars){
                    
                    String s = new String(new char[]{typeChar});
                    
                    if(s.equals("_")){
                        upperCase = true;
                    }
                    else{
                        if(upperCase){
                            type += s.toUpperCase();
                            upperCase = false;
                        }
                        else{
                            type += s;
                        }
                    }
                }
                types.add(type);
            }
        }
        
        return types;
    }
    

}
