package it.cnr.istc.stlab.ktools.sentilo.utils;


public class SparqlQuerySentilo {
    
    public static String text_query = "PREFIX vnrole: <http://www.ontologydesignpatterns.org/ont/vn/abox/role/> "+
    " PREFIX boxer: <http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#> "+
    " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "+
    " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "+
    " PREFIX : <http://www.ontologydesignpatterns.org/ont/sentilo.owl#> "+
    " PREFIX earmark: <http://www.essepuntato.it/2008/12/earmark#> "+
    " PREFIX semiotics: <http://ontologydesignpatterns.org/cp/owl/semiotics.owl#> "+
    " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    " PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
    " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    " PREFIX pos: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> "+
    " PREFIX schemaorg: <http://schema.org/> "+
    " PREFIX skos: <http://www.w3.org/2008/05/skos#> "+
    " PREFIX fred: <http://www.ontologydesignpatterns.org/ont/fred/domain.owl#> "+
    " SELECT "+
    " ?event ?eventype ?event_quality ?event_truth_value ?event_mod ?eventlinking"+
    " ?holder ?holdertype ?holder_quality ?holderlinking"+
    " ?mtopic ?mtopic_quality ?mtopic_truth_value ?mtopic_mod ?mtopiclinking ?eventmtopictype ?mtopicsit ?nomeventmtopictype ?entitymtopictype"+
    //" ?subtopic ?subtopic1 ?subtopictype ?subtopic1type ?subtopic_quality ?subtopic1_quality ?s_associated ?s1_associated ?subtopiclinking ?subtopic1linking"+
    " ?subtopic ?infratopic ?nanotopic ?subtopictype ?infratopictype ?nanotopictype ?subtopic_quality ?infratopic_quality ?nanotopic_quality ?m_associated ?s_associated ?s1_associated ?subtopiclinking ?infratopiclinking ?nanotopiclinking ?subtopic_mod ?infratopic_mod ?nanotopic_mod ?subtopic_truth_value ?infratopic_truth_value ?nanotopic_truth_value "+
    "  WHERE { "+

