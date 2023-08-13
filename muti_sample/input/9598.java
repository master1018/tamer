class PolicyParser {
    private static final java.util.ResourceBundle rb =
        java.security.AccessController.doPrivileged
        (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
            public java.util.ResourceBundle run() {
                    return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.AuthResources"));
           }
        });
    private Vector<GrantEntry> grantEntries;
    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("parser", "\t[Auth Policy Parser]");
    private StreamTokenizer st;
    private int lookahead;
    private int linenum;
    private boolean expandProp = false;
    private String keyStoreUrlString = null; 
    private String keyStoreType = null;
    private String expand(String value)
        throws PropertyExpander.ExpandException
    {
        if (expandProp)
            return PropertyExpander.expand(value);
        else
            return value;
    }
    public PolicyParser() {
        grantEntries = new Vector<GrantEntry>();
    }
    public PolicyParser(boolean expandProp) {
        this();
        this.expandProp = expandProp;
    }
    public void read(Reader policy)
        throws ParsingException, IOException
    {
        if (!(policy instanceof BufferedReader)) {
            policy = new BufferedReader(policy);
        }
        st   = new StreamTokenizer(policy);
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
        lookahead = st.nextToken();
        while (lookahead != StreamTokenizer.TT_EOF) {
            if (peek("grant")) {
                GrantEntry ge = parseGrantEntry();
                if (ge != null)
                    add(ge);
            } else if (peek("keystore") && keyStoreUrlString==null) {
                parseKeyStoreEntry();
            } else {
            }
            match(";");
        }
    }
    public void add(GrantEntry ge)
    {
        grantEntries.addElement(ge);
    }
    public void replace(GrantEntry origGe, GrantEntry newGe)
    {
        grantEntries.setElementAt(newGe, grantEntries.indexOf(origGe));
    }
    public boolean remove(GrantEntry ge)
    {
        return grantEntries.removeElement(ge);
    }
    public String getKeyStoreUrl() {
        try {
            if (keyStoreUrlString!=null && keyStoreUrlString.length()!=0) {
                return expand(keyStoreUrlString).replace(File.separatorChar,
                                                         '/');
            }
        } catch (PropertyExpander.ExpandException peee) {
            return null;
        }
        return null;
    }
    public void setKeyStoreUrl(String url) {
        keyStoreUrlString = url;
    }
    public String getKeyStoreType() {
        return keyStoreType;
    }
    public void setKeyStoreType(String type) {
        keyStoreType = type;
    }
    public Enumeration<GrantEntry> grantElements(){
        return grantEntries.elements();
    }
    public void write(Writer policy)
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(policy));
        Enumeration<GrantEntry> enum_ = grantElements();
        out.println("");
        out.println("");
        out.println();
        if (keyStoreUrlString != null) {
            writeKeyStoreEntry(out);
        }
        while (enum_.hasMoreElements()) {
            GrantEntry ge = enum_.nextElement();
            ge.write(out);
            out.println();
        }
        out.flush();
    }
    private void parseKeyStoreEntry() throws ParsingException, IOException {
        match("keystore");
        keyStoreUrlString = match("quoted string");
        if (!peek(",")) {
            return; 
        }
        match(",");
        if (peek("\"")) {
            keyStoreType = match("quoted string");
        } else {
            throw new ParsingException(st.lineno(),
                        rb.getString("expected.keystore.type"));
        }
    }
    private void writeKeyStoreEntry(PrintWriter out) {
        out.print("keystore \"");
        out.print(keyStoreUrlString);
        out.print('"');
        if (keyStoreType != null && keyStoreType.length() > 0)
            out.print(", \"" + keyStoreType + "\"");
        out.println(";");
        out.println();
    }
    private GrantEntry parseGrantEntry()
        throws ParsingException, IOException
    {
        GrantEntry e = new GrantEntry();
        LinkedList<PrincipalEntry> principals = null;
        boolean ignoreEntry = false;
        match("grant");
        while(!peek("{")) {
            if (peekAndMatch("Codebase")) {
                e.codeBase = match("quoted string");
                peekAndMatch(",");
            } else if (peekAndMatch("SignedBy")) {
                e.signedBy = match("quoted string");
                peekAndMatch(",");
            } else if (peekAndMatch("Principal")) {
                if (principals == null) {
                    principals = new LinkedList<PrincipalEntry>();
                }
                String principalClass;
                if (peek("*")) {
                    match("*");
                    principalClass = PrincipalEntry.WILDCARD_CLASS;
                } else {
                    principalClass = match("principal type");
                }
                String principalName;
                if (peek("*")) {
                    match("*");
                    principalName = PrincipalEntry.WILDCARD_NAME;
                } else {
                    principalName = match("quoted string");
                }
                if (principalClass.equals(PrincipalEntry.WILDCARD_CLASS) &&
                    !principalName.equals(PrincipalEntry.WILDCARD_NAME)) {
                    if (debug != null)
                        debug.println("disallowing principal that has " +
                                "WILDCARD class but no WILDCARD name");
                    throw new ParsingException
                        (st.lineno(),
                        rb.getString("can.not.specify.Principal.with.a." +
                                     "wildcard.class.without.a.wildcard.name"));
                }
                try {
                    principalName = expand(principalName);
                    principals.add
                        (new PrincipalEntry(principalClass, principalName));
                } catch (PropertyExpander.ExpandException peee) {
                    if (debug != null)
                        debug.println("principal name expansion failed: " +
                                        principalName);
                    ignoreEntry = true;
                }
                peekAndMatch(",");
            } else {
                throw new
                 ParsingException(st.lineno(),
                        rb.getString("expected.codeBase.or.SignedBy"));
            }
        }
        if (principals == null) {
            throw new ParsingException
                (st.lineno(),
                rb.getString("only.Principal.based.grant.entries.permitted"));
        }
        e.principals = principals;
        match("{");
        while(!peek("}")) {
            if (peek("Permission")) {
                try {
                    PermissionEntry pe = parsePermissionEntry();
                    e.add(pe);
                } catch (PropertyExpander.ExpandException peee) {
                    skipEntry();  
                }
                match(";");
            } else {
                throw new
                    ParsingException(st.lineno(),
                    rb.getString("expected.permission.entry"));
            }
        }
        match("}");
        try {
            if (e.codeBase != null)
              e.codeBase = expand(e.codeBase).replace(File.separatorChar, '/');
            e.signedBy = expand(e.signedBy);
        } catch (PropertyExpander.ExpandException peee) {
            return null;
        }
        return (ignoreEntry == true) ? null : e;
    }
    private PermissionEntry parsePermissionEntry()
        throws ParsingException, IOException, PropertyExpander.ExpandException
    {
        PermissionEntry e = new PermissionEntry();
        match("Permission");
        e.permission = match("permission type");
        if (peek("\"")) {
            e.name = expand(match("quoted string"));
        }
        if (!peek(",")) {
            return e;
        }
        match(",");
        if (peek("\"")) {
                e.action = expand(match("quoted string"));
                if (!peek(",")) {
                    return e;
                }
                match(",");
        }
        if (peekAndMatch("SignedBy")) {
            e.signedBy = expand(match("quoted string"));
        }
        return e;
    }
    private boolean peekAndMatch(String expect)
        throws ParsingException, IOException
    {
        if (peek(expect)) {
            match(expect);
            return true;
        } else {
            return false;
        }
    }
    private boolean peek(String expect) {
        boolean found = false;
        switch (lookahead) {
        case StreamTokenizer.TT_WORD:
            if (expect.equalsIgnoreCase(st.sval))
                found = true;
            break;
        case ',':
            if (expect.equalsIgnoreCase(","))
                found = true;
            break;
        case '{':
            if (expect.equalsIgnoreCase("{"))
                found = true;
            break;
        case '}':
            if (expect.equalsIgnoreCase("}"))
                found = true;
            break;
        case '"':
            if (expect.equalsIgnoreCase("\""))
                found = true;
            break;
        case '*':
            if (expect.equalsIgnoreCase("*"))
                found = true;
            break;
        default:
        }
        return found;
    }
    private String match(String expect)
        throws ParsingException, IOException
    {
        String value = null;
        switch (lookahead) {
        case StreamTokenizer.TT_NUMBER:
            throw new ParsingException(st.lineno(), expect,
                                        rb.getString("number.") +
                                        String.valueOf(st.nval));
        case StreamTokenizer.TT_EOF:
            MessageFormat form = new MessageFormat(
                    rb.getString("expected.expect.read.end.of.file."));
            Object[] source = {expect};
            throw new ParsingException(form.format(source));
        case StreamTokenizer.TT_WORD:
            if (expect.equalsIgnoreCase(st.sval)) {
                lookahead = st.nextToken();
            } else if (expect.equalsIgnoreCase("permission type")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else if (expect.equalsIgnoreCase("principal type")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else {
                throw new ParsingException(st.lineno(), expect, st.sval);
            }
            break;
        case '"':
            if (expect.equalsIgnoreCase("quoted string")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else if (expect.equalsIgnoreCase("permission type")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else if (expect.equalsIgnoreCase("principal type")) {
                value = st.sval;
                lookahead = st.nextToken();
            } else {
                throw new ParsingException(st.lineno(), expect, st.sval);
            }
            break;
        case ',':
            if (expect.equalsIgnoreCase(","))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, ",");
            break;
        case '{':
            if (expect.equalsIgnoreCase("{"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, "{");
            break;
        case '}':
            if (expect.equalsIgnoreCase("}"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, "}");
            break;
        case ';':
            if (expect.equalsIgnoreCase(";"))
                lookahead = st.nextToken();
            else
                throw new ParsingException(st.lineno(), expect, ";");
            break;
        case '*':
            if (expect.equalsIgnoreCase("*"))
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
    private void skipEntry()
        throws ParsingException, IOException
    {
      while(lookahead != ';') {
        switch (lookahead) {
        case StreamTokenizer.TT_NUMBER:
            throw new ParsingException(st.lineno(), ";",
                                       rb.getString("number.") +
                                        String.valueOf(st.nval));
        case StreamTokenizer.TT_EOF:
          throw new ParsingException
                (rb.getString("expected.read.end.of.file"));
        default:
          lookahead = st.nextToken();
        }
      }
    }
    static class GrantEntry {
        public String signedBy;
        public String codeBase;
        public LinkedList<PrincipalEntry> principals;
        public Vector<PermissionEntry> permissionEntries;
        public GrantEntry() {
            permissionEntries = new Vector<PermissionEntry>();
        }
        public GrantEntry(String signedBy, String codeBase) {
            this.codeBase = codeBase;
            this.signedBy = signedBy;
            permissionEntries = new Vector<PermissionEntry>();
        }
        public void add(PermissionEntry pe)
        {
            permissionEntries.addElement(pe);
        }
        public boolean remove(PermissionEntry pe)
        {
            return permissionEntries.removeElement(pe);
        }
        public boolean contains(PermissionEntry pe)
        {
            return permissionEntries.contains(pe);
        }
        public Enumeration<PermissionEntry> permissionElements(){
            return permissionEntries.elements();
        }
        public void write(PrintWriter out) {
            out.print("grant");
            if (signedBy != null) {
                out.print(" signedBy \"");
                out.print(signedBy);
                out.print('"');
                if (codeBase != null)
                    out.print(", ");
            }
            if (codeBase != null) {
                out.print(" codeBase \"");
                out.print(codeBase);
                out.print('"');
                if (principals != null && principals.size() > 0)
                    out.print(",\n");
            }
            if (principals != null && principals.size() > 0) {
                ListIterator<PrincipalEntry> pli = principals.listIterator();
                while (pli.hasNext()) {
                    out.print("\tPrincipal ");
                    PrincipalEntry pe = pli.next();
                    out.print(pe.principalClass +
                                " \"" + pe.principalName + "\"");
                    if (pli.hasNext())
                        out.print(",\n");
                }
            }
            out.println(" {");
            Enumeration<PermissionEntry> enum_ = permissionEntries.elements();
            while (enum_.hasMoreElements()) {
                PermissionEntry pe = enum_.nextElement();
                out.write("  ");
                pe.write(out);
            }
            out.println("};");
        }
    }
    static class PrincipalEntry {
        static final String WILDCARD_CLASS = "WILDCARD_PRINCIPAL_CLASS";
        static final String WILDCARD_NAME = "WILDCARD_PRINCIPAL_NAME";
        String principalClass;
        String principalName;
        public PrincipalEntry(String principalClass, String principalName) {
            if (principalClass == null || principalName == null)
                throw new NullPointerException
                        ("null principalClass or principalName");
            this.principalClass = principalClass;
            this.principalName = principalName;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof PrincipalEntry))
                return false;
            PrincipalEntry that = (PrincipalEntry)obj;
            if (this.principalClass.equals(that.principalClass) &&
                this.principalName.equals(that.principalName)) {
                return true;
            }
            return false;
        }
        public int hashCode() {
            return principalClass.hashCode();
        }
    }
    static class PermissionEntry {
        public String permission;
        public String name;
        public String action;
        public String signedBy;
        public PermissionEntry() {
        }
        public PermissionEntry(String permission,
                        String name,
                        String action) {
            this.permission = permission;
            this.name = name;
            this.action = action;
        }
        public int hashCode() {
            int retval = permission.hashCode();
            if (name != null) retval ^= name.hashCode();
            if (action != null) retval ^= action.hashCode();
            return retval;
        }
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (! (obj instanceof PermissionEntry))
                return false;
            PermissionEntry that = (PermissionEntry) obj;
            if (this.permission == null) {
                if (that.permission != null) return false;
            } else {
                if (!this.permission.equals(that.permission)) return false;
            }
            if (this.name == null) {
                if (that.name != null) return false;
            } else {
                if (!this.name.equals(that.name)) return false;
            }
            if (this.action == null) {
                if (that.action != null) return false;
            } else {
                if (!this.action.equals(that.action)) return false;
            }
            if (this.signedBy == null) {
                if (that.signedBy != null) return false;
            } else {
                if (!this.signedBy.equals(that.signedBy)) return false;
            }
            return true;
        }
        public void write(PrintWriter out) {
            out.print("permission ");
            out.print(permission);
            if (name != null) {
                out.print(" \"");
                if (name.indexOf("\"") != -1) {
                    int numQuotes = 0;
                    char[] chars = name.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (chars[i] == '"')
                            numQuotes++;
                    }
                    char[] newChars = new char[chars.length + numQuotes];
                    for (int i = 0, j = 0; i < chars.length; i++) {
                        if (chars[i] != '"') {
                            newChars[j++] = chars[i];
                        } else {
                            newChars[j++] = '\\';
                            newChars[j++] = chars[i];
                        }
                    }
                    name = new String(newChars);
                }
                out.print(name);
                out.print('"');
            }
            if (action != null) {
                out.print(", \"");
                out.print(action);
                out.print('"');
            }
            if (signedBy != null) {
                out.print(", signedBy \"");
                out.print(signedBy);
                out.print('"');
            }
            out.println(";");
        }
    }
    static class ParsingException extends GeneralSecurityException {
        private static final long serialVersionUID = 8240970523155877400L;
        public ParsingException(String msg) {
            super(msg);
        }
        public ParsingException(int line, String msg) {
            super(rb.getString("line.") + line + rb.getString("COLON") + msg);
        }
        public ParsingException(int line, String expect, String actual) {
            super(rb.getString("line.") + line + rb.getString(".expected.") +
                expect + rb.getString(".found.") + actual +
                rb.getString("QUOTE"));
        }
    }
    public static void main(String arg[]) throws Exception {
        PolicyParser pp = new PolicyParser(true);
        pp.read(new FileReader(arg[0]));
        FileWriter fr = new FileWriter(arg[1]);
        pp.write(fr);
        fr.close();
    }
}
