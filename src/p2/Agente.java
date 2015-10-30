package p2;

import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Agente extends SingleAgent {
	
	String key;
	int x, y, bateria;
	ArrayList<Integer> scanner;
	ArrayList<Integer> radar;
	
	public Agente(AgentID aid) throws Exception {
		super(aid);
	}
	
	public void init() {
		scanner = new ArrayList<Integer>(25);
		radar = new ArrayList<Integer>(25);
	}
	
	
	public void login() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("command","login");
			json.put("world","map5");
			json.put("radar","botz");
			json.put("scanner","botz");
			
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
	
	public void logout() {
		
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
	
	public ArrayList<Integer> recibirScanner() {
		ArrayList<Integer> res = new ArrayList<Integer>(25);
		ACLMessage inbox = new ACLMessage();
		try {
			inbox = this.receiveACLMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject receive = new JSONObject(inbox.getContent());
			JSONArray jArray = receive.getJSONArray("scanner");
			for (int i=0;i<25;i++) {
				res.add(jArray.getInt(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	public ArrayList<Integer> recibirRadar() {
		ArrayList<Integer> res = new ArrayList<Integer>(25);
		ACLMessage inbox = new ACLMessage();
		try {
			inbox = this.receiveACLMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject receive = new JSONObject(inbox.getContent());
			JSONArray jArray = receive.getJSONArray("radar");
			for (int i=0;i<25;i++) {
				res.add(jArray.getInt(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	public int posicionMenor(ArrayList<Integer> array) {
		int pos = 0;
		int menor = 90000000;
		for (int i=0;i<array.size();i++) {
			if (radar.get(i) !=1 && i!=12) {
				if (array.get(i) < menor) {
					pos = i;
					menor = array.get(i);
				}
			}
		}
		return pos;
	}
	public String think() {
		String res = null;
		int mejor = posicionMenor(scanner);
		
		if (mejor==0||mejor==1||mejor==5||mejor==6) res = "moveNW";
		if (mejor==3||mejor==4||mejor==8||mejor==9) res = "moveNE";
		if (mejor==15||mejor==16||mejor==20||mejor==21) res = "moveSW";
		if (mejor==18||mejor==19||mejor==23||mejor==24) res = "moveSE";

		if (mejor==2||mejor==7) res = "moveN";
		if (mejor==13||mejor==14) res = "moveE";
		if (mejor==17||mejor==22) res = "moveS";
		if (mejor==10||mejor==11) res = "moveW";
		
		return res;
	}
	
	public String move(String movimiento) throws InterruptedException {
		String result = null;
		try {
			JSONObject json = new JSONObject();
			json.put("command",movimiento);
			json.put("key",key);
			ACLMessage m = new ACLMessage();
			m.setSender(this.getAid());
			m.setReceiver(new AgentID("Izar"));
			m.setContent(json.toString());
			this.send(m);
			
			ACLMessage r = new ACLMessage();
			r = this.receiveACLMessage();
			JSONObject rjson = new JSONObject(r.getContent());
			result = rjson.getString("result");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		bateria--;
		System.out.println(movimiento);
		return result;
	}
	
	public String refuel() throws InterruptedException {
		String result = null;
		try {
			JSONObject json = new JSONObject();
			json.put("command","refuel");
			json.put("key",key);
			ACLMessage m = new ACLMessage();
			m.setSender(this.getAid());
			m.setReceiver(new AgentID("Izar"));
			m.setContent(json.toString());
			this.send(m);
			
			ACLMessage r = new ACLMessage();
			r = this.receiveACLMessage();
			JSONObject rjson = new JSONObject(r.getContent());
			result = rjson.getString("result");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		bateria = 100;
		return result;
	}
	
	public void execute() {
		
		login();
		boolean moverse = true;
		
		//System.out.println(key);
			
		try {
			radar = recibirRadar();
			scanner = recibirScanner();
			
			refuel();
			while(moverse) {
				System.out.println(bateria);
				System.out.println(scanner.toString());
				radar = recibirRadar();
				scanner = recibirScanner();

				if(scanner.get(12)==0) {
					System.out.println("Encontrado");
					moverse = false;
				} else {
					if (bateria < 25) System.out.println("Repostando "+refuel()); else System.out.println("Nos movemos "+move(think()));
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logout();
	}
	
	public void finalize() {
		System.out.println("Agente("+this.getName()+") Terminando");       
        super.finalize();
	}
	

}
