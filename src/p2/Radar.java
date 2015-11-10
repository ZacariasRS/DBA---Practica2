package src.p2;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 * 
 * @author Fernando Suárez Jiménez
 * 
 * Agente para recibir el estado del radar del servidor.
 */
public class Radar extends SingleAgent {
	
	private JSONObject data;
	private boolean state;

	public Radar(AgentID aid) throws Exception {
		super(aid);
		data = null;
		state = false;
	}
	
	@Override
	public void execute () {
		ACLMessage inbox, outbox;
		JSONObject outMsg;
		String sender;
		
		while (true) {
			try {
				inbox = receiveACLMessage();
				sender = inbox.getSender().name;
				if (sender.equals(RescueBots.nBot)) {
					if (state) {
						outMsg = data;;
						state = false;
					} else {
						outMsg = new JSONObject();
						outMsg.put("error", "no radar data");
					}
					outbox = new ACLMessage();
					outbox.setSender(this.getAid());
					outbox.setReceiver(new AgentID(RescueBots.nBot));
					outbox.setContent(outMsg.toString());
					this.send(outbox);
				} else {
					data = new JSONObject(inbox.getContent());
					state = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
