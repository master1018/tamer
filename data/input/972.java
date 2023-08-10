public class Connector {
    public static Match newMatch(int dim, long periodoTimer) throws RemoteException, MalformedURLException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        Match obj = null;
        obj = new MatchEngine(dim, periodoTimer);
        Naming.rebind("
        System.out.println("Match e' stato pubblicato");
        return obj;
    }
    public static Match connectMatch(String server) throws MalformedURLException, RemoteException, NotBoundException, RefusedConnectionException {
        Match match = (Match) Naming.lookup("
        if (match.acceptConnections()) return match; else throw new RefusedConnectionException("Unable to allocate free slots to play as a remote player.\n" + "This match has just the correct number of connected players");
    }
    public static void unBindMatch(String server) throws RemoteException, MalformedURLException, NotBoundException {
        Naming.unbind("
    }
    public static void publishController(RemoteController controller, String playerName) throws RemoteException, MalformedURLException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        String rmiObjName = "
        Naming.rebind(rmiObjName, controller);
    }
    public static RemoteController retrieveController(String server, String playerName) throws MalformedURLException, RemoteException, NotBoundException {
        return (RemoteController) Naming.lookup("
    }
    public static void unPublishController(String playerName) throws RemoteException, MalformedURLException, NotBoundException {
        String rmiObjName = "
        Naming.unbind(rmiObjName);
    }
}
