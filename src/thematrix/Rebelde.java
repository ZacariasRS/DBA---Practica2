package src.thematrix;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Rebelde extends SingleAgent {
    // Estados internos del agente
    private final int INTERROGADO=0, RESPUESTA=1, FIN=2;
    // Estado actual del agente
    private int status;
    // Mensajes
    private ACLMessage inbox, outbox;
    // Control de la vida del agente
    private boolean exit;
    
    public Rebelde(AgentID aid) throws Exception  {
        super(aid);
    }
    
    @Override
    public void init()  {
        System.out.println("Rebelde("+this.getName()+") Iniciado");
        // Es indiferente hacer estas inicializaciones aquí o en el constructor
        status = INTERROGADO;
        inbox = null;
        outbox = null;
        exit = false;        
    }
    
    @Override
    public void execute()  {
        System.out.println("Rebelde("+this.getName()+") Ejecución");
        while (!exit)  {
            switch(status)  {
                case INTERROGADO:
                    System.out.println("Rebelde("+this.getName()+") Esperando pregunta");
                    try {
                           inbox = receiveACLMessage();
                           if (inbox.getContent().equals("Pregunta")) 
                               status = RESPUESTA;
                           if (inbox.getContent().equals("Liberado"))
                               status = FIN;
                       } catch (InterruptedException ex) {
                           System.err.println("Rebelde("+this.getName()+") Error de comunicación");
                           exit=true;
                       }
                    break;
                case RESPUESTA:
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    outbox.setReceiver(inbox.getSender());
                    // Siempre responde bien. Pensar qué pasaría si no lo hiciese
                    outbox.setContent("Respuesta");
                    System.out.println("Rebelde("+this.getName()+") Responde ");
                    this.send(outbox);
                    status = INTERROGADO;
                    break;
                case FIN:
                    // En realidad este estado es aparentemente innecesario
                    System.out.println("Rebelde("+this.getName()+") Terminando ejecución");                   
                    exit = true;
                    break;
            }
        }
    }
    
    @Override
    public void finalize()  {
        // Espacio para hacer un shutdown ordenado del agente
        System.out.println("Rebelde("+this.getName()+") Terminado");
        super.finalize();
    }
}
