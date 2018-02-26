package it.cnr.istc.stlab.kore.sentilo.web.resources;

import static it.cnr.istc.stlab.tipalo.web.CorsHelper.addCORSOrigin;
import static it.cnr.istc.stlab.tipalo.web.CorsHelper.enableCORS;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import it.cnr.istc.stlab.kore.sentilo.web.SentiloFragment;
import it.cnr.istc.stlab.tipalo.api.FredInput;
import it.cnr.istc.stlab.tipalo.api.FredWrapper;
import it.cnr.istc.stlab.tipalo.api.jena.JenaSparqlProvider;
import it.cnr.istc.stlab.tipalo.drt.fred.graphviz.GraphViz;
import it.cnr.istc.stlab.tipalo.web.ContextHelper;
import it.cnr.istc.stlab.tipalo.web.resource.BaseTipaloResource;
import it.cnr.istc.stlab.tipalo.web.writers.FREDResult;

import java.net.URL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.view.ImplicitProduces;
import com.sun.jersey.api.view.Viewable;

/**
 * 
 * @author anuzzolese
 * 
 */
@Path("/sentilo")
@ImplicitProduces(MediaType.TEXT_HTML + ";qs=2")
public class SentiloResource extends BaseTipaloResource {

    private Logger log = LoggerFactory.getLogger(getClass());

    private FredWrapper fredWrapper;
    
    private FREDResult fredResult;
    
    private String locationURL;
    
    private Serializer serializer;
    
    private GraphViz graphViz;
    
    private TcManager tcManager;
    
    private final String baseUri = "http://wit.istc.cnr.it/stlab-tools";
    
    
    /**
     * To get the RuleStoreImpl where are stored the rules and the recipes
     * 
     * @param servletContext
     *            {To get the context where the REST service is running.}
     */
    public SentiloResource(@Context ServletContext servletContext) {

        this.serializer = ContextHelper.getServiceFromContext(Serializer.class, servletContext);
        BundleContext bundleContext = ContextHelper.getBundleContext(servletContext);
        
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(FredWrapper.class.getName(), "(component.name=it.cnr.istc.stlab.ktools.sentilo.FREDSentilo)");
            log.info("Sentilo registered services {}", serviceReferences.length);
            fredWrapper = (FredWrapper)bundleContext.getService(serviceReferences[0]);
            log.info("Sentilo registered service {}",  fredWrapper.getClass());
            
        } catch (InvalidSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        this.graphViz = ContextHelper.getServiceFromContext(GraphViz.class, servletContext);
        this.tcManager = ContextHelper.getServiceFromContext(TcManager.class, servletContext);

    }
    /*
    @GET
    @Produces(TEXT_HTML)
    public Response get(@Context HttpHeaders headers) {
        ResponseBuilder responseBuilder = Response.ok(new Viewable("index", this), TEXT_HTML);
       
        return responseBuilder.build();
    }
    */
    
    
    
