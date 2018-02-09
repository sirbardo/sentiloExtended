package it.cnr.istc.stlab.ktools.sentilo;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import it.cnr.istc.stlab.kapi.ontologygenerator.fred.SimpleFREDWrapper;
import it.cnr.istc.stlab.ktools.sentilo.senti.FrequencyBasedWSD;
import it.cnr.istc.stlab.ktools.sentilo.utils.Util;
import it.cnr.istc.stlab.tipalo.api.FoundationalTypeMatcher;
import it.cnr.istc.stlab.tipalo.api.FredInput;
import it.cnr.istc.stlab.tipalo.api.FredWrapper;
import it.cnr.istc.stlab.tipalo.api.OntologyGenerator;
import it.cnr.istc.stlab.tipalo.api.SentenceModelProvider;
import it.cnr.istc.stlab.tipalo.api.TypeSelector;
import opennlp.tools.util.InvalidFormatException;

@Component(immediate = true, metatype = true)
@Service(FredWrapper.class)
public class FREDSentilo implements FredWrapper {
    
    private final Logger log = LoggerFactory.getLogger(FREDSentilo.class);
    
    public static final String SENTENCE_MODEL = "it.cnr.istc.stlab.tipalo.dbpedia.sentence_model";
    
    public static final String STANBOL_ADDRESS = "it.cnr.istc.stlab.kapi.ontologygenerator.fred.stanbol.location";

    public static final String SENTIVERB_DATA_ADDRESS = "it.cnr.istc.stlab.sentilo.sentiverb.location"; // DIEGO

    public static final String SENTICNET_DATA_ADDRESS = "it.cnr.istc.stlab.sentilo.senticnet.location"; // DIEGO

    public static final String SENTIWORDNET_DATA_ADDRESS = "it.cnr.istc.stlab.sentilo.sentiwordnet.location"; // DIEGO

    public static final String SENSITIVENESS_DATA_ADDRESS = "it.cnr.istc.stlab.sentilo.sensitiveness.location"; // DIEGO

    public static final String FREQUENCYBASED_DATA_ADDRESS = "it.cnr.istc.stlab.sentilo.frequency_based.location"; // DIEGO

    //public static final String USE_SENTIWORDNET = "it.cnr.istc.stlab.sentilo.use_sentiwordnet"; // DIEGO

    private static final String _STANBOL_ADDRESS_DEFAULT_ = "";
    
