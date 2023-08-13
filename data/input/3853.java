public class TokenList extends ArrayList<String> implements CharSequence {
    protected String separator;
    protected boolean frozen;
    public TokenList() {
        this.separator = " ";
    }
    public TokenList(Collection<? extends Object> tokens) {
        super(tokens.size());
        this.separator = " ";
        addTokens(tokens);
    }
    public TokenList(Collection<? extends Object> tokens, String separator) {
        super(tokens.size());
        this.separator = separator;
        addTokens(tokens);
    }
    public TokenList(Object[] tokens) {
        super(tokens.length);
        this.separator = " ";
        addTokens(tokens, 0, tokens.length);
    }
    public TokenList(Object[] tokens, int beg, int end) {
        super(end - beg);  
        this.separator = " ";
        addTokens(tokens, beg, end);
    }
    public TokenList(Object[] tokens, int beg, int end, String separator) {
        super(end - beg);  
        this.separator = separator;
        addTokens(tokens, beg, end);
    }
    public TokenList(String tokenStr) {
        this(tokenStr, " ", false);
    }
    public TokenList(String tokenStr, String separator) {
        this(tokenStr, separator, true);
    }
    public TokenList(String tokenStr, String separator, boolean allowNulls) {
        super(tokenStr.length() / 5);
        this.separator = separator;
        addTokens(tokenStr, allowNulls);
    }
    static public final TokenList EMPTY;
    static {
        TokenList tl = new TokenList(new Object[0]);
        tl.freeze();
        EMPTY = tl;
    }
    public void freeze() {
        if (!frozen) {
            for (ListIterator<String> i = listIterator(); i.hasNext();) {
                i.set(i.next().toString());
            }
            trimToSize();
            frozen = true;
        }
    }
    public boolean isFrozen() {
        return frozen;
    }
    void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("cannot modify frozen TokenList");
        }
    }
    public String getSeparator() {
        return separator;
    }
    public void setSeparator(String separator) {
        checkNotFrozen();
        this.separator = separator;
    }
    public String set(int index, String o) {
        checkNotFrozen();
        return super.set(index, o);
    }
    public boolean add(String o) {
        checkNotFrozen();
        return super.add(o);
    }
    public void add(int index, String o) {
        checkNotFrozen();
        super.add(index, o);
    }
    public boolean addAll(Collection<? extends String> c) {
        checkNotFrozen();
        return super.addAll(c);
    }
    public boolean addAll(int index, Collection<? extends String> c) {
        checkNotFrozen();
        return super.addAll(index, c);
    }
    public boolean remove(Object o) {
        checkNotFrozen();
        return super.remove(o);
    }
    public String remove(int index) {
        checkNotFrozen();
        return super.remove(index);
    }
    public void clear() {
        checkNotFrozen();
        super.clear();
    }
    public boolean addTokens(Collection<? extends Object> tokens) {
        boolean added = false;
        for (Object token : tokens) {
            add(token.toString());
            added = true;
        }
        return added;
    }
    public boolean addTokens(Object[] tokens, int beg, int end) {
        boolean added = false;
        for (int i = beg; i < end; i++) {
            add(tokens[i].toString());
            added = true;
        }
        return added;
    }
    public boolean addTokens(String tokenStr) {
        return addTokens(tokenStr, false);
    }
    public boolean addTokens(String tokenStr, boolean allowNulls) {
        boolean added = false;
        int pos = 0, limit = tokenStr.length(), sep = limit;
        while (pos < limit) {
            sep = tokenStr.indexOf(separator, pos);
            if (sep < 0) {
                sep = limit;
            }
            if (sep == pos) {
                if (allowNulls) {
                    add("");
                    added = true;
                }
                pos += separator.length();
            } else {
                add(tokenStr.substring(pos, sep));
                added = true;
                pos = sep + separator.length();
            }
        }
        if (allowNulls && sep < limit) {
            add("");
            added = true;
        }
        return added;
    }
    public boolean addToken(Object token) {
        return add(token.toString());
    }
    public String format(String separator, String[] quotes) {
        return ""; 
    }
    protected int[] lengths;
    protected static final int MODC = 0, HINT = 1, BEG0 = 2, END0 = 3;
    protected final CharSequence getCS(int i) {
        return (CharSequence) get(i);
    }
    protected int[] getLengths() {
        int[] lengths = this.lengths;
        ;
        int sepLength = separator.length();
        if (lengths == null || lengths[MODC] != modCount) {
            int size = this.size();
            lengths = new int[END0 + size + (size == 0 ? 1 : 0)];
            lengths[MODC] = modCount;
            int end = -sepLength;  
            lengths[BEG0] = end;
            for (int i = 0; i < size; i++) {
                end += sepLength;  
                end += getCS(i).length();
                lengths[END0 + i] = end;
            }
            this.lengths = lengths;
        }
        return lengths;
    }
    public int length() {
        int[] lengths = getLengths();
        return lengths[lengths.length - 1];
    }
    protected int which(int i) {
        if (i < 0) {
            return -1;
        }
        int[] lengths = getLengths();
        for (int hint = lengths[HINT];; hint = 0) {
            for (int wh = hint; wh < lengths.length - END0; wh++) {
                int beg = lengths[BEG0 + wh];
                int end = lengths[END0 + wh];
                if (i >= beg && i < end) {
                    lengths[HINT] = wh;
                    return wh;
                }
            }
            if (hint == 0) {
                return size();  
            }
        }
    }
    public char charAt(int i) {
        if (i < 0) {
            return "".charAt(i);
        }
        int wh = which(i);
        int beg = lengths[BEG0 + wh];
        int j = i - beg;
        int sepLength = separator.length();
        if (j < sepLength) {
            return separator.charAt(j);
        }
        return getCS(wh).charAt(j - sepLength);
    }
    public CharSequence subSequence(int beg, int end) {
        if (beg == end) {
            return "";
        }
        if (beg < 0) {
            charAt(beg);  
        }
        if (beg > end) {
            charAt(-1);   
        }
        int begWh = which(beg);
        int endWh = which(end);
        if (endWh == size() || end == lengths[BEG0 + endWh]) {
            --endWh;
        }
        int begBase = lengths[BEG0 + begWh];
        int endBase = lengths[BEG0 + endWh];
        int sepLength = separator.length();
        int begFrag = 0;
        if ((beg - begBase) < sepLength) {
            begFrag = sepLength - (beg - begBase);
            beg += begFrag;
        }
        int endFrag = 0;
        if ((end - endBase) < sepLength) {
            endFrag = (end - endBase);
            end = endBase;
            endBase = lengths[BEG0 + --endWh];
        }
        if (false) {
            System.out.print("beg[wbf]end[wbf]");
            int pr[] = {begWh, begBase, begFrag, beg, endWh, endBase, endFrag, end};
            for (int k = 0; k < pr.length; k++) {
                System.out.print((k == 4 ? "   " : " ") + (pr[k]));
            }
            System.out.println();
        }
        if (begFrag > 0 && (end + endFrag) - begBase <= sepLength) {
            beg -= begFrag;
            end += endFrag;
            return separator.substring(beg - begBase, end - begBase);
        }
        if (begWh == endWh && (begFrag + endFrag) == 0) {
            return getCS(begWh).subSequence(beg - begBase - sepLength,
                    end - endBase - sepLength);
        }
        Object[] subTokens = new Object[1 + (endWh - begWh) + 1];
        int fillp = 0;
        if (begFrag == sepLength) {
            subTokens[fillp++] = "";
            begFrag = 0;
        }
        for (int wh = begWh; wh <= endWh; wh++) {
            CharSequence cs = getCS(wh);
            if (wh == begWh || wh == endWh) {
                int csBeg = (wh == begWh) ? (beg - begBase) - sepLength : 0;
                int csEnd = (wh == endWh) ? (end - endBase) - sepLength : cs.length();
                cs = cs.subSequence(csBeg, csEnd);
                if (begFrag > 0 && wh == begWh) {
                    cs = separator.substring(sepLength - begFrag) + cs;
                }
                if (endFrag > 0 && wh == endWh) {
                    cs = cs.toString() + separator.substring(0, endFrag);
                }
            }
            subTokens[fillp++] = cs;
        }
        return new TokenList(subTokens, 0, fillp, separator);
    }
    public String toString() {
        StringBuilder buf = new StringBuilder(length());
        int size = this.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(get(i));
        }
        return buf.toString();
    }
    /*---- TESTING CODE ----
    public static void main(String[] av) {
    if (av.length == 0)  av = new String[]{"one", "2", "", "four"};
    TokenList ts = new TokenList();
    final String SEP = ", ";
    ts.setSeparator(SEP);
    for (int i = -1; i < av.length; i++) {
    if (i >= 0)  ts.addToken(av[i]);
    {
    TokenList tsCopy = new TokenList(ts.toString(), SEP);
    if (!tsCopy.equals(ts)) {
    tsCopy.setSeparator(")(");
    System.out.println("!= ("+tsCopy+")");
    }
    }
    {
    TokenList tsBar = new TokenList(ts, "|");
    tsBar.add(0, "[");
    tsBar.add("]");
    System.out.println(tsBar);
    }
    if (false) {
    int[] ls = ts.getLengths();
    System.out.println("ts: "+ts);
    System.out.print("ls: {");
    for (int j = 0; j < ls.length; j++)  System.out.print(" "+ls[j]);
    System.out.println(" }");
    }
    assert0(ts.size() == i+1);
    assert0(i < 0 || ts.get(i) == av[i]);
    String tss = ts.toString();
    int tslen = tss.length();
    assert0(ts.length() == tss.length());
    for (int n = 0; n < tslen; n++) {
    assert0(ts.charAt(n) == tss.charAt(n));
    }
    for (int j = 0; j < tslen; j++) {
    for (int k = tslen; k >= j; k--) {
    CharSequence sub = ts.subSequence(j, k);
    assert0(sub.toString().equals(tss.substring(j, k)));
    }
    }
    }
    }
    static void assert0(boolean z) {
    if (!z)  throw new RuntimeException("assert failed");
    }
}
