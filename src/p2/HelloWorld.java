package p2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.core.SingleAgent;


public class HelloWorld {
	
	public static void main(String[] args) throws Exception {
		
		AgentsConnection.connect("isg2.ugr.es",6000,"Izar","Boyero","Pamuk",false);
		Agente bot = new Agente(new AgentID("botz"));
		bot.start();
	}

}
