public class SimpleStandard
    extends NotificationBroadcasterSupport
    implements SimpleStandardMBean {
    public String getState() {
        return state;
    }
    public void setState(String s) {
        state = s;
        nbChanges++;
    }
    public int getNbChanges() {
        return nbChanges;
    }
    public void reset() {
        AttributeChangeNotification acn =
            new AttributeChangeNotification(this,
                                            0,
                                            0,
                                            "NbChanges reset",
                                            "NbChanges",
                                            "Integer",
                                            new Integer(nbChanges),
                                            new Integer(0));
        state = "initial state";
        nbChanges = 0;
        nbResets++;
        sendNotification(acn);
    }
    public int getNbResets() {
        return nbResets;
    }
    private String state = "initial state";
    private int nbChanges = 0;
    private int nbResets = 0;
}
