package p2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


public class RescueBots {

	static final String nServer = "Izar";
	static final String nBot = "botZ";
	static final String nBattery = "baterryZ";
	static final String nGPS = "gpsZ";
	static final String nScanner = "scannerZ";
	static final String nRadar = "radarZ";
	
	public static void main(String[] args) throws Exception {
		
		AgentsConnection.connect("isg2.ugr.es", 6000, nServer, "Boyero", "Pamuk", false);
		
		BotZ bot = new BotZ(new AgentID(nBot));
		Battery battery = new Battery(new AgentID(nBattery));
		GPS gps = new GPS(new AgentID(nGPS));
		Scanner scanner = new Scanner(new AgentID(nScanner));
		Radar radar = new Radar(new AgentID(nRadar));
		
		System.out.println("Agentes creados");
		
		battery.start();
		gps.start();
		scanner.start();
		radar.start();
		bot.start();
	}

}