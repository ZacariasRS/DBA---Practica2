package src.helloworld;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Agente extends SingleAgent {
	
	public Agente(AgentID aid) throws Exception {
		super(aid);
	}
	
	public void execute() {
		JSONObject json = new JSONObject();
	
		try {
			json.append("command","login");
			json.append("world","map1");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ACLMessage login = new ACLMessage();
		login.setSender(this.getAid());
		login.setReceiver(new AgentID("Izar"));
		login.setContent(json.toString());
		
		ACLMessage inbox = new ACLMessage();
		try {
			inbox = this.receiveACLMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Recibido mensaje"+inbox.getContent()+" de "+inbox.getSender().getLocalName());
		
	}
	

}