    /**
     * This method implements a REST service that allows to create a new empty recipe in the store with a
     * given description.<br/>
     * The description parameter is OPTIONAL.
     * 
     * @param recipeID
     *            {@link String}
     * @param description
     *            {@link String} - OPTIONAL
     * @param headers
     *            {@link HttpHeaders}
     * @return <ul>
     *         <li>200 - if the recipe is created</li>
     *         <li>409 - if a recipe with the same identifier exists in the store</li>
     *         </ul>
     */
    @GET
    @Produces(value={MediaType.TEXT_HTML+";accept-charset=UTF-8",
                     "text/functional",
                     MediaType.TEXT_PLAIN,
                     SupportedFormat.N3,
                     SupportedFormat.N_TRIPLE,
                     SupportedFormat.RDF_XML,
                     SupportedFormat.TURTLE,
                     SupportedFormat.X_TURTLE,
                     SupportedFormat.RDF_JSON,
                     MediaType.APPLICATION_JSON,
                     MediaType.APPLICATION_OCTET_STREAM,
                     "application/json+jit"})
    public Response getRDF(@QueryParam("text") String text, @QueryParam("format") String format, @QueryParam("prefix") String prefix, @QueryParam("namespace") String namespace, @QueryParam("scores") boolean showOnlyScores, @QueryParam("sentiwordnet") boolean sentiwordnet, @Context HttpHeaders headers) {
    

        if(namespace == null){
            namespace = "http://www.ontologydesignpatterns.org/ont/fred/domain.owl#";
        }
        
        if(prefix == null){
            prefix = "fred:";
        }
        
        String inputPrefix = prefix;
        
        if(inputPrefix.endsWith(":")){
            inputPrefix = inputPrefix.substring(0, inputPrefix.length()-2);
        }
        
        Dictionary<String,Object> configuration = new Hashtable<String,Object>();
        configuration.put(FredInput.NAMESPACE_PREFIX, inputPrefix);
        configuration.put(FredInput.NAMESPACE_URI, namespace);
        configuration.put(FredInput.FRAMENET_ROLES, false);
        configuration.put(FredInput.TENSE, false);
        configuration.put("sentiwordnet", sentiwordnet);
        
        
        FredInput fredInput = new FredInput(text, configuration);
        
        List<String> accepts = headers.getRequestHeader("Accept");
        
        log.debug("Received media type: {}", accepts.toString());
        
        boolean isHtml = false;
        for(int j=0, k=accepts.size(); j<k && !isHtml; j++){
            String accept = accepts.get(j);
            
            String[] acpts = accept.split(",");
            
            for(int i=0; i<acpts.length && !isHtml; i++){
                String acpt = acpts[i];
                
                if(acpt.equals(MediaType.TEXT_HTML)){
                    isHtml = true;
                }
                
                log.info("Accept: {}", acpt);
                log.info("Accept equality test: {}", acpt.equals(MediaType.TEXT_HTML));
            }
            
        }
        TripleCollection tc = null;
        if(isHtml){
            if(text==null || text.isEmpty()){
                ResponseBuilder responseBuilder = Response.ok(new Viewable("index", this), TEXT_HTML);
                return responseBuilder.build();
            }
            else{
                /*
                try {
                    text = new String(text.getBytes(), "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    log.error(e1.getMessage(), e1);
                }
                */
                try {
                    text = new String(text.getBytes(), "ASCII");
                } catch (UnsupportedEncodingException e1) {
                    log.error(e1.getMessage(), e1);
                }
                log.info("Text: {}", text);
                
                log.info("Received request to parse text with FRED. Input text: {}", text);
                if(format == null || format.trim().isEmpty()){
                    format = "image/png";
                }
                
                try {
                    
                    log.info("kapi in now instance of: {}", fredWrapper.getClass());
                    tc = fredWrapper.read(fredInput);
                    
                    /*
                    if(namespace != null && !namespace.equals("http://www.ontologydesignpatterns.org/ont/fred/domain.owl#")){
                        tc = modifyNamespace(tc, namespace);
                    }
                    else{
                        namespace = "http://www.ontologydesignpatterns.org/ont/fred/domain.owl#";
                    }
                    
                    if(prefix == null){
                        prefix = "fred:";
                    }
                    */
                    
                    if(tc != null && showOnlyScores){
                        String sparql = "CONSTRUCT {?topic <http://ontologydesignpatterns.org/ont/sentilo.owl#score> ?score}" +
                                        "WHERE{?topic <http://ontologydesignpatterns.org/ont/sentilo.owl#score> ?score}";
                        
                        tc = (TripleCollection)JenaSparqlProvider.execute(tcManager, tc, sparql);
                    }
                    
                    fredResult = new FREDResult(text, MediaType.valueOf(format), prefix, namespace, tc);
                } catch (IOException e) {
		    e.printStackTrace();
                    log.error(e.getMessage(), e);
                } catch (ParseException e) {
		    e.printStackTrace();
                    log.error(e.getMessage(), e);
                }
                
                ResponseBuilder responseBuilder;
                
                
                
                if(format.equals("image/png")){
                    responseBuilder = Response.ok(new Viewable("result_png", this), TEXT_HTML);
                }
                else{
                    responseBuilder = Response.ok(new Viewable("result", this), TEXT_HTML);
                }
                
                addCORSOrigin(servletContext, responseBuilder, headers);
                return responseBuilder.build();
            }
        }
        else{
            try {
                tc = fredWrapper.read(fredInput);
                
                if(tc != null && showOnlyScores){
                    String sparql = "CONSTRUCT {?topic <http://ontologydesignpatterns.org/ont/sentilo.owl#score> ?score}" +
                                    "WHERE{?topic <http://ontologydesignpatterns.org/ont/sentilo.owl#score> ?score}";
                    
                    tc = (TripleCollection)JenaSparqlProvider.execute(tcManager, tc, sparql);
                }
                
                /*
                if(namespace != null && namespace.equals("http://www.ontologydesignpatterns.org/ont/fred/domain.owl#")){
                    tc = modifyNamespace(tc, namespace);
                }
                */
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
            
            ResponseBuilder responseBuilder = Response.ok(tc); 
            
            addCORSOrigin(servletContext, responseBuilder, headers);
            return responseBuilder.build();
        }
        
        

    }
    
    @GET
    @Path("/graphviz/{name}")
    @Produces(value={"image/png"})
    public Response getRDF(@PathParam("name") String name, @Context HttpHeaders headers) {
        ResponseBuilder responseBuilder = Response.ok(new File("/tmp/" + name));
        addCORSOrigin(servletContext, responseBuilder, headers);
        return responseBuilder.build();
    }
    
    private TripleCollection modifyNamespace(TripleCollection graph, String namespace){
        String defaultNS = "http://www.ontologydesignpatterns.org/ont/fred/domain.owl#";
        
        Iterator<Triple> tripleIt = graph.iterator();
        
        MGraph g = new SimpleMGraph();
        while(tripleIt.hasNext()){
            Triple triple = tripleIt.next();
            NonLiteral subj = triple.getSubject();
            UriRef pred = triple.getPredicate();
            Resource obj = triple.getObject();
            
            if(subj.toString().startsWith("<" + defaultNS)){
                String ns = subj.toString().replace(defaultNS, namespace);
                subj = new UriRef(ns);
            }
            
            if(pred.toString().startsWith("<" + defaultNS)){
                String ns = pred.toString().replace(defaultNS, namespace);
                pred = new UriRef(ns);
            }
            
            if(obj.toString().startsWith("<" + defaultNS)){
                String ns = obj.toString().replace(defaultNS, namespace);
                obj = new UriRef(ns);
            }
            
            g.add(new TripleImpl(subj, pred, obj));
            
            
        }
        
        return g;
    }
    
    public String getText(){
        return fredResult.getText();
    }
    
    public String getGraph(){
        
        TripleCollection tripleCollection = fredResult.getTripleCollection();
        MediaType mediaType = fredResult.getMediaType();
        
        
        if( mediaType.toString().equals("image/png")){
            //Response.seeOther(redirectUri).build();}
            String fileName =  getGraphImage(tripleCollection, fredResult.getPrefix(), fredResult.getNamespace());
            fileName = fileName.replace("http://localhost:9191", baseUri);
            return fileName;
        }
        
        //SerializingProvider serializingProvider = new TextFunctionalTCSerializer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        log.info("The media type is: {}", mediaType.toString());
        serializer.serialize(out, tripleCollection, mediaType.toString());
        //serializingProvider.serialize(out, tripleCollection, mediaType.toString());
        
        return out.toString();
    }
    
    public String getLocationURL() {
        return locationURL;
    }

    @OPTIONS
    public Response handleCorsPreflight(@Context HttpHeaders headers) {
        ResponseBuilder rb = Response.ok();
        enableCORS(servletContext, rb, headers);
        return rb.build();
    }
    
    private String getGraphImage(TripleCollection tripleCollection, String prefix, String namespace){
        
        Date d = new Date();
        String fileName = graphViz.getGraphURL(mGraphToGraphVizSource(tripleCollection, prefix, namespace), "graphviz_" + d.getTime() + ".png", "png");
        log.info("pngImage: {}", fileName);
        
        
        return fileName;
    }
    
    private String mGraphToGraphVizSource(TripleCollection tripleCollection, String prefix, String namespace){
        String source = "digraph " + "edutella" + "{ \n";

	List<Resource> visited = new ArrayList<Resource>();

	URL circle = getClass().getResource(SentiloFragment.STATIC_RESOURCE_PATH + "/img/circle.png");
	URL diamond = getClass().getResource(SentiloFragment.STATIC_RESOURCE_PATH + "/img/rombo.png");

	log.debug("Path {}", SentiloFragment.STATIC_RESOURCE_PATH + "/img/circle.png");
	log.debug("CIRCLE {}", circle.getFile());

        for (Triple triple : tripleCollection) {
            if(toShow(triple, namespace)){

		String colorliteral = "";
		String colornonliteral = "";

		NonLiteral nonLiteral = triple.getSubject();
		Resource res = triple.getObject();

		String sparql = "SELECT ?class WHERE{" +
			"{?ind a ?class}" +
			"UNION" +
			"{?x " + org.apache.clerezza.rdf.ontologies.RDFS.subClassOf + " ?class}" +
			"UNION" +
			"{?class " + org.apache.clerezza.rdf.ontologies.RDFS.subClassOf + "?y}" +
			"UNION" +
			"{?v " + OWL.equivalentClass + "?class}" +
			"UNION" +
			"{?z <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasQuality> ?class}" +
			"}";

		ResultSet resultSet = (ResultSet) JenaSparqlProvider.execute(tcManager, tripleCollection, sparql);

		List<Resource> classes = new ArrayList<Resource>();
		while(resultSet.hasNext()){
			SolutionMapping solutionMapping = resultSet.next();
			classes.add(solutionMapping.get("class"));
		}

		sparql = "SELECT ?literal WHERE{" +
			"?x ?p ?literal . FILTER(isLiteral(?literal))}";

		resultSet = (ResultSet) JenaSparqlProvider.execute(tcManager, tripleCollection, sparql);

		List<Resource> literals = new ArrayList<Resource>();
		while(resultSet.hasNext()){
			SolutionMapping solutionMapping = resultSet.next();
			literals.add(solutionMapping.get("literal"));
		}

                String subjectName = triple.getSubject().toString().replace("<", "").replace(">", "");
                
                subjectName = subjectName.replace("http://dbpedia.org/resource/", "dbpedia:")
                                .replace("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#", "dul:")
                                .replace("http://www.ontologydesignpatterns.org/ont/d0.owl#", "d0:")
                                .replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:")
                                .replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:")
                                .replace("http://www.w3.org/2002/07/owl#", "owl:")
                                .replace("http://www.w3.org/2006/03/wn/wn30/instances/", "wn30:")
                                .replace("http://www.w3.org/2006/03/wn/wn30/schema/", "wn-schema30:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#", "boxer:")
                                .replace("http://ontologydesignpatterns.org/cp/owl/semiotics.owl#", "semion:")
                                .replace("http://www.ontologydesignpatterns.org/ont/vn/abox/role/", "vn.role:")
                                .replace("http://www.ontologydesignpatterns.org/ont/vn/data/", "vn.data:")
                                .replace("http://www.w3.org/2008/05/skos#", "skos:")
                                .replace("http://xmlns.com/foaf/0.1/", "foaf:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/title.owl#", "bt:")
                                .replace("http://dbpedia.org/ontology/", "dbpedia-owl:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#", "boxing:")
                                .replace("http://www.w3.org/2001/XMLSchema#", "xsd:")
                                .replace("http://schema.org/", "schemaorg:")
				.replace("http://www.ontologydesignpatterns.org/ont/sentilo.owl#", "sentilo:")
				.replace("http://ontologydesignpatterns.org/ont/sentilo.owl#", "sentilo:")
                                .replace(namespace, prefix);
                
                
                String predicateName = triple.getPredicate().toString().replace("<", "").replace(">", "");

                
                if(!predicateName.equals(RDFS.comment.getURI()) && !predicateName.equals("http://www.ontologydesignpatterns.org/ont/fred/pos.owl#pos")){
                
                    predicateName = predicateName.replace("http://dbpedia.org/resource/", "dbpedia:")
                                .replace("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#", "dul:")
                                .replace("http://www.ontologydesignpatterns.org/ont/d0.owl#", "d0:")
                                .replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:")
                                .replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:")
                                .replace("http://www.w3.org/2002/07/owl#", "owl:")
                                .replace("http://www.w3.org/2006/03/wn/wn30/instances/", "wn30:")
                                .replace("http://www.w3.org/2006/03/wn/wn30/schema/", "wn-schema30:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#", "boxer:")
                                .replace("http://ontologydesignpatterns.org/cp/owl/semiotics.owl#", "semion:")
                                .replace("http://www.ontologydesignpatterns.org/ont/vn/abox/role/", "vn.role:")
                                .replace("http://www.ontologydesignpatterns.org/ont/vn/data/", "vn.data:")
                                .replace("http://www.w3.org/2008/05/skos#", "skos:")
                                .replace("http://xmlns.com/foaf/0.1/", "foaf:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/title.owl#", "bt:")
                                .replace("http://dbpedia.org/ontology/", "dbpedia-owl:")
                                .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#", "boxing:")
                                .replace("http://www.w3.org/2001/XMLSchema#", "xsd:")
                                .replace("http://schema.org/", "schemaorg:")
				.replace("http://www.ontologydesignpatterns.org/ont/sentilo.owl#", "sentilo:")
				.replace("http://ontologydesignpatterns.org/ont/sentilo.owl#", "sentilo:")
                                .replace(namespace, prefix);
                    
                    
                    
                    String objectString;
                    
                    Resource object = triple.getObject();
                    if(object instanceof NonLiteral){
                    
                        objectString = object.toString().replace("<", "").replace(">", "");
                        
                        objectString = objectString.replace("http://dbpedia.org/resource/", "dbpedia:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#", "dul:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/d0.owl#", "d0:")
                                    .replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:")
                                    .replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:")
                                    .replace("http://www.w3.org/2002/07/owl#", "owl:")
                                    .replace("http://www.w3.org/2006/03/wn/wn30/instances/", "wn30:")
                                    .replace("http://www.w3.org/2006/03/wn/wn30/schema/", "wn-schema30:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#", "boxer:")
                                    .replace("http://ontologydesignpatterns.org/cp/owl/semiotics.owl#", "semion:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/vn/abox/role/", "vn.role:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/vn/data/", "vn.data:")
                                    .replace("http://www.w3.org/2008/05/skos#", "skos:")
                                    .replace("http://xmlns.com/foaf/0.1/", "foaf:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/boxer/title.owl#", "bt:")
                                    .replace("http://dbpedia.org/ontology/", "dbpedia-owl:")
                                    .replace("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#", "boxing:")
                                    .replace("http://www.w3.org/2001/XMLSchema#", "xsd:")
                                    .replace("http://schema.org/", "schemaorg:")
				    .replace("http://ontologydesignpatterns.org/ont/sentilo.owl#", "sentilo:")
                                    .replace(namespace, prefix);
                    }
                    else{
                        objectString = ((Literal)object).getLexicalForm();
                        objectString = objectString.replaceAll("\"", "'");
                        
                        log.info("LITERAL: {}", objectString);
                    }
		    if(predicateName.equals("sentilo:hasText")) {
			Resource object1 = triple.getObject();
			String objectString1 = ((Literal)object1).getLexicalForm();
			objectString1 = objectString1.replaceAll("\"", "'");
 			source += "\""+objectString1+"\" [];\n ";//style=filled fillcolor=khaki1];\n ";
			colorliteral = " bgcolor='#FFFF99;' ";
			colornonliteral = " bgcolor='#FF9966;' ";
		    }
		    else {
		    	if(subjectName.contains("opinion_sentence")==false && subjectName.contains("opinionated_context")==false && subjectName.contains("opinion_trigger_context")==false) {
			    source += "\"" + subjectName + "\" [];\n ";//style=filled fillcolor=lightskyblue1];\n ";
			    colornonliteral = " bgcolor='#66CCFF;' ";
		    	}
			else {
 				source += "\""+subjectName+"\" [];\n ";//style=filled fillcolor=lightsalmon];\n ";
				colornonliteral = " bgcolor='#FF9966;' ";
			}
		    	if(objectString.contains("opinion_sentence")==false && objectString.contains("opinionated_context")==false && objectString.contains("opinion_trigger_context")==false) {
				if(predicateName.contains("Score")==false) {
			    		source += "\"" + objectString + "\" [];\n ";//style=filled fillcolor=lightskyblue1];\n ";
					colorliteral = " bgcolor='#66CCFF;' ";
				}
				else {
					if(objectString.startsWith("-")) {
			    			source += "\"" + objectString + "\" [];\n ";// [style=filled fillcolor=red];\n ";
						colorliteral = " bgcolor='#FF0000;' ";
					}
					else {
			    			source += "\"" + objectString + "\" [];\n ";  //[style=filled fillcolor=green];\n ";
						colorliteral = " bgcolor='#00FF00;' ";
					}
				}
				//Mood triples color //Luca
				if(predicateName.contains("Afraid") || predicateName.contains("Amused") || predicateName.contains("Angry") || predicateName.contains("Annoyed") ||
                        predicateName.contains("DontCare") || predicateName.contains("Happy") || predicateName.contains("Inspired") || predicateName.contains("Sad")){

				        Double moodValue = Double.parseDouble(objectString);
                        source += "\"" + objectString + "\" [];\n ";
                        //Set a shade of orange based on the mood value
				        if (moodValue < 0.07){
				            colorliteral = " bgcolor='#FFe0B2;'";
                        }
                        else if (moodValue < 0.1){
                            colorliteral = " bgcolor='#FFCC80;'";
                        }
                        else if (moodValue < 0.15){
                            colorliteral = " bgcolor='#FFB74D;'";
                        }
                        else if (moodValue < 0.25){
                            colorliteral = " bgcolor='#FFA726;'";
                        }
                        else if (moodValue < 1.0){
                            colorliteral = " bgcolor='#FF9800;'";
                        }

                }
                if(predicateName.contains("hasDeepSentiment"))
                    colorliteral = " bgcolor='#BA55D3'";

				if(predicateName.contains("hasAvgMood"))
				    colorliteral = " bgcolor='#00C78C'";
			}

			if(objectString.contains("opinion_trigger_context")==true || objectString.contains("opinionated_context")==true) {
 				source += "\""+objectString+"\" [];\n ";//style=filled fillcolor=lightsalmon];\n ";
				colorliteral = " bgcolor='#FF9966;' ";
			}
		    }
			
		    String style_edge = "";
		    if(predicateName.contains("_dotted")) {
			    predicateName = predicateName.replace("_dotted","");
			    style_edge = " style=dotted ";
		    }
		    if(predicateName.contains("participatesIn")) {
			    style_edge = " color=blue ";
		    }
		    if(predicateName.contains("hasPosScore")) {
			    style_edge = " color=green ";
		    }
		    if(predicateName.contains("hasNegScore")) {
			    style_edge = " color=red ";
		    }
		    if(predicateName.contains("hasAvgScore")) {
			    style_edge = " color=orange ";
		    }
		    if(predicateName.contains("playsSensitiveRole")) {
			    style_edge = " color=darkorchid1 ";
		    }
		    if(predicateName.contains("hasPosFactual")) {
			    style_edge = " color= springgreen";
		    }
		    if(predicateName.contains("hasNegFactual")) {
			    style_edge = " color=tomato ";
		    }
		    if(predicateName.contains("hasNewScore")) {
			    style_edge = " color=tan4 ";
		    }
		   // else
		    //	if(predicateName.contains("participatesIn")) {
		//		style_edge = " color=blue, fontcolor=blue ";
		  //  	}

		    if(!visited.contains(nonLiteral)) {
			    if(classes.contains(nonLiteral)){
				    source += "\"" + subjectName + "\" [shape=none, margin=0, label=<<table BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\"><tr "+colornonliteral+"><td "+colornonliteral+"><img scale=\"true\" src=\"/Volumes/Terzo/software/tipalo/circle.png\" /></td><td port=\"label\" "+colornonliteral+">" + subjectName + "</td></tr></table>>];";
			    }
			    else {
				    source += "\"" + subjectName + "\" [shape=none, margin=0, label=<<table BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\"><tr "+colornonliteral+"><td "+colornonliteral+"><img scale=\"true\" src=\"/Volumes/Terzo/software/tipalo/rombo.png\" /></td><td  port=\"label\"  "+colornonliteral+">" + subjectName + "</td></tr></table>>];";
			    }
			    visited.add(nonLiteral);
		    }
		    if(!visited.contains(res)) {
			    if(literals.contains(res)){
				    source += "\"" + objectString + "\" [shape=none, margin=0, label=<<table BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\"><tr "+colorliteral+"><td "+colorliteral+"><img scale=\"true\" src=\"/Volumes/Terzo/software/tipalo/literal.png\" /></td><td  port=\"label\" "+colorliteral+">" + objectString + "</td></tr></table>>];";
			    }
			    else{
				    if(classes.contains(res)){
					    source += "\"" + objectString + "\" [shape=none, margin=0, label=<<table BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\"><tr "+colorliteral+"><td "+colorliteral+"><img scale=\"true\" src=\"/Volumes/Terzo/software/tipalo/circle.png\" /></td><td  port=\"label\" "+colorliteral+">" + objectString + "</td></tr></table>>];";
				    }
				    else {
					    source += "\"" + objectString + "\" [shape=none, margin=0, label=<<table BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\"><tr "+colorliteral+"><td "+colorliteral+"><img scale=\"true\" src=\"/Volumes/Terzo/software/tipalo/rombo.png\" /></td><td  port=\"label\" "+colorliteral+">" + objectString + "</td></tr></table>>];";
				    }
			    }
			    visited.add(res);
		    }

                    source +=
                        "\""
                            + subjectName
                            + "\":label -> \""
                            + objectString
                            + "\":label [label=\""
                            + predicateName
                            + "\" "+style_edge+" ];\n";
                }
            }
        }


//	source += "\"diego \" [style=filled fillcolor=blue];\n ";
//	source += "\"diego \" -> \"pippo\" [label=\"cane\"];\n ";

        source += " }\n";

	//System.out.println("source:"+source);
        
        log.debug("mGraphToGraphVizSource: {}", source);
        
        return source;
    }
    
    private boolean toShow(Triple triple, String namespace){
        String subjectName = triple.getSubject().toString().replace("<", "").replace(">", "");
        String predicateName = triple.getPredicate().toString().replace("<", "").replace(">", "");
        String objectName = triple.getObject().toString().replace("<", "").replace(">", "");
        
        String earmarkNS = "http://www.essepuntato.it/2008/12/earmark#";
        String semionNS = "http://ontologydesignpatterns.org/cp/owl/semiotics.owl#";
        String objectProp = OWL2.ObjectProperty.getURI();
        String datatypeProp = OWL2.DatatypeProperty.getURI();
        String boxerPossibleType = "http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#possibleType";
        String glossProp = "http://www.w3.org/2006/03/wn/wn30/schema/gloss"; 
        //String vnDataNS = "http://www.ontologydesignpatterns.org/ont/vn/data";
        
        if(subjectName.startsWith(earmarkNS) || 
           predicateName.startsWith(earmarkNS) ||
           predicateName.startsWith(semionNS) ||
           predicateName.equals(boxerPossibleType) ||
           predicateName.equals(glossProp) ||
           objectName.startsWith(earmarkNS) ||
           objectName.equals(objectProp) ||
           objectName.equals(datatypeProp) ||
           subjectName.startsWith(namespace + "offset_") ||
           objectName.startsWith(namespace + "offset_")
	 //  predicateName.equals("http://ontologydesignpatterns.org/ont/sentilo.owl#score") ||
	 //  predicateName.equals("http://ontologydesignpatterns.org/ont/sentilo.owl#participatesIn") ||
	 //  predicateName.equals("http://ontologydesignpatterns.org/ont/sentilo.owl#hasPosFactual") ||
	 //  predicateName.equals("http://ontologydesignpatterns.org/ont/sentilo.owl#hasNegFactual") ||
	 //  predicateName.equals("http://ontologydesignpatterns.org/ont/sentilo.owl#hasSensitiveness") ||
	   /*||
           objectName.startsWith(vnDataNS)*/){
            
            return false;
        }
        else{
            return true;
        }
    }

}
