final class AclEnumerator implements Enumeration {
    Acl acl;
    Enumeration u1;
    Enumeration u2;
    Enumeration g1;
    Enumeration g2;
    AclEnumerator(Acl acl1, Hashtable hashtable, Hashtable hashtable1, Hashtable hashtable2, Hashtable hashtable3) {
        acl = acl1;
        u1 = hashtable.elements();
        u2 = hashtable2.elements();
        g1 = hashtable1.elements();
        g2 = hashtable3.elements();
    }
    public boolean hasMoreElements() {
        return u1.hasMoreElements() || u2.hasMoreElements() || g1.hasMoreElements() || g2.hasMoreElements();
    }
    public Object nextElement() {
        Acl acl1 = acl;
        if(u2.hasMoreElements()) return u2.nextElement();
        if(g1.hasMoreElements()) return g1.nextElement();
        if(u1.hasMoreElements()) return u1.nextElement();
        if(g2.hasMoreElements()) return g2.nextElement();
        return acl1;
    }
}