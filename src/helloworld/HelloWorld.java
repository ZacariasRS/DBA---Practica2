package helloworld;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.core.SingleAgent;


public class HelloWorld {
	
	public static void main(String[] args) throws Exception {
		
		AgentsConnection.connect("isg2.ugr.es",6000,"Izar","Boyero","Pamuk",false);
		Agente smith = new Agente(new AgentID("Smith"));
		smith.start();
	}

}
