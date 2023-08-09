public final class SigIObjectIdentifiers
{
    private SigIObjectIdentifiers()
    {
    }
    public final static DERObjectIdentifier id_sigi = new DERObjectIdentifier("1.3.36.8");
    public final static DERObjectIdentifier id_sigi_kp = new DERObjectIdentifier(id_sigi + ".2");
    public final static DERObjectIdentifier id_sigi_cp = new DERObjectIdentifier(id_sigi + ".1");
    public final static DERObjectIdentifier id_sigi_on = new DERObjectIdentifier(id_sigi + ".4");
    public static final DERObjectIdentifier id_sigi_kp_directoryService = new DERObjectIdentifier(id_sigi_kp + ".1");
    public static final DERObjectIdentifier id_sigi_on_personalData = new DERObjectIdentifier(id_sigi_on + ".1");
    public static final DERObjectIdentifier id_sigi_cp_sigconform = new DERObjectIdentifier(id_sigi_cp + ".1");
}
