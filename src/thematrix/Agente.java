package thematrix;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

public class Agente extends SingleAgent {
    // Estados internos del agente
    private final int INTERROGAR=0, ESCUCHAR=1, FIN=2;
    // Estado actual del agente
    private int status;
    // Mensajes
    private ACLMessage inbox, outbox;
    // Control de la vida del agente
    private boolean exit;
    
    public Agente(AgentID aid) throws Exception  {
        super(aid);
    }
    
    @Override
    public void init()  {
        System.out.println("Agente("+this.getName()+") Iniciando");
        status = INTERROGAR;
        inbox = null;
        outbox = null;
        exit = false;        
    }
    
    @Override
    public void execute()  {
        String msgaux;
        System.out.println("Agente("+this.getName()+") Ejecución");
        while (!exit)  {
            switch(status)  {
                case INTERROGAR:
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    // Es necesario saber el nombre del agente al mandar
                    // un mensaje por primera vez. También se puede pasar por
                    // parámetro al constructor
                    outbox.setReceiver(new AgentID("Neo"));
                    if (Math.random()>0.3)  {
                        msgaux="Pregunta";
                        status = ESCUCHAR;
                    }
                    else  {
                        msgaux="Liberado";
                        status = FIN;                        
                    }
                    outbox.setContent(msgaux);
                    System.out.println("Agente("+this.getName()+") Interrogando ..."+msgaux);            
                    this.send(outbox);
                    break;
                case ESCUCHAR:
                    System.out.println("Agente("+this.getName()+") Esperando respuesta");
                    boolean repetir=true;
                    while (repetir)  {
                        try {
                            inbox = receiveACLMessage();
                            if (inbox.getContent().equals("Respuesta")) { 
                                status = INTERROGAR;
                                repetir=false;
                            }
                        } catch (InterruptedException ex) {
                            System.err.println("Agente("+this.getName()+") Error de comunicación");
                            repetir=false;
                            exit = true;
                        }                        
                    }
                    break;
                case FIN:
                    // En realidad este estado es aparentemente innecesario
                    System.out.println("Agente("+this.getName()+") Terminando ejecución");                   
                    exit = true;
                    break;
            }
        }
    }
    
    @Override
    public void finalize()  {
        System.out.println("Agente("+this.getName()+") Terminando");       
        super.finalize();
    }
}
