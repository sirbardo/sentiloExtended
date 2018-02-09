package it.cnr.istc.stlab.tipalo.drt.fred.web.resources;

import it.cnr.istc.stlab.tipalo.api.FredInput;
import it.cnr.istc.stlab.tipalo.api.OntologyGenerator;
import it.cnr.istc.stlab.tipalo.drt.fred.Fred;
import it.cnr.istc.stlab.tipalo.web.writers.TextFunctionalTCSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.core.sparql.QueryEngine;
import org.apache.clerezza.rdf.jena.sparql.JenaSparqlEngine;
import org.apache.clerezza.rdf.simple.storage.SimpleTcProvider;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class FREDResourceTest {
    
    static OntologyGenerator ontologyGenerator;
    static SerializingProvider serializer;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        class SpecialTcManager extends TcManager {
            public SpecialTcManager(QueryEngine qe, WeightedTcProvider wtcp) {
                super();
                bindQueryEngine(qe);
                bindWeightedTcProvider(wtcp);
            }
        }
        
        
        QueryEngine qe = new JenaSparqlEngine();
        WeightedTcProvider wtcp = new SimpleTcProvider();
        
        TcManager tcManager = new SpecialTcManager(qe, wtcp);
        ontologyGenerator = new Fred(new URI("http://wit.istc.cnr.it/stlab-tools/fred/api"), tcManager);
        
        serializer = new TextFunctionalTCSerializer();
    }
    
    @Test
    public void serializingTest(){
        String text = "UFO is a British television science fiction series created by Gerry Anderson and Sylvia Anderson";
        
        TripleCollection tc = ontologyGenerator.generate(new FredInput(text, null, null, false, false));
        
        OutputStream out = new ByteArrayOutputStream();
        serializer.serialize(out, tc, TextFunctionalTCSerializer.SUPPORTED_FORMAT);
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
        
        InputStreamReader reader = new InputStreamReader(inputStream);
        
        BufferedReader bufferedReader = new BufferedReader(reader);
        
        String line = null;
        String content = "";
        try {
            while((line = bufferedReader.readLine()) != null){
                content += line;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println(content);
        
        
    }
}
