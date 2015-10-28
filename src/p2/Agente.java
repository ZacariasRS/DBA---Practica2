package p2;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Agente extends SingleAgent {
	
	String key;
	int x, y;
	
	public Agente(AgentID aid) throws Exception {
		super(aid);
	}
	
	
	public void login() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("command","login");
			json.put("world","map1");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ACLMessage login = new ACLMessage();
		login.setSender(this.getAid());
		login.setReceiver(new AgentID("Izar"));
		login.setContent(json.toString());
		this.send(login);
		
		ACLMessage inbox = new ACLMessage();
		try {
			inbox = this.receiveACLMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject receive = new JSONObject(inbox.getContent());
			this.key = receive.getString("result");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void logout(String key) {
		
		try {
			JSONObject json = new JSONObject();
			json.put("command","logout");
			json.put("key",key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ACLMessage logout = new ACLMessage();
		logout.setSender(this.getAid());
		logout.setReceiver(new AgentID("Izar"));
		logout.setContent(logout.toString());
		this.send(logout);
		
	}
	public void execute() {
		
		login();
		
		System.out.println(key);
		logout(key);
	}
	
	public void finalize() {
		System.out.println("Agente("+this.getName()+") Terminando");       
        super.finalize();
	}
	

}
