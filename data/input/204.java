final public class CNNameParser implements NameParser {
    private static final Properties mySyntax = new Properties();
    private static final char kindSeparator = '.';
    private static final char compSeparator = '/';
    private static final char escapeChar = '\\';
    static {
        mySyntax.put("jndi.syntax.direction", "left_to_right");
        mySyntax.put("jndi.syntax.separator", ""+compSeparator);
        mySyntax.put("jndi.syntax.escape", ""+escapeChar);
    };
    public CNNameParser() {
    }
    public Name parse(String name) throws NamingException {
        Vector comps = insStringToStringifiedComps(name);
        return new CNCompoundName(comps.elements());
    }
    static NameComponent[] nameToCosName(Name name)
        throws InvalidNameException {
            int len = name.size();
            if (len == 0) {
                return new NameComponent[0];
            }
            NameComponent[] answer = new NameComponent[len];
            for (int i = 0; i < len; i++) {
                answer[i] = parseComponent(name.get(i));
            }
            return answer;
    }
    static String cosNameToInsString(NameComponent[] cname) {
      StringBuffer str = new StringBuffer();
      for ( int i = 0; i < cname.length; i++) {
          if ( i > 0) {
              str.append(compSeparator);
          }
          str.append(stringifyComponent(cname[i]));
      }
      return str.toString();
    }
    static Name cosNameToName(NameComponent[] cname) {
        Name nm = new CompositeName();
        for ( int i = 0; cname != null && i < cname.length; i++) {
            try {
                nm.add(stringifyComponent(cname[i]));
            } catch (InvalidNameException e) {
            }
        }
        return nm;
    }
    private static Vector insStringToStringifiedComps(String str)
        throws InvalidNameException {
        int len = str.length();
        Vector components = new Vector(10);
        char[] id = new char[len];
        char[] kind = new char[len];
        int idCount, kindCount;
        boolean idMode;
        for (int i = 0; i < len; ) {
            idCount = kindCount = 0; 
            idMode = true;           
            while (i < len) {
                if (str.charAt(i) == compSeparator) {
                    break;
                } else if (str.charAt(i) == escapeChar) {
                    if (i + 1 >= len) {
                        throw new InvalidNameException(str +
                            ": unescaped \\ at end of component");
                    } else if (isMeta(str.charAt(i+1))) {
                        ++i; 
                        if (idMode) {
                            id[idCount++] = str.charAt(i++);
                        } else {
                            kind[kindCount++] = str.charAt(i++);
                        }
                    } else {
                        throw new InvalidNameException(str +
                            ": invalid character being escaped");
                    }
                } else if (idMode && str.charAt(i) == kindSeparator) {
                    ++i; 
                    idMode = false;
                } else {
                    if (idMode) {
                        id[idCount++] = str.charAt(i++);
                    } else {
                        kind[kindCount++] = str.charAt(i++);
                    }
                }
            }
            components.addElement(stringifyComponent(
                new NameComponent(new String(id, 0, idCount),
                    new String(kind, 0, kindCount))));
            if (i < len) {
                ++i; 
            }
        }
        return components;
    }
    private static NameComponent parseComponent(String compStr)
    throws InvalidNameException {
        NameComponent comp = new NameComponent();
        int kindSep = -1;
        int len = compStr.length();
        int j = 0;
        char[] newStr = new char[len];
        boolean escaped = false;
        for (int i = 0; i < len && kindSep < 0; i++) {
            if (escaped) {
                newStr[j++] = compStr.charAt(i);
                escaped = false;
            } else if (compStr.charAt(i) == escapeChar) {
                if (i + 1 >= len) {
                    throw new InvalidNameException(compStr +
                            ": unescaped \\ at end of component");
                } else if (isMeta(compStr.charAt(i+1))) {
                    escaped = true;
                } else {
                    throw new InvalidNameException(compStr +
                        ": invalid character being escaped");
                }
            } else if (compStr.charAt(i) == kindSeparator) {
                kindSep = i;
            } else {
                newStr[j++] = compStr.charAt(i);
            }
        }
        comp.id = new String(newStr, 0, j);
        if (kindSep < 0) {
            comp.kind = "";  
        } else {
            j = 0;
            escaped = false;
            for (int i = kindSep+1; i < len; i++) {
                if (escaped) {
                    newStr[j++] = compStr.charAt(i);
                    escaped = false;
                } else if (compStr.charAt(i) == escapeChar) {
                    if (i + 1 >= len) {
                        throw new InvalidNameException(compStr +
                            ": unescaped \\ at end of component");
                    } else if (isMeta(compStr.charAt(i+1))) {
                        escaped = true;
                    } else {
                        throw new InvalidNameException(compStr +
                            ": invalid character being escaped");
                    }
                } else {
                    newStr[j++] = compStr.charAt(i);
                }
            }
            comp.kind = new String(newStr, 0, j);
        }
        return comp;
    }
    private static String stringifyComponent(NameComponent comp) {
        StringBuffer one = new StringBuffer(escape(comp.id));
        if (comp.kind != null && !comp.kind.equals("")) {
            one.append(kindSeparator + escape(comp.kind));
        }
        if (one.length() == 0) {
            return ""+kindSeparator;  
        } else {
            return one.toString();
        }
    }
    private static String escape(String str) {
        if (str.indexOf(kindSeparator) < 0 &&
            str.indexOf(compSeparator) < 0 &&
            str.indexOf(escapeChar) < 0) {
            return str;                         
        } else {
            int len = str.length();
            int j = 0;
            char[] newStr = new char[len+len];
            for (int i = 0; i < len; i++) {
                if (isMeta(str.charAt(i))) {
                    newStr[j++] = escapeChar;   
                }
                newStr[j++] = str.charAt(i);
            }
            return new String(newStr, 0, j);
        }
    }
    private static boolean isMeta(char ch) {
        switch (ch) {
        case kindSeparator:
        case compSeparator:
        case escapeChar:
            return true;
        }
        return false;
    }
    static final class CNCompoundName extends CompoundName {
        CNCompoundName(Enumeration enum_) {
            super(enum_, CNNameParser.mySyntax);
        }
        public Object clone() {
            return new CNCompoundName(getAll());
        }
        public Name getPrefix(int posn) {
            Enumeration comps = super.getPrefix(posn).getAll();
            return new CNCompoundName(comps);
        }
        public Name getSuffix(int posn) {
            Enumeration comps = super.getSuffix(posn).getAll();
            return new CNCompoundName(comps);
        }
        public String toString() {
            try {
                return cosNameToInsString(nameToCosName(this));
            } catch (InvalidNameException e) {
                return super.toString();
            }
        }
        private static final long serialVersionUID = -6599252802678482317L;
    }
}
