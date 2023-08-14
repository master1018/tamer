public class SearchFilter implements AttrFilter {
    interface StringFilter extends AttrFilter {
        public void parse() throws InvalidSearchFilterException;
    }
    String                      filter;
    int                         pos;
    private StringFilter        rootFilter;
    protected static final boolean debug = false;
    protected static final char         BEGIN_FILTER_TOKEN = '(';
    protected static final char         END_FILTER_TOKEN = ')';
    protected static final char         AND_TOKEN = '&';
    protected static final char         OR_TOKEN = '|';
    protected static final char         NOT_TOKEN = '!';
    protected static final char         EQUAL_TOKEN = '=';
    protected static final char         APPROX_TOKEN = '~';
    protected static final char         LESS_TOKEN = '<';
    protected static final char         GREATER_TOKEN = '>';
    protected static final char         EXTEND_TOKEN = ':';
    protected static final char         WILDCARD_TOKEN = '*';
    public SearchFilter(String filter) throws InvalidSearchFilterException {
        this.filter = filter;
        pos = 0;
        normalizeFilter();
        rootFilter = this.createNextFilter();
    }
    public boolean check(Attributes targetAttrs) throws NamingException {
        if (targetAttrs == null)
            return false;
        return rootFilter.check(targetAttrs);
    }
    protected void normalizeFilter() {
        skipWhiteSpace(); 
        if(getCurrentChar() != BEGIN_FILTER_TOKEN) {
            filter = BEGIN_FILTER_TOKEN + filter + END_FILTER_TOKEN;
        }
        if(debug) {System.out.println("SearchFilter: normalized filter:" +
                                      filter);}
    }
    private void skipWhiteSpace() {
        while (Character.isWhitespace(getCurrentChar())) {
            consumeChar();
        }
    }
    protected StringFilter createNextFilter()
        throws InvalidSearchFilterException {
        StringFilter filter;
        skipWhiteSpace();
        try {
            if(getCurrentChar() != BEGIN_FILTER_TOKEN) {
                throw new InvalidSearchFilterException("expected \"" +
                                                       BEGIN_FILTER_TOKEN +
                                                       "\" at position " +
                                                       pos);
            }
            this.consumeChar();
            skipWhiteSpace();
            switch(getCurrentChar()) {
            case AND_TOKEN:
                if (debug) {System.out.println("SearchFilter: creating AND");}
                filter = new CompoundFilter(true);
                filter.parse();
                break;
            case OR_TOKEN:
                if (debug) {System.out.println("SearchFilter: creating OR");}
                filter = new CompoundFilter(false);
                filter.parse();
                break;
            case NOT_TOKEN:
                if (debug) {System.out.println("SearchFilter: creating OR");}
                filter = new NotFilter();
                filter.parse();
                break;
            default:
                if (debug) {System.out.println("SearchFilter: creating SIMPLE");}
                filter = new AtomicFilter();
                filter.parse();
                break;
            }
            skipWhiteSpace();
            if(getCurrentChar() != END_FILTER_TOKEN) {
                throw new InvalidSearchFilterException("expected \"" +
                                                       END_FILTER_TOKEN +
                                                       "\" at position " +
                                                       pos);
            }
            this.consumeChar();
        } catch (InvalidSearchFilterException e) {
            if (debug) {System.out.println("rethrowing e");}
            throw e; 
        } catch  (Exception e) {
            if(debug) {System.out.println(e.getMessage());e.printStackTrace();}
            throw new InvalidSearchFilterException("Unable to parse " +
                    "character " + pos + " in \""+
                    this.filter + "\"");
        }
        return filter;
    }
    protected char getCurrentChar() {
        return filter.charAt(pos);
    }
    protected char relCharAt(int i) {
        return filter.charAt(pos + i);
    }
    protected void consumeChar() {
        pos++;
    }
    protected void consumeChars(int i) {
        pos += i;
    }
    protected int relIndexOf(int ch) {
        return filter.indexOf(ch, pos) - pos;
    }
    protected String relSubstring(int beginIndex, int endIndex){
        if(debug){System.out.println("relSubString: " + beginIndex +
                                     " " + endIndex);}
        return filter.substring(beginIndex+pos, endIndex+pos);
    }
    final class CompoundFilter implements StringFilter {
        private Vector  subFilters;
        private boolean polarity;
        CompoundFilter(boolean polarity) {
            subFilters = new Vector();
            this.polarity = polarity;
        }
        public void parse() throws InvalidSearchFilterException {
            SearchFilter.this.consumeChar(); 
            while(SearchFilter.this.getCurrentChar() != END_FILTER_TOKEN) {
                if (debug) {System.out.println("CompoundFilter: adding");}
                StringFilter filter = SearchFilter.this.createNextFilter();
                subFilters.addElement(filter);
                skipWhiteSpace();
            }
        }
        public boolean check(Attributes targetAttrs) throws NamingException {
            for(int i = 0; i<subFilters.size(); i++) {
                StringFilter filter = (StringFilter)subFilters.elementAt(i);
                if(filter.check(targetAttrs) != this.polarity) {
                    return !polarity;
                }
            }
            return polarity;
        }
    } 
    final class NotFilter implements StringFilter {
        private StringFilter    filter;
        public void parse() throws InvalidSearchFilterException {
            SearchFilter.this.consumeChar(); 
            filter = SearchFilter.this.createNextFilter();
        }
        public boolean check(Attributes targetAttrs) throws NamingException {
            return !filter.check(targetAttrs);
        }
    } 
    static final int EQUAL_MATCH = 1;
    static final int APPROX_MATCH = 2;
    static final int GREATER_MATCH = 3;
    static final int LESS_MATCH = 4;
    final class AtomicFilter implements StringFilter {
        private String attrID;
        private String value;
        private int    matchType;
        public void parse() throws InvalidSearchFilterException {
            skipWhiteSpace();
            try {
                int endPos = SearchFilter.this.relIndexOf(END_FILTER_TOKEN);
                int i = SearchFilter.this.relIndexOf(EQUAL_TOKEN);
                if(debug) {System.out.println("AtomicFilter: = at " + i);}
                int qualifier = SearchFilter.this.relCharAt(i-1);
                switch(qualifier) {
                case APPROX_TOKEN:
                    if (debug) {System.out.println("Atomic: APPROX found");}
                    matchType = APPROX_MATCH;
                    attrID = SearchFilter.this.relSubstring(0, i-1);
                    value = SearchFilter.this.relSubstring(i+1, endPos);
                    break;
                case GREATER_TOKEN:
                    if (debug) {System.out.println("Atomic: GREATER found");}
                    matchType = GREATER_MATCH;
                    attrID = SearchFilter.this.relSubstring(0, i-1);
                    value = SearchFilter.this.relSubstring(i+1, endPos);
                    break;
                case LESS_TOKEN:
                    if (debug) {System.out.println("Atomic: LESS found");}
                    matchType = LESS_MATCH;
                    attrID = SearchFilter.this.relSubstring(0, i-1);
                    value = SearchFilter.this.relSubstring(i+1, endPos);
                    break;
                case EXTEND_TOKEN:
                    if(debug) {System.out.println("Atomic: EXTEND found");}
                    throw new OperationNotSupportedException("Extensible match not supported");
                default:
                    if (debug) {System.out.println("Atomic: EQUAL found");}
                    matchType = EQUAL_MATCH;
                    attrID = SearchFilter.this.relSubstring(0,i);
                    value = SearchFilter.this.relSubstring(i+1, endPos);
                    break;
                }
                attrID = attrID.trim();
                value = value.trim();
                SearchFilter.this.consumeChars(endPos);
            } catch (Exception e) {
                if (debug) {System.out.println(e.getMessage());
                            e.printStackTrace();}
                InvalidSearchFilterException sfe =
                    new InvalidSearchFilterException("Unable to parse " +
                    "character " + SearchFilter.this.pos + " in \""+
                    SearchFilter.this.filter + "\"");
                sfe.setRootCause(e);
                throw(sfe);
            }
            if(debug) {System.out.println("AtomicFilter: " + attrID + "=" +
                                          value);}
        }
        public boolean check(Attributes targetAttrs) {
            Enumeration candidates;
            try {
                Attribute attr = targetAttrs.get(attrID);
                if(attr == null) {
                    return false;
                }
                candidates = attr.getAll();
            } catch (NamingException ne) {
                if (debug) {System.out.println("AtomicFilter: should never " +
                                               "here");}
                return false;
            }
            while(candidates.hasMoreElements()) {
                String val = candidates.nextElement().toString();
                if (debug) {System.out.println("Atomic: comparing: " + val);}
                switch(matchType) {
                case APPROX_MATCH:
                case EQUAL_MATCH:
                    if(substringMatch(this.value, val)) {
                    if (debug) {System.out.println("Atomic: EQUAL match");}
                        return true;
                    }
                    break;
                case GREATER_MATCH:
                    if (debug) {System.out.println("Atomic: GREATER match");}
                    if(val.compareTo(this.value) >= 0) {
                        return true;
                    }
                    break;
                case LESS_MATCH:
                    if (debug) {System.out.println("Atomic: LESS match");}
                    if(val.compareTo(this.value) <= 0) {
                        return true;
                    }
                    break;
                default:
                    if (debug) {System.out.println("AtomicFilter: unkown " +
                                                   "matchType");}
                }
            }
            return false;
        }
        private boolean substringMatch(String proto, String value) {
            if(proto.equals(new Character(WILDCARD_TOKEN).toString())) {
                if(debug) {System.out.println("simple presence assertion");}
                return true;
            }
            if(proto.indexOf(WILDCARD_TOKEN) == -1) {
                return proto.equalsIgnoreCase(value);
            }
            if(debug) {System.out.println("doing substring comparison");}
            int currentPos = 0;
            StringTokenizer subStrs = new StringTokenizer(proto, "*", false);
            if(proto.charAt(0) != WILDCARD_TOKEN &&
               !value.toString().toLowerCase().startsWith(
                      subStrs.nextToken().toLowerCase())) {
                if(debug) {System.out.println("faild initial test");}
                return false;
            }
            while(subStrs.hasMoreTokens()) {
                String currentStr = subStrs.nextToken();
                if (debug) {System.out.println("looking for \"" +
                                               currentStr +"\"");}
                currentPos = value.toLowerCase().indexOf(
                       currentStr.toLowerCase(), currentPos);
                if(currentPos == -1) {
                    return false;
                }
                currentPos += currentStr.length();
            }
            if(proto.charAt(proto.length() - 1) != WILDCARD_TOKEN &&
               currentPos != value.length() ) {
                if(debug) {System.out.println("faild final test");}
                return false;
            }
            return true;
        }
    } 
    public static String format(Attributes attrs) throws NamingException {
        if (attrs == null || attrs.size() == 0) {
            return "objectClass=*";
        }
        String answer;
        answer = "(& ";
        Attribute attr;
        for (NamingEnumeration e = attrs.getAll(); e.hasMore(); ) {
            attr = (Attribute)e.next();
            if (attr.size() == 0 || (attr.size() == 1 && attr.get() == null)) {
                answer += "(" + attr.getID() + "=" + "*)";
            } else {
                for (NamingEnumeration ve = attr.getAll();
                     ve.hasMore();
                        ) {
                    String val = getEncodedStringRep(ve.next());
                    if (val != null) {
                        answer += "(" + attr.getID() + "=" + val + ")";
                    }
                }
            }
        }
        answer += ")";
        return answer;
    }
    private static void hexDigit(StringBuffer buf, byte x) {
        char c;
        c = (char) ((x >> 4) & 0xf);
        if (c > 9)
            c = (char) ((c-10) + 'A');
        else
            c = (char)(c + '0');
        buf.append(c);
        c = (char) (x & 0xf);
        if (c > 9)
            c = (char)((c-10) + 'A');
        else
            c = (char)(c + '0');
        buf.append(c);
    }
    private static String getEncodedStringRep(Object obj) throws NamingException {
        String str;
        if (obj == null)
            return null;
        if (obj instanceof byte[]) {
            byte[] bytes = (byte[])obj;
            StringBuffer b1 = new StringBuffer(bytes.length*3);
            for (int i = 0; i < bytes.length; i++) {
                b1.append('\\');
                hexDigit(b1, bytes[i]);
            }
            return b1.toString();
        }
        if (!(obj instanceof String)) {
            str = obj.toString();
        } else {
            str = (String)obj;
        }
        int len = str.length();
        StringBuffer buf = new StringBuffer(len);
        char ch;
        for (int i = 0; i < len; i++) {
            switch (ch=str.charAt(i)) {
            case '*':
                buf.append("\\2a");
                break;
            case '(':
                buf.append("\\28");
                break;
            case ')':
                buf.append("\\29");
                break;
            case '\\':
                buf.append("\\5c");
                break;
            case 0:
                buf.append("\\00");
                break;
            default:
                buf.append(ch);
            }
        }
        return buf.toString();
    }
    public static int findUnescaped(char ch, String val, int start) {
        int len = val.length();
        while (start < len) {
            int where = val.indexOf(ch, start);
            if (where == start || where == -1 || val.charAt(where-1) != '\\')
                return where;
            start = where + 1;
        }
        return -1;
    }
    public static String format(String expr, Object[] args)
        throws NamingException {
         int param;
         int where = 0, start = 0;
         StringBuffer answer = new StringBuffer(expr.length());
         while ((where = findUnescaped('{', expr, start)) >= 0) {
             int pstart = where + 1; 
             int pend = expr.indexOf('}', pstart);
             if (pend < 0) {
                 throw new InvalidSearchFilterException("unbalanced {: " + expr);
             }
             try {
                 param = Integer.parseInt(expr.substring(pstart, pend));
             } catch (NumberFormatException e) {
                 throw new InvalidSearchFilterException(
                     "integer expected inside {}: " + expr);
             }
             if (param >= args.length) {
                 throw new InvalidSearchFilterException(
                     "number exceeds argument list: " + param);
             }
             answer.append(expr.substring(start, where)).append(getEncodedStringRep(args[param]));
             start = pend + 1; 
         }
         if (start < expr.length())
             answer.append(expr.substring(start));
        return answer.toString();
    }
    public static Attributes selectAttributes(Attributes originals,
        String[] attrIDs) throws NamingException {
        if (attrIDs == null)
            return originals;
        Attributes result = new BasicAttributes();
        for(int i=0; i<attrIDs.length; i++) {
            Attribute attr = originals.get(attrIDs[i]);
            if(attr != null) {
                result.put(attr);
            }
        }
        return result;
    }
}
