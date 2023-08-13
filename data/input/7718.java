class CryptoPermission extends java.security.Permission {
    private static final long serialVersionUID = 8987399626114087514L;
    private String alg;
    private int maxKeySize = Integer.MAX_VALUE; 
    private String exemptionMechanism = null;
    private AlgorithmParameterSpec algParamSpec = null;
    private boolean checkParam = false; 
    static final String ALG_NAME_WILDCARD = "*";
    CryptoPermission(String alg) {
        super(null);
        this.alg = alg;
    }
    CryptoPermission(String alg, int maxKeySize) {
        super(null);
        this.alg = alg;
        this.maxKeySize = maxKeySize;
    }
    CryptoPermission(String alg,
                     int maxKeySize,
                     AlgorithmParameterSpec algParamSpec) {
        super(null);
        this.alg = alg;
        this.maxKeySize = maxKeySize;
        this.checkParam = true;
        this.algParamSpec = algParamSpec;
    }
    CryptoPermission(String alg,
                     String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
    }
    CryptoPermission(String alg,
                     int maxKeySize,
                     String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
        this.maxKeySize = maxKeySize;
    }
    CryptoPermission(String alg,
                     int maxKeySize,
                     AlgorithmParameterSpec algParamSpec,
                     String exemptionMechanism) {
        super(null);
        this.alg = alg;
        this.exemptionMechanism = exemptionMechanism;
        this.maxKeySize = maxKeySize;
        this.checkParam = true;
        this.algParamSpec = algParamSpec;
    }
    public boolean implies(Permission p) {
        if (!(p instanceof CryptoPermission))
            return false;
        CryptoPermission cp = (CryptoPermission)p;
        if ((!alg.equalsIgnoreCase(cp.alg)) &&
            (!alg.equalsIgnoreCase(ALG_NAME_WILDCARD))) {
            return false;
        }
        if (cp.maxKeySize <= this.maxKeySize) {
            if (!impliesParameterSpec(cp.checkParam, cp.algParamSpec)) {
                return false;
            }
            if (impliesExemptionMechanism(cp.exemptionMechanism)) {
                return true;
            }
        }
        return false;
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CryptoPermission))
            return false;
        CryptoPermission that = (CryptoPermission) obj;
        if (!(alg.equalsIgnoreCase(that.alg)) ||
            (maxKeySize != that.maxKeySize)) {
            return false;
        }
        if (this.checkParam != that.checkParam) {
            return false;
        }
        return (equalObjects(this.exemptionMechanism,
                             that.exemptionMechanism) &&
                equalObjects(this.algParamSpec,
                             that.algParamSpec));
    }
    public int hashCode() {
        int retval = alg.hashCode();
        retval ^= maxKeySize;
        if (exemptionMechanism != null) {
            retval ^= exemptionMechanism.hashCode();
        }
        if (checkParam) retval ^= 100;
        if (algParamSpec != null) {
            retval ^= algParamSpec.hashCode();
        }
        return retval;
    }
    public String getActions()
    {
        return null;
    }
    public PermissionCollection newPermissionCollection() {
        return new CryptoPermissionCollection();
    }
    final String getAlgorithm() {
        return alg;
    }
    final String getExemptionMechanism() {
        return exemptionMechanism;
    }
    final int getMaxKeySize() {
        return maxKeySize;
    }
    final boolean getCheckParam() {
        return checkParam;
    }
    final AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return algParamSpec;
    }
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        buf.append("(CryptoPermission " + alg + " " + maxKeySize);
        if (algParamSpec != null) {
            if (algParamSpec instanceof RC2ParameterSpec) {
                buf.append(" , effective " +
                    ((RC2ParameterSpec)algParamSpec).getEffectiveKeyBits());
            } else if (algParamSpec instanceof RC5ParameterSpec) {
                buf.append(" , rounds " +
                    ((RC5ParameterSpec)algParamSpec).getRounds());
            }
        }
        if (exemptionMechanism != null) { 
            buf.append(" " + exemptionMechanism);
        }
        buf.append(")");
        return buf.toString();
    }
    private boolean impliesExemptionMechanism(String exemptionMechanism) {
        if (this.exemptionMechanism == null) {
            return true;
        }
        if (exemptionMechanism == null) {
            return false;
        }
        if (this.exemptionMechanism.equals(exemptionMechanism)) {
            return true;
        }
        return false;
    }
    private boolean impliesParameterSpec(boolean checkParam,
                                         AlgorithmParameterSpec algParamSpec) {
        if ((this.checkParam) && checkParam) {
            if (algParamSpec == null) {
                return true;
            } else if (this.algParamSpec == null) {
                return false;
            }
            if (this.algParamSpec.getClass() != algParamSpec.getClass()) {
                return false;
            }
            if (algParamSpec instanceof RC2ParameterSpec) {
                if (((RC2ParameterSpec)algParamSpec).getEffectiveKeyBits() <=
                    ((RC2ParameterSpec)
                     (this.algParamSpec)).getEffectiveKeyBits()) {
                    return true;
                }
            }
            if (algParamSpec instanceof RC5ParameterSpec) {
                if (((RC5ParameterSpec)algParamSpec).getRounds() <=
                    ((RC5ParameterSpec)this.algParamSpec).getRounds()) {
                    return true;
                }
            }
            if (algParamSpec instanceof PBEParameterSpec) {
                if (((PBEParameterSpec)algParamSpec).getIterationCount() <=
                    ((PBEParameterSpec)this.algParamSpec).getIterationCount()) {
                    return true;
                }
            }
            if (this.algParamSpec.equals(algParamSpec)) {
                return true;
            }
            return false;
        } else if (this.checkParam) {
            return false;
        } else {
            return true;
        }
    }
    private boolean equalObjects(Object obj1, Object obj2) {
        if (obj1 == null) {
            return (obj2 == null ? true : false);
        }
        return obj1.equals(obj2);
    }
}
final class CryptoPermissionCollection extends PermissionCollection
implements Serializable {
    private static final long serialVersionUID = -511215555898802763L;
    private Vector permissions;
    CryptoPermissionCollection() {
        permissions = new Vector(3);
    }
    public void add(Permission permission)
    {
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission " +
                                        "to a readonly PermissionCollection");
        if (!(permission instanceof CryptoPermission))
            return;
        permissions.addElement(permission);
    }
    public boolean implies(Permission permission) {
        if (!(permission instanceof CryptoPermission))
            return false;
        CryptoPermission cp = (CryptoPermission)permission;
        Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            CryptoPermission x = (CryptoPermission) e.nextElement();
            if (x.implies(cp)) {
                return true;
            }
        }
        return false;
    }
    public Enumeration elements()
    {
        return permissions.elements();
    }
}
