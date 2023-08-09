final class CryptoPermissions extends PermissionCollection
implements Serializable {
    private static final long serialVersionUID = 4946547168093391015L;
    private Hashtable perms;
    CryptoPermissions() {
        perms = new Hashtable(7);
    }
    void load(InputStream in)
        throws IOException, CryptoPolicyParser.ParsingException {
        CryptoPolicyParser parser = new CryptoPolicyParser();
        parser.read(new BufferedReader(new InputStreamReader(in, "UTF-8")));
        CryptoPermission[] parsingResult = parser.getPermissions();
        for (int i = 0; i < parsingResult.length; i++) {
            this.add(parsingResult[i]);
        }
    }
    boolean isEmpty() {
        return perms.isEmpty();
    }
    public void add(Permission permission) {
        if (isReadOnly())
            throw new SecurityException("Attempt to add a Permission " +
                                        "to a readonly CryptoPermissions " +
                                        "object");
        if (!(permission instanceof CryptoPermission))
            return;
        CryptoPermission cryptoPerm = (CryptoPermission)permission;
        PermissionCollection pc =
                        getPermissionCollection(cryptoPerm);
        pc.add(cryptoPerm);
        String alg = cryptoPerm.getAlgorithm();
        if (!perms.containsKey(alg)) {
            perms.put(alg, pc);
        }
    }
    public boolean implies(Permission permission) {
        if (!(permission instanceof CryptoPermission)) {
            return false;
        }
        CryptoPermission cryptoPerm = (CryptoPermission)permission;
        PermissionCollection pc =
            getPermissionCollection(cryptoPerm.getAlgorithm());
        return pc.implies(cryptoPerm);
    }
    public Enumeration elements() {
        return new PermissionsEnumerator(perms.elements());
    }
    CryptoPermissions getMinimum(CryptoPermissions other) {
        if (other == null) {
            return null;
        }
        if (this.perms.containsKey(CryptoAllPermission.ALG_NAME)) {
            return other;
        }
        if (other.perms.containsKey(CryptoAllPermission.ALG_NAME)) {
            return this;
        }
        CryptoPermissions ret = new CryptoPermissions();
        PermissionCollection thatWildcard =
            (PermissionCollection)other.perms.get(
                                        CryptoPermission.ALG_NAME_WILDCARD);
        int maxKeySize = 0;
        if (thatWildcard != null) {
            maxKeySize = ((CryptoPermission)
                    thatWildcard.elements().nextElement()).getMaxKeySize();
        }
        Enumeration thisKeys = this.perms.keys();
        while (thisKeys.hasMoreElements()) {
            String alg = (String)thisKeys.nextElement();
            PermissionCollection thisPc =
                (PermissionCollection)this.perms.get(alg);
            PermissionCollection thatPc =
                (PermissionCollection)other.perms.get(alg);
            CryptoPermission[] partialResult;
            if (thatPc == null) {
                if (thatWildcard == null) {
                    continue;
                }
                partialResult = getMinimum(maxKeySize, thisPc);
            } else {
                partialResult = getMinimum(thisPc, thatPc);
            }
            for (int i = 0; i < partialResult.length; i++) {
                ret.add(partialResult[i]);
            }
        }
        PermissionCollection thisWildcard =
            (PermissionCollection)this.perms.get(
                                      CryptoPermission.ALG_NAME_WILDCARD);
        if (thisWildcard == null) {
            return ret;
        }
        maxKeySize =
            ((CryptoPermission)
                    thisWildcard.elements().nextElement()).getMaxKeySize();
        Enumeration thatKeys = other.perms.keys();
        while (thatKeys.hasMoreElements()) {
            String alg = (String)thatKeys.nextElement();
            if (this.perms.containsKey(alg)) {
                continue;
            }
            PermissionCollection thatPc =
                (PermissionCollection)other.perms.get(alg);
            CryptoPermission[] partialResult;
            partialResult = getMinimum(maxKeySize, thatPc);
            for (int i = 0; i < partialResult.length; i++) {
                ret.add(partialResult[i]);
            }
        }
        return ret;
    }
    private CryptoPermission[] getMinimum(PermissionCollection thisPc,
                                          PermissionCollection thatPc) {
        Vector permVector = new Vector(2);
        Enumeration thisPcPermissions = thisPc.elements();
        while (thisPcPermissions.hasMoreElements()) {
            CryptoPermission thisCp =
                (CryptoPermission)thisPcPermissions.nextElement();
            Enumeration thatPcPermissions = thatPc.elements();
            while (thatPcPermissions.hasMoreElements()) {
                CryptoPermission thatCp =
                    (CryptoPermission)thatPcPermissions.nextElement();
                if (thatCp.implies(thisCp)) {
                    permVector.addElement(thisCp);
                    break;
                }
                if (thisCp.implies(thatCp)) {
                    permVector.addElement(thatCp);
                }
            }
        }
        CryptoPermission[] ret = new CryptoPermission[permVector.size()];
        permVector.copyInto(ret);
        return ret;
    }
    private CryptoPermission[] getMinimum(int maxKeySize,
                                          PermissionCollection pc) {
        Vector permVector = new Vector(1);
        Enumeration enum_ = pc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp =
                (CryptoPermission)enum_.nextElement();
            if (cp.getMaxKeySize() <= maxKeySize) {
                permVector.addElement(cp);
            } else {
                if (cp.getCheckParam()) {
                    permVector.addElement(
                           new CryptoPermission(cp.getAlgorithm(),
                                                maxKeySize,
                                                cp.getAlgorithmParameterSpec(),
                                                cp.getExemptionMechanism()));
                } else {
                    permVector.addElement(
                           new CryptoPermission(cp.getAlgorithm(),
                                                maxKeySize,
                                                cp.getExemptionMechanism()));
                }
            }
        }
        CryptoPermission[] ret = new CryptoPermission[permVector.size()];
        permVector.copyInto(ret);
        return ret;
    }
    PermissionCollection getPermissionCollection(String alg) {
        if (perms.containsKey(CryptoAllPermission.ALG_NAME)) {
            return
                (PermissionCollection)(perms.get(CryptoAllPermission.ALG_NAME));
        }
        PermissionCollection pc = (PermissionCollection)perms.get(alg);
        if (pc == null) {
            pc = (PermissionCollection)perms.get(
                                       CryptoPermission.ALG_NAME_WILDCARD);
        }
        return pc;
    }
    private PermissionCollection getPermissionCollection(
                                          CryptoPermission cryptoPerm) {
        String alg = cryptoPerm.getAlgorithm();
        PermissionCollection pc = (PermissionCollection)perms.get(alg);
        if (pc == null) {
            pc = cryptoPerm.newPermissionCollection();
        }
        return pc;
    }
}
final class PermissionsEnumerator implements Enumeration {
    private Enumeration perms;
    private Enumeration permset;
    PermissionsEnumerator(Enumeration e) {
        perms = e;
        permset = getNextEnumWithMore();
    }
    public synchronized boolean hasMoreElements() {
        if (permset == null)
            return  false;
        if (permset.hasMoreElements())
            return true;
        permset = getNextEnumWithMore();
        return (permset != null);
    }
    public synchronized Object nextElement() {
        if (hasMoreElements()) {
            return permset.nextElement();
        } else {
            throw new NoSuchElementException("PermissionsEnumerator");
        }
    }
    private Enumeration getNextEnumWithMore() {
        while (perms.hasMoreElements()) {
            PermissionCollection pc =
                (PermissionCollection) perms.nextElement();
            Enumeration next = pc.elements();
            if (next.hasMoreElements())
                return next;
        }
        return null;
    }
}
