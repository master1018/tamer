public class Triple {
    private Object p1;
    private Object p2;
    private Object p3;
    public Triple() {
        p1 = null;
        p2 = null;
        p3 = null;
    }
    public Triple(Object one, Object two, Object three) {
        p1 = one;
        p2 = two;
        p3 = three;
    }
    public boolean equals(Triple p) {
        if (p1.equals(p.p1) && p2.equals(p.p2) && p3.equals(p.p3)) {
            return true;
        } else {
            return false;
        }
    }
    public Object getFirst() {
        return p1;
    }
    public Object getSecond() {
        return p2;
    }
    public Object getThird() {
        return p3;
    }
    public void setFirst(Object p) {
        p1 = p;
    }
    public void setSecond(Object p) {
        p2 = p;
    }
    public void setThird(Object p) {
        p3 = p;
    }
}
