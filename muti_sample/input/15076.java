class XProtocol {
    private final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XProtocol");
    private Map<XAtom, XAtomList> atomToList = new HashMap<XAtom, XAtomList>();
    private Map<XAtom, Long> atomToAnchor = new HashMap<XAtom, Long>();
    volatile boolean firstCheck = true;
    boolean checkProtocol(XAtom listName, XAtom protocol) {
        XAtomList protocols = atomToList.get(listName);
        if (protocols != null) {
            return protocols.contains(protocol);
        }
        protocols = listName.getAtomListPropertyList(XToolkit.getDefaultRootWindow());
        atomToList.put(listName, protocols);
        try {
            return protocols.contains(protocol);
        } finally {
            if (firstCheck) {
                firstCheck = false;
                log.fine("{0}:{1} supports {2}", this, listName, protocols);
            }
        }
    }
    long checkAnchorImpl(XAtom anchorProp, long anchorType) {
        long root_xref, self_xref;
        XToolkit.awtLock();
        try {
            root_xref = anchorProp.get32Property(XToolkit.getDefaultRootWindow(),
                                                 anchorType);
        } finally {
            XToolkit.awtUnlock();
        }
        if (root_xref == 0) {
            return 0;
        }
        self_xref = anchorProp.get32Property(root_xref, anchorType);
        if (self_xref != root_xref) {
            return 0;
        }
        return self_xref;
    }
    public long checkAnchor(XAtom anchorProp, long anchorType) {
        Long val = atomToAnchor.get(anchorProp);
        if (val != null) {
            return val.longValue();
        }
        long res = checkAnchorImpl(anchorProp, anchorType);
        atomToAnchor.put(anchorProp, res);
        return res;
    }
    public long checkAnchor(XAtom anchorProp, XAtom anchorType) {
        return checkAnchor(anchorProp, anchorType.getAtom());
    }
}
