package thematrix;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class TheMatrix {

    public static void main(String[] args) {
        Rebelde Anderson=null;
        Agente Smith=null;

        AgentsConnection.connect("localhost",5672, "test", "guest", "guest", false);
        try {
            Anderson = new Rebelde(new AgentID("Neo"));
            Smith = new Agente(new AgentID("Smith"));
        } catch (Exception ex) {
            System.err.println("Error creando agentes");
            System.exit(1);
        }
        Anderson.start();
        Smith.start();
    }
}
