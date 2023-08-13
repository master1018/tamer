final class LdapSchemaParser {
    private static final boolean debug = false;
    static final String OBJECTCLASSDESC_ATTR_ID = "objectClasses";
    static final String ATTRIBUTEDESC_ATTR_ID = "attributeTypes";
    static final String SYNTAXDESC_ATTR_ID = "ldapSyntaxes";
    static final String MATCHRULEDESC_ATTR_ID = "matchingRules";
    static final String OBJECTCLASS_DEFINITION_NAME =
                        "ClassDefinition";
    private static final String[] CLASS_DEF_ATTRS = {
                         "objectclass", "ClassDefinition"};
            static final String ATTRIBUTE_DEFINITION_NAME =
                        "AttributeDefinition";
    private static final String[] ATTR_DEF_ATTRS = {
                        "objectclass", "AttributeDefinition" };
            static final String SYNTAX_DEFINITION_NAME =
                        "SyntaxDefinition";
    private static final String[] SYNTAX_DEF_ATTRS = {
                        "objectclass", "SyntaxDefinition" };
            static final String MATCHRULE_DEFINITION_NAME =
                        "MatchingRule";
    private static final String[] MATCHRULE_DEF_ATTRS = {
                        "objectclass", "MatchingRule" };
    private static final char   SINGLE_QUOTE = '\'';
    private static final char   WHSP = ' ';
    private static final char   OID_LIST_BEGIN = '(';
    private static final char   OID_LIST_END = ')';
    private static final char   OID_SEPARATOR = '$';
    private static final String  NUMERICOID_ID = "NUMERICOID";
    private static final String        NAME_ID = "NAME";
    private static final String        DESC_ID = "DESC";
    private static final String    OBSOLETE_ID = "OBSOLETE";
    private static final String         SUP_ID = "SUP";
    private static final String     PRIVATE_ID = "X-";
    private static final String    ABSTRACT_ID = "ABSTRACT";
    private static final String  STRUCTURAL_ID = "STRUCTURAL";
    private static final String    AUXILARY_ID = "AUXILIARY";
    private static final String        MUST_ID = "MUST";
    private static final String         MAY_ID = "MAY";
    private static final String    EQUALITY_ID = "EQUALITY";
    private static final String    ORDERING_ID = "ORDERING";
    private static final String      SUBSTR_ID = "SUBSTR";
    private static final String      SYNTAX_ID = "SYNTAX";
    private static final String  SINGLE_VAL_ID = "SINGLE-VALUE";
    private static final String  COLLECTIVE_ID = "COLLECTIVE";
    private static final String NO_USER_MOD_ID = "NO-USER-MODIFICATION";
    private static final String       USAGE_ID = "USAGE";
    private static final String SCHEMA_TRUE_VALUE = "true";
    private boolean netscapeBug;
    LdapSchemaParser(boolean netscapeBug) {
        this.netscapeBug = netscapeBug;
    }
    final static void LDAP2JNDISchema(Attributes schemaAttrs,
        LdapSchemaCtx schemaRoot) throws NamingException {
        Attribute               objectClassesAttr = null;
        Attribute               attributeDefAttr = null;
        Attribute               syntaxDefAttr = null;
        Attribute               matchRuleDefAttr = null;
        objectClassesAttr = schemaAttrs.get(OBJECTCLASSDESC_ATTR_ID);
        if(objectClassesAttr != null) {
            objectDescs2ClassDefs(objectClassesAttr,schemaRoot);
        }
        attributeDefAttr = schemaAttrs.get(ATTRIBUTEDESC_ATTR_ID);
        if(attributeDefAttr != null) {
            attrDescs2AttrDefs(attributeDefAttr, schemaRoot);
        }
        syntaxDefAttr = schemaAttrs.get(SYNTAXDESC_ATTR_ID);
        if(syntaxDefAttr != null) {
            syntaxDescs2SyntaxDefs(syntaxDefAttr, schemaRoot);
        }
        matchRuleDefAttr = schemaAttrs.get(MATCHRULEDESC_ATTR_ID);
        if(matchRuleDefAttr != null) {
            matchRuleDescs2MatchRuleDefs(matchRuleDefAttr, schemaRoot);
        }
    }
    final private static DirContext objectDescs2ClassDefs(Attribute objDescsAttr,
                                                   LdapSchemaCtx schemaRoot)
        throws NamingException {
        NamingEnumeration       objDescs;
        Attributes      objDef;
        LdapSchemaCtx   classDefTree;
        Attributes attrs = new BasicAttributes(LdapClient.caseIgnore);
        attrs.put(CLASS_DEF_ATTRS[0], CLASS_DEF_ATTRS[1]);
        classDefTree = schemaRoot.setup(LdapSchemaCtx.OBJECTCLASS_ROOT,
            OBJECTCLASS_DEFINITION_NAME, attrs);
        objDescs = objDescsAttr.getAll();
        String currentName;
        while(objDescs.hasMore()) {
            String objDesc = (String)objDescs.next();
            try {
                Object[] def = desc2Def(objDesc);
                currentName = (String) def[0];
                objDef = (Attributes) def[1];
                classDefTree.setup(LdapSchemaCtx.OBJECTCLASS,
                    currentName, objDef);
            } catch (NamingException ne) {
            }
        }
        return classDefTree;
    }
    final private static DirContext attrDescs2AttrDefs(Attribute attributeDescAttr,
                                                LdapSchemaCtx schemaRoot)
        throws NamingException {
        NamingEnumeration       attrDescs;
        Attributes      attrDef;
        LdapSchemaCtx   attrDefTree;
        Attributes attrs = new BasicAttributes(LdapClient.caseIgnore);
        attrs.put(ATTR_DEF_ATTRS[0], ATTR_DEF_ATTRS[1]);
        attrDefTree = schemaRoot.setup(LdapSchemaCtx.ATTRIBUTE_ROOT,
            ATTRIBUTE_DEFINITION_NAME, attrs);
        attrDescs = attributeDescAttr.getAll();
        String currentName;
        while(attrDescs.hasMore()) {
            String attrDesc = (String)attrDescs.next();
            try {
                Object[] def = desc2Def(attrDesc);
                currentName = (String) def[0];
                attrDef = (Attributes) def[1];
                attrDefTree.setup(LdapSchemaCtx.ATTRIBUTE,
                    currentName, attrDef);
            } catch (NamingException ne) {
            }
        }
        return attrDefTree;
    }
    final private static DirContext syntaxDescs2SyntaxDefs(
                                                Attribute syntaxDescAttr,
                                                LdapSchemaCtx schemaRoot)
        throws NamingException {
        NamingEnumeration       syntaxDescs;
        Attributes      syntaxDef;
        LdapSchemaCtx   syntaxDefTree;
        Attributes attrs = new BasicAttributes(LdapClient.caseIgnore);
        attrs.put(SYNTAX_DEF_ATTRS[0], SYNTAX_DEF_ATTRS[1]);
        syntaxDefTree = schemaRoot.setup(LdapSchemaCtx.SYNTAX_ROOT,
            SYNTAX_DEFINITION_NAME, attrs);
        syntaxDescs = syntaxDescAttr.getAll();
        String currentName;
        while(syntaxDescs.hasMore()) {
            String syntaxDesc = (String)syntaxDescs.next();
            try {
                Object[] def = desc2Def(syntaxDesc);
                currentName = (String) def[0];
                syntaxDef = (Attributes) def[1];
                syntaxDefTree.setup(LdapSchemaCtx.SYNTAX,
                    currentName, syntaxDef);
            } catch (NamingException ne) {
            }
        }
        return syntaxDefTree;
    }
    final private static DirContext matchRuleDescs2MatchRuleDefs(
                                                Attribute matchRuleDescAttr,
                                                LdapSchemaCtx schemaRoot)
        throws NamingException {
        NamingEnumeration       matchRuleDescs;
        Attributes      matchRuleDef;
        LdapSchemaCtx   matchRuleDefTree;
        Attributes attrs = new BasicAttributes(LdapClient.caseIgnore);
        attrs.put(MATCHRULE_DEF_ATTRS[0], MATCHRULE_DEF_ATTRS[1]);
        matchRuleDefTree = schemaRoot.setup(LdapSchemaCtx.MATCHRULE_ROOT,
            MATCHRULE_DEFINITION_NAME, attrs);
        matchRuleDescs = matchRuleDescAttr.getAll();
        String currentName;
        while(matchRuleDescs.hasMore()) {
            String matchRuleDesc = (String)matchRuleDescs.next();
            try {
                Object[] def = desc2Def(matchRuleDesc);
                currentName = (String) def[0];
                matchRuleDef = (Attributes) def[1];
                matchRuleDefTree.setup(LdapSchemaCtx.MATCHRULE,
                    currentName, matchRuleDef);
            } catch (NamingException ne) {
            }
        }
        return matchRuleDefTree;
    }
    final private static Object[] desc2Def(String desc)
        throws NamingException {
        Attributes      attrs = new BasicAttributes(LdapClient.caseIgnore);
        Attribute       attr = null;
        int[]           pos = new int[]{1}; 
        boolean         moreTags = true;
        attr = readNumericOID(desc, pos);
        String currentName = (String) attr.get(0);  
        attrs.put(attr);
        skipWhitespace(desc, pos);
        while (moreTags) {
            attr = readNextTag(desc, pos);
            attrs.put(attr);
            if (attr.getID().equals(NAME_ID)) {
                currentName = (String) attr.get(0);  
            }
            skipWhitespace(desc, pos);
            if( pos[0] >= desc.length() -1 ) {
                moreTags = false;
            }
        }
        return new Object[] {currentName, attrs};
    }
    final private static int findTrailingWhitespace(String string, int pos) {
        for(int i = pos; i > 0; i--) {
            if(string.charAt(i) != WHSP) {
                return i + 1;
            }
        }
        return 0;
    }
    final private static void skipWhitespace(String string, int[] pos) {
        for(int i=pos[0]; i < string.length(); i++) {
            if(string.charAt(i) != WHSP) {
                pos[0] = i;
                if (debug) {
                    System.err.println("skipWhitespace: skipping to "+i);
                }
                return;
            }
        }
    }
    final private static Attribute readNumericOID(String string, int[] pos)
        throws NamingException {
        if (debug) {
            System.err.println("readNumericoid: pos="+pos[0]);
        }
        int begin, end;
        String value = null;
        skipWhitespace(string, pos);
        begin = pos[0];
        end = string.indexOf(WHSP, begin);
        if (end == -1 || end - begin < 1) {
            throw new InvalidAttributeValueException("no numericoid found: "
                                                     + string);
        }
        value = string.substring(begin, end);
        pos[0] += value.length();
        return new BasicAttribute(NUMERICOID_ID, value);
    }
    final private static Attribute readNextTag(String string, int[] pos)
        throws NamingException {
        Attribute       attr = null;
        String          tagName = null;
        String[]        values = null;
        skipWhitespace(string, pos);
        if (debug) {
            System.err.println("readNextTag: pos="+pos[0]);
        }
        int trailingSpace = string.indexOf( WHSP, pos[0] );
        if (trailingSpace < 0) {
            tagName = string.substring( pos[0], string.length() - 1);
        } else {
            tagName = string.substring( pos[0], trailingSpace );
        }
        values = readTag(tagName, string, pos);
        if(values.length < 0) {
            throw new InvalidAttributeValueException("no values for " +
                                                     "attribute \"" +
                                                     tagName + "\"");
        }
        attr = new BasicAttribute(tagName, values[0]);
        for(int i = 1; i < values.length; i++) {
            attr.add(values[i]);
        }
        return attr;
    }
    final private static String[] readTag(String tag, String string, int[] pos)
        throws NamingException {
        if (debug) {
            System.err.println("ReadTag: " + tag + " pos="+pos[0]);
        }
        pos[0] += tag.length();
        skipWhitespace(string, pos);
        if (tag.equals(NAME_ID)) {
            return readQDescrs(string, pos);  
        }
        if(tag.equals(DESC_ID)) {
           return readQDString(string, pos);
        }
        if (
           tag.equals(EQUALITY_ID) ||
           tag.equals(ORDERING_ID) ||
           tag.equals(SUBSTR_ID) ||
           tag.equals(SYNTAX_ID)) {
            return readWOID(string, pos);
        }
        if (tag.equals(OBSOLETE_ID) ||
            tag.equals(ABSTRACT_ID) ||
            tag.equals(STRUCTURAL_ID) ||
            tag.equals(AUXILARY_ID) ||
            tag.equals(SINGLE_VAL_ID) ||
            tag.equals(COLLECTIVE_ID) ||
            tag.equals(NO_USER_MOD_ID)) {
            return new String[] {SCHEMA_TRUE_VALUE};
        }
        if (tag.equals(SUP_ID) ||   
            tag.equals(MUST_ID) ||
            tag.equals(MAY_ID) ||
            tag.equals(USAGE_ID)) {
            return readOIDs(string, pos);
        }
        return readQDStrings(string, pos);
    }
    final private static String[] readQDString(String string, int[] pos)
        throws NamingException {
        int begin, end;
        begin = string.indexOf(SINGLE_QUOTE, pos[0]) + 1;
        end = string.indexOf(SINGLE_QUOTE, begin);
        if (debug) {
            System.err.println("ReadQDString: pos=" + pos[0] +
                               " begin=" + begin + " end=" + end);
        }
        if(begin == -1 || end == -1 || begin == end) {
            throw new InvalidAttributeIdentifierException("malformed " +
                                                          "QDString: " +
                                                          string);
        }
        if (string.charAt(begin - 1) != SINGLE_QUOTE) {
            throw new InvalidAttributeIdentifierException("qdstring has " +
                                                          "no end mark: " +
                                                          string);
        }
        pos[0] = end+1;
        return new String[] {string.substring(begin, end)};
    }
    private final static String[] readQDStrings(String string, int[] pos)
        throws NamingException {
        return readQDescrs(string, pos);
    }
    final private static String[] readQDescrs(String string, int[] pos)
        throws NamingException {
        if (debug) {
            System.err.println("readQDescrs: pos="+pos[0]);
        }
        skipWhitespace(string, pos);
        switch( string.charAt(pos[0]) ) {
        case OID_LIST_BEGIN:
            return readQDescrList(string, pos);
        case SINGLE_QUOTE:
            return readQDString(string, pos);
        default:
            throw new InvalidAttributeValueException("unexpected oids " +
                                                     "string: " + string);
        }
    }
    final private static String[] readQDescrList(String string, int[] pos)
        throws NamingException {
        int     begin, end;
        Vector  values = new Vector(5);
        if (debug) {
            System.err.println("ReadQDescrList: pos="+pos[0]);
        }
        pos[0]++; 
        skipWhitespace(string, pos);
        begin = pos[0];
        end = string.indexOf(OID_LIST_END, begin);
        if(end == -1) {
            throw new InvalidAttributeValueException ("oidlist has no end "+
                                                      "mark: " + string);
        }
        while(begin < end) {
            String[] one = readQDString(string,  pos);
            if (debug) {
                System.err.println("ReadQDescrList: found '" + one[0] +
                                   "' at begin=" + begin + " end =" + end);
            }
            values.addElement(one[0]);
            skipWhitespace(string, pos);
            begin = pos[0];
        }
        pos[0] = end+1; 
        String[] answer = new String[values.size()];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = (String)values.elementAt(i);
        }
        return answer;
    }
    final private static String[] readWOID(String string, int[] pos)
        throws NamingException {
        if (debug) {
            System.err.println("readWOIDs: pos="+pos[0]);
        }
        skipWhitespace(string, pos);
        if (string.charAt(pos[0]) == SINGLE_QUOTE) {
            return readQDString(string, pos);
        }
        int begin, end;
        begin = pos[0];
        end = string.indexOf(WHSP, begin);
        if (debug) {
            System.err.println("ReadWOID: pos=" + pos[0] +
                               " begin=" + begin + " end=" + end);
        }
        if(end == -1 || begin == end) {
            throw new InvalidAttributeIdentifierException("malformed " +
                                                          "OID: " +
                                                          string);
        }
        pos[0] = end+1;
        return new String[] {string.substring(begin, end)};
    }
    final private static String[] readOIDs(String string, int[] pos)
        throws NamingException {
        if (debug) {
            System.err.println("readOIDs: pos="+pos[0]);
        }
        skipWhitespace(string, pos);
        if (string.charAt(pos[0]) != OID_LIST_BEGIN) {
            return readWOID(string, pos);
        }
        int     begin, cur, end;
        String  oidName = null;
        Vector  values = new Vector(5);
        if (debug) {
            System.err.println("ReadOIDList: pos="+pos[0]);
        }
        pos[0]++;
        skipWhitespace(string, pos);
        begin = pos[0];
        end = string.indexOf(OID_LIST_END, begin);
        cur = string.indexOf(OID_SEPARATOR, begin);
        if(end == -1) {
            throw new InvalidAttributeValueException ("oidlist has no end "+
                                                      "mark: " + string);
        }
        if(cur == -1 || end < cur) {
            cur = end;
        }
        while(cur < end && cur > 0) {
            int wsBegin = findTrailingWhitespace(string, cur - 1);
            oidName = string.substring(begin, wsBegin);
            if (debug) {
                System.err.println("ReadOIDList: found '" + oidName +
                                   "' at begin=" + begin + " end =" + end);
            }
            values.addElement(oidName);
            pos[0] = cur + 1;
            skipWhitespace(string, pos);
            begin = pos[0];
            cur = string.indexOf(OID_SEPARATOR, begin);
            if(debug) {System.err.println("ReadOIDList: begin = " + begin);}
        }
        if (debug) {
            System.err.println("ReadOIDList: found '" + oidName +
                               "' at begin=" + begin + " end =" + end);
        }
        int wsBegin = findTrailingWhitespace(string, end - 1);
        oidName = string.substring(begin, wsBegin);
        values.addElement(oidName);
        pos[0] = end+1;
        String[] answer = new String[values.size()];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = (String)values.elementAt(i);
        }
        return answer;
    }
    final private String classDef2ObjectDesc(Attributes attrs)
        throws NamingException {
        StringBuffer objectDesc = new StringBuffer("( ");
        Attribute attr = null;
        int count = 0;
        attr = attrs.get(NUMERICOID_ID);
        if (attr != null) {
            objectDesc.append(writeNumericOID(attr));
            count++;
        } else {
            throw new ConfigurationException("Class definition doesn't" +
                                             "have a numeric OID");
        }
        attr = attrs.get(NAME_ID);
        if (attr != null) {
            objectDesc.append(writeQDescrs(attr));
            count++;
        }
        attr = attrs.get(DESC_ID);
        if (attr != null) {
            objectDesc.append(writeQDString(attr));
            count++;
        }
        attr = attrs.get(OBSOLETE_ID);
        if (attr != null) {
            objectDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(SUP_ID);
        if (attr != null) {
            objectDesc.append(writeOIDs(attr));
            count++;
        }
        attr = attrs.get(ABSTRACT_ID);
        if (attr != null) {
            objectDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(STRUCTURAL_ID);
        if (attr != null) {
            objectDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(AUXILARY_ID);
        if (attr != null) {
            objectDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(MUST_ID);
        if (attr != null) {
            objectDesc.append(writeOIDs(attr));
            count++;
        }
        attr = attrs.get(MAY_ID);
        if (attr != null) {
            objectDesc.append(writeOIDs(attr));
            count++;
        }
        if (count < attrs.size()) {
            String attrId = null;
            for (NamingEnumeration ae = attrs.getAll();
                ae.hasMoreElements(); ) {
                attr = (Attribute)ae.next();
                attrId = attr.getID();
                if (attrId.equals(NUMERICOID_ID) ||
                    attrId.equals(NAME_ID) ||
                    attrId.equals(SUP_ID) ||
                    attrId.equals(MAY_ID) ||
                    attrId.equals(MUST_ID) ||
                    attrId.equals(STRUCTURAL_ID) ||
                    attrId.equals(DESC_ID) ||
                    attrId.equals(AUXILARY_ID) ||
                    attrId.equals(ABSTRACT_ID) ||
                    attrId.equals(OBSOLETE_ID)) {
                    continue;
                } else {
                    objectDesc.append(writeQDStrings(attr));
                }
            }
        }
        objectDesc.append(")");
        return objectDesc.toString();
    }
    final private String attrDef2AttrDesc(Attributes attrs)
        throws NamingException {
        StringBuffer attrDesc = new StringBuffer("( "); 
        Attribute attr = null;
        int count = 0;
        attr = attrs.get(NUMERICOID_ID);
        if (attr != null) {
            attrDesc.append(writeNumericOID(attr));
            count++;
        } else {
            throw new ConfigurationException("Attribute type doesn't" +
                                             "have a numeric OID");
        }
        attr = attrs.get(NAME_ID);
        if (attr != null) {
            attrDesc.append(writeQDescrs(attr));
            count++;
        }
        attr = attrs.get(DESC_ID);
        if (attr != null) {
            attrDesc.append(writeQDString(attr));
            count++;
        }
        attr = attrs.get(OBSOLETE_ID);
        if (attr != null) {
            attrDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(SUP_ID);
        if (attr != null) {
            attrDesc.append(writeWOID(attr));
            count++;
        }
        attr = attrs.get(EQUALITY_ID);
        if (attr != null) {
            attrDesc.append(writeWOID(attr));
            count++;
        }
        attr = attrs.get(ORDERING_ID);
        if (attr != null) {
            attrDesc.append(writeWOID(attr));
            count++;
        }
        attr = attrs.get(SUBSTR_ID);
        if (attr != null) {
            attrDesc.append(writeWOID(attr));
            count++;
        }
        attr = attrs.get(SYNTAX_ID);
        if (attr != null) {
            attrDesc.append(writeWOID(attr));
            count++;
        }
        attr = attrs.get(SINGLE_VAL_ID);
        if (attr != null) {
            attrDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(COLLECTIVE_ID);
        if (attr != null) {
            attrDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(NO_USER_MOD_ID);
        if (attr != null) {
            attrDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(USAGE_ID);
        if (attr != null) {
            attrDesc.append(writeQDString(attr));
            count++;
        }
        if (count < attrs.size()) {
            String attrId = null;
            for (NamingEnumeration ae = attrs.getAll();
                ae.hasMoreElements(); ) {
                attr = (Attribute)ae.next();
                attrId = attr.getID();
                if (attrId.equals(NUMERICOID_ID) ||
                    attrId.equals(NAME_ID) ||
                    attrId.equals(SYNTAX_ID) ||
                    attrId.equals(DESC_ID) ||
                    attrId.equals(SINGLE_VAL_ID) ||
                    attrId.equals(EQUALITY_ID) ||
                    attrId.equals(ORDERING_ID) ||
                    attrId.equals(SUBSTR_ID) ||
                    attrId.equals(NO_USER_MOD_ID) ||
                    attrId.equals(USAGE_ID) ||
                    attrId.equals(SUP_ID) ||
                    attrId.equals(COLLECTIVE_ID) ||
                    attrId.equals(OBSOLETE_ID)) {
                    continue;
                } else {
                    attrDesc.append(writeQDStrings(attr));
                }
            }
        }
        attrDesc.append(")");  
        return attrDesc.toString();
    }
    final private String syntaxDef2SyntaxDesc(Attributes attrs)
        throws NamingException {
        StringBuffer syntaxDesc = new StringBuffer("( "); 
        Attribute attr = null;
        int count = 0;
        attr = attrs.get(NUMERICOID_ID);
        if (attr != null) {
            syntaxDesc.append(writeNumericOID(attr));
            count++;
        } else {
            throw new ConfigurationException("Attribute type doesn't" +
                                             "have a numeric OID");
        }
        attr = attrs.get(DESC_ID);
        if (attr != null) {
            syntaxDesc.append(writeQDString(attr));
            count++;
        }
        if (count < attrs.size()) {
            String attrId = null;
            for (NamingEnumeration ae = attrs.getAll();
                ae.hasMoreElements(); ) {
                attr = (Attribute)ae.next();
                attrId = attr.getID();
                if (attrId.equals(NUMERICOID_ID) ||
                    attrId.equals(DESC_ID)) {
                    continue;
                } else {
                    syntaxDesc.append(writeQDStrings(attr));
                }
            }
        }
        syntaxDesc.append(")");
        return syntaxDesc.toString();
    }
    final private String matchRuleDef2MatchRuleDesc(Attributes attrs)
        throws NamingException {
        StringBuffer matchRuleDesc = new StringBuffer("( "); 
        Attribute attr = null;
        int count = 0;
        attr = attrs.get(NUMERICOID_ID);
        if (attr != null) {
            matchRuleDesc.append(writeNumericOID(attr));
            count++;
        } else {
            throw new ConfigurationException("Attribute type doesn't" +
                                             "have a numeric OID");
        }
        attr = attrs.get(NAME_ID);
        if (attr != null) {
            matchRuleDesc.append(writeQDescrs(attr));
            count++;
        }
        attr = attrs.get(DESC_ID);
        if (attr != null) {
            matchRuleDesc.append(writeQDString(attr));
            count++;
        }
        attr = attrs.get(OBSOLETE_ID);
        if (attr != null) {
            matchRuleDesc.append(writeBoolean(attr));
            count++;
        }
        attr = attrs.get(SYNTAX_ID);
        if (attr != null) {
            matchRuleDesc.append(writeWOID(attr));
            count++;
        } else {
            throw new ConfigurationException("Attribute type doesn't" +
                                             "have a syntax OID");
        }
        if (count < attrs.size()) {
            String attrId = null;
            for (NamingEnumeration ae = attrs.getAll();
                ae.hasMoreElements(); ) {
                attr = (Attribute)ae.next();
                attrId = attr.getID();
                if (attrId.equals(NUMERICOID_ID) ||
                    attrId.equals(NAME_ID) ||
                    attrId.equals(SYNTAX_ID) ||
                    attrId.equals(DESC_ID) ||
                    attrId.equals(OBSOLETE_ID)) {
                    continue;
                } else {
                    matchRuleDesc.append(writeQDStrings(attr));
                }
            }
        }
        matchRuleDesc.append(")");
        return matchRuleDesc.toString();
    }
    final private String writeNumericOID(Attribute nOIDAttr)
        throws NamingException {
        if(nOIDAttr.size() != 1) {
            throw new InvalidAttributeValueException(
                "A class definition must have exactly one numeric OID");
        }
        return (String)(nOIDAttr.get()) + WHSP;
    }
    final private String writeWOID(Attribute attr) throws NamingException {
        if (netscapeBug)
            return writeQDString(attr);
        else
            return attr.getID() + WHSP + attr.get() + WHSP;
    }
    final private String writeQDString(Attribute qdStringAttr)
        throws NamingException {
        if(qdStringAttr.size() != 1) {
            throw new InvalidAttributeValueException(
                qdStringAttr.getID() + " must have exactly one value");
        }
        return qdStringAttr.getID() + WHSP +
            SINGLE_QUOTE + qdStringAttr.get() + SINGLE_QUOTE + WHSP;
    }
    private final String writeQDStrings(Attribute attr) throws NamingException {
        return writeQDescrs(attr);
    }
    private final String writeQDescrs(Attribute attr) throws NamingException {
        switch(attr.size()) {
        case 0:
            throw new InvalidAttributeValueException(
                attr.getID() + "has no values");
        case 1:
            return writeQDString(attr);
        }
        StringBuffer qdList = new StringBuffer(attr.getID());
        qdList.append(WHSP);
        qdList.append(OID_LIST_BEGIN);
        NamingEnumeration values = attr.getAll();
        while(values.hasMore()) {
            qdList.append(WHSP);
            qdList.append(SINGLE_QUOTE);
            qdList.append((String)values.next());
            qdList.append(SINGLE_QUOTE);
            qdList.append(WHSP);
        }
        qdList.append(OID_LIST_END);
        qdList.append(WHSP);
        return qdList.toString();
    }
    final private String writeOIDs(Attribute oidsAttr)
        throws NamingException {
        switch(oidsAttr.size()) {
        case 0:
            throw new InvalidAttributeValueException(
                oidsAttr.getID() + "has no values");
        case 1:
            if (netscapeBug) {
                break; 
            }
            return writeWOID(oidsAttr);
        }
        StringBuffer oidList = new StringBuffer(oidsAttr.getID());
        oidList.append(WHSP);
        oidList.append(OID_LIST_BEGIN);
        NamingEnumeration values = oidsAttr.getAll();
        oidList.append(WHSP);
        oidList.append(values.next());
        while(values.hasMore()) {
            oidList.append(WHSP);
            oidList.append(OID_SEPARATOR);
            oidList.append(WHSP);
            oidList.append((String)values.next());
        }
        oidList.append(WHSP);
        oidList.append(OID_LIST_END);
        oidList.append(WHSP);
        return oidList.toString();
    }
    private final String writeBoolean(Attribute booleanAttr)
        throws NamingException {
            return booleanAttr.getID() + WHSP;
    }
    final Attribute stringifyObjDesc(Attributes classDefAttrs)
        throws NamingException {
        Attribute objDescAttr = new BasicAttribute(OBJECTCLASSDESC_ATTR_ID);
        objDescAttr.add(classDef2ObjectDesc(classDefAttrs));
        return objDescAttr;
    }
    final Attribute stringifyAttrDesc(Attributes attrDefAttrs)
        throws NamingException {
        Attribute attrDescAttr = new BasicAttribute(ATTRIBUTEDESC_ATTR_ID);
        attrDescAttr.add(attrDef2AttrDesc(attrDefAttrs));
        return attrDescAttr;
    }
    final Attribute stringifySyntaxDesc(Attributes syntaxDefAttrs)
    throws NamingException {
        Attribute syntaxDescAttr = new BasicAttribute(SYNTAXDESC_ATTR_ID);
        syntaxDescAttr.add(syntaxDef2SyntaxDesc(syntaxDefAttrs));
        return syntaxDescAttr;
    }
    final Attribute stringifyMatchRuleDesc(Attributes matchRuleDefAttrs)
    throws NamingException {
        Attribute matchRuleDescAttr = new BasicAttribute(MATCHRULEDESC_ATTR_ID);
        matchRuleDescAttr.add(matchRuleDef2MatchRuleDesc(matchRuleDefAttrs));
        return matchRuleDescAttr;
    }
}
