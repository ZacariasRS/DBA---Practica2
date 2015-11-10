package p2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


public class RescueBots {

	static final String nServer = "Izar";
	static final String nBot = "bot5";
	static final String nBattery = "baterry5";
	static final String nGPS = "gps5";
	static final String nScanner = "scanner5";
	static final String nRadar = "radar5";
	
	public static void main(String[] args) throws Exception {
		
		AgentsConnection.connect("isg2.ugr.es", 6000, nServer, "Boyero", "Pamuk", false);
		
		Bot bot = new Bot(new AgentID(nBot));
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