    " {?event a ?eventype . ?eventype rdfs:subClassOf+ dul:Event}  "+
        " OPTIONAL {?eventype owl:equivalentClass ?eventequi} "+
        " OPTIONAL {?eventype rdfs:subClassOf+ ?eventsuper} "+
        " OPTIONAL {?nominalevent owl:sameAs ?n . ?n a schemaorg:Event} "+
        " OPTIONAL {?event owl:sameAs ?eventlinking} "+
        " OPTIONAL {?event dul:hasQuality ?event_quality} "+
        " OPTIONAL {?event boxing:hasTruthValue ?event_truth_value} "+
        " OPTIONAL {?event boxing:hasModality ?event_mod} "+
        " OPTIONAL {{{?event vnrole:Experiencer ?holder} "+
    " UNION {?event vnrole:Actor ?holder} "+
    " UNION {?event vnrole:Actor1 ?holder} "+
    " UNION {?event vnrole:Actor2 ?holder} "+
    " UNION {?event vnrole:Agent ?holder} "+
    " UNION {?event fred:inOpinionOf ?holder} "+
    " UNION {?event boxer:agent ?holder}} "+
    " UNION  "+
    " {{?event vnrole:Theme ?holder}  "+
    " UNION {?event boxer:theme ?holder} "+
    " UNION {?event boxer:Theme1 ?holder} "+
    " UNION {?event boxer:Theme2 ?holder} "+
    " FILTER NOT EXISTS { "+
    " {?event vnrole:Experiencer ?holder1} "+
    " UNION {?event vnrole:Actor ?holder1} "+
    " UNION {?event vnrole:Actor1 ?holder1} "+
    " UNION {?event vnrole:Actor2 ?holder1} "+
    " UNION {?event vnrole:Agent ?holder1} "+
    " UNION {?event boxer:agent ?holder1}}} "+
    " OPTIONAL {?holder dul:associatedWith*/owl:sameAs ?holderlinking} "+
    " OPTIONAL {{?holder a ?holdertype } UNION {?holder a ?holdertype1 . ?holdertype1 rdfs:subClassOf+ ?holdertype}} "+
    " OPTIONAL {?holder dul:hasQuality ?holder_quality} "+
        " } "+
        " OPTIONAL {{?event vnrole:Topic ?mtopic}  "+
    " UNION {?event boxing:declaration ?mtopic} "+
    " UNION {?event vnrole:Beneficiary ?mtopic} "+
    " UNION {?event vnrole:Patient ?mtopic} "+
    " UNION {?event vnrole:Patient1 ?mtopic} "+
    " UNION {?event vnrole:Patient2 ?mtopic} "+
    " UNION {?event boxer:patient ?mtopic} "+
    " UNION "+
        " {{{?event vnrole:Theme ?mtopic}  "+
        " UNION {?event boxer:theme ?mtopic}} "+
    " UNION {?event boxer:Theme1 ?mtopic} "+
    " UNION {?event boxer:Theme2 ?mtopic} "+
    " {{?event vnrole:Experiencer ?holder} "+
    " UNION {?event vnrole:Actor ?holder} "+
    " UNION {?event fred:inOpinionOf ?holder} "+
    " UNION {?event vnrole:Actor1 ?holder} "+
    " UNION {?event vnrole:Actor2 ?holder} "+
    " UNION {?event vnrole:Agent ?holder} "+
    " UNION {?event boxer:agent ?holder}}} "+
    " {{{?mtopic a ?eventmtopictype . ?eventmtopictype rdfs:subClassOf+ dul:Event OPTIONAL {?eventmtopictype owl:equivalentClass ?eventmtopicequi} "+
    " OPTIONAL {?eventmtopictype rdfs:subClassOf+ ?eventmtopicsuper}} "+
    " UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation)} "+
    " UNION {?mtopic a ?nomeventmtopictype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event OPTIONAL {?nomeventmtopictype owl:equivalentClass ?nomeventmtopicequi} "+
    " OPTIONAL {?nomeventmtopictype rdfs:subClassOf+ ?nomeventmtopicsuper}} "+
    " UNION {{{?mtopic dul:associatedWith*/owl:sameAs*/rdf:type ?entitymtopictype} UNION {?mtopic boxer:possibleType ?possibletype FILTER NOT EXISTS {?mtopic owl:sameAs*/rdf:type ?entitymtopictype}} UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic owl:sameAs*/rdf:type ?entitymtopictype}}} "+
    " OPTIONAL {?entitymtopictype owl:equivalentClass ?entitymtopicequi} OPTIONAL {?entitymtopictype rdfs:subClassOf+ ?entitymtopicsuper} "+
    " FILTER NOT EXISTS {?otherevent ?otherole ?mtopic . ?otherevent a ?othereventype .  "+
        " ?othereventype rdfs:subClassOf+ dul:Event FILTER (?otherevent != ?event)}  "+
        " FILTER NOT EXISTS {?otherevent ?otherinvolves ?mtopic . ?otherevent a boxing:Situation FILTER (?otherevent != ?event)}} "+
    " } FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?nominalmtopic owl:sameAs ?n . ?n a schemaorg:Event} "+
    " OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} "+
    " OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} "+
    " OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} "+
    " OPTIONAL {?mtopic dul:associatedWith*/owl:sameAs ?mtopiclinking} "+
    " OPTIONAL {?mtopic ?p ?subtopic  "+
    " FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} "+
    " FILTER(isIRI(?subtopic)) "+
        " FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:boxerpos && ?p != pos:pennpos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
        " OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
        " OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    " OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    " OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    " OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
    " OPTIONAL {?subtopic dul:associatedWith*/owl:sameAs ?subtopiclinking} "+
    " OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
        " OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:boxerpos && ?q != pos:pennpos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
        " OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
        " OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    " OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    " OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    " OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
    " OPTIONAL {?infratopic dul:associatedWith*/owl:sameAs ?infratopiclinking} "+
    " OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    " OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:boxerpos && ?r != pos:pennpos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)    "+
        " OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse} "+
    " OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
        " OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    " OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
        " OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    " OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
    " OPTIONAL {?nanotopic dul:associatedWith*/owl:sameAs ?nanotopiclinking} "+
    " } "+
    " } "+
    " } "+
    " } "+
    " }  } }} ";

    
    public static String SELECT_QUERY = "PREFIX vnrole: <http://www.ontologydesignpatterns.org/ont/vn/abox/role/> "+
    	    " PREFIX boxer: <http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#> "+
    	    " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#> "+
    	    " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> "+
    	    " PREFIX : <http://www.ontologydesignpatterns.org/ont/sentilo.owl#> "+
    	    " PREFIX earmark: <http://www.essepuntato.it/2008/12/earmark#> "+
    	    " PREFIX semiotics: <http://ontologydesignpatterns.org/cp/owl/semiotics.owl#> "+
    	    " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    	    " PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
    	    " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    	    " PREFIX pos: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> "+
    	    " PREFIX schemaorg: <http://schema.org/> "+
    	    " PREFIX skos: <http://www.w3.org/2008/05/skos#> "+
    	    " PREFIX fred: <http://www.ontologydesignpatterns.org/ont/fred/domain.owl#> "+
    	    " SELECT DISTINCT "+
    	    " ?event ?eventype ?event_quality ?event_truth_value ?event_mod ?eventlinking"+
    	    " ?holder ?holdertype ?holder_quality ?holderlinking"+
    	    " ?mtopic ?mtopic_quality ?mtopic_truth_value ?mtopic_mod ?mtopiclinking ?eventmtopictype ?mtopicsit ?nomeventmtopictype ?entitymtopictype"+
    	    //" ?subtopic ?subtopic1 ?subtopictype ?subtopic1type ?subtopic_quality ?subtopic1_quality ?s_associated ?s1_associated ?subtopiclinking ?subtopic1linking"+
    	    " ?subtopic ?infratopic ?nanotopic ?subtopictype ?infratopictype ?nanotopictype ?subtopic_quality ?infratopic_quality ?nanotopic_quality ?m_associated ?s_associated ?s1_associated ?subtopiclinking ?infratopiclinking ?nanotopiclinking ?subtopic_mod ?infratopic_mod ?nanotopic_mod ?subtopic_truth_value ?infratopic_truth_value ?nanotopic_truth_value "+
    	    "  WHERE { "+

    	    " {?event a ?eventype . ?eventype rdfs:subClassOf+ dul:Event}  "+
    	        " OPTIONAL {?eventype owl:equivalentClass ?eventequi} "+
    	        " OPTIONAL {?eventype rdfs:subClassOf+ ?eventsuper} "+
    	        " OPTIONAL {?nominalevent owl:sameAs ?n . ?n a schemaorg:Event} "+
    	        " OPTIONAL {?event owl:sameAs ?eventlinking} "+
    	        " OPTIONAL {?event dul:hasQuality ?event_quality} "+
    	        " OPTIONAL {?event boxing:hasTruthValue ?event_truth_value} "+
    	        " OPTIONAL {?event boxing:hasModality ?event_mod} "+
    	        " OPTIONAL {{{?event vnrole:Experiencer ?holder} "+
    	    " UNION {?event vnrole:Actor ?holder} "+
    	    " UNION {?event vnrole:Actor1 ?holder} "+
    	    " UNION {?event vnrole:Actor2 ?holder} "+
    	    " UNION {?event vnrole:Agent ?holder} "+
    	    " UNION {?event fred:inOpinionOf ?holder} "+
    	    " UNION {?event boxer:agent ?holder}} "+
    	    " UNION  "+
    	    " {{?event vnrole:Theme ?holder}  "+
    	    " UNION {?event boxer:theme ?holder} "+
    	    " UNION {?event boxer:Theme1 ?holder} "+
    	    " UNION {?event boxer:Theme2 ?holder} "+
    	    " FILTER NOT EXISTS { "+
    	    " {?event vnrole:Experiencer ?holder1} "+
    	    " UNION {?event vnrole:Actor ?holder1} "+
    	    " UNION {?event vnrole:Actor1 ?holder1} "+
    	    " UNION {?event vnrole:Actor2 ?holder1} "+
    	    " UNION {?event vnrole:Agent ?holder1} "+
    	    " UNION {?event boxer:agent ?holder1}}} "+
    	    " OPTIONAL {?holder dul:associatedWith*/owl:sameAs ?holderlinking} "+
    	    " OPTIONAL {{?holder a ?holdertype } UNION {?holder a ?holdertype1 . ?holdertype1 rdfs:subClassOf+ ?holdertype}} "+
    	    " OPTIONAL {?holder dul:hasQuality ?holder_quality} "+
    	        " } "+
    	        " OPTIONAL {{?event vnrole:Topic ?mtopic}  "+
    	    " UNION {?event boxing:declaration ?mtopic} "+
    	    " UNION {?event vnrole:Beneficiary ?mtopic} "+
    	    " UNION {?event vnrole:Patient ?mtopic} "+
    	    " UNION {?event vnrole:Patient1 ?mtopic} "+
    	    " UNION {?event vnrole:Patient2 ?mtopic} "+
    	    " UNION {?event boxer:patient ?mtopic} "+
    	    " UNION "+
    	        " {{{?event vnrole:Theme ?mtopic}  "+
    	        " UNION {?event boxer:theme ?mtopic}} "+
    	    " UNION {?event boxer:Theme1 ?mtopic} "+
    	    " UNION {?event boxer:Theme2 ?mtopic} "+
    	    " {{?event vnrole:Experiencer ?holder} "+
    	    " UNION {?event vnrole:Actor ?holder} "+
    	    " UNION {?event fred:inOpinionOf ?holder} "+
    	    " UNION {?event vnrole:Actor1 ?holder} "+
    	    " UNION {?event vnrole:Actor2 ?holder} "+
    	    " UNION {?event vnrole:Agent ?holder} "+
    	    " UNION {?event boxer:agent ?holder}}} "+
    	    " {{{?mtopic a ?eventmtopictype . ?eventmtopictype rdfs:subClassOf+ dul:Event OPTIONAL {?eventmtopictype owl:equivalentClass ?eventmtopicequi} "+
    	    " OPTIONAL {?eventmtopictype rdfs:subClassOf+ ?eventmtopicsuper}} "+
    	    " UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation)} "+
    	    " UNION {?mtopic a ?nomeventmtopictype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event OPTIONAL {?nomeventmtopictype owl:equivalentClass ?nomeventmtopicequi} "+
    	    " OPTIONAL {?nomeventmtopictype rdfs:subClassOf+ ?nomeventmtopicsuper}} "+
    	    " UNION {{{?mtopic dul:associatedWith*/owl:sameAs*/rdf:type ?entitymtopictype} UNION {?mtopic boxer:possibleType ?possibletype FILTER NOT EXISTS {?mtopic owl:sameAs*/rdf:type ?entitymtopictype}} UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic owl:sameAs*/rdf:type ?entitymtopictype}}} "+
    	    " OPTIONAL {?entitymtopictype owl:equivalentClass ?entitymtopicequi} OPTIONAL {?entitymtopictype rdfs:subClassOf+ ?entitymtopicsuper} "+
    	    " FILTER NOT EXISTS {?otherevent ?otherole ?mtopic . ?otherevent a ?othereventype .  "+
    	        " ?othereventype rdfs:subClassOf+ dul:Event FILTER (?otherevent != ?event)}  "+
    	        " FILTER NOT EXISTS {?otherevent ?otherinvolves ?mtopic . ?otherevent a boxing:Situation FILTER (?otherevent != ?event)}} "+
    	    " } FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    	    " OPTIONAL {?nominalmtopic owl:sameAs ?n . ?n a schemaorg:Event} "+
    	    " OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} "+
    	    " OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} "+
    	    " OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} "+
    	    " OPTIONAL {?mtopic dul:associatedWith*/owl:sameAs ?mtopiclinking} "+
    	    " OPTIONAL {?mtopic ?p ?subtopic  "+
    	    " FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} "+
    	    " FILTER(isIRI(?subtopic)) "+
    	        " FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:boxerpos && ?p != pos:pennpos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
    	        " OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
    	        " OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    	    " OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    	    " OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    	    " OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
    	    " OPTIONAL {?subtopic dul:associatedWith*/owl:sameAs ?subtopiclinking} "+
    	    " OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
    	        " OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:boxerpos && ?q != pos:pennpos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
    	        " OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
    	        " OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    	    " OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    	    " OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    	    " OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
    	    " OPTIONAL {?infratopic dul:associatedWith*/owl:sameAs ?infratopiclinking} "+
    	    " OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    	    " OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:boxerpos && ?r != pos:pennpos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)    "+
    	        " OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse} "+
    	    " OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
    	        " OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    	    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    	    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    	    " OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
    	        " OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    	    " OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
    	    " OPTIONAL {?nanotopic dul:associatedWith*/owl:sameAs ?nanotopiclinking} "+
    	    " } "+
    	    " } "+
    	    " } "+
    	    " } "+
    	    " }  } }}} ";




