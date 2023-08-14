public class Doctor
    extends Activatable
    implements Eliza, Retireable
{
    public Doctor(ActivationID id, MarshalledObject blah)
        throws RemoteException
    {
        super(id, 0);   
        System.out.println("Doctor constructed and exported");
    }
    private boolean asked = false;
    public String complain(String plaint)
    {
        System.out.println("Doctor will see you now");
        if (this.asked) {
            return ("DO GO ON?");
        } else {
            this.asked = true;
            return ("TELL ME ABOUT YOUR MOTHER");
        }
    }
    public void retire()
    {
        System.out.println("Doctor retiring");
        try {
            Activatable.inactive(this.getID());
            ActivationGroup.getSystem().unregisterObject(this.getID());
            (new HaraKiri()).start();
        } catch (UnknownObjectException uoe) {
            System.err.println("Exception in Activatable.inactive:");
            uoe.printStackTrace();
        } catch (ActivationException ae) {
            System.err.println("Exception in Activatable.inactive:");
            ae.printStackTrace();
        } catch (RemoteException re) {
            System.err.println("Exception in Activatable.inactive:");
            re.printStackTrace();
        }
    }
    private static class HaraKiri extends Thread
    {
        public HaraKiri() {
            super("Thread-of-Death");
        }
        public void run()
        {
            try {
                Thread.sleep(5000);
            } catch (Exception foo) {
            }
            System.exit(0);
        }
    }
}
