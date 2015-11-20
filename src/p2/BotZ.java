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
	ArrayList<ArrayList<Integer>> mapa;
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
		mapa = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> aux = new ArrayList<Integer>();
		for (int i=0;i<500;i++) {
			aux.add(0);
		}
		for (int i=0;i<500;i++) {
			mapa.add(aux);
		}
		//
		followWall = false;
	}
	
	
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
				mapa.get(y).set(x,3);
				Map.getInstance().setMapComposition(x, y, Map.MapState.ROBOT);
				received = true;
			}
		}
	}
	
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
	
	public void updateAll() {
		for (int i=0;i<25;i++) {
			if(i!=12) {
				int xaux = (i%5)-2;
				//int aux = i/5;
				//int f = aux%1;
				int yaux = i/5;
				int z = radar.get(i);
				int actualx = x-xaux;
				int actualy = y-yaux;
				if (actualx >= 0 && actualy >= 0) {
					if (mapa.get(actualy).get(actualx) != 3) {
						mapa.get(y-yaux).set(x-xaux,z);
					}
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
	
	public String think() {
		String res = null;
		
		int mejor = posicionMenor();
		
		if (followWall) {
			System.out.println("Estamos en follow. wanted = "+wantedAction+" lastAction="+lastAction);
			switch (wantedAction) {
			case "moveN": 	if (radar.get(7) !=1) {
						  		res = wantedAction;
						  		followWall = false;
							} else {
								res = lastAction;
							}
						  	break;
			case "moveNW": 	if (radar.get(6) !=1) {
			  					res = wantedAction;
			  					followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveNE":  if (radar.get(8) !=1) {
			  					res = wantedAction;
			  					followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveS": 	if (radar.get(17) !=1) {
								res = wantedAction;
								followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveSW":  if (radar.get(16) !=1) {
								res = wantedAction;
								followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveSE": 	if (radar.get(18) !=1) {
								res = wantedAction;
								followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveW": 	if (radar.get(11) !=1) {
								res = wantedAction;
								followWall = false;
							} else {
								res = lastAction;
							}
							break;
			case "moveE": 	if (radar.get(13) !=1) {
									res = wantedAction;
									followWall = false;
								} else {
									res = lastAction;
								}
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
											wantedAction = res;
											res = lastAction;
											followWall = true;
										} else if (scanner.get(6) <= scanner.get(8) && radar.get(6) !=1 && mapa.get(y-1).get(x-1) !=3) res = "moveNW";
										else if (scanner.get(8) <= scanner.get(6) && radar.get(8) !=1 && mapa.get(y-1).get(x+1) !=3) res = "moveNE";
										else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
										else if (radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
									}
								  	break;
					case "moveNW": 	if (radar.get(6) !=1) {
					  					
					  				} else {
					  					if (radar.get(7) == 1 && radar.get(11) == 1) {
					  						if (scanner.get(8) <= scanner.get(17) && radar.get(8) !=1 && mapa.get(y-1).get(x+1) !=3) {
					  							res = "moveNE";
					  						}
					  						else if (scanner.get(16) <= scanner.get(17) && radar.get(16) !=1 && mapa.get(y+1).get(x-1) != 3) {
					  							res = "moveSW";
					  						}
					  						else if ((scanner.get(9)) <= scanner.get(17) && radar.get(9) !=1 && mapa.get(y).get(x+1) !=3) {
					  							res = "moveE";
					  						} else if(scanner.get(13) <= scanner.get(17) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) {
					  							res = "moveE";
					  						} else {
					  							wantedAction = res;
					  							res = "moveS";
					  							followWall = true;
					  						}
										}
					  							else if (scanner.get(11) <= scanner.get(7) && radar.get(11) !=1/* && mapa.get(y).get(x-1) !=3*/) res = "moveW";
					  							else if (scanner.get(10) <= scanner.get(2) && radar.get(11) !=1/* && mapa.get(y).get(x-1) !=3*/) res = "moveW";
												else if (radar.get(7) != 1/* && mapa.get(y-1).get(x) !=3*/) res = "moveN";
												else if (radar.get(8) !=1/* && mapa.get(y-1).get(x+1) !=3*/) res = "moveNE";
												else if (radar.get(13) !=1/* && mapa.get(y).get(x+1) !=3*/) res = "moveE";
					  				}
					  				break;
					case "moveNE":  if (radar.get(8) !=1) {
					  				
									} else {
										if (radar.get(7) == 1 && radar.get(13) == 1) {
											if (scanner.get(6) <= scanner.get(17) && radar.get(6) !=1 && mapa.get(y-1).get(x-1) !=3) {
					  							res = "moveNW";
											}
											else if (scanner.get(18) <= scanner.get(17) && radar.get(18) !=1 && mapa.get(y+1).get(x+1) !=3) {
												res = "moveSE";
											}
					  						else if (scanner.get(5) <= scanner.get(17) && radar.get(5) !=1 && mapa.get(y).get(x-1) !=3) {
					  							res = "moveW";
					  						} else if(scanner.get(11) <= scanner.get(17) && radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) {
					  							res = "moveW";
					  						} else {
					  							wantedAction = res;
					  							res = "moveS";
					  							followWall = true;
					  						}
										} else if (scanner.get(13) <= scanner.get(7) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
										else if (scanner.get(14) <= scanner.get(2) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
												else if (radar.get(7) != 1 && mapa.get(y-1).get(x) !=3) res = "moveN";
												else if (radar.get(6) !=1 && mapa.get(y-1).get(x-1) !=3) res = "moveNW";
												else if (radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
					  				}
					  				break;
					case "moveS": 	if (radar.get(17) !=1) {
				
									} else {
										if (radar.get(16) == 1 && radar.get(18) == 1) {
											wantedAction = res;
											res = lastAction;
											followWall = true;
										} else if (scanner.get(16) <= scanner.get(18) && radar.get(16) !=1 && mapa.get(y+1).get(x-1) !=3) res = "moveSW";
										else if (scanner.get(18) <= scanner.get(16) && radar.get(18) !=1 && mapa.get(y+1).get(x+1) !=3) res = "moveSE";
										else if (scanner.get(11) <= scanner.get(13) && radar.get(11) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
										else if (radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
									}
					  				break;
					case "moveSW":  if (radar.get(16) !=1) {
					  					
									} else {
										if (radar.get(11) == 1 && radar.get(17) == 1) {
											if (scanner.get(18) <= scanner.get(7) && radar.get(18) !=1 && mapa.get(y+1).get(x+1) !=3) {
					  							res = "moveSE";
											}
											else if (scanner.get(6) <= scanner.get(6) && radar.get(6) !=1 && mapa.get(y-1).get(x-1) !=3) {
												res = "moveNW";
											}
					  						else if (scanner.get(15) <= scanner.get(7) && radar.get(15) !=1 && mapa.get(y).get(x+1) !=3) {
					  							res = "moveE";
					  						} else if(scanner.get(13) <= scanner.get(7) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) {
					  							res = "moveE";
					  						} else {
					  							wantedAction = res;
					  							res = "moveS";
					  							followWall = true;
					  						}
										} else if (scanner.get(11) <= scanner.get(17) && radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
										else if (scanner.get(10) <= scanner.get(22) && radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
												else if (radar.get(17) != 1 && mapa.get(y+1).get(x) !=3) res = "moveS";
												else if (radar.get(16) !=1 && mapa.get(y+1).get(x+1) !=3) res = "moveSE";
												else if (radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
					  				}
					  				break;
					case "moveSE": 	if (radar.get(18) !=1) {
									} else {
										if (radar.get(13) == 1 && radar.get(17) == 1) {
											if (scanner.get(16) <= scanner.get(7) && radar.get(16) !=1 && mapa.get(y+1).get(x-1) !=3) {
					  							res = "moveSW";
											}
											else if (scanner.get(8) <= scanner.get(7) && radar.get(8) !=1 && mapa.get(y-1).get(x+1) !=3) {
												res = "moveNE";
											}
					  						else if (scanner.get(15) <= scanner.get(7) && radar.get(15) !=1 && mapa.get(y).get(x-1) !=3) {
					  							res = "moveW";
					  						} else if(scanner.get(11) <= scanner.get(7) && radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) {
					  							res = "moveW";
					  						} else {
					  							wantedAction = res;
					  							res = "moveS";
					  							followWall = true;
					  						}
										} else if (scanner.get(13) <= scanner.get(17) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
										else if (scanner.get(14) <= scanner.get(22) && radar.get(13) !=1 && mapa.get(y).get(x+1) !=3) res = "moveE";
												else if (radar.get(17) != 1 && mapa.get(y+1).get(x) !=3) res = "moveS";
												else if (radar.get(16) !=1 && mapa.get(y+1).get(x-1) !=3) res = "moveSW";
												else if (radar.get(11) !=1 && mapa.get(y).get(x-1) !=3) res = "moveW";
					  				}
					  				break;
					case "moveW": 	if (radar.get(11) !=1) {
									
									} else {
										if (radar.get(6) == 1 && radar.get(16) == 1) {
											wantedAction = res;
											res = lastAction;
											followWall = true;
										} else if (scanner.get(16) <= scanner.get(6) && radar.get(16) !=1 && mapa.get(y+1).get(x-1) !=3) res = "moveSW";
										else if (scanner.get(6) <= scanner.get(16) && radar.get(6) !=1 && mapa.get(y-1).get(x-1) !=3) res = "moveNW";
										else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1 && mapa.get(y+1).get(x) !=3) res = "moveS";
										else if (radar.get(7) !=1 && mapa.get(y-1).get(x) !=3) res = "moveN";
									}
									break;
					case "moveE": 	if (radar.get(13) !=1) {
									
									} else {
										if (radar.get(8) == 1 && radar.get(18) == 1) {
											wantedAction = res;
											res = lastAction;
											followWall = true;
										} else if (scanner.get(18) <= scanner.get(8) && radar.get(18) !=1 && mapa.get(y+1).get(x+1) !=3) res = "moveSE";
										else if (scanner.get(8) <= scanner.get(18) && radar.get(8) !=1 && mapa.get(y-1).get(x+1) !=3) res = "moveNE";
										else if (scanner.get(17) <= scanner.get(7) && radar.get(17) !=1 && mapa.get(y+1).get(x) !=3) res = "moveS";
										else if (radar.get(7) !=1 && mapa.get(y-1).get(x) !=3) res = "moveN";
										else if (radar.get(17) !=1 && mapa.get(y+1).get(x) !=3) res = "moveS";
									}
									break;
				}
			//}
		}
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
		System.out.println(movimiento);
		lastAction = movimiento;
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
		lastAction="refuel";
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
				System.out.println("Scanner:");
				for (int i=0;i<5;i++) {
					System.out.println(scanner.get((i*5)+0)+" "+scanner.get((i*5)+1)+" "+scanner.get((i*5)+2)+" "+scanner.get((i*5)+3)+" "+scanner.get((i*5)+4));
				}
				
				System.out.println("\nRadar:");
				for (int i=0;i<5;i++) {
					System.out.println(radar.get((i*5)+0)+" "+radar.get((i*5)+1)+" "+radar.get((i*5)+2)+" "+radar.get((i*5)+3)+" "+radar.get((i*5)+4));
				}
				
				System.out.println("Estamos en: "+x+","+y+" mapa: "+mapa.get(y).get(x));
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