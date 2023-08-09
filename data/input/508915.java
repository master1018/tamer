public class DefaultPolicyScanner {
    public static class InvalidFormatException extends Exception {
        private static final long serialVersionUID = 5789786270390222184L;
        public InvalidFormatException(String arg0) {
            super(arg0);
        }
    }
    protected StreamTokenizer configure(StreamTokenizer st) {
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.wordChars('_', '_');
        st.wordChars('$', '$');
        return st;
    }
    public void scanStream(Reader r, Collection<GrantEntry> grantEntries,
            List<KeystoreEntry> keystoreEntries) throws IOException,
            InvalidFormatException {
        StreamTokenizer st = configure(new StreamTokenizer(r));
        parsing: while (true) {
            switch (st.nextToken()) {
            case StreamTokenizer.TT_EOF: 
                break parsing;
            case StreamTokenizer.TT_WORD:
                if (Util.equalsIgnoreCase("keystore", st.sval)) { 
                    keystoreEntries.add(readKeystoreEntry(st));
                } else if (Util.equalsIgnoreCase("grant", st.sval)) { 
                    grantEntries.add(readGrantEntry(st));
                } else {
                    handleUnexpectedToken(st, Messages.getString("security.89")); 
                }
                break;
            case ';': 
                break;
            default:
                handleUnexpectedToken(st);
                break;
            }
        }
    }
    protected KeystoreEntry readKeystoreEntry(StreamTokenizer st)
            throws IOException, InvalidFormatException {
        KeystoreEntry ke = new KeystoreEntry();
        if (st.nextToken() == '"') {
            ke.url = st.sval;
            if ((st.nextToken() == '"')
                    || ((st.ttype == ',') && (st.nextToken() == '"'))) {
                ke.type = st.sval;
            } else { 
                st.pushBack();
            }
        } else {
            handleUnexpectedToken(st, Messages.getString("security.8A")); 
        }
        return ke;
    }
    protected GrantEntry readGrantEntry(StreamTokenizer st) throws IOException,
            InvalidFormatException {
        GrantEntry ge = new GrantEntry();
        parsing: while (true) {
            switch (st.nextToken()) {
            case StreamTokenizer.TT_WORD:
                if (Util.equalsIgnoreCase("signedby", st.sval)) { 
                    if (st.nextToken() == '"') {
                        ge.signers = st.sval;
                    } else {
                        handleUnexpectedToken(st, Messages.getString("security.8B")); 
                    }
                } else if (Util.equalsIgnoreCase("codebase", st.sval)) { 
                    if (st.nextToken() == '"') {
                        ge.codebase = st.sval;
                    } else {
                        handleUnexpectedToken(st, Messages.getString("security.8C")); 
                    }
                } else if (Util.equalsIgnoreCase("principal", st.sval)) { 
                    ge.addPrincipal(readPrincipalEntry(st));
                } else {
                    handleUnexpectedToken(st);
                }
                break;
            case ',': 
                break;
            case '{':
                ge.permissions = readPermissionEntries(st);
                break parsing;
            default: 
                st.pushBack();
                break parsing;
            }
        }
        return ge;
    }
    protected PrincipalEntry readPrincipalEntry(StreamTokenizer st)
            throws IOException, InvalidFormatException {
        PrincipalEntry pe = new PrincipalEntry();
        if (st.nextToken() == StreamTokenizer.TT_WORD) {
            pe.klass = st.sval;
            st.nextToken();
        } else if (st.ttype == '*') {
            pe.klass = PrincipalEntry.WILDCARD;
            st.nextToken();
        }
        if (st.ttype == '"') {
            pe.name = st.sval;
        } else if (st.ttype == '*') {
            pe.name = PrincipalEntry.WILDCARD;
        } else {
            handleUnexpectedToken(st, Messages.getString("security.8D")); 
        }
        return pe;
    }
    protected Collection<PermissionEntry> readPermissionEntries(
            StreamTokenizer st) throws IOException, InvalidFormatException {
        Collection<PermissionEntry> permissions = new HashSet<PermissionEntry>();
        parsing: while (true) {
            switch (st.nextToken()) {
            case StreamTokenizer.TT_WORD:
                if (Util.equalsIgnoreCase("permission", st.sval)) { 
                    PermissionEntry pe = new PermissionEntry();
                    if (st.nextToken() == StreamTokenizer.TT_WORD) {
                        pe.klass = st.sval;
                        if (st.nextToken() == '"') {
                            pe.name = st.sval;
                            st.nextToken();
                        }
                        if (st.ttype == ',') {
                            st.nextToken();
                        }
                        if (st.ttype == '"') {
                            pe.actions = st.sval;
                            if (st.nextToken() == ',') {
                                st.nextToken();
                            }
                        }
                        if (st.ttype == StreamTokenizer.TT_WORD
                                && Util.equalsIgnoreCase("signedby", st.sval)) { 
                            if (st.nextToken() == '"') {
                                pe.signers = st.sval;
                            } else {
                                handleUnexpectedToken(st);
                            }
                        } else { 
                            st.pushBack();
                        }
                        permissions.add(pe);
                        continue parsing;
                    }
                }
                handleUnexpectedToken(st, Messages.getString("security.8E")); 
                break;
            case ';': 
                break;
            case '}': 
                break parsing;
            default: 
                handleUnexpectedToken(st);
                break;
            }
        }
        return permissions;
    }
    protected String composeStatus(StreamTokenizer st) {
        return st.toString();
    }
    protected final void handleUnexpectedToken(StreamTokenizer st,
            String message) throws InvalidFormatException {
        throw new InvalidFormatException(Messages.getString("security.8F", 
                composeStatus(st), message));
    }
    protected final void handleUnexpectedToken(StreamTokenizer st)
            throws InvalidFormatException {
        throw new InvalidFormatException(Messages.getString("security.90", 
                composeStatus(st)));
    }
    public static class KeystoreEntry {
        public String url;
        public String type;
    }
    public static class GrantEntry {
        public String signers;
        public String codebase;
        public Collection<PrincipalEntry> principals;
        public Collection<PermissionEntry> permissions;
        public void addPrincipal(PrincipalEntry pe) {
            if (principals == null) {
                principals = new HashSet<PrincipalEntry>();
            }
            principals.add(pe);
        }
    }
    public static class PrincipalEntry {
        public static final String WILDCARD = "*"; 
        public String klass;
        public String name;
    }
    public static class PermissionEntry {
        public String klass;
        public String name;
        public String actions;
        public String signers;
    }
}
