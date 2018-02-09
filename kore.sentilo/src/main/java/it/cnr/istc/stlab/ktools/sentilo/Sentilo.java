package it.cnr.istc.stlab.ktools.sentilo;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.io.*;
import java.net.URL;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.ontologies.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;

import it.cnr.istc.stlab.ktools.sentilo.senti.FrequencyBasedWSD;
import it.cnr.istc.stlab.ktools.sentilo.senti.SensitivenessObject;
import it.cnr.istc.stlab.ktools.sentilo.senti.SentiElement;
import it.cnr.istc.stlab.ktools.sentilo.senti.SentiEvent;
import it.cnr.istc.stlab.ktools.sentilo.senti.SentiHolder;
import it.cnr.istc.stlab.ktools.sentilo.senti.SentiMtopic;
import it.cnr.istc.stlab.ktools.sentilo.senti.SentiSubtopic;
import it.cnr.istc.stlab.ktools.sentilo.senti.TopicScore;
import it.cnr.istc.stlab.ktools.sentilo.utils.Jena2ClerezzaRdfTermConverter;
import it.cnr.istc.stlab.ktools.sentilo.utils.SparqlQuerySentilo;
import it.cnr.istc.stlab.ktools.sentilo.utils.Util;
import it.cnr.istc.stlab.ktools.sentilo.utils.JenaToClerezzaConverter;
import it.cnr.istc.stlab.tipalo.api.WSDObject;
import it.cnr.istc.stlab.tipalo.api.WSDOutput;

public class Sentilo {

	private boolean USEDEFAULTSCORESTRATEGY;
	private boolean DEBUG = false;
	
	private Logger log = LoggerFactory.getLogger(getClass());

	TripleCollection tripleCollection = null;
	private String text = "";
	private TripleCollection tripleSentilo = null;
	private UriRef hot_uri = new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasOpinionTrigger");
	private Vector events = null;
	private Vector holders = null;
	private Vector mtopics = null;
	private Vector subtopics = null;
	private Set list_topics = null;
	private Model modelSenti = null;
	private HashMap sentiwordnet = null;
	private WSDOutput wsdOutput = null;
	private HashMap sensitiveness = null;
	private FrequencyBasedWSD frequencyBasedWSD = null;
	private HashMap tokenToIdx = null;
	private Map<String, Vector<Double>> adjToMoods = null;
	private Map<String, Vector<Double>> verbToMoods = null;
	private Map<String, Vector<Double>> nounToMoods = null;
	private Map<String, Vector<Double>> advToMoods = null;

	public enum Mood{
		AFRAID,	AMUSED,	ANGRY,	ANNOYED, DONT_CARE,	HAPPY,	INSPIRED, SAD
	}

	/* Prepare the content of the FILTER statement for the SPARQL query */
	private String subquery(Vector v) {
		String ret = "";
		for (int i = 0; i < v.size(); i++) {
			String el = (String) v.get(i);
			ret += "regex(str(?eventype), \"" + el.toLowerCase().trim() + "\", \"i\") || ";
		}
		ret = ret.substring(0, ret.length() - 3);
		return ret;
	}
	
	private String subqueryNew(Vector v) {
		String ret = "";
		for (int i = 0; i < v.size(); i++) {
			String el = (String) v.get(i);
			ret += "regex(str(?eventype), \"" + el.toLowerCase().trim() + "\", \"i\") || ";
		}
		ret = ret.substring(0, ret.length() - 3);
		return ret;
	}

	/*
	 * insert into tripleSentilo the paths from value having type, subClassOf
	 * and equivalentClass relations
	 */
	private void getRecursiveType(org.apache.clerezza.rdf.core.Resource value) {
		Vector ll = new Vector();
		Queue qe = new LinkedList();
		qe.add(value);

		while (qe.isEmpty() == false) {
			value = (org.apache.clerezza.rdf.core.Resource) qe.poll();
			String chain = "SELECT ?relation ?node WHERE { " + value.toString()
					+ " a|<http://www.w3.org/2000/01/rdf-schema#subClassOf>|<http://www.w3.org/2002/07/owl#equivalentClass>|((<http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#associatedWith>*)/<http://www.w3.org/2002/07/owl#sameAs>)|<http://www.w3.org/2008/05/skos#relatedMatch> ?node . "
					+ value.toString() + " ?relation ?node . { {" + value.toString() + " a ?node} UNION {"
					+ value.toString() + " <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?node} UNION {"
					+ value.toString() + " <http://www.w3.org/2002/07/owl#equivalentClass> ?node} UNION {"
					+ value.toString() + " <http://www.w3.org/2002/07/owl#sameAs> ?node } UNION { " + value.toString()
					+ " <http://www.w3.org/2008/05/skos#relatedMatch> ?node } } }";

			Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
			Query query = QueryFactory.create(chain, Syntax.syntaxARQ);
			QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
			com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

			while (resultSet.hasNext()) {
				QuerySolution sol = resultSet.next();
				UriRef relation = null;
				org.apache.clerezza.rdf.core.Resource valueob = null;
				for (String key2 : resultSet.getResultVars()) {
					if (key2.equals("relation"))
						relation = new UriRef(sol.get(key2).toString().replace("<", "").replace(">", ""));
					if (key2.equals("node")) {
						valueob = Jena2ClerezzaRdfTermConverter.convert(sol.get(key2));
					}
				}
				if (relation != null && valueob != null) {
					Triple newTriple = new TripleImpl(new UriRef(value.toString().replace("<", "").replace(">", "")),
							relation, valueob);
					tripleSentilo.add(newTriple);
					String newll = newTriple.getObject().toString();
					qe.add(newTriple.getObject());
				}
			}
		}
	}

	/*
	 * Return true if exists a direct path from node1 to node2, false otherwise
	 */
	private boolean PathExists(String node1, String node2) {
		String sparql = "SELECT ?relation ?node WHERE {" + node1
				+ " (a|<http://www.w3.org/2000/01/rdf-schema#subClassOf>|<http://www.w3.org/2002/07/owl#equivalentClass>|<http://www.w3.org/2002/07/owl#sameAs>)+ "
				+ node2 + "}";
		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();
		if (resultSet.hasNext()) {
			return true;
		}
		return false;
	}

