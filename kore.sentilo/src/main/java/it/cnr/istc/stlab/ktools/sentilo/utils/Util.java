package it.cnr.istc.stlab.ktools.sentilo.utils;

import it.cnr.istc.stlab.ktools.sentilo.senti.FrequencyBasedWSD;
import it.cnr.istc.stlab.ktools.sentilo.senti.SensitivenessObject;
import it.cnr.istc.stlab.ktools.sentilo.senti.WSDResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class Util {
    
    private static final Logger LOG = LoggerFactory.getLogger(Util.class); 
    
    /* Read the verbs contained in filename */
    public static Vector sentiRead(String filename) {
	
    	Vector ret = new Vector();
    	try {
    	    String line = "";
    	    BufferedReader br = new BufferedReader(new FileReader(filename));
    	    while((line = br.readLine()) != null) {
    		ret.add(line.toLowerCase().trim());
    	    }
    	}
    	catch(Exception e) {
    	    e.printStackTrace();
    	}
    	return ret;
    }
    
    /* Read the sentiwordnet */
    public static HashMap sentiwordnetRead(String filename) {
    	HashMap ret = new HashMap();
    	try {
    	    BufferedReader input =  new BufferedReader(new FileReader(new File(filename)));
    	    String line = null;
    	    while((line = input.readLine()) != null) {
    		if(line.startsWith("#"))
    		    continue;
    		String[] lines = line.split("\t");
    		Vector el = new Vector();
    		el.add(Double.parseDouble(lines[2].trim()));
    		el.add(Double.parseDouble(lines[3].trim()));
    		ret.put(lines[1].trim(),el);
    	    }
    	}
    	catch(Exception ex) {
    	    ex.printStackTrace();
    	}
    	return ret;
    }
    
    /* Read the sensitiveness */
    // dare in input un ulteriore file, se il verbo è contenuto nel file, quindi ha THM e AGNT allora interpretarlo come PTNT e mettere una entry in piu
    public static HashMap sensitivenessRead(String filename,String filename2) {
    	HashMap ret = new HashMap();
    	System.out.println("sensi1:"+filename);
    	System.out.println("sensi2:"+filename2);
    	Vector themesVerb = new Vector();
    	try {
//    	    BufferedReader input =  new BufferedReader(new FileReader(new File(filename2)));
    	    String line = null;
    //	    while((line = input.readLine()) != null) {
    	//        themesVerb.add(line.trim().toLowerCase());
    	  //  }
    	   // input.close();
    
    	    BufferedReader input =  new BufferedReader(new FileReader(new File(filename)));
    	    line = null;
    	    while((line = input.readLine()) != null) {
    	    	if(line.startsWith("#"))
    	    		continue;
    	    	String cleaned = line.substring(line.indexOf("(")+1,line.indexOf(")"));
    	    	String[] lines = cleaned.split(",");
    	    	String id = lines[0].trim().toLowerCase(); // the verb
    	    	boolean isThemeVerb = false;
    	    	if(themesVerb.contains(id))
    	    		isThemeVerb = true;
    	    	HashMap vet = (HashMap)ret.get(id);
    	    	if(vet == null) {
    	    		vet = new HashMap();
    	    	}
    	    	for(int i=2;i<lines.length;i++) {
    	    		String[] items = lines[i].trim().split(" ");
    	    		SensitivenessObject sens_ob = new SensitivenessObject(lines[i].trim());
    	    		String role = items[0].substring(items[0].indexOf("'")+1,items[0].length()-1).toLowerCase();
    	    		vet.put(role,sens_ob);
    	    		if(role.equals("patient") && isThemeVerb) { // can be patient only once for each verb. If it is a theme verb then consider the role theme as patient
    	    			vet.put("theme",sens_ob);
    	    		}
    	    		if(role.equals("agent") && isThemeVerb==false) { // can be agent only once for each verb. If it is not a theme verb then consider the role theme as agent
    	    			vet.put("theme",sens_ob);
    	    		}
    	    	}
    	    	ret.put(id,vet);
    	    }
    	    input.close();
    	}
    	catch(Exception ex) {
    	    ex.printStackTrace();
    	}
    	//vnpattern(take,10050000, agent:'Agent' op-sens=1 fact-sens=null, patient:'Theme' op-sens=null fact-sens=null)
    	return ret;
    }
    
    /* Read the senticnet file */
    public static Model senticnetRead(String filename) {
    	Model model = null;
    	try {
    	    model = ModelFactory.createDefaultModel();
    	    InputStream in = new FileInputStream(filename); //ddress+"senticnet2.rdf.xml");//FileManager.get().open(senticnetAddress+"senticnet2.rdf.xml");
    	    if (in == null) {
    	        throw new IllegalArgumentException( "File: " + filename + " not found");
    	    }
    	    model.read(new InputStreamReader(in), "");
    	}
    	catch(Exception ex) {
    	    ex.printStackTrace();
    	}
    	return model;
    }
    
    /* Try to extract the score from senticnet local - downloaded locally. If it is not found it assigns -999 */
    public static double getScoreFromSentiWordNetLocal(String idx, HashMap sentiwordnet) {
	
    	Vector ret = (Vector)sentiwordnet.get(idx);
    	if(ret==null)
    	    return -999;
    	
    	double pos = ((Double)ret.get(0)).doubleValue();
    	double neg = ((Double)ret.get(1)).doubleValue();
    	
    	if(pos > 0 && neg > 0)
    	    return (pos - neg)/2;
    	if(pos > 0)
    	    return pos;
    	if(neg > 0)
    	    return -neg;
    	if(pos==0 && neg==0)
    	    return 0;
    	
    	return -999;
	
    }
    
    /* Try to extract the score from senticnet local - downloaded locally. If it is not found it assigns -999 */
    public static double getScoreFromSenticNetLocal(String word, Model modelSenti) {
	
    	double ret = -999;
    	
    	try {
    	    com.hp.hpl.jena.rdf.model.Resource vcard = modelSenti.getResource("http://sentic.net/api/en/concept/"+word);
    	    com.hp.hpl.jena.rdf.model.Property p = modelSenti.createProperty( "http://sentic.net/api/polarity" );
    	    String name = (String) vcard.getRequiredProperty(p).getString();
    	    ret = Double.parseDouble(name);
    	}
    	catch(Exception e) {
    	    e.printStackTrace();
    	}
    	
    	
    	return ret;
	
    }
    
    /* Try to extract the score from senticnet. If it is not found it assigns -999 */
    public static double getScoreFromSenticNetAPI(String word) {
    	double ret = -999;
    	try {
    	    
    	    URL url = new URL("http://sentic.net/api/en/concept/"+word);
    	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	    conn.setRequestMethod("GET");
    	    conn.setRequestProperty("Accept", "application/json");
    	    
    	    if (conn.getResponseCode() != 200) {
    		throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
    	    }
    	    
    	    Model model = ModelFactory.createDefaultModel();
    	    model.read("http://sentic.net/api/en/concept/"+word, null);
    	    Statement sss = model.getProperty(model.getResource("http://sentic.net/api/en/concept/"+word),model.getProperty("http://sentic.net/api/","polarity"));
    	    
    	    String value[] = ((String)sss.getObject().toString()).split("\\^\\^");
    	    ret = Double.parseDouble(value[0]);
    	    conn.disconnect();
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    	return ret;
    }
    
    /* Extract score using the default strategy. Average of Senticnet and up to 3 results of Sentiwordnet with tag count > 1/10 */
    public static double getScoreFromDefaultStrategy(String word,Model modelSenti, HashMap sentiwordnet, FrequencyBasedWSD frequencyBasedWSD) {
    	double ret = -999;
    	double score_senticnet = Util.getScoreFromSenticNetLocal(word,modelSenti);
    	double score_sentiwordnet = 0;
    	int cont_sentiwordnet = 0;
    	Vector result = frequencyBasedWSD.doDisambiguation(word);
    	if(result!=null) {
    	    double freq = -1;
    	    for(int ir=0;ir<result.size();ir++) {
    		WSDResult res = (WSDResult)result.get(ir);
    		if(res!=null) {
    		    if(freq!=-1 && (freq/10 > res.getFrequency()))
    		        break;
    		    freq = res.getFrequency();
    		    String synsetId = res.getSynsetID();
    		    double score_tmp = Util.getScoreFromSentiWordNetLocal(synsetId.substring(1),sentiwordnet);
    		    if(score_tmp!=-999) {
    			score_sentiwordnet += score_tmp;
    			cont_sentiwordnet++;
    		    }
    		}
    	    }
    	}
    	if(cont_sentiwordnet>0) {
    	    score_sentiwordnet = score_sentiwordnet / cont_sentiwordnet;
    	}
    	else
    	    score_sentiwordnet = -999;
    	if(score_senticnet!=-999) {
    	    if(score_sentiwordnet!=-999) {
    		ret = (score_senticnet + score_sentiwordnet)/2;
    	    }
    	    else
    		ret = score_senticnet;
    	}
    	else {
    	    if(score_sentiwordnet!=-999)
    		ret = score_sentiwordnet;
    	}
    	return ret;
    }
    
}
