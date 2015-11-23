package p2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


public class RescueBots {

	static final String nServer = "Izar";
	static final String nBot = "botZZ";
	static final String nBattery = "bateryZZ";
	static final String nGPS = "gpsZZ";
	static final String nScanner = "scannerZZ";
	static final String nRadar = "radarZZ";
	
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