/*
    "  {?event a ?eventype . ?eventype rdfs:subClassOf+ dul:Event} "+
    "  OPTIONAL {?eventype owl:equivalentClass ?eventequi} "+
    "  OPTIONAL {?eventype rdfs:subClassOf+ ?eventsuper} "+
    "  OPTIONAL {?nominalevent owl:sameAs ?n . ?n a schemaorg:Event} "+
//  "  OPTIONAL {?event owl:sameAs ?eventlinking} "+ //ORIGINAL
    "  OPTIONAL {?holder dul:associatedWith* /owl:sameAs ?holderlinking} "+
    "  OPTIONAL {?event dul:associatedWith* /owl:sameAs ?eventlinking} "+
    "  OPTIONAL {?event dul:hasQuality ?event_quality} "+
    "  OPTIONAL {?event boxing:hasTruthValue ?event_truth_value} "+
    "  OPTIONAL {?event boxing:hasModality ?event_mod} "+
    "  OPTIONAL {{{?event vnrole:Experiencer ?holder} "+
    "          UNION {?event vnrole:Actor ?holder} "+
    "          UNION {?event vnrole:Actor1 ?holder} "+
    "          UNION {?event vnrole:Actor2 ?holder} "+
    "          UNION {?event vnrole:Agent ?holder} "+
    "          UNION {?event boxer:agent ?holder}} "+
    "   UNION  "+
    "         {{?event vnrole:Theme ?holder}  "+
    "          UNION {?event boxer:theme ?holder} "+
    "          UNION {?event boxer:Theme1 ?holder} "+
    "          UNION {?event boxer:Theme2 ?holder} "+
    "          FILTER NOT EXISTS { "+
    "          {?event vnrole:Experiencer ?holder1} "+
    "          UNION {?event vnrole:Actor ?holder1} "+
    "          UNION {?event vnrole:Actor1 ?holder1} "+
    "          UNION {?event vnrole:Actor2 ?holder1} "+
    "          UNION {?event vnrole:Agent ?holder1} "+
    "          UNION {?event boxer:agent ?holder1}}} "+
    "         OPTIONAL {{?holder a ?holdertype } UNION {?holder a ?holdertype1 . ?holdertype1 rdfs:subClassOf+ ?holdertype}} "+
    "         OPTIONAL {?holder dul:hasQuality ?holder_quality} "+
    "   } "+
    "  OPTIONAL {{?event vnrole:Topic ?mtopic}  "+
    "             UNION {?event vnrole:Beneficiary ?mtopic} "+
    "             UNION {?event vnrole:Patient ?mtopic} "+
    "             UNION {?event vnrole:Patient1 ?mtopic} "+
    "             UNION {?event vnrole:Patient2 ?mtopic} "+
    "             UNION {?event boxer:patient ?mtopic} "+
    " UNION "+
    "             {{{?event vnrole:Theme ?mtopic}  "+
    "           UNION {?event boxer:theme ?mtopic}} "+
    "              UNION {?event boxer:Theme1 ?mtopic} "+
    "              UNION {?event boxer:Theme2 ?mtopic} "+
    "              {{?event vnrole:Experiencer ?holder} "+
    "           UNION {?event vnrole:Actor ?holder} "+
    "           UNION {?event vnrole:Actor1 ?holder} "+
    "           UNION {?event vnrole:Actor2 ?holder} "+
    "           UNION {?event vnrole:Agent ?holder} "+
    "           UNION {?event boxer:agent ?holder}}} "+
    "             {{{?mtopic a ?eventmtopictype . ?eventmtopictype rdfs:subClassOf+ dul:Event}  "+
    "           UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation)}  "+
    "           UNION {?mtopic a ?nomeventmtopictype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event} "+
    "           UNION {{{?mtopic a ?entitymtopictype} UNION {?mtopic boxer:possibleType ?possibletype FILTER NOT EXISTS {?mtopic a ?entitymtopictype}} UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic a ?entitymtopictype}}} "+
    "                  FILTER NOT EXISTS {?otherevent ?otherole ?mtopic . ?otherevent a ?othereventype .  "+
    "                         ?othereventype rdfs:subClassOf+ dul:Event FILTER (?otherevent != ?event)}  "+
    "                  FILTER NOT EXISTS {?otherevent ?otherinvolves ?mtopic . ?otherevent a boxing:Situation FILTER (?otherevent != ?event)}} "+
    "             } "+
//  "           OPTIONAL {?eventmtopictype owl:equivalentClass ?eventmtopicequi} "+
//      "           OPTIONAL {?eventmtopictype rdfs:subClassOf+ ?eventmtopicsuper} "+
 //     "           OPTIONAL {?nomeventmtopictype owl:equivalentClass ?nomeventmtopicequi} "+
 //     "           OPTIONAL {?nomeventmtopictype rdfs:subClassOf+ ?nomeventmtopicsuper} "+
        "           OPTIONAL {?entitymtopictype owl:equivalentClass ?entitymtopicequi} "+
        "           OPTIONAL {?entitymtopictype rdfs:subClassOf+ ?entitymtopicsuper} "+
    "           OPTIONAL {?nominalmtopic owl:sameAs ?n . ?n a schemaorg:Event} "+
    "              OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} "+ 
    "              OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} "+
    "              OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} "+
    "              OPTIONAL {?mtopic dul:associatedWith* /owl:sameAs ?mtopiclinking} "+
    //"            OPTIONAL {?mtopic owl:sameAs ?mtopiclinking} "+  //ORIGINAL

    "              OPTIONAL {?mtopic ?p ?subtopic  "+
    "                FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} "+
    "                FILTER(isIRI(?subtopic)) "+
    "                FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:pos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
    "                OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
    "                OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    "                OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    "                OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    "                OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
    "                OPTIONAL {?subtopic dul:associatedWith* /owl:sameAs ?subtopiclinking} "+
//  "                OPTIONAL {?subtopic owl:sameAs ?subtopiclinking} "+ //ORIGINAL
    "                OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
    "                      OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:pos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
    "                OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
    "                OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    "                OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    "                OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    "                OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
    "                OPTIONAL {?infratopic dul:associatedWith* /owl:sameAs ?infratopiclinking} "+
//  "                OPTIONAL {?infratopic owl:sameAs ?infratopiclinking} "+ //ORIGINAL
    "                OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    "              OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:pos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)  "+
    "           OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse}"+
    " OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
    "       OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    "       OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    "            OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    "            OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
    "            OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    "       OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
    "   OPTIONAL {?nanotopic dul:associatedWith* /owl:sameAs ?nanotopiclinking} "+
//  "   OPTIONAL {?nanotopic owl:sameAs ?nanotopiclinking} "+ //ORIGINAL
    "                       } "+
    "           } "+
    "           } "+
    "       } "+
    "   }}}} ";
    */
    
    public static String simple_text_query = "SELECT ?mtopic ?mtopictype ?mtopic_quality ?event_mod ?event_truth_value WHERE {{?verb_is  <http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#agent> ?mtopic} UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/vn/abox/role/Agent> ?mtopic} UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#patient> ?mtopic} UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/vn/abox/role/Theme> ?mtopic } UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/vn/abox/role/Destination> ?mtopic} UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/vn/abox/role/Topic> ?mtopic} UNION {?verb_is <http://www.ontologydesignpatterns.org/ont/fred/domain.owl#that> ?mtopic} OPTIONAL {?mtopic a ?mtopictype} OPTIONAL {?mtopic <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasQuality> ?mtopic_quality} OPTIONAL {?verb_is <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasModality> ?event_mod} OPTIONAL {?verb_is <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasTruthValue> ?event_truth_value} }";
    
    public static String simple_text_query2 = "SELECT ?mtopic ?mtopic_quality ?mtopictype WHERE { ?mtopic <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasQuality> ?mtopic_quality OPTIONAL { {{?mtopic a ?mtopictype } UNION {?mtopic a ?mtopictype1 . ?mtopictype1 <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?mtopictype}} } OPTIONAL {?event <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#involves> ?mtopic . ?event <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasTruthValue> ?event_truth_value} OPTIONAL {?event <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#involves> ?mtopic . ?event <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#hasModality> ?event_mod} OPTIONAL {?event <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#involves> ?mtopic} }";
    
    public static String query_general = "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
    " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    " PREFIX earmark: <http://www.essepuntato.it/2008/12/earmark#> "+
    " PREFIX pos: <http://www.ontologydesignpatterns.org/ont/fred/pos.owl#> "+
    " PREFIX semiotics: <http://ontologydesignpatterns.org/cp/owl/semiotics.owl#> "+
    " PREFIX vnrole: <http://www.ontologydesignpatterns.org/ont/vn/abox/role/>"+
    " PREFIX boxer: <http://www.ontologydesignpatterns.org/ont/boxer/boxer.owl#>"+
    " PREFIX boxing: <http://www.ontologydesignpatterns.org/ont/boxer/boxing.owl#>"+
    " PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#>"+
    " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    " PREFIX schemaorg: <http://schema.org/> "+
    " PREFIX fred: <http://www.ontologydesignpatterns.org/ont/fred/domain.owl#> "+
    " PREFIX skos: <http://www.w3.org/2008/05/skos#> "+
    " SELECT "+
    " ?event ?eventype ?event_quality ?event_truth_value ?event_mod ?eventlinking"+
    " ?mtopic ?mtopictype ?mtopic_quality ?mtopic_truth_value ?mtopic_mod ?mtopiclinking"+
    " ?subtopic ?infratopic ?nanotopic ?subtopictype ?infratopictype ?nanotopictype ?subtopic_quality ?infratopic_quality ?nanotopic_quality ?m_associated ?s_associated ?s1_associated ?subtopiclinking ?infratopiclinking ?nanotopiclinking ?subtopic_mod ?infratopic_mod ?nanotopic_mod ?subtopic_truth_value ?infratopic_truth_value ?nanotopic_truth_value "+
    " WHERE "+
    " {{?mtopic a ?eventmtopictype . ?eventmtopictype rdfs:subClassOf+ dul:Event FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic }  "+
    " OPTIONAL {?eventmtopictype owl:equivalentClass ?eventmtopicequi} "+
    " OPTIONAL {?eventmtopictype rdfs:subClassOf+ ?eventmtopicsuper}} "+
    " UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation) FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } } "+
    " UNION {?mtopic a ?nomeventmtopictype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
        " OPTIONAL {?nomeventmtopictype owl:equivalentClass ?nomeventmtopicequi} "+
    " OPTIONAL {?nomeventmtopictype rdfs:subClassOf+ ?nomeventmtopicsuper} FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } } "+
    " UNION {{{?mtopic owl:sameAs*/rdf:type ?entitymtopictype FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } FILTER NOT EXISTS { ?xx owl:sameAs ?mtopic } } UNION {?mtopic boxer:possibleType ?possibletype FILTER NOT EXISTS {?mtopic owl:sameAs*/rdf:type ?entitymtopictype} FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } } UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic owl:sameAs*/rdf:type ?entitymtopictype FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } }}} "+
    " OPTIONAL {?entitymtopictype owl:equivalentClass ?entitymtopicequi}  "+
        " OPTIONAL {?entitymtopictype rdfs:subClassOf+ ?entitymtopicsuper} "+
    " FILTER ((?entitymtopictype != fred:Docuverse) && (?entitymtopictype != rdf:Property) && (?entitymtopictype != owl:ObjectProperty) && (?entitymtopictype != owl:DatatypeProperty) && (?entitymtopictype != earmark:PointerRange) && (?entitymtopictype != earmark:StringDocuverse)) "+
        " FILTER NOT EXISTS {?otherevent ?otherole ?mtopic . ?otherevent a ?othereventype .  "+
        " ?othereventype rdfs:subClassOf+ dul:Event FILTER (?otherevent != ?event)}  "+
        " FILTER NOT EXISTS {?otherevent ?otherinvolves ?mtopic . ?otherevent a boxing:Situation FILTER (?otherevent != ?event)}} "+
        " FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } "+
    " FILTER NOT EXISTS { "+
        " ?primarymtopic ?relation ?mtopic "+
        " {{?primarymtopic a ?primarymtopictype . ?primarymtopictype rdfs:subClassOf+ dul:Event}  "+
        " UNION {?primarymtopic a boxing:Situation} "+
    " UNION {?primarymtopic a ?primarymtopictype . ?primarymtopic owl:sameAs ?linkedprimary . ?linkedprimary a schemaorg:Event} "+
    " } "+
    " } "+
    " OPTIONAL {?nominalmtopic owl:sameAs ?n . ?n a schemaorg:Event} FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?mtopic dul:associatedWith*/owl:sameAs ?mtopiclinking} FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " OPTIONAL {?mtopic ?p ?subtopic  "+
        " FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} FILTER NOT EXISTS { ?xx skos:relatedMatch ?mtopic } "+
    " FILTER(isIRI(?subtopic)) "+
        " FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:boxerpos && ?p != pos:pennpos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
        " OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
        " OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    " OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    " OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    " OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
    " OPTIONAL {?subtopic dul:associatedWith*/owl:sameAs ?subtopiclinking} "+
    " OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
        " OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:boxerpos && ?q != pos:pennpos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
        " OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
        " OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    " OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    " OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    " OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
    " OPTIONAL {?infratopic dul:associatedWith*/owl:sameAs ?infratopiclinking} "+
    " OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    " OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:boxerpos && ?r != pos:pennpos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)    "+
        " OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse} "+
    " OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
        " OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    " OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    " OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
        " OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    " OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
    " OPTIONAL {?nanotopic dul:associatedWith*/owl:sameAs ?nanotopiclinking} "+
    " } " +
    " } "+
    " } "+
    " } "+
    " }} FILTER NOT EXISTS { ?x skos:relatedMatch ?subtopic } FILTER NOT EXISTS { ?x skos:relatedMatch ?infratopic } FILTER NOT EXISTS { ?x skos:relatedMatch ?nanotopic } FILTER NOT EXISTS { ?x skos:relatedMatch ?mtopic } "+
    " } ";






