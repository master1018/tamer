public class PrincipalName
    implements Cloneable {
    public static final int KRB_NT_UNKNOWN =   0;
    public static final int KRB_NT_PRINCIPAL = 1;
    public static final int KRB_NT_SRV_INST =  2;
    public static final int KRB_NT_SRV_HST =   3;
    public static final int KRB_NT_SRV_XHST =  4;
    public static final int KRB_NT_UID = 5;
    public static final String TGS_DEFAULT_SRV_NAME = "krbtgt";
    public static final int TGS_DEFAULT_NT = KRB_NT_SRV_INST;
    public static final char NAME_COMPONENT_SEPARATOR = '/';
    public static final char NAME_REALM_SEPARATOR = '@';
    public static final char REALM_COMPONENT_SEPARATOR = '.';
    public static final String NAME_COMPONENT_SEPARATOR_STR = "/";
    public static final String NAME_REALM_SEPARATOR_STR = "@";
    public static final String REALM_COMPONENT_SEPARATOR_STR = ".";
    private int nameType;
    private String[] nameStrings;  
    private Realm nameRealm;  
    private String salt = null;
    protected PrincipalName() {
    }
    public PrincipalName(String[] nameParts, int type)
        throws IllegalArgumentException, IOException {
        if (nameParts == null) {
            throw new IllegalArgumentException("Null input not allowed");
        }
        nameStrings = new String[nameParts.length];
        System.arraycopy(nameParts, 0, nameStrings, 0, nameParts.length);
        nameType = type;
        nameRealm = null;
    }
    public PrincipalName(String[] nameParts) throws IOException {
        this(nameParts, KRB_NT_UNKNOWN);
    }
    public Object clone() {
        try {
            PrincipalName pName = (PrincipalName) super.clone();
            if (nameStrings != null) {
                pName.nameStrings = nameStrings.clone();
            }
            if (nameRealm != null) {
                pName.nameRealm = (Realm)nameRealm.clone();
            }
            return pName;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Should never happen");
        }
    }
    public boolean equals(Object o) {
        if (o instanceof PrincipalName)
            return equals((PrincipalName)o);
        else
            return false;
    }
    public boolean equals(PrincipalName other) {
        if (!equalsWithoutRealm(other)) {
            return false;
        }
        if ((nameRealm != null && other.nameRealm == null) ||
            (nameRealm == null && other.nameRealm != null)) {
            return false;
        }
        if (nameRealm != null && other.nameRealm != null) {
            if (!nameRealm.equals(other.nameRealm)) {
                return false;
            }
        }
        return true;
    }
    boolean equalsWithoutRealm(PrincipalName other) {
        if (nameType != KRB_NT_UNKNOWN &&
            other.nameType != KRB_NT_UNKNOWN &&
            nameType != other.nameType)
            return false;
        if ((nameStrings != null && other.nameStrings == null) ||
            (nameStrings == null && other.nameStrings != null))
            return false;
        if (nameStrings != null && other.nameStrings != null) {
            if (nameStrings.length != other.nameStrings.length)
                return false;
            for (int i = 0; i < nameStrings.length; i++)
                if (!nameStrings[i].equals(other.nameStrings[i]))
                    return false;
        }
        return true;
    }
    public PrincipalName(DerValue encoding)
        throws Asn1Exception, IOException {
        nameRealm = null;
        DerValue der;
        if (encoding == null) {
            throw new IllegalArgumentException("Null input not allowed");
        }
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x00) {
            BigInteger bint = der.getData().getBigInteger();
            nameType = bint.intValue();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x01F) == 0x01) {
            DerValue subDer = der.getData().getDerValue();
            if (subDer.getTag() != DerValue.tag_SequenceOf) {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
            Vector<String> v = new Vector<>();
            DerValue subSubDer;
            while(subDer.getData().available() > 0) {
                subSubDer = subDer.getData().getDerValue();
                v.addElement(new KerberosString(subSubDer).toString());
            }
            if (v.size() > 0) {
                nameStrings = new String[v.size()];
                v.copyInto(nameStrings);
            } else {
                nameStrings = new String[] {""};
            }
        } else  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public static PrincipalName parse(DerInputStream data,
                                      byte explicitTag, boolean
                                      optional)
        throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) !=
                           explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        else {
            DerValue subDer = der.getData().getDerValue();
            return new PrincipalName(subDer);
        }
    }
    protected static String[] parseName(String name) {
        Vector<String> tempStrings = new Vector<>();
        String temp = name;
        int i = 0;
        int componentStart = 0;
        String component;
        while (i < temp.length()) {
            if (temp.charAt(i) == NAME_COMPONENT_SEPARATOR) {
                if (i > 0 && temp.charAt(i - 1) == '\\') {
                    temp = temp.substring(0, i - 1) +
                        temp.substring(i, temp.length());
                    continue;
                }
                else {
                    if (componentStart < i) {
                        component = temp.substring(componentStart, i);
                        tempStrings.addElement(component);
                    }
                    componentStart = i + 1;
                }
            } else
                if (temp.charAt(i) == NAME_REALM_SEPARATOR) {
                    if (i > 0 && temp.charAt(i - 1) == '\\') {
                        temp = temp.substring(0, i - 1) +
                            temp.substring(i, temp.length());
                        continue;
                    } else {
                        if (componentStart < i) {
                            component = temp.substring(componentStart, i);
                            tempStrings.addElement(component);
                        }
                        componentStart = i + 1;
                        break;
                    }
                }
            i++;
        }
        if (i == temp.length())
        if (componentStart < i) {
            component = temp.substring(componentStart, i);
            tempStrings.addElement(component);
        }
        String[] result = new String[tempStrings.size()];
        tempStrings.copyInto(result);
        return result;
    }
    public PrincipalName(String name, int type)
        throws RealmException {
        if (name == null) {
            throw new IllegalArgumentException("Null name not allowed");
        }
        String[] nameParts = parseName(name);
        Realm tempRealm = null;
        String realmString = Realm.parseRealmAtSeparator(name);
        if (realmString == null) {
            try {
                Config config = Config.getInstance();
                realmString = config.getDefaultRealm();
            } catch (KrbException e) {
                RealmException re =
                    new RealmException(e.getMessage());
                re.initCause(e);
                throw re;
            }
        }
        if (realmString != null)
            tempRealm = new Realm(realmString);
        switch (type) {
        case KRB_NT_SRV_HST:
            if (nameParts.length >= 2) {
                String hostName = nameParts[1];
                try {
                    String canonicalized = (InetAddress.getByName(hostName)).
                            getCanonicalHostName();
                    if (canonicalized.toLowerCase()
                            .startsWith(hostName.toLowerCase()+".")) {
                        hostName = canonicalized;
                    }
                } catch (UnknownHostException e) {
                }
                nameParts[1] = hostName.toLowerCase();
            }
            nameStrings = nameParts;
            nameType = type;
            String mapRealm =  mapHostToRealm(nameParts[1]);
            if (mapRealm != null) {
                nameRealm = new Realm(mapRealm);
            } else {
                nameRealm = tempRealm;
            }
            break;
        case KRB_NT_UNKNOWN:
        case KRB_NT_PRINCIPAL:
        case KRB_NT_SRV_INST:
        case KRB_NT_SRV_XHST:
        case KRB_NT_UID:
            nameStrings = nameParts;
            nameType = type;
            nameRealm = tempRealm;
            break;
        default:
            throw new IllegalArgumentException("Illegal name type");
        }
    }
    public PrincipalName(String name) throws RealmException {
        this(name, KRB_NT_UNKNOWN);
    }
    public PrincipalName(String name, String realm) throws RealmException {
        this(name, KRB_NT_UNKNOWN);
        nameRealm = new Realm(realm);
    }
    public String getRealmAsString() {
        return getRealmString();
    }
    public String getPrincipalNameAsString() {
        StringBuffer temp = new StringBuffer(nameStrings[0]);
        for (int i = 1; i < nameStrings.length; i++)
            temp.append(nameStrings[i]);
        return temp.toString();
    }
    public int hashCode() {
        return toString().hashCode();
    }
    public String getName() {
        return toString();
    }
    public int getNameType() {
        return nameType;
    }
    public String[] getNameStrings() {
        return nameStrings;
    }
    public byte[][] toByteArray() {
        byte[][] result = new byte[nameStrings.length][];
        for (int i = 0; i < nameStrings.length; i++) {
            result[i] = new byte[nameStrings[i].length()];
            result[i] = nameStrings[i].getBytes();
        }
        return result;
    }
    public String getRealmString() {
        if (nameRealm != null)
            return nameRealm.toString();
        return null;
    }
    public Realm getRealm() {
        return nameRealm;
    }
    public void setRealm(Realm new_nameRealm) throws RealmException {
        nameRealm = new_nameRealm;
    }
    public void setRealm(String realmsString) throws RealmException {
        nameRealm = new Realm(realmsString);
    }
    public String getSalt() {
        if (salt == null) {
            StringBuffer salt = new StringBuffer();
            if (nameRealm != null) {
                salt.append(nameRealm.toString());
            }
            for (int i = 0; i < nameStrings.length; i++) {
                salt.append(nameStrings[i]);
            }
            return salt.toString();
        }
        return salt;
    }
    public String toString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < nameStrings.length; i++) {
            if (i > 0)
                str.append("/");
            str.append(nameStrings[i]);
        }
        if (nameRealm != null) {
            str.append("@");
            str.append(nameRealm.toString());
        }
        return str.toString();
    }
    public String getNameString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < nameStrings.length; i++) {
            if (i > 0)
                str.append("/");
            str.append(nameStrings[i]);
        }
        return str.toString();
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        BigInteger bint = BigInteger.valueOf(this.nameType);
        temp.putInteger(bint);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), temp);
        temp = new DerOutputStream();
        DerValue der[] = new DerValue[nameStrings.length];
        for (int i = 0; i < nameStrings.length; i++) {
            der[i] = new KerberosString(nameStrings[i]).toDerValue();
        }
        temp.putSequence(der);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), temp);
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public boolean match(PrincipalName pname) {
        boolean matched = true;
        if ((this.nameRealm != null) && (pname.nameRealm != null)) {
            if (!(this.nameRealm.toString().equalsIgnoreCase(pname.nameRealm.toString()))) {
                matched = false;
            }
        }
        if (this.nameStrings.length != pname.nameStrings.length) {
            matched = false;
        } else {
            for (int i = 0; i < this.nameStrings.length; i++) {
                if (!(this.nameStrings[i].equalsIgnoreCase(pname.nameStrings[i]))) {
                    matched = false;
                }
            }
        }
        return matched;
    }
    public void writePrincipal(CCacheOutputStream cos) throws IOException {
        cos.write32(nameType);
        cos.write32(nameStrings.length);
        if (nameRealm != null) {
            byte[] realmBytes = null;
            realmBytes = nameRealm.toString().getBytes();
            cos.write32(realmBytes.length);
            cos.write(realmBytes, 0, realmBytes.length);
        }
        byte[] bytes = null;
        for (int i = 0; i < nameStrings.length; i++) {
            bytes = nameStrings[i].getBytes();
            cos.write32(bytes.length);
            cos.write(bytes, 0, bytes.length);
        }
    }
    protected PrincipalName(String primary, String instance, String realm,
                            int type)
        throws KrbException {
        if (type != KRB_NT_SRV_INST) {
            throw new KrbException(Krb5.KRB_ERR_GENERIC, "Bad name type");
        }
        String[] nParts = new String[2];
        nParts[0] = primary;
        nParts[1] = instance;
        this.nameStrings = nParts;
        this.nameRealm = new Realm(realm);
        this.nameType = type;
    }
    public String getInstanceComponent()
    {
        if (nameStrings != null && nameStrings.length >= 2)
            {
                return new String(nameStrings[1]);
            }
        return null;
    }
    static String mapHostToRealm(String name) {
        String result = null;
        try {
            String subname = null;
            Config c = Config.getInstance();
            if ((result = c.getDefault(name, "domain_realm")) != null)
                return result;
            else {
                for (int i = 1; i < name.length(); i++) {
                    if ((name.charAt(i) == '.') && (i != name.length() - 1)) { 
                        subname = name.substring(i);
                        result = c.getDefault(subname, "domain_realm");
                        if (result != null) {
                            break;
                        }
                        else {
                            subname = name.substring(i + 1);      
                            result = c.getDefault(subname, "domain_realm");
                            if (result != null) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (KrbException e) {
        }
        return result;
    }
}
