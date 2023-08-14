final class CryptoPolicyParser {
    private Vector grantEntries;
    private StreamTokenizer st;
    private int lookahead;
    CryptoPolicyParser() {
        grantEntries = new Vector();
    }
    void read(Reader policy)
        throws ParsingException, IOException
    {
        if (!(policy instanceof BufferedReader)) {
            policy = new BufferedReader(policy);
        }
        st = new StreamTokenizer(policy);
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('.', '.');
        st.wordChars('0', '9');
        st.wordChars('_', '_');
        st.wordChars('$', '$');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, ' ');
        st.commentChar('/');
        st.quoteChar('\'');
        st.quoteChar('"');
        st.lowerCaseMode(false);
        st.ordinaryChar('/');
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.parseNumbers();
        Hashtable processedPermissions = null;
        lookahead = st.nextToken();
        while (lookahead != StreamTokenizer.TT_EOF) {
            if (peek("grant")) {
                GrantEntry ge = parseGrantEntry(processedPermissions);
                if (ge != null)
                    grantEntries.addElement(ge);
            } else {
                throw new ParsingException(st.lineno(), "expected grant " +
                                           "statement");
            }
            match(";");
        }
    }
    private GrantEntry parseGrantEntry(Hashtable processedPermissions)
        throws ParsingException, IOException
    {
        GrantEntry e = new GrantEntry();
        match("grant");
        match("{");
        while(!peek("}")) {
            if (peek("Permission")) {
                CryptoPermissionEntry pe =
                    parsePermissionEntry(processedPermissions);
                e.add(pe);
                match(";");
            } else {
                throw new
                    ParsingException(st.lineno(), "expected permission entry");
            }
        }
        match("}");
        return e;
    }
    private CryptoPermissionEntry parsePermissionEntry(
                                       Hashtable processedPermissions)
        throws ParsingException, IOException
    {
        CryptoPermissionEntry e = new CryptoPermissionEntry();
        match("Permission");
        e.cryptoPermission = match("permission type");
        if (e.cryptoPermission.equals("javax.crypto.CryptoAllPermission")) {
            e.alg = CryptoAllPermission.ALG_NAME;
            e.maxKeySize = Integer.MAX_VALUE;
            return e;
        }
        if (peek("\"")) {
            e.alg = match("quoted string").toUpperCase(ENGLISH);
        } else {
            if (peek("*")) {
                match("*");
                e.alg = CryptoPermission.ALG_NAME_WILDCARD;
            } else {
                throw new ParsingException(st.lineno(),
                                           "Missing the algorithm name");
            }
        }
        peekAndMatch(",");
        if (peek("\"")) {
            e.exemptionMechanism = match("quoted string").toUpperCase(ENGLISH);
        }
        peekAndMatch(",");
        if (!isConsistent(e.alg, e.exemptionMechanism, processedPermissions)) {
            throw new ParsingException(st.lineno(), "Inconsistent policy");
        }
        if (peek("number")) {
            e.maxKeySize = match();
        } else {
            if (peek("*")) {
                match("*");
                e.maxKeySize = Integer.MAX_VALUE;
            } else {
                if (!peek(";")) {
                    throw new ParsingException(st.lineno(),
                                               "Missing the maximum " +
                                               "allowable key size");
                } else {
                    e.maxKeySize = Integer.MAX_VALUE;
                }
            }
        }
        peekAndMatch(",");
        if (peek("\"")) {
            String algParamSpecClassName = match("quoted string");
            Vector paramsV = new Vector(1);
            while (peek(",")) {
                match(",");
                if (peek("number")) {
                    paramsV.addElement(new Integer(match()));
                } else {
                    if (peek("*")) {
                        match("*");
                        paramsV.addElement(new Integer(Integer.MAX_VALUE));
                    } else {
                        throw new ParsingException(st.lineno(),
                                                   "Expecting an integer");
                    }
                }
            }
            Integer[] params = new Integer[paramsV.size()];
            paramsV.copyInto(params);
            e.checkParam = true;
            e.algParamSpec = getInstance(algParamSpecClassName, params);
        }
        return e;
    }
    private static final AlgorithmParameterSpec getInstance(String type,
                                                            Integer[] params)
        throws ParsingException
    {
        AlgorithmParameterSpec ret = null;
        try {
            Class apsClass = Class.forName(type);
            Class[] paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramClasses[i] = int.class;
            }
            Constructor c = apsClass.getConstructor(paramClasses);
            ret = (AlgorithmParameterSpec) c.newInstance((Object[]) params);
        } catch (Exception e) {
            throw new ParsingException("Cannot call the constructor of " +
                                       type + e);
        }
        return ret;
    }
    private boolean peekAndMatch(String expect)
        throws ParsingException, IOException
    {
        if (peek(expect)) {
            match(expect);
            return true;
        }
        return false;
    }
    private boolean peek(String expect) {
        boolean found = false;
        switch (lookahead) {
        case StreamTokenizer.TT_WORD:
            if (expect.equalsIgnoreCase(st.sval))
                found = true;
            break;
        case StreamTokenizer.TT_NUMBER:
            if (expect.equalsIgnoreCase("number")) {
                found = true;
            }
            break;
        case ',':
            if (expect.equals(","))
                found = true;
            break;
        case '{':
            if (expect.equals("{"))
                found = true;
            break;
        case '}':
            if (expect.equals("}"))
                found = true;
            break;
        case '"':
            if (expect.equals("\""))
                found = true;
            break;
        case '*':
            if (expect.equals("*"))
                found = true;
            break;
        case ';':
            if (expect.equals(";"))
                found = true;
            break;
        default:
            break;
        }
        return found;
    }
    private int match()
        throws ParsingException, IOException
    {
        int value = -1;
        int lineno = st.lineno();
        String sValue = null;
        switch (lookahead) {
        case StreamTokenizer.TT_NUMBER:
            value = (int)st.nval;
            if (value < 0) {
                sValue = String.valueOf(st.nval);
            }
            lookahead = st.nextToken();
            break;
        default:
            sValue = st.sval;
            break;
        }
        if (value <= 0) {
            throw new ParsingException(lineno, "a non-negative number",
                                       sValue);
        }
        return value;
    }
    private String match(String expect)
        throws ParsingException, IOException
    {
        String value = null;
        switch (lookahead) {
        case StreamTokenizer.TT_NUMBER:
            throw new ParsingException(st.lineno(), expect,
                                       "number "+String.valueOf(st.nval));
        case StreamTokenizer.TT_EOF:
           throw new ParsingException("expected "+expect+", read end of file");
        case StreamTokenizer.TT_WORD:
            if (expect.equalsIgnoreCase(st.sval)) {
                lookahead = st.nextToken();
            }
            else if (expect.equalsIgnoreCase("permission type")) {
                value = st.sval;
                lookahead = st.nextToken();
            }
            else
                throw new ParsingException(st.lineno(), expect, st.sval);
            break;
        case '"':
            if (expect.equalsIgnoreCase("quoted string")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else if (expect.equalsIgnoreCase("permission type")) {
                value = st.sval;
                lookahead = st.nextToken();
            }
            else
                throw new ParsingException(st.lineno(), expect, st.sval);
            break;
        case ',':
            if (expect.equals(","))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, ",");
            break;
        case '{':
            if (expect.equals("{"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, "{");
            break;
        case '}':
            if (expect.equals("}"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, "}");
            break;
        case ';':
            if (expect.equals(";"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, ";");
            break;
        case '*':
            if (expect.equals("*"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, "*");
            break;
        default:
            throw new ParsingException(st.lineno(), expect,
                               new String(new char[] {(char)lookahead}));
        }
        return value;
    }
    CryptoPermission[] getPermissions() {
        Vector result = new Vector();
        Enumeration grantEnum = grantEntries.elements();
        while (grantEnum.hasMoreElements()) {
            GrantEntry ge = (GrantEntry)grantEnum.nextElement();
            Enumeration permEnum = ge.permissionElements();
            while (permEnum.hasMoreElements()) {
                CryptoPermissionEntry pe =
                    (CryptoPermissionEntry)permEnum.nextElement();
                if (pe.cryptoPermission.equals(
                                        "javax.crypto.CryptoAllPermission")) {
                    result.addElement(CryptoAllPermission.INSTANCE);
                } else {
                    if (pe.checkParam) {
                        result.addElement(new CryptoPermission(
                                                pe.alg,
                                                pe.maxKeySize,
                                                pe.algParamSpec,
                                                pe.exemptionMechanism));
                    } else {
                        result.addElement(new CryptoPermission(
                                                pe.alg,
                                                pe.maxKeySize,
                                                pe.exemptionMechanism));
                    }
                }
            }
        }
        CryptoPermission[] ret = new CryptoPermission[result.size()];
        result.copyInto(ret);
        return ret;
    }
    private boolean isConsistent(String alg,
                                 String exemptionMechanism,
                                 Hashtable processedPermissions) {
        String thisExemptionMechanism =
            exemptionMechanism == null ? "none" : exemptionMechanism;
        if (processedPermissions == null) {
            processedPermissions = new Hashtable();
            Vector exemptionMechanisms = new Vector(1);
            exemptionMechanisms.addElement(thisExemptionMechanism);
            processedPermissions.put(alg, exemptionMechanisms);
            return true;
        }
        if (processedPermissions.containsKey(CryptoAllPermission.ALG_NAME)) {
            return false;
        }
        Vector exemptionMechanisms;
        if (processedPermissions.containsKey(alg)) {
            exemptionMechanisms = (Vector)processedPermissions.get(alg);
            if (exemptionMechanisms.contains(thisExemptionMechanism)) {
                return false;
            }
        } else {
            exemptionMechanisms = new Vector(1);
        }
        exemptionMechanisms.addElement(thisExemptionMechanism);
        processedPermissions.put(alg, exemptionMechanisms);
        return true;
    }
    private static class GrantEntry {
        private Vector permissionEntries;
        GrantEntry() {
            permissionEntries = new Vector();
        }
        void add(CryptoPermissionEntry pe)
        {
            permissionEntries.addElement(pe);
        }
        boolean remove(CryptoPermissionEntry pe)
        {
            return permissionEntries.removeElement(pe);
        }
        boolean contains(CryptoPermissionEntry pe)
        {
            return permissionEntries.contains(pe);
        }
        Enumeration permissionElements(){
            return permissionEntries.elements();
        }
    }
    private static class CryptoPermissionEntry {
        String cryptoPermission;
        String alg;
        String exemptionMechanism;
        int maxKeySize;
        boolean checkParam;
        AlgorithmParameterSpec algParamSpec;
        CryptoPermissionEntry() {
            maxKeySize = 0;
            alg = null;
            exemptionMechanism = null;
            checkParam = false;
            algParamSpec = null;
        }
        public int hashCode() {
            int retval = cryptoPermission.hashCode();
            if (alg != null) retval ^= alg.hashCode();
            if (exemptionMechanism != null) {
                retval ^= exemptionMechanism.hashCode();
            }
            retval ^= maxKeySize;
            if (checkParam) retval ^= 100;
            if (algParamSpec != null) {
                retval ^= algParamSpec.hashCode();
            }
            return retval;
        }
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof CryptoPermissionEntry))
                return false;
            CryptoPermissionEntry that = (CryptoPermissionEntry) obj;
            if (this.cryptoPermission == null) {
                if (that.cryptoPermission != null) return false;
            } else {
                if (!this.cryptoPermission.equals(
                                                 that.cryptoPermission))
                    return false;
            }
            if (this.alg == null) {
                if (that.alg != null) return false;
            } else {
                if (!this.alg.equalsIgnoreCase(that.alg))
                    return false;
            }
            if (!(this.maxKeySize == that.maxKeySize)) return false;
            if (this.checkParam != that.checkParam) return false;
            if (this.algParamSpec == null) {
                if (that.algParamSpec != null) return false;
            } else {
                if (!this.algParamSpec.equals(that.algParamSpec))
                    return false;
            }
            return true;
        }
    }
    static final class ParsingException extends GeneralSecurityException {
        private static final long serialVersionUID = 7147241245566588374L;
        ParsingException(String msg) {
            super(msg);
        }
        ParsingException(int line, String msg) {
            super("line " + line + ": " + msg);
        }
        ParsingException(int line, String expect, String actual) {
            super("line "+line+": expected '"+expect+"', found '"+actual+"'");
        }
    }
}
