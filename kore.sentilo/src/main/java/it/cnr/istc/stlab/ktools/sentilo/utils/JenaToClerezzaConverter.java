/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package it.cnr.istc.stlab.ktools.sentilo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.parser.JenaParserProvider;
import org.apache.clerezza.rdf.jena.serializer.JenaSerializerProvider;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This class provides static methods to convert:
 * 
 * <ul>
 * <li> a Jena Model (see {@link Model}) to a list of Clerezza triples (see {@link Triple})
 * <li> a Jena Model to a Clerezza MGraph (see {@link MGraph})
 * <li> a Clerezza MGraph a Jena Model
 * <li> a Clerezza MGraph a Jena Graph (see {@link Graph}}
 * </ul>
 * 
 * 
 * @author andrea.nuzzolese
 *
 */

public class JenaToClerezzaConverter {

	
	/**
	 * 
	 * Converts a Jena {@link Model} to an {@link ArrayList} of Clerezza triples (instances of class {@link Triple}).
	 * 
	 * @param model {@link Model}
	 * @return an {@link ArrayList} that contains the generated Clerezza triples (see {@link Triple}) 
	 */
	public static ArrayList<Triple> jenaModelToClerezzaTriples(Model model){
		
		ArrayList<Triple> clerezzaTriples = new ArrayList<Triple>();
		
		MGraph mGraph = jenaModelToClerezzaMGraph(model);
		
		Iterator<Triple> tripleIterator = mGraph.iterator();
		while(tripleIterator.hasNext()){
			Triple triple = tripleIterator.next();
			clerezzaTriples.add(triple);
		}
		
		return clerezzaTriples;
		
	}
	
	/**
	 * 
	 * Converts a Jena {@link Model} to Clerezza {@link MGraph}.
	 * 
	 * @param model {@link Model}
	 * @return the equivalent Clerezza {@link MGraph}.
	 */
	
	public static MGraph jenaModelToClerezzaMGraph(Model model){
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		model.write(out);
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		
		ParsingProvider parser = new JenaParserProvider();		
		
		MGraph mGraph = new SimpleMGraph();
		parser.parse(mGraph,in, SupportedFormat.RDF_XML, null);
		
		return mGraph;
		
	}
	
	
	/**
	 * Converts a Clerezza {@link MGraph} to a Jena {@link Model}.
	 * 
	 * @param mGraph {@link MGraph}
	 * @return the equivalent Jena {@link Model}.
	 */
	public static Model clerezzaMGraphToJenaModel(TripleCollection tripleCollection){
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		SerializingProvider serializingProvider = new JenaSerializerProvider();
		
		serializingProvider.serialize(out, tripleCollection, SupportedFormat.RDF_XML);
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		
		Model jenaModel = ModelFactory.createDefaultModel();
		
		jenaModel.read(in, null);
		
		return jenaModel;
		
	}
	
	
	/**
	 * Converts a Clerezza {@link MGraph} to a Jena {@link Graph}.
	 * 
	 * @param mGraph {@link MGraph}
	 * @return the equivalent Jena {@link Graph}.
	 */
	public static com.hp.hpl.jena.graph.Graph clerezzaMGraphToJenaGraph(TripleCollection tripleCollection){
		
		Model jenaModel = clerezzaMGraphToJenaModel(tripleCollection);
		if(jenaModel != null){
			return jenaModel.getGraph();
		}
		else{
			return null;
		}
		
	}
	
	public static Triple jenaStatementToClerezzaTriple(Statement statement){
        
	    /* 
	     * Convert the subject
	     */
	    Resource subject = statement.getSubject();
	    
	    NonLiteral tripleSubject = null;
	    if(subject.isURIResource()) tripleSubject = new UriRef(subject.getURI());
	    else tripleSubject = new BNode();
	    
	    /* 
         * Convert the predicate
         */
	    Resource predicate = statement.getPredicate();
	    
	    UriRef triplePredicate = new UriRef(predicate.getURI());
	    
	    /* 
         * Convert the object
         */
	    RDFNode object = statement.getObject();
	    
	    org.apache.clerezza.rdf.core.Resource tripleObject = null;
	    if(object.isURIResource()) tripleObject = new UriRef(((Resource)object).getURI());
	    else if(object.isAnon()) tripleObject = new BNode();
	    else {
	        Literal literal = object.asLiteral();
	        RDFDatatype datatype = literal.getDatatype();
	        String language = literal.getLanguage();
	        if(datatype != null) tripleObject = new TypedLiteralImpl(literal.getLexicalForm(), new UriRef(datatype.getURI()));
	        else if(language != null && language.trim().isEmpty()) tripleObject = new PlainLiteralImpl(literal.getLexicalForm(), new Language(language.trim()));
	        else tripleObject = new PlainLiteralImpl(literal.getLexicalForm());
	    }
	    
        return new TripleImpl(tripleSubject, triplePredicate, tripleObject);
        
    }
	
	public static String printTripleCollection(TripleCollection tripleCollection, String format){
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        SerializingProvider serializingProvider = new JenaSerializerProvider();
        
        serializingProvider.serialize(out, tripleCollection, format);
        
        return out.toString();
	}
	
}
