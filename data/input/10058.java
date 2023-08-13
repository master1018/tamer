class MatchQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -7156603696948215014L;
    private AttributeValueExp exp;
    private String pattern;
    public MatchQueryExp() {
    }
    public MatchQueryExp(AttributeValueExp a, StringValueExp s) {
        exp     = a;
        pattern = s.getValue();
    }
    public AttributeValueExp getAttribute()  {
        return exp;
    }
    public String getPattern()  {
        return pattern;
    }
    public boolean apply(ObjectName name) throws
        BadStringOperationException,
        BadBinaryOpValueExpException,
        BadAttributeValueExpException,
        InvalidApplicationException {
        ValueExp val = exp.apply(name);
        if (!(val instanceof StringValueExp)) {
            return false;
        }
        return wildmatch(((StringValueExp)val).getValue(), pattern);
    }
    public String toString()  {
        return exp + " like " + new StringValueExp(pattern);
    }
    private static boolean wildmatch(String s, String p) {
        char c;
        int si = 0, pi = 0;
        int slen = s.length();
        int plen = p.length();
        while (pi < plen) { 
            c = p.charAt(pi++);
            if (c == '?') {
                if (++si > slen)
                    return false;
            } else if (c == '[') { 
                if (si >= slen)
                    return false;
                boolean wantit = true;
                boolean seenit = false;
                if (p.charAt(pi) == '!') {
                    wantit = false;
                    ++pi;
                }
                while ((c = p.charAt(pi)) != ']' && ++pi < plen) {
                    if (p.charAt(pi) == '-' &&
                        pi+1 < plen &&
                        p.charAt(pi+1) != ']') {
                        if (s.charAt(si) >= p.charAt(pi-1) &&
                            s.charAt(si) <= p.charAt(pi+1)) {
                            seenit = true;
                        }
                        ++pi;
                    } else {
                        if (c == s.charAt(si)) {
                            seenit = true;
                        }
                    }
                }
                if ((pi >= plen) || (wantit != seenit)) {
                    return false;
                }
                ++pi;
                ++si;
            } else if (c == '*') { 
                if (pi >= plen)
                    return true;
                do {
                    if (wildmatch(s.substring(si), p.substring(pi)))
                        return true;
                } while (++si < slen);
                return false;
            } else if (c == '\\') {
                if (pi >= plen || si >= slen ||
                    p.charAt(pi++) != s.charAt(si++))
                    return false;
            } else {
                if (si >= slen || c != s.charAt(si++)) {
                    return false;
                }
            }
        }
        return (si == slen);
    }
 }
