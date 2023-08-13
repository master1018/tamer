public class ShutdownThread extends Thread implements Serializable {
    Remote remoteObject = null;
    ActivationID activationID = null;
    public ShutdownThread(Remote remoteObject, ActivationID activationID) {
        remoteObject = remoteObject;
        activationID = activationID;
    }
    public void run() {
        try {
            Activatable.unexportObject(remoteObject, true);
            Activatable.inactive(activationID);
        } catch (Exception e) {
        }
    }
}
