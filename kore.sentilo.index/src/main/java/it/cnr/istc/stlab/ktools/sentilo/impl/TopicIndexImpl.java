package it.cnr.istc.stlab.ktools.sentilo.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import it.cnr.istc.stlab.ktools.sentilo.api.TopicIndex;
import it.cnr.istc.stlab.tipalo.api.JenaToClerezzaConverter;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andrea Nuzzolese
 *
 */

@Component(enabled = false, immediate = false, metatype = true)
@Service(TopicIndex.class)
public class TopicIndexImpl implements TopicIndex {

    Logger log = LoggerFactory.getLogger(TopicIndexImpl.class);
    
    public static final String STANBOL_ENTITYHUB_INSTANCE_ADDRESS = "it.cnr.istc.stlab.ktools.sentilo.index.entityhub";
    public static final String _STANBOL_ENTITYHUB_INSTANCE_ADDRESS_DEFAULT_ = "http://localhost:9494/entityhub/entity";
    
    @Property(name = STANBOL_ENTITYHUB_INSTANCE_ADDRESS, value = _STANBOL_ENTITYHUB_INSTANCE_ADDRESS_DEFAULT_)
    private String stanbolAddress;
    
    @Reference
    private Parser parser;
    
    @Override
    public void addTopic(UriRef id, TripleCollection tripleCollection) {
        
        
        String idString = id.toString().replace("<", "").replace(">", "");
        URL url;
        try {
            
            String requestAddress = stanbolAddress;
            if(!stanbolAddress.endsWith("/")){
                requestAddress += "/";
            }
            
            url = new URL(requestAddress + "?id=" + idString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "text/turtle");
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            
            String graphString = JenaToClerezzaConverter.printTripleCollection(tripleCollection, "text/turtle");
            
            wr.write(graphString);
            wr.flush();
            wr.close();
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public TripleCollection getTopic(UriRef id) {
        
        TripleCollection tripleCollection = null;
        
        String idString = id.toString().replace("<", "").replace(">", "");
        
        URL url;
        try {
            
            String requestAddress = stanbolAddress;
            if(stanbolAddress.endsWith("/")){
                requestAddress += "lookup/";
            }
            else{
                requestAddress += "/lookup/";
            }
            url = new URL(requestAddress + "?id=" + idString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "text/turtle");
            
            InputStream inputStream = connection.getInputStream();
            
            tripleCollection = parser.parse(inputStream, "text/turtle");
            
            
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        
        return tripleCollection;
    }
    
    @Override
    public TripleCollection getTopic(String name) {
        TripleCollection tripleCollection = null;
        
        URL url;
        try {
            
            String requestAddress = stanbolAddress;
            if(stanbolAddress.endsWith("/")){
                requestAddress += "find/";
            }
            else{
                requestAddress += "/find/";
            }
            url = new URL(requestAddress + "?name=" + name);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "text/turtle");
            
            InputStream inputStream = connection.getInputStream();
            
            tripleCollection = parser.parse(inputStream, "text/turtle");
            
            
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        
        return tripleCollection;
        
    }
    
}
