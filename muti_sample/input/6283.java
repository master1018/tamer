public class ProfileDeferralMgr {
    public static boolean deferring = true;
    private static Vector<ProfileActivator> aVector;
    public static void registerDeferral(ProfileActivator pa) {
        if (!deferring) {
            return;
        }
        if (aVector == null) {
            aVector = new Vector<ProfileActivator>(3, 3);
        }
        aVector.addElement(pa);
        return;
    }
    public static void unregisterDeferral(ProfileActivator pa) {
        if (!deferring) {
            return;
        }
        if (aVector == null) {
            return;
        }
        aVector.removeElement(pa);
        return;
    }
    public static void activateProfiles() {
        int i, n;
        deferring = false;
        if (aVector == null) {
            return;
        }
        n = aVector.size();
        for (ProfileActivator pa : aVector) {
            try {
                pa.activate();
            } catch (ProfileDataException e) {
            }
        }
        aVector.removeAllElements();
        aVector = null;
        return;
    }
}
