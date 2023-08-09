public class SnmpIndex implements Serializable {
    private static final long serialVersionUID = 8712159739982192146L;
    public SnmpIndex(SnmpOid[] oidList) {
        size= oidList.length;
        for(int i= 0; i <size; i++) {
            oids.addElement(oidList[i]);
        }
    }
    public SnmpIndex(SnmpOid oid) {
        oids.addElement(oid);
        size= 1;
    }
    public int getNbComponents() {
        return size;
    }
    public Vector<SnmpOid> getComponents() {
        return oids;
    }
    public boolean equals(SnmpIndex index) {
        if (size != index.getNbComponents())
            return false;
        SnmpOid oid1;
        SnmpOid oid2;
        Vector<SnmpOid> components= index.getComponents();
        for(int i=0; i <size; i++) {
            oid1= oids.elementAt(i);
            oid2= components.elementAt(i);
            if (oid1.equals(oid2) == false)
                return false;
        }
        return true;
    }
    public int compareTo(SnmpIndex index) {
        int length= index.getNbComponents();
        Vector<SnmpOid> components= index.getComponents();
        SnmpOid oid1;
        SnmpOid oid2;
        int comp;
        for(int i=0; i < size; i++) {
            if ( i > length) {
                return 1;
            }
            oid1= oids.elementAt(i);
            oid2= components.elementAt(i);
            comp= oid1.compareTo(oid2);
            if (comp == 0)
                continue;
            return comp;
        }
        return 0;
    }
    public String toString() {
        StringBuffer msg= new StringBuffer();
        for(Enumeration e= oids.elements(); e.hasMoreElements(); ) {
            SnmpOid val= (SnmpOid) e.nextElement();
            msg.append( "
        }
        return msg.toString();
    }
    private Vector<SnmpOid> oids = new Vector<SnmpOid>();
    private int size = 0;
}
