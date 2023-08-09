public class HeaderParser {
    String raw;
    String[][] tab;
    int nkeys;
    int asize = 10; 
    public HeaderParser(String raw) {
        this.raw = raw;
        tab = new String[asize][2];
        parse();
    }
    private HeaderParser () {
    }
    public HeaderParser subsequence (int start, int end) {
        if (start == 0 && end == nkeys) {
            return this;
        }
        if (start < 0 || start >= end || end > nkeys)
            throw new IllegalArgumentException ("invalid start or end");
        HeaderParser n = new HeaderParser ();
        n.tab = new String [asize][2];
        n.asize = asize;
        System.arraycopy (tab, start, n.tab, 0, (end-start));
        n.nkeys= (end-start);
        return n;
    }
    private void parse() {
        if (raw != null) {
            raw = raw.trim();
            char[] ca = raw.toCharArray();
            int beg = 0, end = 0, i = 0;
            boolean inKey = true;
            boolean inQuote = false;
            int len = ca.length;
            while (end < len) {
                char c = ca[end];
                if ((c == '=') && !inQuote) { 
                    tab[i][0] = new String(ca, beg, end-beg).toLowerCase();
                    inKey = false;
                    end++;
                    beg = end;
                } else if (c == '\"') {
                    if (inQuote) {
                        tab[i++][1]= new String(ca, beg, end-beg);
                        inQuote=false;
                        do {
                            end++;
                        } while (end < len && (ca[end] == ' ' || ca[end] == ','));
                        inKey=true;
                        beg=end;
                    } else {
                        inQuote=true;
                        end++;
                        beg=end;
                    }
                } else if (c == ' ' || c == ',') { 
                    if (inQuote) {
                        end++;
                        continue;
                    } else if (inKey) {
                        tab[i++][0] = (new String(ca, beg, end-beg)).toLowerCase();
                    } else {
                        tab[i++][1] = (new String(ca, beg, end-beg));
                    }
                    while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
                        end++;
                    }
                    inKey = true;
                    beg = end;
                } else {
                    end++;
                }
                if (i == asize) {
                    asize = asize * 2;
                    String[][] ntab = new String[asize][2];
                    System.arraycopy (tab, 0, ntab, 0, tab.length);
                    tab = ntab;
                }
            }
            if (--end > beg) {
                if (!inKey) {
                    if (ca[end] == '\"') {
                        tab[i++][1] = (new String(ca, beg, end-beg));
                    } else {
                        tab[i++][1] = (new String(ca, beg, end-beg+1));
                    }
                } else {
                    tab[i++][0] = (new String(ca, beg, end-beg+1)).toLowerCase();
                }
            } else if (end == beg) {
                if (!inKey) {
                    if (ca[end] == '\"') {
                        tab[i++][1] = String.valueOf(ca[end-1]);
                    } else {
                        tab[i++][1] = String.valueOf(ca[end]);
                    }
                } else {
                    tab[i++][0] = String.valueOf(ca[end]).toLowerCase();
                }
            }
            nkeys=i;
        }
    }
    public String findKey(int i) {
        if (i < 0 || i > asize)
            return null;
        return tab[i][0];
    }
    public String findValue(int i) {
        if (i < 0 || i > asize)
            return null;
        return tab[i][1];
    }
    public String findValue(String key) {
        return findValue(key, null);
    }
    public String findValue(String k, String Default) {
        if (k == null)
            return Default;
        k = k.toLowerCase();
        for (int i = 0; i < asize; ++i) {
            if (tab[i][0] == null) {
                return Default;
            } else if (k.equals(tab[i][0])) {
                return tab[i][1];
            }
        }
        return Default;
    }
    class ParserIterator implements Iterator {
        int index;
        boolean returnsValue; 
        ParserIterator (boolean returnValue) {
            returnsValue = returnValue;
        }
        public boolean hasNext () {
            return index<nkeys;
        }
        public Object next () {
            return tab[index++][returnsValue?1:0];
        }
        public void remove () {
            throw new UnsupportedOperationException ("remove not supported");
        }
    }
    public Iterator keys () {
        return new ParserIterator (false);
    }
    public Iterator values () {
        return new ParserIterator (true);
    }
    public String toString () {
        Iterator k = keys();
        StringBuffer sbuf = new StringBuffer();
        sbuf.append ("{size="+asize+" nkeys="+nkeys+" ");
        for (int i=0; k.hasNext(); i++) {
            String key = (String)k.next();
            String val = findValue (i);
            if (val != null && "".equals (val)) {
                val = null;
            }
            sbuf.append (" {"+key+(val==null?"":","+val)+"}");
            if (k.hasNext()) {
                sbuf.append (",");
            }
        }
        sbuf.append (" }");
        return new String (sbuf);
    }
    public int findInt(String k, int Default) {
        try {
            return Integer.parseInt(findValue(k, String.valueOf(Default)));
        } catch (Throwable t) {
            return Default;
        }
    }
}