/*  " {{?mtopic a ?eventmtopictype . ?eventmtopictype rdfs:subClassOf+ dul:Event "+
    "       OPTIONAL {?eventmtopictype owl:equivalentClass ?eventmtopicequi} "+
        "       OPTIONAL {?eventmtopictype rdfs:subClassOf+ ?eventmtopicsuper}} "+
    "   UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation)} "+
    "   UNION {?mtopic a ?nomeventmtopictype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event  "+
    "       OPTIONAL {?nomeventmtopictype owl:equivalentClass ?nomeventmtopicequi} "+
    "       OPTIONAL {?nomeventmtopictype rdfs:subClassOf+ ?nomeventmtopicsuper}} "+
    "   UNION {{{?mtopic a ?entitymtopictype} UNION {?mtopic boxer:possibleType ?possibletype FILTER NOT EXISTS {?mtopic a ?entitymtopictype}} UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic a ?entitymtopictype}}} "+
    "       OPTIONAL {?entitymtopictype owl:equivalentClass ?entitymtopicequi}  "+
    "       OPTIONAL {?entitymtopictype rdfs:subClassOf+ ?entitymtopicsuper} "+
    "       FILTER ((?entitymtopictype != fred:Docuverse) && (?entitymtopictype != rdf:Property) && (?entitymtopictype != owl:ObjectProperty) && (?entitymtopictype != owl:DatatypeProperty) && (?entitymtopictype != earmark:PointerRange) && (?entitymtopictype != earmark:StringDocuverse)) "+
    "       FILTER NOT EXISTS {?otherevent ?otherole ?mtopic . ?otherevent a ?othereventype .  "+
    "       ?othereventype rdfs:subClassOf+ dul:Event FILTER (?otherevent != ?event)}  "+
    "       FILTER NOT EXISTS {?otherevent ?otherinvolves ?mtopic . ?otherevent a boxing:Situation FILTER (?otherevent != ?event)}} "+
    "  FILTER NOT EXISTS { "+
    "   ?primarymtopic ?relation ?mtopic "+
    "   {{?primarymtopic a ?primarymtopictype . ?primarymtopictype rdfs:subClassOf+ dul:Event}  "+
    "       UNION {?primarymtopic a boxing:Situation} "+
    "       UNION {?primarymtopic a ?primarymtopictype . ?primarymtopic owl:sameAs ?linkedprimary . ?linkedprimary a schemaorg:Event} "+
    "       } "+
    "   } "+
    "OPTIONAL {?nominalmtopic owl:sameAs ?n . ?n a schemaorg:Event} "+
    "OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} "+
    "OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} "+
    "OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} "+
    "OPTIONAL {?mtopic dul:associatedWith* /owl:sameAs ?mtopiclinking} "+
    "OPTIONAL {?mtopic ?p ?subtopic  "+
    "           FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} "+
    "           FILTER(isIRI(?subtopic)) "+
    "           FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:pos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
    "           OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
    "           OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    "           OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    "           OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    "           OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
    "           OPTIONAL {?subtopic dul:associatedWith* /owl:sameAs ?subtopiclinking} "+
    "           OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
    "               OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:pos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
    "                   OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
    "                   OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    "                   OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    "                   OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    "                   OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
    "                   OPTIONAL {?infratopic dul:associatedWith* /owl:sameAs ?infratopiclinking} "+
    "                   OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    "                       OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:pos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)    "+
    "                           OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse} "+
    "                               OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
    "                               OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    "                               OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    "                               OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    "                               OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
    "                               OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    "                               OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
    "                               OPTIONAL {?nanotopic dul:associatedWith* /owl:sameAs ?nanotopiclinking} "+
    "                           } "+
    "                       } "+
    "                   } "+
    "               } "+
    "           }} "+
    "       } "; */

    
    /*" {{?mtopic a ?eventmtopicype . ?eventmtopicype rdfs:subClassOf+ dul:Event}  "+
    " UNION {?mtopic a ?mtopicsit FILTER (?mtopicsit = boxing:Situation)} "+
    " UNION {?mtopic a ?nomeventmtopicype . ?mtopic owl:sameAs ?linked . ?linked a schemaorg:Event} "+
    " UNION {?mtopic dul:hasQuality ?mtopic_quality OPTIONAL {?mtopic a ?entitymtopictype}} "+
    " FILTER NOT EXISTS { "+
    "   {{?primarymtopic a ?primarymtopicype . ?primarymtopicype rdfs:subClassOf+ dul:Event}  "+
    "   UNION {?primarymtopic a boxing:Situation} "+
    "   UNION {?primarymtopic a ?primarymtopicype . ?primarymtopic owl:sameAs ?linkedprimary . ?linkedprimary a schemaorg:Event}} "+
    "   ?primarymtopic ?relation ?mtopic} "+
    "  } "+
    " OPTIONAL {?mtopicype owl:equivalentClass ?mtopicequi} "+
    " OPTIONAL {?mtopicype rdfs:subClassOf+ ?mtopicsuper} "+
    " OPTIONAL {?mtopic dul:hasQuality ?mtopic_quality} "+
    " OPTIONAL {?mtopic boxing:hasModality ?mtopic_mod} "+
    " OPTIONAL {?mtopic boxing:hasTruthValue ?mtopic_truth_value} "+
//  " OPTIONAL {?mtopic owl:sameAs ?mtopiclinking} "+ //ORIGINAL
    " OPTIONAL {?mtopic dul:associatedWith* /owl:sameAs ?mtopiclinking} "+
    " OPTIONAL {?mtopic ?p ?subtopic "+
    "   FILTER NOT EXISTS {?sometopic dul:hasQuality ?subtopic} "+
    "   FILTER(isIRI(?subtopic)) "+
    "       FILTER (?p != rdf:type && ?p != boxing:hasModality && ?p != rdfs:subClassOf && ?p != boxing:hasTruthValue && ?p != dul:hasQuality && ?p != pos:pos && ?p != boxer:possibleType && ?p != semiotics:hasInterpretant && ?p != dul:associatedWith && ?p != owl:sameAs) "+
    "                   OPTIONAL {?subtopic dul:associatedWith ?m_associated . ?m_associated a ?mass_type}  "+
    "               OPTIONAL {?m_associated dul:associatedWith ?subtopic . ?m_associated a ?mass_type} "+
    "               OPTIONAL {?subtopic dul:hasQuality ?subtopic_quality} "+
    "               OPTIONAL {?subtopic boxing:hasModality ?subtopic_mod} "+
    "               OPTIONAL {?subtopic boxing:hasTruthValue ?subtopic_truth_value} "+
//  "               OPTIONAL {?subtopic owl:sameAs ?subtopiclinking} "+ //ORIGINAL
    "               OPTIONAL {?subtopic dul:associatedWith* /owl:sameAs ?subtopiclinking} "+
    "               OPTIONAL {?subtopic a ?subtopictype OPTIONAL {?subtopictype rdfs:subClassOf+ ?subtopicsuper} OPTIONAL {?subtopictype owl:equivalentClass ?subtopicequi} FILTER NOT EXISTS {?subtopictype a earmark:PointerRange} FILTER NOT EXISTS {?subtopictype a earmark:StringDocuverse} FILTER NOT EXISTS {?something dul:hasQuality ?subtopic}  "+
    "               OPTIONAL {?subtopic ?q ?infratopic FILTER(isIRI(?infratopic)) FILTER NOT EXISTS {?something dul:hasQuality ?infratopic} FILTER (?q != rdf:type && ?q != boxing:hasModality && ?q != rdfs:subClassOf && ?q != boxing:hasTruthValue && ?q != dul:hasQuality && ?q != pos:pos && ?q != boxer:possibleType && ?q != semiotics:hasInterpretant && ?q != dul:associatedWith && ?q != owl:sameAs)  "+
    "               OPTIONAL {?infratopic dul:associatedWith ?s_associated . ?s_associated a ?sass_type}  "+
    "               OPTIONAL {?s_associated dul:associatedWith ?infratopic . ?s_associated a ?sass_type} "+
    "               OPTIONAL {?infratopic dul:hasQuality ?infratopic_quality} "+
    "               OPTIONAL {?infratopic boxing:hasModality ?infratopic_mod} "+
    "               OPTIONAL {?infratopic boxing:hasTruthValue ?infratopic_truth_value} "+
//  "               OPTIONAL {?infratopic owl:sameAs ?infratopiclinking} "+ // ORIGINAL
    "               OPTIONAL {?infratopic dul:associatedWith* /owl:sameAs ?infratopiclinking} "+
    "               OPTIONAL {?infratopic a ?infratopictype OPTIONAL {?infratopictype rdfs:subClassOf+ ?infratopicsuper} OPTIONAL {?infratopictype owl:equivalentClass ?infratopicequi} FILTER NOT EXISTS {?infratopictype a earmark:PointerRange} FILTER NOT EXISTS {?infratopictype a earmark:StringDocuverse} "+
    "               OPTIONAL {?infratopic ?r ?nanotopic FILTER(isIRI(?nanotopic)) FILTER NOT EXISTS {?something dul:hasQuality ?nanotopic} FILTER (?r != rdf:type && ?r != boxing:hasModality && ?r != rdfs:subClassOf && ?r != boxing:hasTruthValue && ?r != dul:hasQuality && ?r != pos:pos && ?r != boxer:possibleType && ?r != semiotics:hasInterpretant && ?r != dul:associatedWith && ?r != owl:sameAs)    "+
    "               OPTIONAL {?nanotopic a ?nanotopictype  OPTIONAL {?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} OPTIONAL {?nanotopictype owl:equivalentClass ?nanotopicequi} FILTER NOT EXISTS {?nanotopictype a earmark:PointerRange} FILTER NOT EXISTS {?nanotopictype a earmark:StringDocuverse} "+
    "               OPTIONAL {?nanotopic dul:associatedWith ?s1_associated . ?s1_associated a ?s1ass_type}  "+
    "               OPTIONAL {?s1_associated dul:associatedWith ?nanotopic . ?s1_associated a ?s1ass_type} "+
    "               OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype owl:equivalentClass ?nanotopicequi} "+
    "               OPTIONAL {?nanotopic a ?nanotopictype . ?nanotopictype rdfs:subClassOf+ ?nanotopicsuper} "+
    "               OPTIONAL {?nanotopic dul:hasQuality ?nanotopic_quality}  "+
    "               OPTIONAL {?nanotopic boxing:hasModality ?nanotopic_mod} "+
    "               OPTIONAL {?nanotopic boxing:hasTruthValue ?nanotopic_truth_value} "+
//  "               OPTIONAL {?nanotopic owl:sameAs ?nanotopiclinking} "+ //ORIGINAL
    "               OPTIONAL {?nanotopic dul:associatedWith* /owl:sameAs ?nanotopiclinking} "+
    "               } "+
    "               } "+
    "               } } } } }";*/
}
