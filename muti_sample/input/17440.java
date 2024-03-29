public abstract class RepositoryIdFactory
{
    private static final RepIdDelegator_1_3 legacyDelegator
        = new RepIdDelegator_1_3();
    private static final RepIdDelegator_1_3_1 ladybirdDelegator
        = new RepIdDelegator_1_3_1();
    private static final RepIdDelegator currentDelegator
        = new RepIdDelegator();
    public static RepositoryIdStrings getRepIdStringsFactory()
    {
        return currentDelegator;
    }
    public static RepositoryIdStrings getRepIdStringsFactory(ORB orb)
    {
        if (orb != null) {
            switch (orb.getORBVersion().getORBType()) {
                case ORBVersion.NEWER:
                case ORBVersion.FOREIGN:
                case ORBVersion.JDK1_3_1_01:
                    return currentDelegator;
                case ORBVersion.OLD:
                    return legacyDelegator;
                case ORBVersion.NEW:
                    return ladybirdDelegator;
                default:
                    return currentDelegator;
            }
        } else
            return currentDelegator;
    }
    public static RepositoryIdUtility getRepIdUtility()
    {
        return currentDelegator;
    }
    public static RepositoryIdUtility getRepIdUtility(ORB orb)
    {
        if (orb != null) {
            switch (orb.getORBVersion().getORBType()) {
                case ORBVersion.NEWER:
                case ORBVersion.FOREIGN:
                case ORBVersion.JDK1_3_1_01:
                    return currentDelegator;
                case ORBVersion.OLD:
                    return legacyDelegator;
                case ORBVersion.NEW:
                    return ladybirdDelegator;
                default:
                    return currentDelegator;
            }
        } else
            return currentDelegator;
    }
}
