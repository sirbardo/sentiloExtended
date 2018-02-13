package it.cnr.istc.stlab.ktools.sentilo.senti;


public class SensitivenessObject {

	String fact_sens = null;
	String op_sens = null;

	public SensitivenessObject(String value) {
		String[] items = value.split(" ");
		this.op_sens = items[1];
		this.fact_sens = items[2];
	}

	public String toString() {
		String ret = "fact_sens:"+this.fact_sens+" op_sens:"+this.op_sens;
		return ret;
	}

	public String getFact() {
		return fact_sens;
	}

	public String getOp() {
		return op_sens;
	}

	//    vnpattern(take,10050000, agent:'Agent' op-sens=1 fact-sens=null, patient:'Theme' op-sens=null fact-sens=null)
}
