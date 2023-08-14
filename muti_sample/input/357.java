public class Realm implements Cloneable {
    private String realm;
    private static boolean DEBUG = Krb5.DEBUG;
    private Realm() {
    }
    public Realm(String name) throws RealmException {
        realm = parseRealm(name);
    }
    public Object clone() {
        Realm new_realm = new Realm();
        if (realm != null) {
            new_realm.realm = new String(realm);
        }
        return new_realm;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Realm)) {
            return false;
        }
        Realm that = (Realm)obj;
        if (this.realm != null && that.realm != null ) {
            return this.realm.equals(that.realm);
        } else {
            return (this.realm == null && that.realm == null);
        }
    }
    public int hashCode() {
        int result = 17 ;
        if( realm != null ) {
            result = 37 * result + realm.hashCode();
        }
        return result;
    }
    public Realm(DerValue encoding)
        throws Asn1Exception, RealmException, IOException {
        if (encoding == null) {
            throw new IllegalArgumentException("encoding can not be null");
        }
        realm = new KerberosString(encoding).toString();
        if (realm == null || realm.length() == 0)
            throw new RealmException(Krb5.REALM_NULL);
        if (!isValidRealmString(realm))
            throw new RealmException(Krb5.REALM_ILLCHAR);
    }
    public String toString() {
        return realm;
    }
    public static String parseRealmAtSeparator(String name)
        throws RealmException {
        if (name == null) {
            throw new IllegalArgumentException
                ("null input name is not allowed");
        }
        String temp = new String(name);
        String result = null;
        int i = 0;
        while (i < temp.length()) {
            if (temp.charAt(i) == PrincipalName.NAME_REALM_SEPARATOR) {
                if (i == 0 || temp.charAt(i - 1) != '\\') {
                    if (i + 1 < temp.length())
                        result = temp.substring(i + 1, temp.length());
                    break;
                }
            }
            i++;
        }
        if (result != null) {
            if (result.length() == 0)
                throw new RealmException(Krb5.REALM_NULL);
            if (!isValidRealmString(result))
                throw new RealmException(Krb5.REALM_ILLCHAR);
        }
        return result;
    }
    public static String parseRealmComponent(String name) {
        if (name == null) {
            throw new IllegalArgumentException
                ("null input name is not allowed");
        }
        String temp = new String(name);
        String result = null;
        int i = 0;
        while (i < temp.length()) {
            if (temp.charAt(i) == PrincipalName.REALM_COMPONENT_SEPARATOR) {
                if (i == 0 || temp.charAt(i - 1) != '\\') {
                    if (i + 1 < temp.length())
                        result = temp.substring(i + 1, temp.length());
                    break;
                }
            }
            i++;
        }
        return result;
    }
    protected static String parseRealm(String name) throws RealmException {
        String result = parseRealmAtSeparator(name);
        if (result == null)
            result = name;
        if (result == null || result.length() == 0)
            throw new RealmException(Krb5.REALM_NULL);
        if (!isValidRealmString(result))
            throw new RealmException(Krb5.REALM_ILLCHAR);
        return result;
    }
    protected static boolean isValidRealmString(String name) {
        if (name == null)
            return false;
        if (name.length() == 0)
            return false;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '/' ||
                name.charAt(i) == ':' ||
                name.charAt(i) == '\0') {
                return false;
            }
        }
        return true;
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream out = new DerOutputStream();
        out.putDerValue(new KerberosString(this.realm).toDerValue());
        return out.toByteArray();
    }
    public static Realm parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException, RealmException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) != explicitTag)) {
            return null;
        }
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new Realm(subDer);
        }
    }
    private static String[] doInitialParse(String cRealm, String sRealm)
        throws KrbException {
            if (cRealm == null || sRealm == null){
                throw new KrbException(Krb5.API_INVALID_ARG);
            }
            if (DEBUG) {
                System.out.println(">>> Realm doInitialParse: cRealm=["
                                   + cRealm + "], sRealm=[" +sRealm + "]");
            }
            if (cRealm.equals(sRealm)) {
                String[] retList = null;
                retList = new String[1];
                retList[0] = new String(cRealm);
                if (DEBUG) {
                    System.out.println(">>> Realm doInitialParse: "
                                       + retList[0]);
                }
                return retList;
            }
            return null;
        }
    public static String[] getRealmsList(String cRealm, String sRealm)
        throws KrbException {
            String[] retList = doInitialParse(cRealm, sRealm);
            if (retList != null && retList.length != 0) {
                return retList;
            }
            retList = parseCapaths(cRealm, sRealm);
            if (retList != null && retList.length != 0) {
                return retList;
            }
            retList = parseHierarchy(cRealm, sRealm);
            return retList;
        }
    private static String[] parseCapaths(String cRealm, String sRealm) throws KrbException {
        String[] retList = null;
        Config cfg = null;
        try {
            cfg = Config.getInstance();
        } catch (Exception exc) {
            if (DEBUG) {
                System.out.println ("Configuration information can not be " +
                                    "obtained " + exc.getMessage());
            }
            return null;
        }
        String intermediaries = cfg.getDefault(sRealm, cRealm);
        if (intermediaries == null) {
            if (DEBUG) {
                System.out.println(">>> Realm parseCapaths: no cfg entry");
            }
            return null;
        }
        String tempTarget = null, tempRealm = null;
        Stack<String> iStack = new Stack<>();
        Vector<String> tempList = new Vector<>(8, 8);
        tempList.add(sRealm);
        int count = 0; 
        tempTarget = sRealm;
        out: do {
            if (DEBUG) {
                count++;
                System.out.println(">>> Realm parseCapaths: loop " +
                                   count + ": target=" + tempTarget);
            }
            if (intermediaries != null &&
                !intermediaries.equals(".") &&
                !intermediaries.equals(cRealm)) {
                if (DEBUG) {
                    System.out.println(">>> Realm parseCapaths: loop " +
                                       count + ": intermediaries=[" +
                                       intermediaries + "]");
                }
                iStack.push(null);
                String[] ints = intermediaries.split("\\s+");
                for (int i = ints.length-1; i>=0; i--)
                {
                    tempRealm = ints[i];
                    if (tempRealm.equals(PrincipalName.REALM_COMPONENT_SEPARATOR_STR)) {
                        break out;
                    }
                    if (!tempList.contains(tempRealm)) {
                        iStack.push(tempRealm);
                        if (DEBUG) {
                            System.out.println(">>> Realm parseCapaths: loop " +
                                               count +
                                               ": pushed realm on to stack: " +
                                               tempRealm);
                        }
                    } else if (DEBUG) {
                        System.out.println(">>> Realm parseCapaths: loop " +
                                           count +
                                           ": ignoring realm: [" +
                                           tempRealm + "]");
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println(">>> Realm parseCapaths: loop " +
                                       count +
                                       ": no intermediaries");
                }
                break;
            }
            try {
                while ((tempTarget = iStack.pop()) == null) {
                    tempList.removeElementAt(tempList.size()-1);
                    if (DEBUG) {
                        System.out.println(">>> Realm parseCapaths: backtrack, remove tail");
                    }
                }
            } catch (EmptyStackException exc) {
                tempTarget = null;
            }
            if (tempTarget == null) {
                break;
            }
            tempList.add(tempTarget);
            if (DEBUG) {
                System.out.println(">>> Realm parseCapaths: loop " + count +
                                   ": added intermediary to list: " +
                                   tempTarget);
            }
            intermediaries = cfg.getDefault(tempTarget, cRealm);
        } while (true);
        if (tempList.isEmpty()) {
            return null;
        }
        retList = new String[tempList.size()];
        retList[0] = cRealm;
        for (int i=1; i<tempList.size(); i++) {
            retList[i] = tempList.elementAt(tempList.size()-i);
        }
        if (DEBUG && retList != null) {
            for (int i = 0; i < retList.length; i++) {
                System.out.println(">>> Realm parseCapaths [" + i +
                                   "]=" + retList[i]);
            }
        }
        return retList;
    }
    private static String[] parseHierarchy(String cRealm, String sRealm)
        throws KrbException
    {
        String[] retList = null;
        String[] cComponents = null;
        String[] sComponents = null;
        StringTokenizer strTok =
        new StringTokenizer(cRealm,
                            PrincipalName.REALM_COMPONENT_SEPARATOR_STR);
        int cCount = strTok.countTokens();
        cComponents = new String[cCount];
        for (cCount = 0; strTok.hasMoreTokens(); cCount++) {
            cComponents[cCount] = strTok.nextToken();
        }
        if (DEBUG) {
            System.out.println(">>> Realm parseHierarchy: cRealm has " +
                               cCount + " components:");
            int j = 0;
            while (j < cCount) {
                System.out.println(">>> Realm parseHierarchy: " +
                                   "cComponents["+j+"]=" + cComponents[j++]);
            }
        }
        strTok = new StringTokenizer(sRealm,
                                     PrincipalName.REALM_COMPONENT_SEPARATOR_STR);
        int sCount = strTok.countTokens();
        sComponents = new String[sCount];
        for (sCount = 0; strTok.hasMoreTokens(); sCount++) {
            sComponents[sCount] = strTok.nextToken();
        }
        if (DEBUG) {
            System.out.println(">>> Realm parseHierarchy: sRealm has " +
                               sCount + " components:");
            int j = 0;
            while (j < sCount) {
                System.out.println(">>> Realm parseHierarchy: sComponents["+j+
                                   "]=" + sComponents[j++]);
            }
        }
        int commonComponents = 0;
        for (sCount--, cCount--; sCount >=0 && cCount >= 0 &&
                 sComponents[sCount].equals(cComponents[cCount]);
             sCount--, cCount--) {
            commonComponents++;
        }
        int cCommonStart = -1;
        int sCommonStart = -1;
        int links = 0;
        if (commonComponents > 0) {
            sCommonStart = sCount+1;
            cCommonStart = cCount+1;
            links += sCommonStart;
            links += cCommonStart;
        } else {
            links++;
        }
        if (DEBUG) {
            if (commonComponents > 0) {
                System.out.println(">>> Realm parseHierarchy: " +
                                   commonComponents + " common component" +
                                   (commonComponents > 1 ? "s" : " "));
                System.out.println(">>> Realm parseHierarchy: common part "
                                   +
                                   "in cRealm (starts at index " +
                                   cCommonStart + ")");
                System.out.println(">>> Realm parseHierarchy: common part in sRealm (starts at index " +
                                   sCommonStart + ")");
                String commonPart = substring(cRealm, cCommonStart);
                System.out.println(">>> Realm parseHierarchy: common part in cRealm=" +
                                   commonPart);
                commonPart = substring(sRealm, sCommonStart);
                System.out.println(">>> Realm parseHierarchy: common part in sRealm=" +
                                   commonPart);
            } else
            System.out.println(">>> Realm parseHierarchy: no common part");
        }
        if (DEBUG) {
            System.out.println(">>> Realm parseHierarchy: total links=" + links);
        }
        retList = new String[links];
        retList[0] = new String(cRealm);
        if (DEBUG) {
            System.out.println(">>> Realm parseHierarchy A: retList[0]=" +
                               retList[0]);
        }
        String cTemp = null, sTemp = null;
        int i;
        for (i = 1, cCount = 0; i < links && cCount < cCommonStart; cCount++) {
            sTemp = substring(cRealm, cCount+1);
            retList[i++] = new String(sTemp);
            if (DEBUG) {
                System.out.println(">>> Realm parseHierarchy B: retList[" +
                                   (i-1) +"]="+retList[i-1]);
            }
        }
        for (sCount = sCommonStart; i < links && sCount - 1 > 0; sCount--) {
            sTemp = substring(sRealm, sCount-1);
            retList[i++] = new String(sTemp);
            if (DEBUG) {
                System.out.println(">>> Realm parseHierarchy D: retList[" +
                                   (i-1) +"]="+retList[i-1]);
            }
        }
        return retList;
    }
    private static String substring(String realm, int componentIndex)
    {
        int i = 0 , j = 0, len = realm.length();
        while(i < len && j != componentIndex) {
            if (realm.charAt(i++) != PrincipalName.REALM_COMPONENT_SEPARATOR)
                continue;
            j++;
        }
        return realm.substring(i);
    }
    static int getRandIndex(int arraySize) {
        return (int)(Math.random() * 16384.0) % arraySize;
    }
    static void printNames(String[] names) {
        if (names == null || names.length == 0)
            return;
        int len = names.length;
        int i = 0;
        System.out.println("List length = " + len);
        while (i < names.length) {
            System.out.println("["+ i +"]=" + names[i]);
            i++;
        }
    }
}
