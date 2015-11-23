package p2;


import java.util.ArrayList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class BotZ extends SingleAgent {
	
	String key;
	int x, y, bateria;
	public ArrayList<Integer> scanner;
	public ArrayList<Integer> radar;
	String lastAction;
	String wantedAction;
	String wallDirection;
	//ArrayList<ArrayList<Integer>> mapa;
	int[][] mapa;
	int movimientos;
	boolean followWall;
	
	public BotZ(AgentID aid) throws Exception {
		super(aid);
	}
	
	@Override
	public void init() {
		scanner = new ArrayList<Integer>(25);
		radar = new ArrayList<Integer>(25);
		lastAction = "idle";
		movimientos = 0;
		/*mapa = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> aux = new ArrayList<Integer>();
		for (int i=0;i<500;i++) {
			aux.add(i,0);
		}
		for (int i=0;i<500;i++) {
			mapa.add(i,aux);
		}*/
		mapa = new int[500][500];
		followWall = false;
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función para conectar con el servidor.
	 */
	public void login() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("command", "login");
			json.put("world", "map2");
			json.put("radar", RescueBots.nRadar);
			json.put("scanner", RescueBots.nScanner);
			json.put("gps", RescueBots.nGPS);
			json.put("battery", RescueBots.nBattery);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ACLMessage login = new ACLMessage();
		login.setSender(this.getAid());
		login.setReceiver(new AgentID(RescueBots.nServer));
		login.setContent(json.toString());
		this.send(login);
		
		ACLMessage inbox = new ACLMessage();
		try {
			inbox = this.receiveACLMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Login OK");
		
		try {
			JSONObject receive = new JSONObject(inbox.getContent());
			this.key = receive.getString("result");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función para desconectar correctamente del servidor.
	 */
	
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

	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que actualiza batería.
	 */
	public void requestBattery () throws InterruptedException, JSONException {
		ACLMessage inbox, outbox;
		boolean received = false;
		
		while (!received) {
			outbox = new ACLMessage();
			outbox.setSender(this.getAid());
			outbox.setReceiver(new AgentID(RescueBots.nBattery));
			this.send(outbox);
			inbox = this.receiveACLMessage();
			JSONObject data = new JSONObject(inbox.getContent());
			if (data.has("battery")) {
				bateria = data.getInt("battery");
				received = true;
				//System.out.println("Bot: battery received: "+bateria);
			}
		}
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que actualiza GPS.
	 */
	public void requestGPS () throws InterruptedException, JSONException {
		ACLMessage inbox, outbox;
		boolean received = false;
		
		while (!received) {
			outbox = new ACLMessage();
			outbox.setSender(this.getAid());
			outbox.setReceiver(new AgentID(RescueBots.nGPS));
			this.send(outbox);
			inbox = this.receiveACLMessage();
			JSONObject data = new JSONObject(inbox.getContent());
			if (data.has("gps")) {
				JSONObject gpsXY = new JSONObject(data.getString("gps"));
				x = gpsXY.getInt("x");
				y = gpsXY.getInt("y");
				//System.out.println("Bot: GPS received: X="+x+", Y="+y);
				Map.getInstance().setMapComposition(x, y, Map.MapState.ROBOT);
				received = true;
			}
		}
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que actualiza Scanner.
	 */
	public void requestScanner() throws InterruptedException, JSONException {
		ArrayList<Integer> res = new ArrayList<Integer>(25);
		ACLMessage inbox, outbox;
		boolean received = false;
		
		while (!received) {
			outbox = new ACLMessage();
			outbox.setSender(this.getAid());
			outbox.setReceiver(new AgentID(RescueBots.nScanner));
			this.send(outbox);
			inbox = this.receiveACLMessage();
			JSONObject data = new JSONObject(inbox.getContent());
			if (data.has("scanner")) {
				JSONObject receive = new JSONObject(inbox.getContent());
				JSONArray jArray = receive.getJSONArray("scanner");
				for (int i=0;i<25;i++) {
					res.add(jArray.getInt(i));
				}
				scanner = res;
				received = true;
			}
		}
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que actualiza radar.
	 */
	public void requestRadar() throws InterruptedException, JSONException {
		ArrayList<Integer> res = new ArrayList<Integer>(25);
		ACLMessage inbox, outbox;
		boolean received = false;
		
		while (!received) {
			outbox = new ACLMessage();
			outbox.setSender(this.getAid());
			outbox.setReceiver(new AgentID(RescueBots.nRadar));
			this.send(outbox);
			inbox = this.receiveACLMessage();
			JSONObject data = new JSONObject(inbox.getContent());
			if (data.has("radar")) {
				JSONObject receive = new JSONObject(inbox.getContent());
				JSONArray jArray = receive.getJSONArray("radar");
				for (int i=0; i<25; i++) {
					res.add(jArray.getInt(i));
				}
				radar = res;
				received = true;
			}
		}
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que actualiza nuestra posicion y la marca en nuestro mapa en memoria,
	 *  y tambien pinta el mapa gráfico
	 */
	
	public void updateAll() {
		System.out.println("Actualizo:"+x+" "+y);
		System.out.println(mapa[x][y]);
		mapa[x][y]++;
		for (int i=0;i<25;i++) {
			if(i!=12) {
				int xaux = (i%5)-2;
				//int aux = i/5;
				//int f = aux%1;
				int yaux = (i/5)-2;
				int z = radar.get(i);
				int actualx = x+xaux;
				int actualy = y+yaux;
				if (actualx >= 0 && actualy >= 0) {
					switch(z) {
						case 0: Map.getInstance().setMapComposition(x-xaux, y-yaux, Map.MapState.KNOWN);
								break;
						case 1: Map.getInstance().setMapComposition(x-xaux, y-yaux, Map.MapState.OBSTACLE);
								break;
						case 2: Map.getInstance().setMapComposition(x-xaux, y-yaux, Map.MapState.GOAL);
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que devuelve un entero, el cual es la celda del array scanner con el menor valor.
	 */
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
			if (true/*radar.get(i) !=1 && i!=12*/) {
				if (scanner.get(i) <= menor) {
					pos = i;
					menor = scanner.get(i);
				}
			}
		}
		return pos;
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función núcleo del agente. Esta función obtiene el movimiento más favorable para el agente.
	 */
	public String think() {
		String res = null;
		
		int mejor = posicionMenor();
		for (int i=0;i<25;i++) {
			int xaux = (i%5)-2;
			int yaux = (i/5)-2;
			int actualx = x+xaux;
			int actualy = y+yaux;
			if (actualx >= 0 && actualy >=0 && actualx <500 && actualy <500) {
				if (radar.get(i) == 1) {
				scanner.set(i,100000);
				} else scanner.set(i,scanner.get(i)+(mapa[actualx][actualy]*2));
			}
		}
		switch (lastAction) {
		case "moveN":
			scanner.set(7,scanner.get(7)-2);
			scanner.set(6,scanner.get(6)-1);
			scanner.set(8,scanner.get(8)-1);
			break;
		case "moveS":
			scanner.set(17,scanner.get(17)-2);
			scanner.set(16,scanner.get(16)-1);
			scanner.set(18,scanner.get(18)-1);
			break;
		case "moveNW":
			scanner.set(6,scanner.get(6)-2);
			scanner.set(7,scanner.get(7)-1);
			scanner.set(11,scanner.get(11)-1);
			break;
		case "moveNE":
			scanner.set(8,scanner.get(8)-2);
			scanner.set(7,scanner.get(7)-1);
			scanner.set(13,scanner.get(13)-1);
			break;
		case "moveSW":
			scanner.set(16,scanner.get(16)-2);
			scanner.set(17,scanner.get(17)-1);
			scanner.set(11,scanner.get(11)-1);
			break;
		case "moveSE":
			scanner.set(18,scanner.get(18)-2);
			scanner.set(17,scanner.get(17)-1);
			scanner.set(13,scanner.get(13)-1);
			break;
		case "moveW":
			scanner.set(11,scanner.get(11)-2);
			scanner.set(6,scanner.get(6)-1);
			scanner.set(16,scanner.get(16)-1);
			break;
		case "moveE": 
			scanner.set(13,scanner.get(13)-2);
			scanner.set(8,scanner.get(8)-1);
			scanner.set(18,scanner.get(18)-1);
			break;
		}
		System.out.println("Scanner:");
		for (int i=0;i<5;i++) {
			System.out.println(scanner.get((i*5)+0)+" "+scanner.get((i*5)+1)+" "+scanner.get((i*5)+2)+" "+scanner.get((i*5)+3)+" "+scanner.get((i*5)+4));
		}
		if (followWall) {
			switch (wallDirection) {
			case "north" :
				scanner.set(7,scanner.get(7)-10);
				scanner.set(6,scanner.get(6)-10);
				scanner.set(8,scanner.get(8)-10);
				if (wantedAction == "moveE") scanner.set(13,scanner.get(13)-10);
				else scanner.set(11,scanner.get(11)-10);
				break;
			case "south" :
				scanner.set(17,scanner.get(17)-10);
				scanner.set(16,scanner.get(16)-10);
				scanner.set(18,scanner.get(18)-10);
				if (wantedAction == "moveE") scanner.set(13,scanner.get(13)-10);
				else scanner.set(11,scanner.get(11)-10);
				break;
			case "east" :
				scanner.set(8,scanner.get(8)-10);
				scanner.set(13,scanner.get(13)-10);
				scanner.set(18,scanner.get(18)-10);
				if (wantedAction == "moveN") scanner.set(7,scanner.get(7)-10);
				else scanner.set(17,scanner.get(17)-10);
				break;
			case "west" :
				scanner.set(6,scanner.get(6)-10);
				scanner.set(11,scanner.get(11)-10);
				scanner.set(16,scanner.get(16)-10);
				if (wantedAction == "moveN") scanner.set(7,scanner.get(7)-10);
				else scanner.set(17,scanner.get(17)-10);
			}
			System.out.println("Estamos en follow. wanted = "+wantedAction+" lastAction="+lastAction);
			switch (wantedAction) {
			case "moveN": 	if (radar.get(7) !=1 && mapa[x][y-1] == 0) {
						  		res = wantedAction;
						  		followWall = false;
							} else if (scanner.get(6) <= scanner.get(8) && radar.get(6) !=1) {res = "moveNW"; followWall=false;}
							else if (scanner.get(8) <= scanner.get(6) && radar.get(8) !=1) {res = "moveNE"; followWall=false;}
							else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(13) <= scanner.get(11) && radar.get(11) !=1) res = "moveE";
							else if (scanner.get(16) <= scanner.get(18) && radar.get(16) !=1) res = "moveSW";
							else if (scanner.get(18) <= scanner.get(16) && radar.get(18) !=1) res = "moveSE";
							else if (radar.get(17) !=1) res = "moveS";
							else if (radar.get(6) !=1) {res = "moveNW"; followWall=false;}
							else if (radar.get(8) !=1) {res = "moveNE"; followWall=false;}
						  	break;
			case "moveNW": 	if (radar.get(6) !=1) {
			  					res = wantedAction;
			  					followWall = false;
							} else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) {res = "moveW"; followWall=false;}
  							else if (scanner.get(10) <= scanner.get(2) && radar.get(11) !=1) {res = "moveW"; followWall=false;}
							else if (scanner.get(7) <= scanner.get(8) && radar.get(7) != 1) {res = "moveN"; followWall=false;}
							else if (scanner.get(8) <= scanner.get(16) && radar.get(8) !=1) res = "moveNE";
							else if (scanner.get(16) <= scanner.get(13) && radar.get(16) !=1) {res = "moveSW"; followWall=false;}
							else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(17) <= scanner.get(18) && radar.get(17) !=1) res = "moveS";
							else if (radar.get(18) !=1) res = "moveSE";
							else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) {res = "moveW"; followWall=false;}
							else if (radar.get(7) !=1) res = "moveN";
							break;
			case "moveNE":  if (radar.get(8) !=1) {
			  					res = wantedAction;
			  					followWall = false;
							} else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(14) <= scanner.get(2) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(7) <= scanner.get(6) && radar.get(7) != 1) res = "moveN";
							else if (scanner.get(6) <= scanner.get(18) && radar.get(6) !=1) res = "moveNW";
							else if (scanner.get(18) <= scanner.get(11) && radar.get(18) !=1) res = "moveSE";
							else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(17) <= scanner.get(16) && radar.get(17) !=1) res = "moveS";
							else if (radar.get(16) !=1) res = "moveSW";
							else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
							else if (radar.get(7) !=1) res = "moveN";
							break;
			case "moveS": 	if (radar.get(17) !=1 && mapa[x][y+1] == 0) {
								res = wantedAction;
								followWall = false;
							} else if (scanner.get(16) <= scanner.get(18) && radar.get(16) !=1) {res = "moveSW"; followWall=false;}
							else if (scanner.get(18) <= scanner.get(16) && radar.get(18) !=1) {res = "moveSE"; followWall=false;}
							else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(13) <= scanner.get(11) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(6) <= scanner.get(8) && radar.get(6) !=1) res = "moveNW";
							else if (scanner.get(8) <= scanner.get(6) && radar.get(8) !=1) res = "moveNE";
							else if (radar.get(7) !=1) res = "moveN";
							else if (radar.get(16) !=1) {res = "moveSW"; followWall=false;}
							else if (radar.get(18) !=1) {res = "moveSE"; followWall=false;}
							break;
			case "moveSW":  if (radar.get(16) !=1) {
								res = wantedAction;
								followWall = false;
							} else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(10) <= scanner.get(22) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(17) <= scanner.get(18) && radar.get(17) != 1) res = "moveS";
							else if (scanner.get(18) <= scanner.get(13) && radar.get(18) !=1) res = "moveSE";
							else if (scanner.get(6) <= scanner.get(13) && radar.get(6) !=1) res = "moveNW";
							else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(7) <= scanner.get(8) && radar.get(7) !=1) res = "moveN";
							else if (radar.get(8) !=1) res = "moveNE";
							else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) res = "moveW";
							else if (radar.get(17) !=1) res = "moveS";
							break;
			case "moveSE": 	if (radar.get(18) !=1) {
								res = wantedAction;
								followWall = false;
							} else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(14) <= scanner.get(22) && radar.get(13) !=1) res = "moveE";
							else if (scanner.get(17) <= scanner.get(16) && radar.get(17) != 1) res = "moveS";
							else if (scanner.get(16) <= scanner.get(8) && radar.get(16) !=1) res = "moveSW";
							else if (scanner.get(8) <= scanner.get(11) && radar.get(8) !=1) res = "moveNE";
							else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) res = "moveW";
							else if (scanner.get(7) <= scanner.get(6) && radar.get(7) !=1) res = "moveN";
							else if (radar.get(6) !=1) res = "moveNW";
							else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) res = "moveE";
							else if (radar.get(17) !=1) res = "moveS";
							break;
			case "moveW": 	if (radar.get(11) !=1 && mapa[x-1][y] == 0) {
								res = wantedAction;
								followWall = false;
							} else if (scanner.get(16) <= scanner.get(6) && radar.get(16) !=1) {res = "moveSW"; followWall=false;}
							else if (scanner.get(6) <= scanner.get(16) && radar.get(6) !=1) {res = "moveNW"; followWall=false;}
							else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1) res = "moveS";
							else if (radar.get(7) !=1) res = "moveN";
							else if (scanner.get(8) <= scanner.get(18) && radar.get(8) !=1) res = "moveNE";
							else if (scanner.get(18) <= scanner.get(13) && radar.get(18) !=1) res = "moveSE";
							else if (radar.get(13) !=1 && mapa[x+1][y] != 3) res = "moveE";
							else if (radar.get(6) !=1) {res = "moveNW"; followWall=false;}
							else if (radar.get(16) !=1) {res = "moveSW"; followWall=false;}
							break;
			case "moveE": 	if (radar.get(13) !=1 && mapa[x+1][y] == 0) {
									res = wantedAction;
									followWall = false;
								} else if (scanner.get(18) <= scanner.get(8) && radar.get(18) !=1) {res = "moveSE"; followWall=false;}
								else if (scanner.get(8) <= scanner.get(18) && radar.get(8) !=1) {res = "moveNE"; followWall=false;}
								else if (scanner.get(7) <= scanner.get(17) && radar.get(7) !=1) res = "moveN";
								else if (scanner.get(6) <= scanner.get(16) && radar.get(6) !=1) res = "moveNW";
								else if (radar.get(11) !=1 && mapa[x-1][y] != 3) res = "moveW";
								else if (scanner.get(16) <= scanner.get(11) && radar.get(16) !=1) res = "moveSW";
								else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1) res = "moveS";
								else if (radar.get(8) !=1) {res = "moveNE"; followWall=false;}
								else if (radar.get(18) !=1) {res = "moveSE"; followWall=false;}
							break;
			}
		} else {
			if (mejor==0||mejor==1||mejor==5||mejor==6) res = "moveNW";
			if (mejor==3||mejor==4||mejor==8||mejor==9) res = "moveNE";
			if (mejor==15||mejor==16||mejor==20||mejor==21) res = "moveSW";
			if (mejor==18||mejor==19||mejor==23||mejor==24) res = "moveSE";
			
			if (mejor==2||mejor==7) res = "moveN";
			if (mejor==13||mejor==14) res = "moveE";
			if (mejor==17||mejor==22) res = "moveS";
			if (mejor==10||mejor==11) res = "moveW";
			/*if (mejor==12) {
				res = lastAction;
				wantedAction= "moveN";
				followWall = true;
			} else {*/
			System.out.println("Entro a switch con "+ res);
				switch (res) {
					case "moveN": 	if (radar.get(7) !=1) {
								  		
									} else {
										if (radar.get(6) == 1 && radar.get(8) == 1) {
											if (scanner.get(10) <= scanner.get(14) && radar.get(11) !=1) {
												wantedAction = res;
												res = "moveW";
												wallDirection = "west";
												followWall = true;
											} else if (radar.get(13) !=1){
												wantedAction = res;
												res = "moveE";
												wallDirection = "east";
												followWall = true;
											} else if (radar.get(18) !=1) res = "moveSE";
											else if (radar.get(17) !=1) res = "moveS";
										} else if (scanner.get(6) <= scanner.get(8) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(8) <= scanner.get(6) && radar.get(8) !=1) res = "moveNE";
										else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1) res = "moveE";
										else if (scanner.get(13) <= scanner.get(11) && radar.get(11) !=1) res = "moveW";
										else if (scanner.get(16) <= scanner.get(18) && radar.get(16) !=1) res = "moveSW";
										else if (scanner.get(18) <= scanner.get(16) && radar.get(18) !=1) res = "moveSE";
										else if (radar.get(17) !=1) res = "moveS";
										else if (radar.get(6) !=1) res = "moveNW";
										else if (radar.get(8) !=1) res = "moveNE";
									}
								  	break;
					case "moveNW": 	if (radar.get(6) !=1) {
					  					
					  				} else {
					  					if (radar.get(7) == 1 && radar.get(11) == 1) {
					  						if (scanner.get(8) <= scanner.get(17) && radar.get(8) !=1) {
					  							res = "moveNE";
					  						}
					  						else if (scanner.get(16) <= scanner.get(17) && radar.get(16) !=1 && mapa[x-1][y+1] != 3) {
					  							res = "moveSW";
					  						}
					  						else if ((scanner.get(9)) <= scanner.get(17) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else if ((scanner.get(3)) <= scanner.get(17) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else if(scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else {
					  							wantedAction = "moveW";
					  							res = "moveS";
					  							wallDirection = "south";
					  							followWall = true;
					  						}
										}
					  							else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) res = "moveW";
					  							//else if (scanner.get(10) <= scanner.get(2) && radar.get(11) !=1) res = "moveW";
												else if (scanner.get(7) <= scanner.get(8) && radar.get(7) != 1) res = "moveN";
												else if (scanner.get(8) <= scanner.get(16) && radar.get(8) !=1) res = "moveNE";
												else if (scanner.get(16) <= scanner.get(13) && radar.get(16) !=1) res = "moveSW";
												else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) res = "moveE";
												else if (scanner.get(17) <= scanner.get(18) && radar.get(17) !=1) res = "moveS";
												else if (radar.get(18) !=1) res = "moveSE";
												else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) res = "moveW";
												else if (radar.get(7) !=1) res = "moveN";
					  				}
					  				break;
					case "moveNE":  if (radar.get(8) !=1) {
					  				
									} else {
										if (radar.get(7) == 1 && radar.get(13) == 1) {
											if (scanner.get(6) <= scanner.get(17) && radar.get(6) !=1) {
					  							res = "moveNW";
											}
											else if (scanner.get(18) <= scanner.get(17) && radar.get(18) !=1) {
												res = "moveSE";
											}
					  						else if (scanner.get(5) <= scanner.get(17) && radar.get(11) !=1) {
					  							res = "moveW";
					  						} else if ((scanner.get(1)) <= scanner.get(17) && radar.get(11) !=1) {
					  							res = "moveW";
					  						} else if(scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) {
					  							res = "moveW";
					  						} else {
					  							wantedAction = "moveE";
					  							res = "moveS";
					  							wallDirection = "south";
					  							followWall = true;
					  						}
										} else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
										//else if (scanner.get(14) <= scanner.get(2) && radar.get(13) !=1) res = "moveE";
										else if (scanner.get(7) <= scanner.get(6) && radar.get(7) != 1) res = "moveN";
										else if (scanner.get(6) <= scanner.get(7) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(18) <= scanner.get(6) && radar.get(18) !=1) res = "moveSE";
										else if (scanner.get(11) <= scanner.get(18) && radar.get(11) !=1) res = "moveW";
										else if (scanner.get(17) <= scanner.get(11) && radar.get(17) !=1) res = "moveS";
										else if (radar.get(16) !=1) res = "moveSW";
										else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
										else if (radar.get(7) !=1) res = "moveN";
					  				}
					  				break;
					case "moveS": 	if (radar.get(17) !=1) {
				
									} else {
										if (radar.get(16) == 1 && radar.get(18) == 1) {
											if (scanner.get(10) <= scanner.get(14) && radar.get(11) !=1) {
												wantedAction = res;
												res = "moveW";
												wallDirection = "west";
												followWall = true;
											} else if (radar.get(13) !=1) {
												wantedAction = res;
												res = "moveE";
												wallDirection = "east";
												followWall = true;
											} else if (radar.get(6) !=1) res = "moveNW";
											else if (radar.get(7) !=1) res = "moveN";
										} else if (scanner.get(16) <= scanner.get(18) && radar.get(16) !=1) res = "moveSW";
										else if (scanner.get(18) <= scanner.get(16) && radar.get(18) !=1) res = "moveSE";
										else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1) res = "moveW";
										else if (scanner.get(13) <= scanner.get(11) && radar.get(13) !=1) res = "moveE";
										else if (scanner.get(6) <= scanner.get(8) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(8) <= scanner.get(6) && radar.get(8) !=1) res = "moveNE";
										else if (radar.get(7) !=1) res = "moveN";
										else if (radar.get(16) !=1) res = "moveSW";
										else if (radar.get(18) !=1) res = "moveSE";
									}
					  				break;
					case "moveSW":  if (radar.get(16) !=1) {
					  					
									} else {
										if (radar.get(11) == 1 && radar.get(17) == 1) {
											if (scanner.get(18) <= scanner.get(7) && radar.get(18) !=1) {
					  							res = "moveSE";
											}
											else if (scanner.get(6) <= scanner.get(6) && radar.get(6) !=1) {
												res = "moveNW";
											}
					  						else if (scanner.get(19) <= scanner.get(7) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else if (scanner.get(23) <= scanner.get(7) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else if(scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) {
					  							res = "moveE";
					  						} else {
					  							wantedAction = "moveW";
					  							res = "moveN";
					  							wallDirection = "north";
					  							followWall = true;
					  						}
										} else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) res = "moveW";
										//else if (scanner.get(10) <= scanner.get(22) && radar.get(11) !=1) res = "moveW";
										else if (scanner.get(17) <= scanner.get(18) && radar.get(17) != 1) res = "moveS";
										else if (scanner.get(18) <= scanner.get(13) && radar.get(18) !=1) res = "moveSE";
										else if (scanner.get(6) <= scanner.get(13) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1) res = "moveE";
										else if (scanner.get(7) <= scanner.get(8) && radar.get(7) !=1) res = "moveN";
										else if (radar.get(8) !=1) res = "moveNE";
										else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1) res = "moveW";
										else if (radar.get(17) !=1) res = "moveS";
					  				}
					  				break;
					case "moveSE": 	if (radar.get(18) !=1) {
									} else {
										if (radar.get(13) == 1 && radar.get(8) == 1) {
											if (scanner.get(16) <= scanner.get(7) && radar.get(16) !=1) {
					  							res = "moveSW";
											}
											else if (scanner.get(8) <= scanner.get(16) && radar.get(8) !=1) {
												res = "moveNE";
											}
											else if (scanner.get(16) <= scanner.get(15) && radar.get(16) !=1) {
												res = "moveSW";
											}
					  						else if (scanner.get(15) <= scanner.get(21) && radar.get(11) !=1) {
					  							res = "moveW";
					  						} else if ((scanner.get(21)) <= scanner.get(11) && radar.get(11) !=1) {
					  							res = "moveW";
					  						}else if(scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) {
					  							res = "moveW";
					  							
					  						} else if (radar.get(16) !=1) res = "moveSW";
					  						else {
					  							wantedAction = "moveE";
					  							res = "moveN";
					  							wallDirection = "north";
					  							followWall = true;
					  						}
										} else if (scanner.get(13) <= scanner.get(8) && radar.get(13) !=1) res = "moveE";
										//else if (scanner.get(14) <= scanner.get(22) && radar.get(13) !=1) res = "moveE";
										else if (scanner.get(8) <= scanner.get(17) && radar.get(8) !=1) res = "moveNE";
										else if (scanner.get(17) <= scanner.get(16) && radar.get(17) != 1) res = "moveS";
										else if (scanner.get(16) <= scanner.get(11) && radar.get(16) !=1) res = "moveSW";
										else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1) res = "moveW";
										else if (scanner.get(7) <= scanner.get(6) && radar.get(7) !=1) res = "moveN";
										else if (radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1) res = "moveE";
										else if (radar.get(17) !=1) res = "moveS";
					  				}
					  				break;
					case "moveW": 	if (radar.get(11) !=1) {
									
									} else {
										if (radar.get(6) == 1 && radar.get(16) == 1) {
											if (scanner.get(2) <= scanner.get(22) && radar.get(7) !=1) {
												wantedAction = res;
												res = "moveN";
												wallDirection = "north";
												followWall = true;
											} else if (radar.get(17) !=1){
												wantedAction = res;
												res = "moveS";
												wallDirection = "south";
												followWall = true;
											} else if (radar.get(18) !=1) res = "moveSE";
											else if (radar.get(13) !=1) res = "moveE";
										} else if (scanner.get(16) <= scanner.get(6) && radar.get(16) !=1) res = "moveSW";
										else if (scanner.get(6) <= scanner.get(16) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1) res = "moveS";
										else if (radar.get(7) !=1) res = "moveN";
										else if (scanner.get(8) <= scanner.get(18) && radar.get(8) !=1) res = "moveNE";
										else if (scanner.get(18) <= scanner.get(13) && radar.get(18) !=1) res = "moveSE";
										else if (radar.get(13) !=1 && mapa[x+1][y] != 3) res = "moveE";
										else if (radar.get(6) !=1) res = "moveNW";
										else if (radar.get(16) !=1) res = "moveSW";
									}
									break;
					case "moveE": 	if (radar.get(13) !=1) {
									
									} else {
										if (radar.get(8) == 1 && radar.get(18) == 1) {
											if (scanner.get(2) <= scanner.get(22) && radar.get(7) !=1) {
												wantedAction = res;
												res = "moveN";
												wallDirection = "north";
												followWall = true;
											} else if (radar.get(17) !=1) {
												wantedAction = res;
												res = "moveS";
												wallDirection = "south";
												followWall = true;
											} else if (radar.get(16) !=1) res = "moveSW";
											else if (radar.get(11) !=1) res = "moveW";
										} else if (scanner.get(18) <= scanner.get(8) && radar.get(18) !=1) res = "moveSE";
										else if (scanner.get(8) <= scanner.get(18) && radar.get(8) !=1) res = "moveNE";
										else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1) res = "moveS";
										else if (scanner.get(7) <= scanner.get(6) && radar.get(7) !=1) res = "moveN";
										else if (scanner.get(6) <= scanner.get(7) && radar.get(6) !=1) res = "moveNW";
										else if (scanner.get(16) <= scanner.get(6) && radar.get(16) !=1) res = "moveSW";
										else if (radar.get(11) !=1 && mapa[x-1][y] != 3) res = "moveW";
										else if (radar.get(8) !=1) res = "moveNE";
										else if (radar.get(18) !=1) res = "moveSE";
									}
									break;
				}
			//}
		}
		movimientos++;
		return res;
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que manda la accion de moverse.
	 */
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
		System.out.println(movimiento);
		lastAction = movimiento;
		return result;
	}
	
	/**
	 * 
	 * @author Zacarías Romero Sellamitou
	 * 
	 * Función que manda la acción de repostar.
	 */
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
		//lastAction="refuel";
		return result;
	}
	
	@Override
	public void execute() {
		boolean moverse = true;
		
		login();
		try {
			
			while(moverse) {
				requestBattery();
				requestGPS();
				requestScanner();
				requestRadar();
				updateAll();
				//System.out.println(bateria);
				
				
				System.out.println("\nRadar:");
				for (int i=0;i<5;i++) {
					System.out.println(radar.get((i*5)+0)+" "+radar.get((i*5)+1)+" "+radar.get((i*5)+2)+" "+radar.get((i*5)+3)+" "+radar.get((i*5)+4));
				}
				
				System.out.println("Mapa");
				for (int i=0;i<25;i++) {
						int xaux = (i%5)-2;
						int yaux = (i/5)-2;
						int actualx = x+xaux;
						int actualy = y+yaux;
						if (actualx >=0 && actualy >=0 && actualx<=499 && actualy<=499) {
							if (i%5==0 && i!=0) {
								if (mapa[actualx][actualy] == 3) {
									//System.out.print("Actual: " +actualx+" "+actualy+" ");
									System.out.print("\n"+mapa[actualx][actualy]+" ");
								} else {
									System.out.print("\n"+mapa[actualx][actualy]+" ");
								}
							} else {
								if (mapa[actualx][actualy] == 3) {
									//System.out.print("Actual: " +actualx+" "+actualy+" ");
									System.out.print(mapa[actualx][actualy]+" ");
								} else {
								System.out.print(+mapa[actualx][actualy]+" ");
								}
							}
						}
				}
				
				System.out.println("Estamos en: "+x+","+y+" mapa: "+mapa[x][y]);
				if(radar.get(12)==2) {
					System.out.println("Encontrado en el punto:("+x+","+y+")");
					moverse = false;
				} else {
					if (bateria < 10) System.out.println("Repostando "+refuel()); else System.out.println("Nos movemos "+move(think()));
				}
			}
		} catch (InterruptedException | JSONException e) {
			e.printStackTrace();
		}
		
		logout();
	}
	
	@Override
	public void finalize() {
		System.out.println("Agente("+this.getName()+") Terminando");
		System.out.println(movimientos);
        super.finalize();
	}

}