package p2;

import java.util.ArrayList;
import java.util.Collections;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Agente extends SingleAgent {
	
	String key;
	int x=100, y=100, bateria;
	ArrayList<Integer> scanner;
	ArrayList<Integer> radar;
	String lastAction;
	ArrayList<ArrayList<Integer>> mapa;
	int movimientos;
	
	public Agente(AgentID aid) throws Exception {
		super(aid);
	}
	
	public void init() {
		scanner = new ArrayList<Integer>(25);
		radar = new ArrayList<Integer>(25);
		lastAction = "idle";
		movimientos = 0;
		mapa = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> aux = new ArrayList<Integer>();
		for (int i=0;i<200;i++) {
			aux.add(0);
		}
		for (int i=0;i<200;i++) {
			mapa.add(aux);
		}
		mapa.get(y).set(x,1);
	}
	
	
	public void login() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("command","login");
			json.put("world","map1");
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
	
	
	public int posicionMenor() {
		int pos = 0;
		int menor = 90000000;
		
		/*
		for (int i=0;i<25;i++) {
			int xaux = (i%5)-2;
			int aux = i/5;
			int f = aux%1;
			int yaux = (aux-f)-2;
			if (mapa.get(y-yaux).get(x-xaux) !=0 && radar.get(i)!=1) {
				System.out.println("sumo");
				scanner.set(i,scanner.get(i)+20);
			}
		}
		*/
		for (int i=0;i<scanner.size();i++) {
			if (radar.get(i) !=1 && i!=12) {
				if (scanner.get(i) < menor) {
					pos = i;
					menor = scanner.get(i);
				}
			}
		}
		return pos;
	}
	
	public String think() {
		String res = null;
		boolean salir = true;
		do {
		int mejor = posicionMenor();
		if (mejor==0||mejor==1||mejor==5||mejor==6) res = "moveNW";
		if (mejor==3||mejor==4||mejor==8||mejor==9) res = "moveNE";
		if (mejor==15||mejor==16||mejor==20||mejor==21) res = "moveSW";
		if (mejor==18||mejor==19||mejor==23||mejor==24) res = "moveSE";
		
		/*if (mejor==5) res = "moveW";
		if (mejor==15) res = "moveW";
		if (mejor==9) res = "moveE";
		if (mejor==19) res = "moveE";
		*/
		if (mejor==2||mejor==7) res = "moveN";
		if (mejor==13||mejor==14) res = "moveE";
		if (mejor==17||mejor==22) res = "moveS";
		if (mejor==10||mejor==11) res = "moveW";
		
		switch (res) {
			case "moveN": 	if (radar.get(7) !=1) {
							mapa.get(y-1).set(x,1);
						  	y--;
						  	salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
						  	break;
			case "moveNW": 	if (radar.get(6) !=1) {
							mapa.get(y-1).set(x-1,1);
			  				y--;
			  				x--;
			  				salir=false;
			  				} else {
			  					//scanner.set(mejor,scanner.get(mejor)+20);
			  				}
			  				break;
			case "moveNE":  if (radar.get(8) !=1) {
							mapa.get(y-1).set(x+1,1);
			  				y--;
			  				x++;
			  				salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
			  				break;
			case "moveS": 	if (radar.get(17) !=1) {
							mapa.get(y+1).set(x,1);
			  				y++;
			  				salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
			  				break;
			case "moveSW":  if (radar.get(16) !=1) {
							mapa.get(y+1).set(x-1,1);
			  				y++;
			  				x--;
			  				salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
			  				break;
			case "moveSE": 	if (radar.get(18) !=1) {
							mapa.get(y+1).set(x+1,1);
							y++;
							x++;
							salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
							break;
			case "moveW": 	if (radar.get(11) !=1) {
							mapa.get(y).set(x-1,1);
							x--;
							salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
							break;
			case "moveE": 	if (radar.get(13) !=1) {
							mapa.get(y).set(x+1,1);
							x++;
							salir=false;
							} else {
								//scanner.set(mejor,scanner.get(mejor)+20);
							}
							break;
		}
		} while (salir);
		movimientos++;
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
				System.out.println(radar.toString());
				System.out.println("Estamos en: "+x+","+y);
				if(radar.get(12)==2) {
					System.out.println("Encontrado en el punto:("+x+","+y+")");
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
		System.out.println(movimientos);
        super.finalize();
	}
	

}