	/* add triples related to the holder */
	private void addTripleSentiElement(Vector el, int role) { // role:1 Holder,
																// role:2 Event,
																// role:3
																// Subtopic,
																// role:4 Mtopic
		// private void addTripleHolder(Vector el) {
		for (int i = 0; i < el.size(); i++) {
			SentiElement se = (SentiElement) el.get(i);
			if (se.getValue() != null) {
				Triple newTriple = null;
				switch (role) {
				case 1: // holder
					newTriple = new TripleImpl(
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_trigger_context"),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasHolder"), se.getValue());
					getRecursiveType(se.getValue());
					break;
				case 2: // event
					newTriple = new TripleImpl(
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_trigger_context"),
							hot_uri, new UriRef(se.getValue().toString().replace("<", "").replace(">", "")));
					break;
				case 3: // Subtopic
					newTriple = new TripleImpl(
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinionated_context"),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasSubTopic"), se.getValue());
					getRecursiveType(se.getValue());
					break;
				case 4: // Mtopic
					newTriple = new TripleImpl(
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinionated_context"),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasTopic"), se.getValue());
					getRecursiveType(se.getValue());
					String top = se.getValue().toString();
					top = top.substring(top.indexOf("#") + 1, top.indexOf(">"));
					list_topics.add(top);
					break;
				}
				tripleSentilo.add(newTriple);
			}
			if (se.getTypeVector() != null) {
				for (int j = 0; j < se.getTypeVector().size(); j++) {
					org.apache.clerezza.rdf.core.Resource type = (org.apache.clerezza.rdf.core.Resource) se
							.getTypeVector().get(j);
					Triple newTriple = new TripleImpl(
							new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
							new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
							new UriRef(type.toString().replace("<", "").replace(">", "")));
					tripleSentilo.add(newTriple);
				}
				if (se.getType() != null) {
					Triple newTriple = new TripleImpl(
							new UriRef(se.getType().toString().replace("<", "").replace(">", "")),
							new UriRef("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
							new UriRef("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Event"));
					tripleSentilo.add(newTriple);
				}
			}
			if (se.getMod() != null) {
				Triple newTriple = new TripleImpl(
						new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
						new UriRef("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasModality"),
						se.getMod());
				tripleSentilo.add(newTriple);
			}
			if (se.getType() != null) {
				Triple newTriple = new TripleImpl(
						new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
						new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						se.getType());
				tripleSentilo.add(newTriple);
				if (role == 2) {
					newTriple = new TripleImpl(new UriRef(se.getType().toString().replace("<", "").replace(">", "")),
							new UriRef("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
							new UriRef("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Event"));
					tripleSentilo.add(newTriple);
				}
			}
			if (se.getTruthValue() != null) {
				Triple newTriple = new TripleImpl(
						new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
						new UriRef("http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasTruthValue"),
						se.getTruthValue());
				tripleSentilo.add(newTriple);
			}
			if (se.getLink() != null) {
				Triple newTriple = new TripleImpl(
						new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
						new UriRef("http://www.w3.org/2002/07/owl#sameAs"),
						se.getLink());
				tripleSentilo.add(newTriple);
			}
			if (se.getQuality() != null) {
				org.apache.clerezza.rdf.core.Resource word = (org.apache.clerezza.rdf.core.Resource) se.getQuality();
				String word_str = word.toString();
				String pref = word_str.substring(1, word_str.indexOf("#"));
				if (DEBUG)
					System.out.println("CCpref-de-de-e---:" + pref);
				if (pref.equals("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl"))
					continue;
				int start_idx = word_str.indexOf("#") + 1;
				int end_idx = word_str.indexOf(">", start_idx);
				word_str = word_str.substring(start_idx, end_idx).toLowerCase();

				boolean noscore = false;
				if (word_str.equals("male") || word_str.equals("female") || word_str.equals("thing"))
					noscore = true;
				if (noscore == false) {
					double score = -999.0;
					if (USEDEFAULTSCORESTRATEGY == false)
						score = Util.getScoreFromSenticNetLocal(word_str, modelSenti);
					else {
						score = Util.getScoreFromDefaultStrategy(word_str, modelSenti, sentiwordnet, frequencyBasedWSD);
					}
					if (score != -999.0) {
						Triple newTriple = new TripleImpl(
								new UriRef(se.getQuality().toString().replace("<", "").replace(">", "")),
								new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
								new PlainLiteralImpl(String.format("%.3f", .6)));
						Triple provaTripleLaerte = new TripleImpl(
								new UriRef(se.getQuality().toString().replace("<", "").replace(">", "")),
								new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasProva"),
								new PlainLiteralImpl(String.format("%.3f", .012)));
						
						
						if (DEBUG)
							System.out.println("Laerte: " + se.getQuality().toString().replace("<", "").replace(">", ""));
							System.out.println("----------SCORE HOLDER-------" + newTriple);
						tripleSentilo.add(provaTripleLaerte);
						tripleSentilo.add(newTriple);
					}
				}
				Triple newTriple = new TripleImpl(
						new UriRef(se.getValue().toString().replace("<", "").replace(">", "")),
						new UriRef("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasQuality"),
						se.getQuality());
				tripleSentilo.add(newTriple);
				newTriple = new TripleImpl(
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_trigger_context"),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasOpinionFeature"),
						se.getQuality());
				tripleSentilo.add(newTriple);
			}
		}
	}

	/*
	 * run the query sparql and populate the vectors events, holders, mtopics,
	 * subtopics with what is found running the query
	 */
	private boolean extractResult(String sparql) {
		// System.out.println("sparql:"+sparql);
		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();
		boolean ret = false;
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			if (DEBUG)
				System.out.println("querySolution>>>>>>>>>>:" + querySolution.toString());
			if (querySolution.get("event") != null) {
				org.apache.clerezza.rdf.core.Resource event = convertFromJenaResource(querySolution.get("event"));
				org.apache.clerezza.rdf.core.Resource eventType = convertFromJenaResource(
						querySolution.get("eventype"));
				org.apache.clerezza.rdf.core.Resource eventQuality = convertFromJenaResource(
						querySolution.get("even_quality"));
				org.apache.clerezza.rdf.core.Resource eventTruthValue = convertFromJenaResource(
						querySolution.get("event_truth_value"));
				org.apache.clerezza.rdf.core.Resource eventMod = convertFromJenaResource(
						querySolution.get("event_mod"));
				org.apache.clerezza.rdf.core.Resource eventLinking = convertFromJenaResource(
						querySolution.get("eventlinking"));

				SentiEvent sent = new SentiEvent(event, eventType, eventQuality, eventTruthValue, eventMod,
						eventLinking);
				if (events.contains(sent) == false) {
					events.add(sent);
				}
				ret = true;
			}
			if (querySolution.get("holder") != null) {
				org.apache.clerezza.rdf.core.Resource holder = convertFromJenaResource(querySolution.get("holder"));
				org.apache.clerezza.rdf.core.Resource holderType = convertFromJenaResource(
						querySolution.get("holdertype"));
				org.apache.clerezza.rdf.core.Resource holderQuality = convertFromJenaResource(
						querySolution.get("holder_quality"));
				org.apache.clerezza.rdf.core.Resource holderlinking = convertFromJenaResource(
						querySolution.get("holderlinking"));

				SentiHolder sent = new SentiHolder(holder, holderType, holderQuality, holderlinking);
				if (holders.contains(sent) == false) {
					holders.add(sent);
				}
				ret = true;
			}
			if (querySolution.get("subtopic") != null) {

				if (DEBUG)
					System.out.println("SUBTOPIC::::" + querySolution.get("subtopic"));
				org.apache.clerezza.rdf.core.Resource subtopic = convertFromJenaResource(querySolution.get("subtopic"));
				org.apache.clerezza.rdf.core.Resource subtopicType = convertFromJenaResource(
						querySolution.get("subtopictype"));
				org.apache.clerezza.rdf.core.Resource subtopicQuality = convertFromJenaResource(
						querySolution.get("subtopic_quality"));
				org.apache.clerezza.rdf.core.Resource sAssociated = convertFromJenaResource(
						querySolution.get("m_associated"));
				org.apache.clerezza.rdf.core.Resource subtopicLinking = convertFromJenaResource(
						querySolution.get("subtopiclinking"));
				org.apache.clerezza.rdf.core.Resource subtopicMod = convertFromJenaResource(
						querySolution.get("subtopic_mod"));
				org.apache.clerezza.rdf.core.Resource subtopicTruthValue = convertFromJenaResource(
						querySolution.get("subtopic_truth_value"));

				SentiSubtopic sent = new SentiSubtopic(subtopic, subtopicType, subtopicQuality, sAssociated,
						subtopicLinking, subtopicMod, subtopicTruthValue);
				if (subtopics.contains(sent) == false) {
					subtopics.add(sent);
				}
				ret = true;
			}
			/*
			 * if(querySolution.get("infratopic")!=null) {
			 * 
			 * if(DEBUG) System.out.println("INFRATOPIC::::"+querySolution.get(
			 * "infratopic"));
			 * 
			 * org.apache.clerezza.rdf.core.Resource infratopic =
			 * convertFromJenaResource(querySolution.get("infratopic"));
			 * org.apache.clerezza.rdf.core.Resource infratopicType =
			 * convertFromJenaResource(querySolution.get("infratopictype"));
			 * org.apache.clerezza.rdf.core.Resource infratopicQuality =
			 * convertFromJenaResource(querySolution.get("infratopic_quality"));
			 * org.apache.clerezza.rdf.core.Resource s1Associated =
			 * convertFromJenaResource(querySolution.get("s_associated"));
			 * org.apache.clerezza.rdf.core.Resource infratopicLinking =
			 * convertFromJenaResource(querySolution.get("infratopiclinking"));
			 * org.apache.clerezza.rdf.core.Resource infratopicMod =
			 * convertFromJenaResource(querySolution.get("infratopic_mod"));
			 * org.apache.clerezza.rdf.core.Resource infratopicTruthValue =
			 * convertFromJenaResource(querySolution.get(
			 * "infratopic_truth_value"));
			 * 
			 * SentiSubtopic sent = new SentiSubtopic(infratopic,
			 * infratopicType, infratopicQuality, s1Associated,
			 * infratopicLinking, infratopicMod, infratopicTruthValue);
			 * if(subtopics.contains(sent)==false) { subtopics.add(sent); } ret
			 * = true; }
			 */
			if (querySolution.get("nanotopic") != null) {

				if (DEBUG)
					System.out.println("NANOTOPIC::::" + querySolution.get("nanotopic"));

				org.apache.clerezza.rdf.core.Resource nanotopic = convertFromJenaResource(
						querySolution.get("nanotopic"));
				org.apache.clerezza.rdf.core.Resource nanotopicType = convertFromJenaResource(
						querySolution.get("nanotopictype"));
				org.apache.clerezza.rdf.core.Resource nanotopicQuality = convertFromJenaResource(
						querySolution.get("nanotopic_quality"));
				org.apache.clerezza.rdf.core.Resource s1Associated = convertFromJenaResource(
						querySolution.get("s1_associated"));
				org.apache.clerezza.rdf.core.Resource nanotopicLinking = convertFromJenaResource(
						querySolution.get("nanotopiclinking"));
				org.apache.clerezza.rdf.core.Resource nanotopicMod = convertFromJenaResource(
						querySolution.get("nanotopic_mod"));
				org.apache.clerezza.rdf.core.Resource nanotopicTruthValue = convertFromJenaResource(
						querySolution.get("nanotopic_truth_value"));

				SentiSubtopic sent = new SentiSubtopic(nanotopic, nanotopicType, nanotopicQuality, s1Associated,
						nanotopicLinking, nanotopicMod, nanotopicTruthValue);
				if (subtopics.contains(sent) == false) {
					subtopics.add(sent);
				}
				ret = true;
			}

			if (querySolution.get("mtopic") != null) {
				org.apache.clerezza.rdf.core.Resource mtopic = convertFromJenaResource(querySolution.get("mtopic"));
				Vector mtopictype = new Vector();
				org.apache.clerezza.rdf.core.Resource eventmtopictype = convertFromJenaResource(
						querySolution.get("eventmtopictype"));
				org.apache.clerezza.rdf.core.Resource mtopicsit = convertFromJenaResource(
						querySolution.get("mtopicsit"));
				org.apache.clerezza.rdf.core.Resource nomeventmtopictype = convertFromJenaResource(
						querySolution.get("nomeventmtopictype"));
				org.apache.clerezza.rdf.core.Resource entitymtopictype = convertFromJenaResource(
						querySolution.get("entitymtopictype"));
				if (eventmtopictype != null)
					mtopictype.add(eventmtopictype);
				if (mtopicsit != null)
					mtopictype.add(mtopicsit);
				if (nomeventmtopictype != null)
					mtopictype.add(nomeventmtopictype);
				if (entitymtopictype != null)
					mtopictype.add(entitymtopictype);

				org.apache.clerezza.rdf.core.Resource mtopicQuality = convertFromJenaResource(
						querySolution.get("mtopic_quality"));
				org.apache.clerezza.rdf.core.Resource mtopicLinking = convertFromJenaResource(
						querySolution.get("mtopiclinking"));
				if (DEBUG)
					System.out.println("mtopicLinking---------:" + mtopicLinking);
				org.apache.clerezza.rdf.core.Resource mtopicMod = convertFromJenaResource(
						querySolution.get("mtopic_mod"));
				org.apache.clerezza.rdf.core.Resource mtopicTruthValue = convertFromJenaResource(
						querySolution.get("mtopic_truth_value"));

				SentiMtopic sent = new SentiMtopic(mtopic, mtopicQuality, mtopicTruthValue, mtopicMod, mtopicLinking,
						mtopictype, eventmtopictype);
				if (mtopics.contains(sent) == false) {
					mtopics.add(sent);
				}
				ret = true;
			}
			// if(querySolution.get("oblique")!=null) {
			// sentiOblique sent = new
			// sentiOblique(solutionMapping.get("oblique"),solutionMapping.get("obliquetype"),solutionMapping.get("oblique_quality"));
			// obliques.add(sent);
			// handle oblique
			// ret = true;
			// }

		}
		return ret;
	}
	
	private boolean extractResultNew(List<QuerySolution> querySolutions, Vector...vects) {
		// System.out.println("sparql:"+sparql);
		boolean ret = false;
		for(QuerySolution querySolution : querySolutions) {
			Resource eventT = querySolution.getResource("eventype");
			if(eventT != null){
				String eventTypeUri = eventT.getURI();
				boolean found = false;
				for(int k=0; k<vects.length && !found; k++){
					Vector conds = vects[k];
					for(int i=0, j=conds.size(); i<j && !found; i++){
						String el = (String) conds.get(i);
						if(eventTypeUri.toLowerCase().contains(el.toLowerCase()))
							found = true;
					}
				}
				
				if(found){
					if (DEBUG)
						System.out.println("querySolution>>>>>>>>>>:" + querySolution.toString());
					if (querySolution.get("event") != null) {
						org.apache.clerezza.rdf.core.Resource event = convertFromJenaResource(querySolution.get("event"));
						org.apache.clerezza.rdf.core.Resource eventType = convertFromJenaResource(
								querySolution.get("eventype"));
						org.apache.clerezza.rdf.core.Resource eventQuality = convertFromJenaResource(
								querySolution.get("even_quality"));
						org.apache.clerezza.rdf.core.Resource eventTruthValue = convertFromJenaResource(
								querySolution.get("event_truth_value"));
						org.apache.clerezza.rdf.core.Resource eventMod = convertFromJenaResource(
								querySolution.get("event_mod"));
						org.apache.clerezza.rdf.core.Resource eventLinking = convertFromJenaResource(
								querySolution.get("eventlinking"));

						SentiEvent sent = new SentiEvent(event, eventType, eventQuality, eventTruthValue, eventMod,
								eventLinking);
						if (events.contains(sent) == false) {
							events.add(sent);
						}
						ret = true;
					}
					if (querySolution.get("holder") != null) {
						org.apache.clerezza.rdf.core.Resource holder = convertFromJenaResource(querySolution.get("holder"));
						org.apache.clerezza.rdf.core.Resource holderType = convertFromJenaResource(
								querySolution.get("holdertype"));
						org.apache.clerezza.rdf.core.Resource holderQuality = convertFromJenaResource(
								querySolution.get("holder_quality"));
						org.apache.clerezza.rdf.core.Resource holderlinking = convertFromJenaResource(
								querySolution.get("holderlinking"));

						SentiHolder sent = new SentiHolder(holder, holderType, holderQuality, holderlinking);
						if (holders.contains(sent) == false) {
							holders.add(sent);
						}
						ret = true;
					}
					if (querySolution.get("subtopic") != null) {

						if (DEBUG)
							System.out.println("SUBTOPIC::::" + querySolution.get("subtopic"));
						org.apache.clerezza.rdf.core.Resource subtopic = convertFromJenaResource(querySolution.get("subtopic"));
						org.apache.clerezza.rdf.core.Resource subtopicType = convertFromJenaResource(
								querySolution.get("subtopictype"));
						org.apache.clerezza.rdf.core.Resource subtopicQuality = convertFromJenaResource(
								querySolution.get("subtopic_quality"));
						org.apache.clerezza.rdf.core.Resource sAssociated = convertFromJenaResource(
								querySolution.get("m_associated"));
						org.apache.clerezza.rdf.core.Resource subtopicLinking = convertFromJenaResource(
								querySolution.get("subtopiclinking"));
						org.apache.clerezza.rdf.core.Resource subtopicMod = convertFromJenaResource(
								querySolution.get("subtopic_mod"));
						org.apache.clerezza.rdf.core.Resource subtopicTruthValue = convertFromJenaResource(
								querySolution.get("subtopic_truth_value"));

						SentiSubtopic sent = new SentiSubtopic(subtopic, subtopicType, subtopicQuality, sAssociated,
								subtopicLinking, subtopicMod, subtopicTruthValue);
						if (subtopics.contains(sent) == false) {
							subtopics.add(sent);
						}
						ret = true;
					}
					if (querySolution.get("nanotopic") != null) {

						if (DEBUG)
							System.out.println("NANOTOPIC::::" + querySolution.get("nanotopic"));

						org.apache.clerezza.rdf.core.Resource nanotopic = convertFromJenaResource(
								querySolution.get("nanotopic"));
						org.apache.clerezza.rdf.core.Resource nanotopicType = convertFromJenaResource(
								querySolution.get("nanotopictype"));
						org.apache.clerezza.rdf.core.Resource nanotopicQuality = convertFromJenaResource(
								querySolution.get("nanotopic_quality"));
						org.apache.clerezza.rdf.core.Resource s1Associated = convertFromJenaResource(
								querySolution.get("s1_associated"));
						org.apache.clerezza.rdf.core.Resource nanotopicLinking = convertFromJenaResource(
								querySolution.get("nanotopiclinking"));
						org.apache.clerezza.rdf.core.Resource nanotopicMod = convertFromJenaResource(
								querySolution.get("nanotopic_mod"));
						org.apache.clerezza.rdf.core.Resource nanotopicTruthValue = convertFromJenaResource(
								querySolution.get("nanotopic_truth_value"));

						SentiSubtopic sent = new SentiSubtopic(nanotopic, nanotopicType, nanotopicQuality, s1Associated,
								nanotopicLinking, nanotopicMod, nanotopicTruthValue);
						if (subtopics.contains(sent) == false) {
							subtopics.add(sent);
						}
						ret = true;
					}

					if (querySolution.get("mtopic") != null) {
						org.apache.clerezza.rdf.core.Resource mtopic = convertFromJenaResource(querySolution.get("mtopic"));
						Vector mtopictype = new Vector();
						org.apache.clerezza.rdf.core.Resource eventmtopictype = convertFromJenaResource(
								querySolution.get("eventmtopictype"));
						org.apache.clerezza.rdf.core.Resource mtopicsit = convertFromJenaResource(
								querySolution.get("mtopicsit"));
						org.apache.clerezza.rdf.core.Resource nomeventmtopictype = convertFromJenaResource(
								querySolution.get("nomeventmtopictype"));
						org.apache.clerezza.rdf.core.Resource entitymtopictype = convertFromJenaResource(
								querySolution.get("entitymtopictype"));
						if (eventmtopictype != null)
							mtopictype.add(eventmtopictype);
						if (mtopicsit != null)
							mtopictype.add(mtopicsit);
						if (nomeventmtopictype != null)
							mtopictype.add(nomeventmtopictype);
						if (entitymtopictype != null)
							mtopictype.add(entitymtopictype);

						org.apache.clerezza.rdf.core.Resource mtopicQuality = convertFromJenaResource(
								querySolution.get("mtopic_quality"));
						org.apache.clerezza.rdf.core.Resource mtopicLinking = convertFromJenaResource(
								querySolution.get("mtopiclinking"));
						if (DEBUG)
							System.out.println("mtopicLinking---------:" + mtopicLinking);
						org.apache.clerezza.rdf.core.Resource mtopicMod = convertFromJenaResource(
								querySolution.get("mtopic_mod"));
						org.apache.clerezza.rdf.core.Resource mtopicTruthValue = convertFromJenaResource(
								querySolution.get("mtopic_truth_value"));

						SentiMtopic sent = new SentiMtopic(mtopic, mtopicQuality, mtopicTruthValue, mtopicMod, mtopicLinking,
								mtopictype, eventmtopictype);
						if (mtopics.contains(sent) == false) {
							mtopics.add(sent);
						}
						ret = true;
					}
				}
			}

		}
		return ret;
	}

	private boolean toShow(Triple triple) {

		String subjectName = triple.getSubject().toString().replace("<", "").replace(">", "");
		String predicateName = triple.getPredicate().toString().replace("<", "").replace(">", "");
		String objectName = triple.getObject().toString().replace("<", "").replace(">", "");

		String earmarkNS = "http://www.essepuntato.it/2008/12/earmark#";
		String semionNS = "http://ontologydesignpatterns.org/cp/owl/semiotics.owl#";
		String pos = "http://www.ontologydesignpatterns.org/ont/fred/pos.owl#pos";
		String objectProp = OWL2.ObjectProperty.getURI();
		String datatypeProp = OWL2.DatatypeProperty.getURI();
		String boxerPossibleType = "http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#possibleType";
		String glossProp = "http://www.w3.org/2006/03/wn/wn30/schema/gloss";

		if (subjectName.startsWith(earmarkNS) || predicateName.startsWith(earmarkNS)
				|| predicateName.startsWith(semionNS) || predicateName.equals(boxerPossibleType)
				|| predicateName.equals(glossProp) || predicateName.equals(pos) || objectName.startsWith(earmarkNS)
				|| objectName.equals(objectProp) || objectName.equals(datatypeProp)) {
			return false;
		} else {
			return true;
		}
	}

	public TripleCollection getSentiTriples() {
		return tripleSentilo;
	}

	/* add headers triples to sentilo */
	private void addSentiHeaderNodes() {
		if (tripleSentilo != null) {
			Triple newTriple = new TripleImpl(
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_sentence"),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasText"),
					new PlainLiteralImpl(text));
			tripleSentilo.add(newTriple);
			newTriple = new TripleImpl(new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_sentence"),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasOpinionTriggerContext"),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_trigger_context"));
			tripleSentilo.add(newTriple);
			newTriple = new TripleImpl(new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinion_sentence"),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasOpinionatedContext"),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#opinionated_context"));
			tripleSentilo.add(newTriple);
		}
	}

	/* add offset tag nodes to tripleSentilo for SENTILO's UI */
	private void addOffsetTagNodes() {
		String sparql = " SELECT ?node ?rel ?val WHERE { ?node ?rel ?val FILTER ( regex(str(?node), \"^http://www.ontologydesignpatterns.org/ont/fred/domain.owl#offset_.+\",\"i\" ) ) } ";

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();

			RDFNode object = sol.get("val");
			org.apache.clerezza.rdf.core.Resource objectResource = Jena2ClerezzaRdfTermConverter.convert(object);

			Triple triple = new TripleImpl(new UriRef(sol.getResource("node").getURI()),
					new UriRef(sol.getResource("rel").getURI()), objectResource);
			tripleSentilo.add(triple);
		}
	}

	/* add pos tag nodes to tripleSentilo for noun entities */
	private void addPosTagNodes() {
		String sparql = " SELECT ?node ?val WHERE { ?node <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#pos> ?val FILTER ( !regex(str(?node), \"^http://www.ontologydesignpatterns.org/ont/fred/domain.owl#offset_\",\"i\" ) ) } ";

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();

			RDFNode object = sol.get("val");
			org.apache.clerezza.rdf.core.Resource objectResource = Jena2ClerezzaRdfTermConverter.convert(object);
			Triple triple = new TripleImpl(new UriRef(sol.getResource("node").getURI()),
					new UriRef("http://www.ontologydesignpatterns.org/ont/fred/pos.owl#pos"), objectResource);

			tripleSentilo.add(triple);
		}
	}

	/* complete sparql query with subtopics */
	private String addRegexSubtopics() {
		String ret = "";
		Set els = new HashSet();
		for (int i = 0; i < subtopics.size(); i++) {
			SentiSubtopic m = (SentiSubtopic) subtopics.get(i);
			if (m.subtopic != null)
				els.add(m.subtopic.toString().replace("<", "").replace(">", ""));
			if (m.subtopic_quality != null)
				els.add(m.subtopic_quality.toString().replace("<", "").replace(">", ""));
		}
		Iterator it = els.iterator();
		while (it.hasNext()) {
			String ll = (String) it.next();
			ret += "regex(str(?obj), \"" + ll + "\",\"i\") || ";
		}
		if (ret.equals("") == false)
			ret = ret.substring(0, ret.length() - 3);
		return ret;
	}

	/* complete sparql query with holders */
	private String addRegexHolders() {
		String ret = "";
		Set els = new HashSet();
		for (int i = 0; i < holders.size(); i++) {
			SentiHolder m = (SentiHolder) holders.get(i);
			if (m.holder != null) {
				els.add(m.holder.toString().replace("<", "").replace(">", ""));
			}
		}
		Iterator it = els.iterator();
		while (it.hasNext()) {
			String ll = (String) it.next();
			ret += "regex(str(?obj), \"" + ll + "\",\"i\") || ";
		}
		if (ret.equals("") == false)
			ret = ret.substring(0, ret.length() - 3);
		return ret;
	}

	/* complete sparql query with topics */
	private String addRegexMtopics() {
		String ret = "";
		Set els = new HashSet();
		for (int i = 0; i < mtopics.size(); i++) {
			SentiMtopic m = (SentiMtopic) mtopics.get(i);
			if (m.mtopic != null) {
				els.add(m.mtopic.toString().replace("<", "").replace(">", ""));
			}
			if (m.mtopic_quality != null) {
				els.add(m.mtopic_quality.toString().replace("<", "").replace(">", ""));
			}
		}
		Iterator it = els.iterator();
		while (it.hasNext()) {
			String ll = (String) it.next();
			ret += "regex(str(?obj), \"" + ll + "\",\"i\") || ";
		}
		if (ret.equals("") == false)
			ret = ret.substring(0, ret.length() - 3);
		return ret;
	}

	/*
	 * Add participates relations between subtopic and topic-subtopic if
	 * subtopic is part of the topic
	 */
	private void addParticipatesRelations() {
		String sparql = " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
				+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ " SELECT ?sub ?situation WHERE { { ?parent sentilo:hasSubTopic ?sub . ?situation ?rel ?sub . ?situation a boxing:Situation .  { ?parent sentilo:hasTopic ?situation } UNION { ?parent sentilo:hasSubTopic ?situation } FILTER NOT EXISTS { ?parent1 sentilo:hasOpinionTrigger ?situation } }  UNION { ?parent sentilo:hasSubTopic ?sub . ?situation ?rel ?sub . ?situation a ?eventype . ?eventype rdfs:subClassOf+ dul:Event . {?parent sentilo:hasTopic ?situation } UNION { ?parent sentilo:hasSubTopic ?situation } FILTER NOT EXISTS { ?parent1 sentilo:hasOpinionTrigger ?situation }  }  } ";
		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#participatesIn"),
					new UriRef(sol.get("situation").toString().replace("<", "").replace(">", "")));
			tripleSentilo.add(newTriple);
		}
	}

	/* Add sensitiveness and Factual information relations to subtopics */
	private void addSensitivenessRelations() {
		// read subtopics, checks whether they are objects of events, if
		// relation is one in memory, reads the sensitiveness and adds a
		// relation between subtopics and events.
		String sparql = " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ " PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ " SELECT ?eventype ?prop ?sub ?event WHERE { { { ?obj sentilo:hasSubTopic ?sub } UNION { ?obj sentilo:hasTopic ?sub } } . ?event ?prop ?sub . { { ?event a ?eventype . ?eventype rdfs:subClassOf dul:Event } UNION { ?event a ?eventype0 . ?eventype0 rdfs:subClassOf+ ?eventype . ?eventype rdfs:subClassOf dul:Event } } } ORDER BY ?eventype ?prop ";

		if (DEBUG)
			System.out.println("addSensitivenessRelations query:" + sparql);
		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();

			String eventURI = sol.getResource("eventype").getURI();
			String event = eventURI.substring(eventURI.indexOf("#") + 1).toLowerCase();

			String propURI = sol.getResource("prop").getURI();
			if (DEBUG)
				System.out.println("propURI:" + propURI + " event:" + event);
			if (propURI.indexOf("_dotted") == -1) // is isdotted
				continue;
			String prop = propURI.substring(propURI.lastIndexOf("/") + 1, sol.get("prop").toString().lastIndexOf("_"));
			if (prop.indexOf("#") != -1) {
				prop = prop.substring(prop.indexOf("#") + 1);
			}
			prop = prop.toLowerCase();
			if (DEBUG)
				System.out.println("Event:" + event + " role:" + prop);

			HashMap vet = (HashMap) sensitiveness.get(event);

			if (vet != null) {

				SensitivenessObject so = (SensitivenessObject) vet.get(prop);
				if (so == null) {
					// case of theme when not in the theme resource. If there is
					// AGNT then we choose the same behaviour of PTNT. Otherwise
					// we pick AGNT
					if (prop.equals("theme")) {
						// it should never go inside here....
						String query_eq = "SELECT ?obj WHERE { <" + sol.getResource("event").getURI()
								+ "> ?prop ?obj FILTER ( regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/vn/abox/role/Actor_dotted\",\"i\") || regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/vn/abox/role/Actor1_dotted\",\"i\") || regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/vn/abox/role/Actor2_dotted\",\"i\") || regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/vn/abox/role/Agent_dotted\",\"i\") || regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/vn/abox/role/Experiencer_dotted\",\"i\") || regex(str(?prop), \"http://www.ontologydesignpatterns.org/ont/boxer/agent_dotted\",\"i\") ) } ";
						Query query_eq1 = QueryFactory.create(query_eq, Syntax.syntaxARQ);
						QueryExecution queryExecution_eq = QueryExecutionFactory.create(query_eq1, model);
						com.hp.hpl.jena.query.ResultSet resultSet_eq = queryExecution_eq.execSelect();
						if (resultSet_eq.hasNext()) {
							so = (SensitivenessObject) vet.get("patient");
						} else {
							so = (SensitivenessObject) vet.get("agent");
						}
					}
					if (so == null)
						continue;
				}

				if (so.getFact().equals("p")) {
					Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#isPositivelyAffectedBy"),
							new UriRef(sol.getResource("event").getURI()));
					tripleSentilo.add(newTriple);
				}
				if (so.getFact().equals("n")) {
					Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#isNegativelyAffectedBy"),
							new UriRef(sol.getResource("event").getURI()));
					tripleSentilo.add(newTriple);
				}

				if (so.getOp().equals("1")) {
					Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#playsSensitiveRole"),
							new UriRef(sol.getResource("event").getURI()));
					tripleSentilo.add(newTriple);
				}
			} else { // if verb is not in resource assigns role to any of the
						// following role
				if (prop.equals("actor") || prop.equals("actor1") || prop.equals("actor2") || prop.equals("agent")
						|| prop.equals("experiencer") || prop.equals("patient") || prop.equals("patient1")
						|| prop.equals("patient2") || prop.equals("theme")) {
					double sc = -999;
					if (USEDEFAULTSCORESTRATEGY == false)
						sc = Util.getScoreFromSenticNetLocal(event, modelSenti);
					else {
						sc = Util.getScoreFromDefaultStrategy(event, modelSenti, sentiwordnet, frequencyBasedWSD);
					}
					if (sc != -999) {
						if (sc > 0) {
							Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
									new UriRef(
											"http://ontologydesignpatterns.org/ont/sentilo.owl#isPositivelyAffectedBy"),
									new UriRef(sol.getResource("event").getURI()));
							tripleSentilo.add(newTriple);
						}
						if (sc < 0) {
							Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
									new UriRef(
											"http://ontologydesignpatterns.org/ont/sentilo.owl#isNegativelyAffectedBy"),
									new UriRef(sol.getResource("event").getURI()));
							tripleSentilo.add(newTriple);
						}
						
						/* 
						 * FIXME
						 * 
						 *  Andrea on 21/04/2017. This is a test
						 * 
						 */
						Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
								new UriRef(
										"http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
								new TypedLiteralImpl(String.valueOf(sc), XSD.double_));
						tripleSentilo.add(newTriple);
					}
					Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#playsSensitiveRole"),
							new UriRef(sol.getResource("event").getURI()));
					tripleSentilo.add(newTriple);
				}
			}

		}
	}

	/*
	 * In some situation, checks whether situation is directly connected to some
	 * node. If yes, create a relation (style dotted)
	 */
	private void addTripleDottedRelations() {
		if (DEBUG) {
			System.out.println("MTOPICS-------:" + mtopics);
			System.out.println("SUBTOPICS------:" + subtopics);
		}
		String mtop_str = addRegexMtopics();
		String msubtop_str = addRegexSubtopics();
		String holder_str = addRegexHolders();
		String parent_start = "";
		String parent_start1 = "";
		String or = "";
		String parent_end = "";
		Set toAdd = new HashSet();

		if (mtop_str.equals("") && msubtop_str.equals("") && holder_str.equals(""))
			return;
		if (mtop_str.equals("") == false || msubtop_str.equals("") == false || holder_str.equals("") == false) {
			parent_start = " && ( ";
			parent_start1 = " ( ";
			parent_end = " ) ";
		}
		if (mtop_str.equals("") == false && msubtop_str.equals("") == false) {
			or = " || ";
		}
		// execute that only if in tripleSentilo there is a node
		// situation_[0123456789]+
		// add info about event only if in tripleSentilo there is a node
		// dul:Event
		//
		if (DEBUG) {
			System.out.println("mtop_str:" + mtop_str);
			System.out.println("msubtop_str:" + msubtop_str);
			System.out.println("holder_str:" + holder_str);
		}
		if (holder_str.equals("") == false && (mtop_str.equals("") == false || msubtop_str.equals("") == false))
			holder_str = " || " + holder_str;
		String sparql = "SELECT ?sub ?rel ?obj WHERE { ?sub ?rel ?obj FILTER ( regex(str(?sub), \"http://www.ontologydesignpatterns.org/ont/fred/domain.owl#situation_[0123456789]+"
				+ "\", \"i\") " + parent_start + mtop_str + " " + or + msubtop_str + holder_str + parent_end + " ) } ";
		if (DEBUG)
			System.out.println("addTripleDottedRelations query:" + sparql);

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			Triple newTriple = new TripleImpl(new UriRef(sol.getResource("sub").getURI()),
					new UriRef(sol.getResource("rel").getURI() + "_dotted"),
					new UriRef(sol.getResource("obj").getURI()));
			toAdd.add(newTriple);
		}

		String sufx = parent_start1 + mtop_str + " " + or + msubtop_str + holder_str + parent_end;
		if (sufx.trim().equals("") == false) {
			sufx = " FILTER " + sufx;
		}
		sparql = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ " SELECT ?event ?rel ?obj WHERE { ?event a ?type . ?type rdfs:subClassOf+ dul:Event . ?event ?rel ?obj "
				+ sufx + " } ";
		// { ?type <http://www.w3.org/2000/01/rdf-schema#subClassOf>
		// <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Event>} UNION
		// { ?type <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?type1 .
		// ?type1 <http://www.w3.org/2000/01/rdf-schema#subClassOf>
		// <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Event> } .
		// ?event ?rel ?obj "+sufx + " } ";
		//
		if (DEBUG)
			System.out.println("...................." + sparql);

		query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		queryExecution = QueryExecutionFactory.create(query, model);
		resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();

			Triple newTriple = new TripleImpl(new UriRef(sol.getResource("event").getURI()),
					new UriRef(sol.getResource("rel").getURI() + "_dotted"),
					new UriRef(sol.getResource("obj").getURI()));
			toAdd.add(newTriple);
		}

		Iterator it = toAdd.iterator();
		while (it.hasNext()) {
			Triple t = (Triple) it.next();
			tripleSentilo.add(t);
		}
	}

	/* Remove duplicate triples with different relation */
	/*
	 * if same object and value then if property contains _dotted then remove
	 * this property and keep the other
	 */
	/*
	 * if same object and value then if property is involves then remove
	 * involves
	 */
	public void removeDuplicates() {
		String query = "SELECT ?event ?rel ?rel1 ?obj WHERE { ?event ?rel ?obj . ?event1 ?rel1 ?obj1 FILTER ( ?rel != ?rel1 && ?event=?event1 && ?obj = ?obj1) } ORDER BY ?event ?obj";
		if (DEBUG)
			System.out.println("removeDuplicates query:" + query);
		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query_q = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query_q, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		int cont = 0;
		String rel = "", rel1 = "";
		Set toDelete = new HashSet();
		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			if (cont % 2 == 0) {
				// System.out.println("sub:"+sol.get("event").toString()+"
				// rel:"+sol.get("rel").toString()+"
				// rel1:"+sol.get("rel1").toString()+"
				// obj:"+sol.get("obj").toString());

				rel = sol.getResource("rel").getURI().replace("<", "").replace(">", "");
				rel1 = sol.getResource("rel1").getURI().replace("<", "").replace(">", "");

				if ((rel.contains("_dotted") && rel1.contains("_dotted") == false)) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				} else if (rel1.contains("_dotted") && rel.contains("_dotted") == false) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel1").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				}

				if ((rel.contains("involves") && rel1.contains("involves") == false)) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				} else if (rel1.contains("involves") && rel.contains("involves") == false) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel1").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				}

				if ((rel.contains("hasTopic") && rel1.contains("hasSubTopic"))) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel1").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				} else if (rel1.contains("hasTopic") && rel.contains("hasSubTopic")) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("event").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("rel").getURI().replace("<", "").replace(">", "")),
							new UriRef(sol.getResource("obj").getURI().replace("<", "").replace(">", "")));
					toDelete.add(triple);
				}

			}
			cont++;
			// Add other ifs and check whether two equal triples can exist in
			// tripleSentilo (meaning rel = rel1)
		}
		MGraph g = new SimpleMGraph(tripleSentilo);
		Iterator it = toDelete.iterator();
		while (it.hasNext()) {
			Triple triple = (Triple) it.next();
			g.remove(triple);
		}
		tripleSentilo = (TripleCollection) g;
	}

	/*
	 * Add the md5 to the opinionated_context node. md5 is computed for the
	 * sorted list of topics
	 */
	private void changeOpinionatedContextWithMD5() {
		try {
			String[] ar = (String[]) list_topics.toArray(new String[0]);
			Arrays.sort(ar);
			String md5 = "";
			for (int i = 0; i < ar.length; i++)
				md5 = md5 + ar[i] + " ";
			md5 = md5.trim();
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(md5.getBytes(), 0, md5.length());
			md5 = new BigInteger(1, m.digest()).toString(16);
			Set toadd = new HashSet();
			Set todelete = new HashSet();
			for (Triple triple : tripleSentilo) {
				String subjectName = triple.getSubject().toString().replace("<", "").replace(">", "");
				String predicateName = triple.getPredicate().toString().replace("<", "").replace(">", "");
				String objectName = triple.getObject().toString().replace("<", "").replace(">", "");
				if (subjectName.contains("opinionated_context")) {
					todelete.add(
							new TripleImpl(new UriRef(subjectName), new UriRef(predicateName), new UriRef(objectName)));
					toadd.add(new TripleImpl(new UriRef(subjectName + "_" + md5), new UriRef(predicateName),
							new UriRef(objectName)));
					continue;
				}
				if (objectName.contains("opinionated_context") && subjectName.contains("opinion_sentence")) {
					todelete.add(
							new TripleImpl(new UriRef(subjectName), new UriRef(predicateName), new UriRef(objectName)));
					toadd.add(new TripleImpl(new UriRef(subjectName + "_" + md5), new UriRef(predicateName),
							new UriRef(objectName + "_" + md5)));
					continue;
				}
				if (subjectName.contains("opinion_sentence") && predicateName.contains("hasText")) {
					todelete.add(new TripleImpl(new UriRef(subjectName), new UriRef(predicateName),
							new PlainLiteralImpl("\"" + objectName.replaceAll("\"", "") + "\"")));
					toadd.add(new TripleImpl(new UriRef(subjectName + "_" + md5), new UriRef(predicateName),
							new PlainLiteralImpl("\"" + objectName.replaceAll("\"", "") + "\"")));
					continue;
				}
				if (subjectName.contains("opinion_trigger_context")) {
					todelete.add(
							new TripleImpl(new UriRef(subjectName), new UriRef(predicateName), new UriRef(objectName)));
					toadd.add(new TripleImpl(new UriRef(subjectName + "_" + md5), new UriRef(predicateName),
							new UriRef(objectName)));
					continue;
				}
				if (objectName.contains("opinion_trigger_context") && subjectName.contains("opinion_sentence")) {
					todelete.add(
							new TripleImpl(new UriRef(subjectName), new UriRef(predicateName), new UriRef(objectName)));
					toadd.add(new TripleImpl(new UriRef(subjectName + "_" + md5), new UriRef(predicateName),
							new UriRef(objectName + "_" + md5)));
					continue;
				}
			}
			MGraph g = new SimpleMGraph(tripleSentilo);
			Iterator it = todelete.iterator();
			while (it.hasNext()) {
				Triple triple = (Triple) it.next();
				g.remove(triple);
			}
			it = toadd.iterator();
			while (it.hasNext()) {
				Triple triple = (Triple) it.next();
				g.add(triple);
			}
			tripleSentilo = (TripleCollection) g;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* set a score to each verb being subclass of dul:Event and trigger */
	// REMEMBER TO ADD VERBS AGGETTIVANTI NON TRIGGER
	public void setScoreToEachVerb() {

		String query = "PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ "  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "  PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
				+ "  PREFIX fred: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> "
				+ "  PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ "  SELECT ?verb ?verb_is WHERE { ?verb_is a ?verb . ?verb rdfs:subClassOf+ dul:Event . ?parent sentilo:hasOpinionTrigger ?verb_is }";

		if (DEBUG)
			System.out.println("setScoreToEachVerb query:" + query);

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query_q = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query_q, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			if (sol.getResource("verb") != null) {
				String verb = sol.getResource("verb").getURI()
						.substring(sol.getResource("verb").getURI().indexOf("#") + 1).toLowerCase();
				String pref = sol.getResource("verb").getURI().substring(0,
						sol.getResource("verb").getURI().indexOf("#"));
				if (DEBUG)
					System.out.println("EEpref-de-de-e---:" + pref + " verb:" + verb);
				if (pref.equals("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl"))
					continue;

				double score = -999.0;
				if (USEDEFAULTSCORESTRATEGY == false)
					score = Util.getScoreFromSenticNetLocal(verb, modelSenti);
				else {
					score = Util.getScoreFromDefaultStrategy(verb, modelSenti, sentiwordnet, frequencyBasedWSD);
				}
				if (score != -999.0) {
					Triple triple = new TripleImpl(
							new UriRef(sol.getResource("verb_is").getURI().replace("<", "").replace(">", "")),
							new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
							new PlainLiteralImpl(String.format("%.3f", .7)));
					tripleSentilo.add(triple);
					if (DEBUG)
						System.out.println("----------SCORE  VERB-------" + triple);
				}
			}
		}
	}

	/* Set a sentiment score to each node, subnode of topics and subtopics */
	/*
	 * if isnoun then look in sentiwordnet. If not found look in senticnet. If
	 * is not noun look in senticnet. When found add triple with score
	 */
	public void setScoreToEachNode() {

		String query = "";
		query = "  PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ "  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "  PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
				+ "  PREFIX fred: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> "
				+ "  PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ "  SELECT ?node ?node1 ?topic ?parent WHERE { "
				+ "  { { ?parent sentilo:hasTopic ?topic FILTER NOT EXISTS { ?parent1 dul:hasQuality ?topic } } OPTIONAL { ?node fred:pos fred:n . ?topic a ?node } OPTIONAL { ?node fred:pos fred:n . ?topic a ?node . ?node rdfs:subClassOf+ ?node1 . ?node1 fred:pos fred:n } } "
				+ "  UNION { { ?parent sentilo:hasSubTopic ?topic FILTER NOT EXISTS { ?parent1 dul:hasQuality ?topic } } OPTIONAL { ?node fred:pos fred:n . ?topic a ?node } OPTIONAL { ?node fred:pos fred:n . ?topic a ?node . ?node rdfs:subClassOf+ ?node1 . ?node1 fred:pos fred:n } OPTIONAL { ?node fred:pos fred:n . ?topic rdfs:subClassOf+ ?node } } } ";
		if (DEBUG)
			System.out.println("setScoreToEachNode query:" + query);

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query_q = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query_q, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			if (sol.getResource("topic") != null) {

				String top = sol.getResource("topic").getURI()
						.substring(sol.getResource("topic").getURI().indexOf("#") + 1);

				if (sol.getResource("node") != null) {
					String pref = sol.getResource("node").getURI().substring(0,
							sol.getResource("node").getURI().indexOf("#"));
					if (pref.equals("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl"))
						continue;
					String nod = sol.getResource("node").getURI()
							.substring(sol.getResource("node").getURI().indexOf("#") + 1);
					String[] camelCaseWords = nod.split("(?=[A-Z])");
					double avg = 0;
					int cont = 0;
					for (int i = 0; i < camelCaseWords.length; i++) {
						if (camelCaseWords[i].trim().equals(""))
							continue;
						// if(camelCaseWords[i].toLowerCase().equals(qual.toLowerCase())==false)
						// {
						// look for score
						boolean noscore = false;
						if (camelCaseWords[i].toLowerCase().trim().equals("male")
								|| camelCaseWords[i].toLowerCase().trim().equals("female")
								|| camelCaseWords[i].toLowerCase().trim().equals("thing"))
							noscore = true;
						if (noscore == false) {
							double sc = -999;
							// FIRST WE need to pass from camelCaseWords[i] to
							// sentiwordnet id
							if (USEDEFAULTSCORESTRATEGY == false) {
								sc = Util.getScoreFromSenticNetLocal(camelCaseWords[i].toLowerCase().trim(),
										modelSenti);
							} else {
								sc = Util.getScoreFromDefaultStrategy(camelCaseWords[i].toLowerCase().trim(),
										modelSenti, sentiwordnet, frequencyBasedWSD);
							}
							if (sc != -999) {
								avg += sc;
								cont++;
							}
						}
						// }
					}
					if (cont > 0) {
						avg /= cont;
						Triple triple = null;
						triple = new TripleImpl(
								new UriRef(sol.getResource("topic").getURI().replace("<", "").replace(">", "")),
								new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
								new PlainLiteralImpl(String.format("%.3f", .8)));
						tripleSentilo.add(triple);
						// add score triple
					}
					// if found score in camelCase then average and then create
					// a triple
				}
				if (sol.getResource("node1") != null) {
					String pref = sol.getResource("node1").getURI().substring(0,
							sol.getResource("node1").getURI().indexOf("#"));
					if (pref.equals("http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl"))
						continue;
					String nod = sol.getResource("node1").getURI()
							.substring(sol.getResource("node1").getURI().indexOf("#") + 1);
					String[] camelCaseWords = nod.split("(?=[A-Z])");
					double avg = 0;
					int cont = 0;
					for (int i = 0; i < camelCaseWords.length; i++) {
						if (camelCaseWords[i].trim().equals(""))
							continue;
						// if(camelCaseWords[i].toLowerCase().equals(qual.toLowerCase())==false)
						// {
						// look for score
						boolean noscore = false;
						if (camelCaseWords[i].toLowerCase().trim().equals("male")
								|| camelCaseWords[i].toLowerCase().trim().equals("female")
								|| camelCaseWords[i].toLowerCase().trim().equals("thing"))
							noscore = true;
						if (noscore == false) {
							double sc = -999;
							if (USEDEFAULTSCORESTRATEGY == false) {
								sc = Util.getScoreFromSenticNetLocal(camelCaseWords[i].toLowerCase().trim(),
										modelSenti);
							} else {
								sc = Util.getScoreFromDefaultStrategy(camelCaseWords[i].toLowerCase().trim(),
										modelSenti, sentiwordnet, frequencyBasedWSD);
							}
							if (sc != -999) {
								avg += sc;
								cont++;
							}
						}
						// }
					}
					if (cont > 0) {
						avg /= cont;
						Triple triple = new TripleImpl(
								new UriRef(sol.getResource("node1").getURI().replace("<", "").replace(">", "")),
								new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
								new PlainLiteralImpl(String.format("%.3f", 0.1)));
						tripleSentilo.add(triple);
						// add score triple
					}
					// if found score in camelCase then average and then create
					// a triple
				}
			}
		}
	}

	/*
	 * Input: RDF graph with topic, sub-topic, holder, situation with dotted
	 * relations, score for each oew, sensitiveness relations, participatesIn
	 * relations
	 */
	/*
	 * Read the tripleSentilo triples and compute a score for each
	 * topic/subtopic adding triples to tripleSentilo
	 */

	// score solo a hasQuality e verbi di trigger
	// Use Set and while is not empty. Sort elements here first main topic, then
	// subtopic

	public void sentiloScore(Vector holder_pol_pos, Vector holder_pol_neg, Vector holder_act_true,
			Vector holder_act_false) {
			
		// -------------------------
		// Static results for testing
		Triple triple = null;
		// -------------------------

		// -------------------------
		// 1. Use SPARQL query to extract topics and subtopics
		// isPositivelyAffectedBy isNegativelyAffectedBy playsSensitiveRole
		// First subtopics with participatesIn, sensitiveness, posFactual,
		// negFactual relations are extracted, then subtopics, then topics
		String query = "  PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ "  SELECT ?obj ?sub ?posFact ?negFact ?sens ?othertop ?x WHERE {{?obj sentilo:hasTopic ?sub OPTIONAL {?sub sentilo:isPositivelyAffectedBy ?posFact} OPTIONAL {?sub sentilo:isNegativelyAffectedBy ?negFact} OPTIONAL {?sub sentilo:playsSensitiveRole ?sens} bind (1 as $x) } UNION {?obj sentilo:hasSubTopic ?sub . ?sub sentilo:participatesIn ?othertop OPTIONAL {?sub sentilo:isPositivelyAffectedBy ?posFact} OPTIONAL {?sub sentilo:isNegativelyAffectedBy ?negFact} OPTIONAL {?sub sentilo:playsSensitiveRole ?sens} bind (3 as $x) } UNION {?obj sentilo:hasSubTopic ?sub OPTIONAL {?sub sentilo:isPositivelyAffectedBy ?posFact} OPTIONAL {?sub sentilo:isNegativelyAffectedBy ?negFact} OPTIONAL {?sub sentilo:playsSensitiveRole ?sens} bind (2 as $x) FILTER NOT EXISTS { ?sub sentilo:participatesIn ?othertop } } } ORDER BY ?x ";

		Model model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		Query query1 = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query1, model);
		com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();

		HashMap<String, TopicScore> topics = new HashMap();
		LinkedList topics_order = new LinkedList();

		while (resultSet.hasNext()) {

			QuerySolution sol = resultSet.next();

			String topic = sol.getResource("sub").getURI().substring(sol.getResource("sub").getURI().indexOf("#") + 1);

			if (topics_order.contains(topic) == false)
				topics_order.add(topic);

			TopicScore ts = (TopicScore) topics.get(topic);
			String posFact = null;
			if (sol.getResource("posFact") != null)
				posFact = sol.getResource("posFact").getURI();
			if (posFact != null) {
				posFact = posFact.substring(posFact.indexOf("#") + 1);
			}
			String negFact = null;
			if (sol.getResource("negFact") != null)
				negFact = sol.getResource("negFact").getURI();
			if (negFact != null) {
				negFact = negFact.substring(negFact.indexOf("#") + 1);
			}
			String sens = null;
			if (sol.getResource("sens") != null)
				sens = sol.getResource("sens").getURI();
			if (sens != null) {
				sens = sens.substring(sens.indexOf("#") + 1);
			}
			String othertop = null;
			if (sol.getResource("othertop") != null)
				othertop = sol.getResource("othertop").getURI();
			if (othertop != null) {
				othertop = othertop.substring(othertop.indexOf("#") + 1);
			}
			if (ts == null) {
				// TopicScore contains the topic and the name of the verbs for
				// which there is a posFact, negFact, sens relation between
				// topic and verb/situation (participatesIn)
				ts = new TopicScore("<" + sol.getResource("sub").getURI() + ">", posFact, negFact, sens, othertop);
			} else {
				ts.update(posFact, negFact, sens, othertop);
			}
			topics.put(topic, ts);
		}
		// -------------------------

		if (DEBUG) {
			System.out.println("||||Topics||||" + topics);
			System.out.println("||||topics_ordered||||" + topics_order);
		}

		// -------------------------
		// 2. For each topic and subtopic in order: subtopic with participatesIn
		// relation, subtopic, topic
		// if ts participatesIn to something not computed then insert el back in
		// topics_order and continue
		Vector processed = new Vector();
		while (topics_order.size() > 0) {

			String topic = (String) topics_order.removeFirst();
			if (DEBUG) {
				System.out.println("-------------> topic:" + topic);
				System.out.println("value:" + topics.get(topic));
			}

			TopicScore ts = (TopicScore) topics.get(topic);
			Vector toCheck = ts.participatesIn();
			boolean tocontinue = false;
			for (int itc = 0; itc < toCheck.size(); itc++) {
				String toc = (String) toCheck.get(itc);
				if (processed.contains(toc) == false) {
					if (DEBUG)
						System.out.println("topic:" + toc + " to be processed. Skip topic. CHECK!!!!!");
					tocontinue = true;
					break;
				} else {
					if (DEBUG)
						System.out.println("topic:" + toc + " already processed");
				}
			}
			if (tocontinue) {
				topics_order.addLast(topic);
				continue;
			}
			processed.add(topic);

			// -------------------------
			// if the current topic has hasTopic relation then it is a main
			// topic
			String sparql_in = "SELECT ?value WHERE { { ?value <http://ontologydesignpatterns.org/ont/sentilo.owl#hasTopic> "
					+ ts.getTopic() + " } } ";
			// UNION { ?value
			// <http://ontologydesignpatterns.org/ont/sentilo.owl#hasOpinionTrigger>
			// "+ts.getTopic()+" } } ";
			if (DEBUG)
				System.out.println("sparql_in:" + sparql_in);
			//
			model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
			Query query_in = QueryFactory.create(sparql_in, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query_in, model);
			com.hp.hpl.jena.query.ResultSet resultSet_in = queryExecution.execSelect();

			boolean mainTopic = false;
			if (resultSet_in.hasNext())
				mainTopic = true;
			if (DEBUG)
				System.out.println("maintopic:" + mainTopic);
			// -------------------------

			// pos Vector contains the number of positive score of the current
			// topic
			// neg Vector contains the number of negative score of the current
			// topic
			// neutral contains the number of neutral score of the current topic
			// -------------------------
			Vector pos = new Vector();
			Vector neg = new Vector();
			int neutral = 0;
			// -------------------------

			boolean computeScore = false;

			// If the topic is not a main topic see if we need to compute its
			// score : either it participates in a situation or participates to
			// an event and has sensitiveness to that event. Otherwise
			// we do not compute its score
			// -------------------------
			if (mainTopic == false) {
				String query_context = "  PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
						+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
						+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						+ " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
						+ " SELECT ?node WHERE { { " + ts.getTopic()
						+ " sentilo:participatesIn ?node . ?node a boxing:Situation } UNION { " + ts.getTopic()
						+ " sentilo:participatesIn ?node . ?node a ?nodetype . ?nodetype rdfs:subClassOf+ dul:Event . "
						+ ts.getTopic() + " sentilo:playsSensitiveRole ?node }  } ";

				if (DEBUG)
					System.out.println("query_context:" + query_context);

				model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
				query_in = QueryFactory.create(query_context, Syntax.syntaxARQ);
				queryExecution = QueryExecutionFactory.create(query_in, model);
				com.hp.hpl.jena.query.ResultSet resultSet_context = queryExecution.execSelect();

				if (resultSet_context.hasNext()) {
					computeScore = true;
					if (DEBUG)
						System.out.println("COMPUTERSCORE TRUE");
				} else { // if computeScore is false we see if there are quality
							// associated and in that case we give those score
							// and in case there are truthValue associated with
							// the topic we take them into account
					if (DEBUG)
						System.out.println("COMPUTERSCORE FALSE");
					String query2 = " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
							+ " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
							+ " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
							+ " SELECT ?quality ?score ?truth WHERE { " + ts.getTopic()
							+ " dul:hasQuality ?quality . ?quality sentilo:hasScore ?score OPTIONAL {" + ts.getTopic()
							+ " boxing:hasTruthValue ?truth } } ";
					if (DEBUG)
						System.out.println("QUERY2---------" + query2);
					model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
					Query query_op1 = QueryFactory.create(query2, Syntax.syntaxARQ);
					queryExecution = QueryExecutionFactory.create(query_op1, model);
					com.hp.hpl.jena.query.ResultSet resultSet_out = queryExecution.execSelect();

					while (resultSet_out.hasNext()) {
						QuerySolution sol = resultSet_out.next();
						String score = sol.getLiteral("score").getLexicalForm().replaceAll("\"", "");
						double score_num = Double.parseDouble(score);
						if (score_num > 0.0) {
							if (sol.getResource("truth") != null)
								neg.add(-Math.abs(score_num));
							else
								pos.add(score_num);
						} else if (score_num < 0) {
							if (sol.getResource("truth") != null)
								pos.add(Math.abs(score_num));
							else
								neg.add(score_num);
						} else
							neutral++;
					}
				}
			}
			// -------------------------

			if (DEBUG) {
				System.out.println("AFTER IF MAINTOPIC==false");
				System.out.println("pos:" + pos);
				System.out.println("neg:" + neg);
				System.out.println("neutral:" + neutral);
			}
			// if the topic is not mainTopic then
			// if it participates in a situation with a score, assigns a score
			// to it
			// if it participates in an event with a score and has a factual
			// relation, assigns a score to it
			// -------------------------
			boolean topicScoreSign = false;
			if (mainTopic == false) {
				String query_real = " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
						+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
						+ " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
						+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						+ " SELECT ?score ?x ?posfact ?negfact ?truth_value ?node ?truth_sit WHERE { { " + ts.getTopic()
						+ " sentilo:participatesIn ?node . ?node a boxing:Situation { { ?node sentilo:hasScore ?score } UNION { ?node sentilo:hasPosScore ?score} UNION {?node sentilo:hasNegScore ?score} UNION { ?node boxing:hasTruthValue ?truth_value FILTER NOT EXISTS { ?node sentilo:hasAvgScore ?sc } } } OPTIONAL {"
						+ ts.getTopic() + " a boxing:Situation . " + ts.getTopic()
						+ " boxing:hasTruthValue ?truth_sit } bind ('1' as ?x) } UNION { " + ts.getTopic()
						+ " sentilo:participatesIn ?node . ?node a ?nodetype . ?nodetype rdfs:subClassOf+ dul:Event . "
						+ ts.getTopic()
						+ " sentilo:playsSensitiveRole ?node . { { ?node sentilo:hasScore ?score } UNION { ?node sentilo:hasPosScore ?score } UNION { ?node sentilo:hasNegScore ?score } UNION { ?node sentilo:participatesIn ?situation . ?situation a boxing:Situation . ?el sentilo:participatesIn ?situation . ?el sentilo:hasPosScore ?score } UNION { ?node sentilo:participatesIn ?situation . ?situation a boxing:Situation . ?el sentilo:participatesIn ?situation . ?el sentilo:hasNegScore ?score } UNION { ?node boxing:hasTruthValue ?truth_value FILTER NOT EXISTS { ?node sentilo:hasAvgScore ?sc } } } OPTIONAL { "
						+ ts.getTopic() + " sentilo:isPositivelyAffectedBy ?posfact } OPTIONAL { " + ts.getTopic()
						+ " sentilo:isNegativelyAffectedBy ?negfact }  bind ('2' as ?x) } } ORDER BY ?node ";

				if (DEBUG)
					System.out.println("query_real:" + query_real);

				model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
				query_in = QueryFactory.create(query_real, Syntax.syntaxARQ);
				queryExecution = QueryExecutionFactory.create(query_in, model);
				com.hp.hpl.jena.query.ResultSet resultSet_real = queryExecution.execSelect();

				String current = ""; // current solves the problem of nested
										// situations with truth_value or isNeg.
										// If two situations are tied with the
										// same topic,
				// it is assumed they have the same truth_value and isNeg
				// scores. So we just count one
				// Example John and Mary do not think that Robert is not a good
				// guy
				Vector pos_local = new Vector();
				Vector neg_local = new Vector();
				while (resultSet_real.hasNext()) {
					QuerySolution solctx = resultSet_real.next();
					if (DEBUG)
						System.out.println("ALEXALEXALEAX:" + topicScoreSign + " node:" + solctx.getResource("node")
								+ " " + solctx.getResource("truth_value") + " " + solctx.getLiteral("score"));
					if (current.equals(""))
						current = solctx.getResource("node").getURI();
					else if (current.equals(solctx.getResource("node").getURI()) == false) {
						topicScoreSign = false;
						current = solctx.getResource("node").getURI();
					}
					if (solctx.getResource("truth_value") != null || solctx.getResource("truth_sit") != null)
						topicScoreSign = (topicScoreSign == true) ? false : true;
					if (solctx.getLiteral("score") != null) {
						String opt = solctx.getLiteral("x").getLexicalForm();
						String score_str = solctx.getLiteral("score").getLexicalForm().replaceAll("\"", "");
						if (score_str.equals("isNeg")) {
							topicScoreSign = (topicScoreSign == true) ? false : true;
						} else {
							if (DEBUG)
								System.out.println("DENTRO ELSE..." + opt);
							double score = Double.parseDouble(score_str);
							if (opt.equals("1")) {
								if (score < 0)
									neg_local.add(score);
								else if (score > 0)
									pos_local.add(score);
								else
									neutral++;
							} else if (opt.equals("2")) {
								if (DEBUG)
									System.out.println("NEG-----NEG-----NEGFACT----score: " + score);
								if ((score < 0 && solctx.getResource("negfact") == null)
										|| (score > 0 && solctx.getResource("negfact") != null))
									neg_local.add(-Math.abs(score));
								else if ((score < 0 && solctx.getResource("negfact") != null)
										|| (score > 0 && solctx.getResource("negfact") == null))
									pos_local.add(Math.abs(score));
							}
						}
					}
					if (DEBUG)
						System.out.println("DEDEJDJEDEJ:" + topicScoreSign);
				}
				if (topicScoreSign) { // invert the scores just collected
					Vector tmp = pos_local;
					pos_local = neg_local;
					neg_local = tmp;
					for (int i = 0; i < pos_local.size(); i++) {
						double el = ((Double) pos_local.get(i)).doubleValue();
						pos_local.set(i, -el);
						pos.add(-el);
					}
					for (int i = 0; i < neg_local.size(); i++) {
						double el = ((Double) neg_local.get(i)).doubleValue();
						neg_local.set(i, -el);
						neg.add(-el);
					}
				} else { // add the collected scores to the global scores
					pos.addAll(pos_local);
					neg.addAll(neg_local);
				}
			}
			// -------------------------
			if (DEBUG) {
				System.out.println("DOPO ALEX");
				System.out.println("POS:" + pos);
				System.out.println("NEG:" + neg);
			}

			// if it is a mainTopic or computeScore is true (see above)
			// if the current topic has a hastruthvalue relation, invert the
			// score of each in all the hasQuality relations the current topic
			// has (if any)
			// if the current topic has a hasQuality relation, check whether the
			// value of that relation has a hastruthvalue relation and in that
			// case invert the score of the quality
			// -------------------------
			/*
			 * if(computeScore == true || mainTopic==true) { String query2 =
			 * "PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
			 * +
			 * " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
			 * +
			 * " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
			 * + " SELECT ?quality ?score WHERE { { "+ts.getTopic()+
			 * " boxing:hasTruthValue ?value . "+ts.getTopic()+
			 * " dul:hasQuality ?quality . ?quality sentilo:hasScore ?score } } "
			 * ; // re ?score} } UNION { ?quality sentilo:hasNewScore ?score } }
			 * } } ";// UNION { "+ts.getTopic()+
			 * " dul:hasQuality ?quality . ?quality boxing:hasTruthValue ?value bind ('2' as ?x)} UNION { "
			 * +ts.getTopic()+
			 * " boxing:hasTruthValue ?value bind ('3' as ?x) FILTER NOT EXISTS { "
			 * +ts.getTopic()+" dul:hasQuality ?quality } } UNION { "
			 * +ts.getTopic()+
			 * " dul:hasQuality ?quality . { { ?quality sentilo:hasScore ?score  FILTER NOT EXISTS {?quality sentilo:hasNewScore ?score} } UNION { ?quality sentilo:hasNewScore ?score } }  . ?quality boxing:hasTruthValue ?val bind ('4' as ?x) FILTER NOT EXISTS {"
			 * +ts.getTopic()+" boxing:hasTruthValue ?value } } } "; //if(DEBUG)
			 * // System.out.println("QUE:"+query2);
			 * 
			 * model =
			 * JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
			 * Query query_op1 = QueryFactory.create(query2, Syntax.syntaxARQ);
			 * queryExecution = QueryExecutionFactory.create(query_op1, model);
			 * com.hp.hpl.jena.query.ResultSet resultSet_out =
			 * queryExecution.execSelect();
			 * 
			 * while(resultSet_out.hasNext()) {
			 * 
			 * QuerySolution sol = resultSet_out.next(); //String score =
			 * sol.getLiteral("score").getLexicalForm().replaceAll("\"","");
			 * //double score_num = Double.parseDouble(score); //triple = new
			 * TripleImpl(new UriRef(sol.getResource("quality").getURI()),new
			 * UriRef(
			 * "http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),new
			 * PlainLiteralImpl(new Double(score_num).toString()));
			 * //tripleSentilo.remove(triple); //score_num *= -1; //triple = new
			 * TripleImpl(new UriRef(sol.getResource("quality").getURI()),new
			 * UriRef(
			 * "http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),new
			 * PlainLiteralImpl(String.format("%.3f",score_num))); if(DEBUG)
			 * System.out.println(
			 * "---------**********************a*********************** -SCORE REMOVED AND INSERTED-------"
			 * +triple); //tripleSentilo.add(triple); //topicScoreSign =
			 * (topicScoreSign == false) ? true : false; // QUESTA
			 * 
			 * }
			 * 
			 * // ho aggiunto il secondo pezzo dell'union query2 =
			 * "PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
			 * +
			 * " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
			 * +
			 * " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
			 * + " SELECT ?value WHERE { { "+ts.getTopic()+
			 * " boxing:hasTruthValue ?value FILTER NOT EXISTS { { "
			 * +ts.getTopic()+" dul:hasQuality ?quality } UNION { "
			 * +ts.getTopic()+" a boxing:Situation } } } } ";// UNION {
			 * "+ts.getTopic()+" sentilo:participatesIn ?situation . ?situation
			 * a boxing:Situation . ?situation boxing:hasTruthValue ?value } }
			 * ";
			 * 
			 * model =
			 * JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
			 * query_op1 = QueryFactory.create(query2, Syntax.syntaxARQ);
			 * queryExecution = QueryExecutionFactory.create(query_op1, model);
			 * resultSet_out = queryExecution.execSelect();
			 * 
			 * if(resultSet_out.hasNext()) topicScoreSign = (topicScoreSign ==
			 * false) ? true : false;
			 * 
			 * }
			 */
			// -------------------------

			if (mainTopic || computeScore) {
				// Get the hasScore relation from the current topic and updates
				// pos, neg, neutral accordingly
				// -------------------------
				String query_op = "PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
						+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
						+ " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
						+ " SELECT ?score ?truth ?isneg WHERE { { " + ts.getTopic()
						+ " sentilo:hasScore ?score OPTIONAL { " + ts.getTopic() + " boxing:hasTruthValue ?truth } } " + // UNION
																															// {
																															// "+ts.getTopic()+"
																															// sentilo:participatesIn
																															// ?sub
																															// .
																															// ?sub
																															// sentilo:hasAvgScore
																															// ?score
																															// }
						" UNION { " + ts.getTopic()
						+ " dul:hasQuality ?quality . ?quality sentilo:hasScore ?score OPTIONAL { " + ts.getTopic()
						+ " boxing:hasTruthValue ?truth }  OPTIONAL { " + ts.getTopic()
						+ " ?prop ?sit . ?sit a boxing:Situation . ?sit boxing:hasTruthValue ?truth FILTER NOT EXISTS { "
						+ ts.getTopic() + " a boxing:Situation } } OPTIONAL { " + ts.getTopic()
						+ " ?prop ?sit . ?sit a boxing:Situation . ?sit sentilo:hasScore ?isneg FILTER NOT EXISTS { "
						+ ts.getTopic() + " a boxing:Situation } } } } ";

				if (DEBUG)
					System.out.println("QUERY_OP:" + query_op);
				model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
				Query query_op1 = QueryFactory.create(query_op, Syntax.syntaxARQ);
				queryExecution = QueryExecutionFactory.create(query_op1, model);
				com.hp.hpl.jena.query.ResultSet res_qu = queryExecution.execSelect();

				while (res_qu.hasNext()) {
					QuerySolution sol = res_qu.next();
					boolean change = false;
					if (sol.getLiteral("isneg") != null)
						if (sol.getLiteral("isneg").getLexicalForm().replaceAll("\"", "").equals("isNeg"))
							change = (change == true) ? false : true;
					if (sol.getResource("truth") != null)
						change = (change == true) ? false : true;
					if (sol.getLiteral("score") != null) {
						String score = sol.getLiteral("score").getLexicalForm();// .substring(1,sol.get("score").getURI().length()-1);
						double score_num = Double.parseDouble(score);
						if (score_num > 0.0) {
							if (change == false)
								pos.add(score_num);
							else
								neg.add(-Math.abs(score_num));
						} else if (score_num < 0) {
							if (change == false)
								neg.add(score_num);
							else
								pos.add(Math.abs(score_num));
						} else
							neutral++;
					}
				}
			}

			if (DEBUG) {
				System.out.println("POSS:" + pos);
				System.out.println("NEGG:" + neg);
			}

			// case of I do not think that John loves Mary. think is trigger and
			// considered as topics. Here we look for derivative scores and
			// hasTruthValue
			/*
			 * query_op =
			 * "PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
			 * +
			 * " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
			 * +
			 * " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
			 * + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
			 * " SELECT ?score WHERE { "+ts.getTopic()+
			 * " ?rel ?node . ?node a ?nodetype . ?nodetype rdfs:subClassOf+ dul:Event . ?node sentilo:hasAvgScore ?score } "
			 * ;
			 * 
			 * System.out.println("QUERY_OPP:"+query_op); model =
			 * JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
			 * query_op1 = QueryFactory.create(query_op, Syntax.syntaxARQ);
			 * queryExecution = QueryExecutionFactory.create(query_op1, model);
			 * res_qu = queryExecution.execSelect();
			 * 
			 * while(res_qu.hasNext()) { QuerySolution sol = res_qu.next();
			 * System.out.println("ANNI:"+sol.getLiteral("score"));
			 * if(sol.getLiteral("score")!=null) { String score =
			 * sol.getLiteral("score").getLexicalForm();//
			 * .substring(1,sol.get("score").getURI().length()-1); double
			 * score_num = Double.parseDouble(score); if(score_num>0.0) {
			 * pos.add(score_num); } else if(score_num<0) { neg.add(score_num);
			 * } else neutral++; } } System.out.println("OP1:pos:"+pos);
			 * System.out.println("OP1:neg:"+neg);
			 */
			if (DEBUG)
				System.out.println("topicScoreSign:" + topicScoreSign);

			// if there is the trigger associated to the sentence and that
			// carries a positive or negative sentiment, include that in pos,
			// neg, neutral according to the hasTruthValue rel
			// -------------------------

			if (mainTopic == true) {

				String sparql = " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
						+ " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
						+ " SELECT ?score ?truth_value ?truth_value_top ?type WHERE { ?trigger ?rel " + ts.getTopic()
						+ " . ?node sentilo:hasOpinionTrigger ?trigger . ?trigger a ?type OPTIONAL { " + ts.getTopic()
						+ " boxing:hasTruthValue ?truth_value_top } OPTIONAL { ?trigger sentilo:hasScore ?score } OPTIONAL { ?trigger boxing:hasTruthValue ?truth_value} FILTER NOT EXISTS { ?node sentilo:hasOpinionTrigger "
						+ ts.getTopic() + " } } ";

				if (DEBUG)
					System.out.println(" QUERY_TRIG:" + sparql);

				model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
				Query query_trig = QueryFactory.create(sparql, Syntax.syntaxARQ);
				queryExecution = QueryExecutionFactory.create(query_trig, model);
				com.hp.hpl.jena.query.ResultSet resultSet_trig = queryExecution.execSelect();

				while (resultSet_trig.hasNext()) {
					QuerySolution sol = resultSet_trig.next();
					String verb = sol.getResource("type").getURI()
							.substring(sol.getResource("type").getURI().indexOf("#") + 1).toLowerCase();
					String score = "";
					double score_num = -999;
					if (sol.getLiteral("score") != null) {
						score = sol.getLiteral("score").getLexicalForm();
						score_num = Double.parseDouble(score);
					}
					if (DEBUG)
						System.out.println("verb:" + verb);

					boolean change = false;
					if (sol.get("truth_value") != null)
						change = (change == false) ? true : false;
					// if(sol.get("truth_value_top")!=null)
					// change = (change == false) ? true : false;
					if (change) {
						if (holder_pol_pos.contains(verb)) { // ||
																// holder_act_true.contains(verb))
							if (score_num != -999)
								pos.add(Math.abs(score_num));
						}
						if (holder_pol_neg.contains(verb)) { // ||
																// holder_act_false.contains(verb))
							if (score_num != -999)
								neg.add(-Math.abs(score_num));
						}
						if (holder_act_true.contains(verb)) {
							triple = new TripleImpl(new UriRef(ts.getTopic().replace("<", "").replace(">", "")),
									new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
									new PlainLiteralImpl("isNeg"));
							tripleSentilo.add(triple);
						}
						Vector tmp = pos;
						pos = neg;
						neg = tmp;
						for (int i = 0; i < pos.size(); i++) {
							double el = ((Double) pos.get(i)).doubleValue();
							pos.set(i, -el);
						}
						for (int i = 0; i < neg.size(); i++) {
							double el = ((Double) neg.get(i)).doubleValue();
							neg.set(i, -el);
						}
					} else {
						if (holder_pol_pos.contains(verb)) // ||
															// holder_act_true.contains(verb))
							if (score_num != -999)
								pos.add(Math.abs(score_num));
						if (holder_pol_neg.contains(verb)) // ||
															// holder_act_false.contains(verb))
							if (score_num != -999)
								neg.add(-Math.abs(score_num));
						if (holder_act_false.contains(verb)) {
							triple = new TripleImpl(new UriRef(ts.getTopic().replace("<", "").replace(">", "")),
									new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasScore"),
									new PlainLiteralImpl("isNeg"));
							tripleSentilo.add(triple);
						}
						/*
						 * if(holder_act_false.contains(verb)) { Vector tmp =
						 * pos; pos = neg; neg = tmp; for(int
						 * i=0;i<pos.size();i++) { double el =
						 * ((Double)pos.get(i)).doubleValue(); pos.set(i,-el); }
						 * for(int i=0;i<neg.size();i++) { double el =
						 * ((Double)neg.get(i)).doubleValue(); neg.set(i,-el); }
						 * }
						 */
					}
				}
				if (DEBUG) {
					System.out.println("DOPO QUERY TRIG");
					System.out.println("OP1:pos:" + pos);
					System.out.println("OP1:neg:" + neg);
				}

				/*
				 * sparql =
				 * "PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
				 * +
				 * " PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				 * + " SELECT ?truth_value WHERE { {?situation ?rel "+
				 * ts.getTopic()+
				 * " . ?situation a boxing:Situation . ?situation boxing:hasTruthValue ?truth_value } UNION { "
				 * + ts.getTopic()+" a boxing:Situation . ?verb ?prop "+
				 * ts.getTopic()+
				 * " . ?verb boxing:hasTruthValue ?truth_value . ?node sentilo:hasOpinionTrigger ?verb } } "
				 * ; System.out.println("query situation truth:value:"+sparql);
				 * model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(
				 * tripleSentilo); query_trig = QueryFactory.create(sparql,
				 * Syntax.syntaxARQ); queryExecution =
				 * QueryExecutionFactory.create(query_trig, model);
				 * resultSet_trig = queryExecution.execSelect();
				 * 
				 * if(resultSet_trig.hasNext()) { System.out.println(
				 * "SITUATION is attached to topic and has truthValue"); Vector
				 * tmp = pos; pos = neg; neg = tmp; for(int
				 * i=0;i<pos.size();i++) { double el =
				 * ((Double)pos.get(i)).doubleValue(); pos.set(i,-el); } for(int
				 * i=0;i<neg.size();i++) { double el =
				 * ((Double)neg.get(i)).doubleValue(); neg.set(i,-el); } }
				 */
			}
			// -------------------------

			// if the sign of the score has changed invert pos and neg vectors
			// -------------------------
			/*
			 * if(topicScoreSign==true) { Vector tmp = pos; pos = neg; neg =
			 * tmp; for(int i=0;i<pos.size();i++) { double el =
			 * ((Double)pos.get(i)).doubleValue(); pos.set(i,-el); } for(int
			 * i=0;i<neg.size();i++) { double el =
			 * ((Double)neg.get(i)).doubleValue(); neg.set(i,-el); } }
			 */
			if (DEBUG) {
				System.out.println("DOPO if topicScoreSign");
				System.out.println("OP1:pos:" + pos);
				System.out.println("OP1:neg:" + neg);
			}
			// -------------------------

			// if the current topic is a mainTopic

			if (DEBUG) {
				System.out.println("POS:" + pos);
				System.out.println("NEG:" + neg);
				System.out.println("neutral:" + neutral);
			}
			double avg_all = 0;
			double avg_pos = 0;
			double avg_neg = 0;

			if (pos.size() > 0) {
				for (int i = 0; i < pos.size(); i++) {
					avg_all += (Double) pos.get(i);
					avg_pos += (Double) pos.get(i);
				}
				avg_pos /= pos.size();
				String avg_pos_str = String.format("%.3f", .2);
				triple = new TripleImpl(new UriRef(ts.getTopic().replace("<", "").replace(">", "")),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasPosScore"),
						new PlainLiteralImpl(avg_pos_str));
				tripleSentilo.add(triple);
			} else
				avg_pos = -999;

			if (neg.size() > 0) {
				for (int i = 0; i < neg.size(); i++) {
					avg_all += (Double) neg.get(i);
					avg_neg += (Double) neg.get(i);
				}
				avg_neg /= neg.size();
				String avg_neg_str = String.format("%.3f", .3);
				triple = new TripleImpl(new UriRef(ts.getTopic().replace("<", "").replace(">", "")),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasNegScore"),
						new PlainLiteralImpl(avg_neg_str));
				tripleSentilo.add(triple);
			} else
				avg_neg = -999;

			if (pos.size() > 0 || neg.size() > 0 || neutral > 0) {
				avg_all /= (pos.size() + neg.size() + neutral);
				String avg_str = String.format("%.3f", .4);
				triple = new TripleImpl(new UriRef(ts.getTopic().replace("<", "").replace(">", "")),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasAvgScore"),
						new PlainLiteralImpl(avg_str));

				tripleSentilo.add(triple);
			} else
				avg_all = -999;

			if (DEBUG)
				System.out.println("TOPIC " + ts.getTopic() + "Avg_all:" + avg_all + " avg_pos:" + avg_pos + " avg_neg:"
						+ avg_neg);

		}

		// compute scores for holders
		query = "  PREFIX sentilo: <http://ontologydesignpatterns.org/ont/sentilo.owl#> "
				+ " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "
				+ " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "
				+ "  SELECT ?holder ?quality ?score ?truth_value WHERE { ?node sentilo:hasHolder ?holder . ?holder dul:hasQuality ?quality . ?quality sentilo:hasScore ?score OPTIONAL {?holder boxing:hasTruthValue ?truth_value } FILTER NOT EXISTS { { ?node1 sentilo:hasTopic ?holder } UNION { ?node1 sentilo:hasSubTopic ?holder } } }";
		if (DEBUG)
			System.out.println("SCORE HOLDER:" + query);

		model = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleSentilo);
		query1 = QueryFactory.create(query, Syntax.syntaxARQ);
		queryExecution = QueryExecutionFactory.create(query1, model);
		resultSet = queryExecution.execSelect();

		HashMap map_holder_score = new HashMap();
		HashMap map_holder_count = new HashMap();

		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			triple = null;
			String score = sol.getLiteral("score").getLexicalForm();
			String holder = sol.getResource("holder").toString().replace("<", "").replace(">", "");
			if (DEBUG)
				System.out.println("HOLDER:" + holder);

			double score_num_old = 0;
			if (map_holder_score.get(holder) != null)
				score_num_old = ((Double) map_holder_score.get(holder)).doubleValue();
			double score_num = Double.parseDouble(score);
			if (sol.getResource("truth_value") != null) {
				score_num_old += (-score_num);
			} else
				score_num_old += score_num;

			int count_holder = 0;
			if (map_holder_count.get(holder) != null)
				count_holder = ((Integer) map_holder_count.get(holder)).intValue();
			count_holder++;
			map_holder_count.put(holder, count_holder);
			map_holder_score.put(holder, score_num_old);
		}

		Iterator it = map_holder_score.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry holder_entry = (Map.Entry) it.next();
			String holder = (String) holder_entry.getKey();
			double score = ((Double) holder_entry.getValue()).doubleValue();
			int count = ((Integer) map_holder_count.get(holder)).intValue();
			score /= count;
			String avg_str = String.format("%.3f", .5);
			triple = null;
			if (score > 0)
				triple = new TripleImpl(new UriRef(holder),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasPosScore"),
						new PlainLiteralImpl(avg_str));
			else
				triple = new TripleImpl(new UriRef(holder),
						new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasNegScore"),
						new PlainLiteralImpl(avg_str));
			tripleSentilo.add(triple);
			triple = new TripleImpl(new UriRef(holder),
					new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#hasAvgScore"),
					new PlainLiteralImpl(avg_str));
			tripleSentilo.add(triple);
		}
	}

	public Sentilo(String text, TripleCollection tripleCollection, Vector holder_act_true, Vector holder_act_false,
			Vector holder_pol_pos, Vector holder_pol_neg, Model model, HashMap sentiwordnet, WSDOutput wsdOutput,
			HashMap sensitiveness, FrequencyBasedWSD frequencyBasedWSD, boolean useSenticnet, boolean error) {

		this.events = new Vector();
		this.holders = new Vector();
		this.mtopics = new Vector();
		this.subtopics = new Vector();
		this.text = text;
		this.tripleCollection = tripleCollection;
		this.list_topics = new HashSet();
		this.modelSenti = model;
		this.sentiwordnet = sentiwordnet;
		this.wsdOutput = wsdOutput;
		this.sensitiveness = sensitiveness;
		this.frequencyBasedWSD = frequencyBasedWSD;
		this.USEDEFAULTSCORESTRATEGY = (useSenticnet == true) ? false : true;
		this.adjToMoods = new HashMap();
		this.advToMoods = new HashMap();
		this.verbToMoods = new HashMap();
		this.nounToMoods = new HashMap();


		try{

			InputStream depecheMood = Sentilo.class.getResourceAsStream("/DepecheMood_tfidf.txt");

			BufferedReader reader = new BufferedReader(new InputStreamReader(depecheMood));
			String lineMood = null;

			reader.readLine(); // Get rid of the first line of depechemood, as it contains no useful info

			while ((lineMood = reader.readLine()) != null){

				//TODO: make this parsing better
				String word = lineMood.split("#")[0];
				String type = lineMood.split("#")[1].split("	")[0];
				Double afraid = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.AFRAID.ordinal()+1]);
				Double amused = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.AMUSED.ordinal()+1]);
				Double angry = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.ANGRY.ordinal()+1]);
				Double annoyed = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.ANNOYED.ordinal()+1]);
				Double dontCare = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.DONT_CARE.ordinal()+1]);
				Double happy = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.HAPPY.ordinal()+1]);
				Double inspired = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.INSPIRED.ordinal()+1]);
				Double sad = Double.parseDouble(lineMood.split("#")[1].split("	")[Mood.SAD.ordinal()+1]);

				Vector<Double> moods = new Vector<Double>();

				moods.add(afraid);
				moods.add(amused);
				moods.add(angry);
				moods.add(annoyed);
				moods.add(dontCare);
				moods.add(happy);
				moods.add(inspired);
				moods.add(sad);

				if (DEBUG) {
					System.out.println("I just read the word: " + word + " which is of type: " + type);
					System.out.println(afraid);
					System.out.println(amused);
					System.out.println(angry);
					System.out.println(annoyed);
					System.out.println(dontCare);
					System.out.println(happy);
					System.out.println(inspired);
					System.out.println(sad);
				}


				if (type.equals("n"))
					nounToMoods.put(word, moods);
				else if (type.equals("a"))
					adjToMoods.put(word, moods);
				else if (type.equals("v"))
					verbToMoods.put(word, moods);
				else
					advToMoods.put(word, moods);
			}



		if (DEBUG) {
			System.out.println("Let's try \"output\" noun: " + nounToMoods.get("output"));
			System.out.println("Let's try \"output\" verb: " + verbToMoods.get("output"));


			System.out.println("Let's try \"output\" noun at angry: " + nounToMoods.get("output").get(Mood.ANGRY.ordinal()));
			System.out.println("Let's try \"output\" verb at amused: " + verbToMoods.get("output").get(Mood.AMUSED.ordinal()));
		}



		}catch(Exception e)
		{			
			System.out.println("^^^^ECCEZIONE^^^^");
			System.out.println(e.getMessage());
			System.out.println("^^^^ECCEZIONE^^^^");
		}


		System.out.println("SENS:"+this.sensitiveness);
		
		if (error) {
			tripleSentilo = new SimpleMGraph();
			Triple newTriple = new TripleImpl(new UriRef("http://ontologydesignpatterns.org/ont/sentilo.owl#Error"),
					new UriRef("http://www.ontologydesignpatterns.org/ont/sentilo.owl#Error"),
					new PlainLiteralImpl("Error"));
			tripleSentilo.add(newTriple);

			return;
		}

		if (DEBUG) {
			System.out.println("USEDEFAULTSCORESTRATEGY:" + this.USEDEFAULTSCORESTRATEGY);
			System.out.println("-----+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+++-+-+-+-+" + tripleCollection);
		}

		/* Word Sense Disambiguation */
		try {
			if (this.wsdOutput != null) { // null by default
				tokenToIdx = new HashMap();
				for (WSDObject wsd : this.wsdOutput.getWsdObjects()) {
					if (DEBUG)
						System.out.println("TOK:" + wsd.getToken() + " OFFSET:" + wsd.getOffset() + "POS:"
								+ wsd.getPos() + " SYN:" + wsd.getSynset() + " TYPE:" + wsd.getFredType());
					tokenToIdx.put(wsd.getToken().toLowerCase().trim(), wsd.getSynset().substring(1));
					// creating hashmap token -> wordnet id

				}
				if (DEBUG)
					System.out.println("WSD" + this.wsdOutput.getWsdObjects());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		tripleSentilo = new SimpleMGraph();

		
		long start = System.currentTimeMillis();
		
		// add the four header nodes to tripleSentilo
		addSentiHeaderNodes();

		// Four query triggering for four different kind of triggering verbs
		//String hat = subquery(holder_act_true);
		//String sparql_hat = SparqlQuerySentilo.text_query + " FILTER (" + hat + ")} ";

		//String haf = subquery(holder_act_false);
		//String sparql_haf = SparqlQuerySentilo.text_query + " FILTER (" + haf + ")} ";

		//String hpp = subquery(holder_pol_pos);
		//String sparql_hpp = SparqlQuerySentilo.text_query + " FILTER (" + hpp + ")} ";

		//String hpn = subquery(holder_pol_neg);
		//String sparql_hpn = SparqlQuerySentilo.text_query + " FILTER (" + hpn + ")}";

		// Non-triggering query
		//String sparql_simple1 = SparqlQuerySentilo.text_query + " } ";

		// System.out.println(sparql_hat);
		
		Query query = QueryFactory.create(SparqlQuerySentilo.SELECT_QUERY, Syntax.syntaxARQ);
		Model m = JenaToClerezzaConverter.clerezzaMGraphToJenaModel(tripleCollection);
		long end = System.currentTimeMillis();
		log.info("Operation 1.1-a {}", (end-start));
		
		
		start = System.currentTimeMillis();
		QueryExecution queryExecution = QueryExecutionFactory.create(query, m);
		ResultSet resultSet = queryExecution.execSelect();
		List<QuerySolution> querySolutions = new ArrayList<QuerySolution>();
		while(resultSet.hasNext())
			querySolutions.add(resultSet.next());
		end = System.currentTimeMillis();
		
		log.info("Operation 1.1 {}", (end-start));
 
		// Looking for results for any query. Triggering first and, if any
		// result is not found, look into the non-triggering
		//boolean has_ret_hat = extractResult(sparql_hat);
		start = System.currentTimeMillis();
		boolean has_ret_hat = extractResultNew(querySolutions, holder_act_true, holder_act_false, holder_pol_pos, holder_pol_neg);
		end = System.currentTimeMillis();
		/*log.info("Operation 1.2 {}", (end-start));
		// System.out.println("RESULT FOR HOLDER ACTIVATION FALSE");
		//boolean has_ret_haf = extractResult(sparql_haf);
		start = System.currentTimeMillis();
		boolean has_ret_haf = extractResultNew(querySolutions, holder_act_false);
		end = System.currentTimeMillis();
		log.info("Operation 1.3 {}", (end-start));
		
		// System.out.println("RESULT FOR HOLDER POLARITY POSITIVE");
		//boolean has_ret_hpp = extractResult(sparql_hpp);
		start = System.currentTimeMillis();
		boolean has_ret_hpp = extractResultNew(querySolutions, holder_pol_pos);
		end = System.currentTimeMillis();
		log.info("Operation 1.4 {}", (end-start));
		// System.out.println("RESULT FOR HOLDER POLARITY NEGATIVE");
		//boolean has_ret_hpn = extractResult(sparql_hpn);
		start = System.currentTimeMillis();
		boolean has_ret_hpn = extractResultNew(querySolutions, holder_pol_neg);
		end = System.currentTimeMillis();
		log.info("Operation 1.5 {}", (end-start));
		*/
		
		start = System.currentTimeMillis();
		//if (!has_ret_hat && !has_ret_haf && !has_ret_hpp && !has_ret_hpn) {
		if (!has_ret_hat) {
			String query_general = SparqlQuerySentilo.query_general;// simple_text_query2;
			boolean query_general_ret = extractResult(query_general);
			if (DEBUG)
				System.out.println("QUERY_GENERAL:" + query_general);
			if (query_general_ret == false) {
				if (DEBUG)
					System.out.println("Query simple....");
				String sparql_simple = SparqlQuerySentilo.simple_text_query2;
				boolean has_ret_simple = extractResult(sparql_simple);
			}
		}
		
		end = System.currentTimeMillis();
		
		log.info("Operation 1.6 {}", (end-start));

		// Vector discard = null;//FilterObjects(events);
		// addTripleEvent(events);
		
		start = System.currentTimeMillis();
		addTripleSentiElement(events, 2);
		end = System.currentTimeMillis();
		log.info("Operation 2 {}", (end-start));
		
		// discard = null;//FilterObjects(holders);
		// addTripleHolder(holders);
		start = System.currentTimeMillis();
		addTripleSentiElement(holders, 1);
		end = System.currentTimeMillis();
		log.info("Operation 3 {}", (end-start));

		// discard = null;//FilterObjects(mtopics);
		// addTripleMtopic(mtopics);
		start = System.currentTimeMillis();
		addTripleSentiElement(mtopics, 4);
		end = System.currentTimeMillis();
		log.info("Operation 4 {}", (end-start));
		
		// discard = null;//FilterObjects(subtopics);
		// addTripleSubtopic(subtopics);
		start = System.currentTimeMillis();
		addTripleSentiElement(subtopics, 3);
		end = System.currentTimeMillis();
		log.info("Operation 5 {}", (end-start));

		
		start = System.currentTimeMillis();
		addTripleDottedRelations();
		end = System.currentTimeMillis();
		log.info("Operation 6 {}", (end-start));

		start = System.currentTimeMillis();
		removeDuplicates();
		end = System.currentTimeMillis();
		log.info("Operation 7 {}", (end-start));

		start = System.currentTimeMillis();
		changeOpinionatedContextWithMD5();
		end = System.currentTimeMillis();
		log.info("Operation 8 {}", (end-start));

		start = System.currentTimeMillis();
		addPosTagNodes();
		end = System.currentTimeMillis();
		log.info("Operation 9 {}", (end-start));

		
		start = System.currentTimeMillis();
		addOffsetTagNodes();
		end = System.currentTimeMillis();
		log.info("Operation 10 {}", (end-start));

		// setScoreToEachNode(); // for nouns
		start = System.currentTimeMillis();
		setScoreToEachVerb();
		end = System.currentTimeMillis();
		log.info("Operation 11 {}", (end-start));

		start = System.currentTimeMillis();
		addParticipatesRelations();
		end = System.currentTimeMillis();
		log.info("Operation 12 {}", (end-start));

		// DA QUI
		start = System.currentTimeMillis();		
		addSensitivenessRelations();
		end = System.currentTimeMillis();
		log.info("Operation 13 {}", (end-start));
		
		start = System.currentTimeMillis();
		sentiloScore(holder_pol_pos, holder_pol_neg, holder_act_true, holder_act_false);
		end = System.currentTimeMillis();
		log.info("Operation 14 {}", (end-start));

	}

	private org.apache.clerezza.rdf.core.Resource convertFromJenaResource(RDFNode rdfNode) {
		org.apache.clerezza.rdf.core.Resource resource = null;
		if (rdfNode != null) {
			if (rdfNode.isResource()) {
				resource = new UriRef(((Resource) rdfNode).getURI());
			} else {
				resource = new PlainLiteralImpl(((Literal) rdfNode).getLexicalForm());
			}
		}

		return resource;
	}
}
