public class Queue {
    Token[] m = new Token[64];
    int a = 0;
    int b = 0;
    public boolean isEmpty() {
        return size() == 0;
    }
    public void add(Token u) {
        if (a < m.length) {
            m[a] = u;
            a++;
        } else {
            if (b > 0) {
                int d = b;
                System.arraycopy(m, b, m, 0, a - b);
                b = b - d;
                a = a - d;
                m[a] = u;
                a++;
            } else {
                int n = m.length * 2;
                Token[] nm = new Token[2 * n];
                System.arraycopy(m, 0, nm, 0, m.length);
                Arrays.fill(m, null);
                nm[a] = u;
                m = nm;
                a++;
            }
        }
    }
    public void clear() {
        a = b = 0;
        Arrays.fill(m, null);
    }
    public Token get(int ahead) {
        int p = b + ahead;
        if (p < a) return m[p]; else throw new ArrayIndexOutOfBoundsException("Not enough tokens");
    }
    public Token next() {
        if (a == b) throw new ArrayIndexOutOfBoundsException("queue empty");
        Token r = m[b];
        m[b] = null;
        b++;
        return r;
    }
    public int size() {
        return a - b;
    }
}
