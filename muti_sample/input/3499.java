public abstract class ActivationGroupInit
{
    public static void main(String args[])
    {
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            MarshalInputStream in = new MarshalInputStream(System.in);
            ActivationGroupID id  = (ActivationGroupID)in.readObject();
            ActivationGroupDesc desc = (ActivationGroupDesc)in.readObject();
            long incarnation = in.readLong();
            ActivationGroup.createGroup(id, desc, incarnation);
        } catch (Exception e) {
            System.err.println("Exception in starting ActivationGroupInit:");
            e.printStackTrace();
        } finally {
            try {
                System.in.close();
            } catch (Exception ex) {
            }
        }
    }
}
