package it.cnr.istc.stlab.ktools.sentilo.senti;

public class WSDResult {
    private String synset;
    private String synsetID;
    private String wnSuperSense;
    private String dulType;
    private String gloss;
    private String word;
    private int frequency;
    
    public WSDResult(String synset, String synsetID, String wnSuperSense, String dulType, String gloss, String word, int frequency) {
        this.synset = synset;
        this.synsetID = synsetID;
        this.wnSuperSense = wnSuperSense;
        this.dulType = dulType;
        this.gloss = gloss;
        this.word = word;
        this.frequency = frequency;
    }
    
    public String getDulType() {
        return dulType;
    }
    
    public String getSynset() {
        return synset;
    }
    
    public String getSynsetID() {
        return synsetID;
    }
    
    public String getWnSuperSense() {
        return wnSuperSense;
    }

    public String getGloss() {
        return gloss;
    }
    
    public String getWord() {
        return word;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
}
