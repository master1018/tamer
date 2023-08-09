public class RegexpPool {
    private RegexpNode prefixMachine = new RegexpNode();
    private RegexpNode suffixMachine = new RegexpNode();
    private static final int BIG = 0x7FFFFFFF;
    private int lastDepth = BIG;
    public RegexpPool () {
    }
    public void add(String re, Object ret) throws REException {
        add(re, ret, false);
    }
    public void replace(String re, Object ret) {
        try {
            add(re, ret, true);
        } catch(Exception e) {
        }
    }
    public Object delete(String re) {
        Object o = null;
        RegexpNode p = prefixMachine;
        RegexpNode best = p;
        int len = re.length() - 1;
        int i;
        boolean prefix = true;
        if (!re.startsWith("*") ||
            !re.endsWith("*"))
            len++;
        if (len <= 0)
            return null;
        for (i = 0; p != null; i++) {
            if (p.result != null && p.depth < BIG
                && (!p.exact || i == len)) {
                best = p;
            }
            if (i >= len)
                break;
            p = p.find(re.charAt(i));
        }
        p = suffixMachine;
        for (i = len; --i >= 0 && p != null;) {
            if (p.result != null && p.depth < BIG) {
                prefix = false;
                best = p;
            }
            p = p.find(re.charAt(i));
        }
        if (prefix) {
            if (re.equals(best.re)) {
                o = best.result;
                best.result = null;
            }
        } else {
            if (re.equals(best.re)) {
                o = best.result;
                best.result = null;
            }
        }
        return o;
    }
    public Object match(String s) {
        return matchAfter(s, BIG);
    }
    public Object matchNext(String s) {
        return matchAfter(s, lastDepth);
    }
    private void add(String re, Object ret, boolean replace) throws REException {
        int len = re.length();
        RegexpNode p;
        if (re.charAt(0) == '*') {
            p = suffixMachine;
            while (len > 1)
                p = p.add(re.charAt(--len));
        } else {
            boolean exact = false;
            if (re.charAt(len - 1) == '*')
                len--;
            else
                exact = true;
            p = prefixMachine;
            for (int i = 0; i < len; i++)
                p = p.add(re.charAt(i));
            p.exact = exact;
        }
        if (p.result != null && !replace)
            throw new REException(re + " is a duplicate");
        p.re = re;
        p.result = ret;
    }
    private Object matchAfter(String s, int lastMatchDepth) {
        RegexpNode p = prefixMachine;
        RegexpNode best = p;
        int bst = 0;
        int bend = 0;
        int len = s.length();
        int i;
        if (len <= 0)
            return null;
        for (i = 0; p != null; i++) {
            if (p.result != null && p.depth < lastMatchDepth
                && (!p.exact || i == len)) {
                lastDepth = p.depth;
                best = p;
                bst = i;
                bend = len;
            }
            if (i >= len)
                break;
            p = p.find(s.charAt(i));
        }
        p = suffixMachine;
        for (i = len; --i >= 0 && p != null;) {
            if (p.result != null && p.depth < lastMatchDepth) {
                lastDepth = p.depth;
                best = p;
                bst = 0;
                bend = i+1;
            }
            p = p.find(s.charAt(i));
        }
        Object o = best.result;
        if (o != null && o instanceof RegexpTarget)
            o = ((RegexpTarget) o).found(s.substring(bst, bend));
        return o;
    }
    public void reset() {
        lastDepth = BIG;
    }
    public void print(PrintStream out) {
        out.print("Regexp pool:\n");
        if (suffixMachine.firstchild != null) {
            out.print(" Suffix machine: ");
            suffixMachine.firstchild.print(out);
            out.print("\n");
        }
        if (prefixMachine.firstchild != null) {
            out.print(" Prefix machine: ");
            prefixMachine.firstchild.print(out);
            out.print("\n");
        }
    }
}
class RegexpNode {
    char c;
    RegexpNode firstchild;
    RegexpNode nextsibling;
    int depth;
    boolean exact;
    Object result;
    String re = null;
    RegexpNode () {
        c = '#';
        depth = 0;
    }
    RegexpNode (char C, int depth) {
        c = C;
        this.depth = depth;
    }
    RegexpNode add(char C) {
        RegexpNode p = firstchild;
        if (p == null)
            p = new RegexpNode (C, depth+1);
        else {
            while (p != null)
                if (p.c == C)
                    return p;
                else
                    p = p.nextsibling;
            p = new RegexpNode (C, depth+1);
            p.nextsibling = firstchild;
        }
        firstchild = p;
        return p;
    }
    RegexpNode find(char C) {
        for (RegexpNode p = firstchild;
                p != null;
                p = p.nextsibling)
            if (p.c == C)
                return p;
        return null;
    }
    void print(PrintStream out) {
        if (nextsibling != null) {
            RegexpNode p = this;
            out.print("(");
            while (p != null) {
                out.write(p.c);
                if (p.firstchild != null)
                    p.firstchild.print(out);
                p = p.nextsibling;
                out.write(p != null ? '|' : ')');
            }
        } else {
            out.write(c);
            if (firstchild != null)
                firstchild.print(out);
        }
    }
}
