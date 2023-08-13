public class BufferManagerFactory
{
    public static final int GROW    = 0;
    public static final int COLLECT = 1;
    public static final int STREAM  = 2;
    public static BufferManagerRead newBufferManagerRead(
            GIOPVersion version, byte encodingVersion, ORB orb) {
        if (encodingVersion != Message.CDR_ENC_VERSION) {
            return new BufferManagerReadGrow(orb);
        }
        switch (version.intValue())
        {
            case GIOPVersion.VERSION_1_0:
                return new BufferManagerReadGrow(orb);
            case GIOPVersion.VERSION_1_1:
            case GIOPVersion.VERSION_1_2:
                return new BufferManagerReadStream(orb);
            default:
                throw new INTERNAL("Unknown GIOP version: "
                                   + version);
        }
    }
    public static BufferManagerRead newBufferManagerRead(
            int strategy, byte encodingVersion, ORB orb) {
        if (encodingVersion != Message.CDR_ENC_VERSION) {
            if (strategy != BufferManagerFactory.GROW) {
                ORBUtilSystemException wrapper =
                    ORBUtilSystemException.get((ORB)orb,
                                               CORBALogDomains.RPC_ENCODING);
                throw wrapper.invalidBuffMgrStrategy("newBufferManagerRead");
            }
            return new BufferManagerReadGrow(orb);
        }
        switch (strategy) {
            case BufferManagerFactory.GROW:
                return new BufferManagerReadGrow(orb);
            case BufferManagerFactory.COLLECT:
                throw new INTERNAL("Collect strategy invalid for reading");
            case BufferManagerFactory.STREAM:
                return new BufferManagerReadStream(orb);
            default:
                throw new INTERNAL("Unknown buffer manager read strategy: "
                                   + strategy);
        }
    }
    public static BufferManagerWrite newBufferManagerWrite(
            int strategy, byte encodingVersion, ORB orb) {
        if (encodingVersion != Message.CDR_ENC_VERSION) {
            if (strategy != BufferManagerFactory.GROW) {
                ORBUtilSystemException wrapper =
                    ORBUtilSystemException.get((ORB)orb,
                                               CORBALogDomains.RPC_ENCODING);
                throw wrapper.invalidBuffMgrStrategy("newBufferManagerWrite");
            }
            return new BufferManagerWriteGrow(orb);
        }
        switch (strategy) {
            case BufferManagerFactory.GROW:
                return new BufferManagerWriteGrow(orb);
            case BufferManagerFactory.COLLECT:
                return new BufferManagerWriteCollect(orb);
            case BufferManagerFactory.STREAM:
                return new BufferManagerWriteStream(orb);
            default:
                throw new INTERNAL("Unknown buffer manager write strategy: "
                                   + strategy);
        }
    }
    public static BufferManagerWrite newBufferManagerWrite(
        GIOPVersion version, byte encodingVersion, ORB orb) {
        if (encodingVersion != Message.CDR_ENC_VERSION) {
            return new BufferManagerWriteGrow(orb);
        }
        return BufferManagerFactory.newBufferManagerWrite(
            orb.getORBData().getGIOPBuffMgrStrategy(version),
            encodingVersion, orb);
    }
    public static BufferManagerRead defaultBufferManagerRead(ORB orb) {
        return new BufferManagerReadGrow(orb);
    }
}