    @Property(name = STANBOL_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String stanbolAddress;

    @Property(name = SENTIVERB_DATA_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String sentiverbAddress;

    @Property(name = SENTICNET_DATA_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String senticnetAddress;

    @Property(name = SENTIWORDNET_DATA_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String sentiwordnetAddress;

    @Property(name = SENSITIVENESS_DATA_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String sensitivenessAddress;

    @Property(name = FREQUENCYBASED_DATA_ADDRESS, value = _STANBOL_ADDRESS_DEFAULT_)
    private String frequencybasedAddress;

    //@Property(name = USE_SENTIWORDNET, value = _STANBOL_ADDRESS_DEFAULT_)
    //private boolean useSentiwordnet;
    
    @Reference
    private OntologyGenerator ontologyGenerator;
    
    @Reference
    private TypeSelector typeSelector;
    
    @Reference
    private FoundationalTypeMatcher foundationalTypeMatcher;
    
    @Reference(cardinality=ReferenceCardinality.MANDATORY_MULTIPLE, referenceInterface=FredWrapper.class, bind="bind", unbind="unbind", policy=ReferencePolicy.DYNAMIC)
    private FredWrapper fredWrapper;
    
    @Reference
    private TcManager tcManager;
    
    @Reference
    private SentenceModelProvider sentenceModelProvider;
    
    @Reference
    private Parser parser;
    
    public FREDSentilo() {
        
    }

    public Vector holder_act_true;
    public Vector holder_act_false;
    public Vector holder_pol_pos;
    public Vector holder_pol_neg;
    public Model model;
    public HashMap sentiwordnet;
    public HashMap sensitiveness;
    public FrequencyBasedWSD frequencyBasedWSD;
    
    protected void bind(FredWrapper fredWrapper){
        if(fredWrapper instanceof SimpleFREDWrapper){
            this.fredWrapper = fredWrapper;
        }
    }
 
    protected void unbind(FredWrapper fredWrapper){
        if(fredWrapper instanceof SimpleFREDWrapper){
            this.fredWrapper = null;
        }
    }
   
    public TripleCollection read(FredInput fredInput) throws IOException, ParseException{
        
        TripleCollection theory = new SimpleMGraph();
        
        Dictionary<String,Object> configuration = new Hashtable<String,Object>();
        configuration.put(FredInput.NAMESPACE_PREFIX, fredInput.getValue(FredInput.NAMESPACE_PREFIX));
        configuration.put(FredInput.NAMESPACE_URI, fredInput.getValue(FredInput.NAMESPACE_URI));
        configuration.put(FredInput.SEMANTIC_SUBGRAPH, false);
        configuration.put(FredInput.TENSE, false);
        configuration.put(FredInput.WSD, true);
        configuration.put(FredInput.WFD, false);
        configuration.put(FredInput.FRAMENET_ROLES, false);
        configuration.put(FredInput.ALWAYS_MERGE, false);
        configuration.put("sentiwordnet", fredInput.getValue("sentiwordnet"));
        fredInput = new FredInput(fredInput.getText(), configuration);
        TripleCollection tripleCollection = fredWrapper.read(fredInput);

        String text = fredInput.getText();
        
	    boolean useSenticnet = ((Boolean)fredInput.getValue("sentiwordnet")).booleanValue();
	    
	    System.out.println("text:"+text);
	    System.out.println("hat:"+holder_act_true.toString());
	    System.out.println("haf:"+holder_act_false.toString());
	    System.out.println("hpp:"+holder_pol_pos.toString());
	    System.out.println("hpn:"+holder_pol_neg.toString());
//	    System.out.println("sww"+sentiwordnet.toString());
	    System.out.println("ssn"+sensitiveness.toString());
	    Sentilo senti = new Sentilo(text,tripleCollection,holder_act_true,holder_act_false,holder_pol_pos,holder_pol_neg,model,sentiwordnet,null,sensitiveness,frequencyBasedWSD,useSenticnet,false);
	    //theory.clear();
	    theory.addAll(senti.getSentiTriples());

	    /* 
         * DIEGO
         * label vn.role:Agent -> source node: istanza verbo, dst node object of verb
         * label owl:sameAs -> source node: entity
         * da situation come faccio a capire chi e' l'agente della situation. //John thinks Bob is great
         * System.out.println("ciao"+tripleCollection);
         * tripleCollection;
         * add one triple of example and see if it gets generated
         * analysis, look for named entities, adjective (call senticnet), object, add more triples to tripleCollection
         */
        
        return theory;
    }
    
    /**
     * Used to configure an instance within an OSGi container.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Activate
    protected void activate(ComponentContext context) throws IOException {
        log.info("Component " + getClass() + " is being activated with context " + context);
        


        //START DIEGO
        try {
            
            /* 
             * Andrea added the following lines till -/-
             * These lines are required for setting up the environment variables automatically 
             * from a configuration file.  
             */
            Properties props = new Properties();
            URL location = context.getBundleContext().getBundle().getResource("META-INF/configuration.properties");
            props.load(location.openConnection().getInputStream());
            
            Dictionary<String,Object> configuration = new Hashtable<String,Object>();
            ((Hashtable<String,Object>)configuration).putAll((Map<? extends String,? extends Object>) context.getProperties());
            
            String value = (String)configuration.get(STANBOL_ADDRESS);
            if(value == null || value.trim().isEmpty()) configuration.put(STANBOL_ADDRESS, props.get(STANBOL_ADDRESS));
            
            value = (String)configuration.get(SENTIVERB_DATA_ADDRESS);
            if(value == null || value.trim().isEmpty()) configuration.put(SENTIVERB_DATA_ADDRESS, props.get(SENTIVERB_DATA_ADDRESS));
            
            value = (String)configuration.get(SENTICNET_DATA_ADDRESS);
            if(value == null || value.isEmpty()) configuration.put(SENTICNET_DATA_ADDRESS, props.get(SENTICNET_DATA_ADDRESS));
            
            value = (String)configuration.get(SENTIWORDNET_DATA_ADDRESS);
            if(value == null || value.isEmpty()) configuration.put(SENTIWORDNET_DATA_ADDRESS, props.get(SENTIWORDNET_DATA_ADDRESS));
            
            value = (String)configuration.get(SENSITIVENESS_DATA_ADDRESS);
            if(value == null || value.isEmpty()) configuration.put(SENSITIVENESS_DATA_ADDRESS, props.get(SENSITIVENESS_DATA_ADDRESS));
            
            value = (String)configuration.get(FREQUENCYBASED_DATA_ADDRESS);
            if(value == null || value.isEmpty()) configuration.put(FREQUENCYBASED_DATA_ADDRESS, props.get(FREQUENCYBASED_DATA_ADDRESS));
            /*
             * -/-
             */
            
            activate(configuration);

    	    holder_act_true = Util.sentiRead(sentiverbAddress+"holder_activation_true");
    	    holder_act_false = Util.sentiRead(sentiverbAddress+"holder_activation_false");
      	    holder_pol_pos = Util.sentiRead(sentiverbAddress+"holder_polarity_pos");
    	    holder_pol_neg = Util.sentiRead(sentiverbAddress+"holder_polarity_neg");
    
    	    // loading senticnet graph data...
    	    model = Util.senticnetRead(senticnetAddress+"senticnet2.rdf.xml");
    	    // end loading senticnet graph data...
    	    //
    	    // loading sentiwordnet data...
    	    sentiwordnet = Util.sentiwordnetRead(sentiwordnetAddress+"SentiWordNet_3.0.0_20130122.txt");
    	    // end loading sentiwordnet data...
    	    //
    	    // loading sensitiveness data...
    	    sensitiveness = Util.sensitivenessRead(sensitivenessAddress+"Sensitiveness.txt",sensitivenessAddress+"SensitivenessTheme.txt");
    	    // end loading sensitiveness data...
    	    //
    	    // loading frequency based
    	     frequencyBasedWSD = new FrequencyBasedWSD(frequencybasedAddress);
    	    // end loading frequency based


        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	catch(IOException ex) {
		ex.printStackTrace();
	}
    }

    /**
     * Should be called within both OSGi and non-OSGi environments.
     * 
     * @param configuration
     * @throws ClassNotFoundException 
     * @throws InvalidFormatException 
     * @throws IOException
     */
    protected void activate(Dictionary<String,Object> configuration) throws ClassNotFoundException, InvalidFormatException, IOException {

        if (stanbolAddress == null || stanbolAddress.trim().isEmpty()) {
            String value = (String) configuration.get(STANBOL_ADDRESS);
            if (value != null && !value.trim().isEmpty()) stanbolAddress = value;
            else stanbolAddress = _STANBOL_ADDRESS_DEFAULT_;
        }

        if (sentiverbAddress == null || sentiverbAddress.trim().isEmpty()) { //DIEGO
            String value = (String) configuration.get(SENTIVERB_DATA_ADDRESS); // DIEGO
            if (value != null && !value.trim().isEmpty()) sentiverbAddress = value; //DIEGO
            else sentiverbAddress = _STANBOL_ADDRESS_DEFAULT_; //DIEGO
            log.info("Sentiverb {}", sentiverbAddress);
        }

        if (senticnetAddress == null || senticnetAddress.trim().isEmpty()) { //DIEGO
            String value = (String) configuration.get(SENTICNET_DATA_ADDRESS); // DIEGO
            if (value != null && !value.trim().isEmpty()) senticnetAddress = value; //DIEGO
            else senticnetAddress = _STANBOL_ADDRESS_DEFAULT_; //DIEGO
        }

        if (sentiwordnetAddress == null || sentiwordnetAddress.trim().isEmpty()) { //DIEGO
            String value = (String) configuration.get(SENTIWORDNET_DATA_ADDRESS); // DIEGO
            if (value != null && !value.trim().isEmpty()) sentiwordnetAddress = value; //DIEGO
            else sentiwordnetAddress = _STANBOL_ADDRESS_DEFAULT_; //DIEGO
        }
	
    	if(sensitivenessAddress == null || sensitivenessAddress.trim().isEmpty()) {
    	    String value = (String) configuration.get(SENSITIVENESS_DATA_ADDRESS);
    	    if (value != null && !value.trim().isEmpty()) sensitivenessAddress = value;
    	    else sensitivenessAddress = _STANBOL_ADDRESS_DEFAULT_;
    	}

    	if(frequencybasedAddress == null || frequencybasedAddress.trim().isEmpty()) {
    	    String value = (String) configuration.get(FREQUENCYBASED_DATA_ADDRESS);
    	    if (value != null && !value.trim().isEmpty()) frequencybasedAddress = value;
    	    else frequencybasedAddress = _STANBOL_ADDRESS_DEFAULT_;
    	}
        
        if(!stanbolAddress.endsWith("/")){
            stanbolAddress += "/";
        }
        log.info("Component " + getClass() + " is active.");
    }
    
    
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("in " + getClass() + " deactivate with context " + context);
    }

    public static void main(String[] args){
        
        String timestamp = "+15280415";
        timestamp = timestamp.replace("+", "");
        long timestampValue = Long.valueOf(timestamp).longValue();
        
        Timestamp stamp = new Timestamp(timestampValue);
        System.out.println(stamp.toString());
    }

    @Override
    public String[] getHiddenTriples() {
        // TODO Auto-generated method stub
        return null;
    }
    
    

}

class TextPointerCouple {
    private int start;
    private int end;
    
    public TextPointerCouple(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public boolean sameContext(int start, int end){
        if(this.start == start && this.end == end)
            return true;
        else
            return false;
    }
}
