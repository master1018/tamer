public class AgentServerImpl
    extends UnicastRemoteObject
    implements AgentServer
{
    public AgentServerImpl() throws RemoteException {
    }
    public static void main(String args[]) {
        try {
            AgentServerImpl server = new AgentServerImpl();
            Naming.rebind("/AgentServer", server);
            System.out.println("Ready to receive agents.");
                System.err.println("DTI_DoneInitializing");
        } catch (Exception e) {
                System.err.println("DTI_Error");
            System.err.println("Did not establish server");
            e.printStackTrace();
        }
    }
    public synchronized void accept(Agent agent)
        throws RemoteException 
    {
        Thread t;
        t = new Thread(agent);
        System.out.println("Agent Accepted: " + t);
        t.start();
    }
    public synchronized void returnHome(Agent agent)
        throws RemoteException 
    {
        Enumeration info = null;
        boolean bErrorsOccurred = false;
        info = agent.getInfo().elements();
        System.out.println("Collected information:");
        while (info.hasMoreElements()) {
            System.out.println("     " + (String) info.nextElement());
        }
        System.out.println("\nErrors:");
        System.out.println(agent.getErrors());
        if(!(agent.getErrors()).equals(""))
                bErrorsOccurred = true;
        if(bErrorsOccurred)
    {
                System.err.println("DTI_Error");
                System.err.println("DTI_DoneExecuting");
        }
        else
    {
                System.err.println("DTI_DoneExecuting");
    }
        }
}
