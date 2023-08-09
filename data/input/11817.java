public class ReplayCache extends LinkedList<AuthTime> {
    private static final long serialVersionUID = 2997933194993803994L;
    private String principal;
    private CacheTable table;
    private int nap = 10 * 60 * 1000; 
    private boolean DEBUG = Krb5.DEBUG;
    public ReplayCache (String p, CacheTable ct) {
        principal = p;
        table = ct;
    }
    public synchronized void put(AuthTime t, long currentTime) {
        if (this.size() == 0) {
            addFirst(t);
        }
        else {
            AuthTime temp = getFirst();
            if (temp.kerberosTime < t.kerberosTime) {
                addFirst(t);
            }
            else if (temp.kerberosTime == t.kerberosTime) {
                if (temp.cusec < t.cusec) {
                    addFirst(t);
                }
            }
            else {
                ListIterator<AuthTime> it = listIterator(1);
                while (it.hasNext()) {
                    temp = it.next();
                    if (temp.kerberosTime < t.kerberosTime) {
                        add(indexOf(temp), t);
                        break;
                    }
                    else if (temp.kerberosTime == t.kerberosTime) {
                        if (temp.cusec < t.cusec) {
                            add(indexOf(temp), t);
                            break;
                        }
                    }
                }
            }
        }
        long timeLimit = currentTime - KerberosTime.getDefaultSkew() * 1000L;
        ListIterator<AuthTime> it = listIterator(0);
        AuthTime temp = null;
        int index = -1;
        while (it.hasNext()) {
            temp = it.next();
            if (temp.kerberosTime < timeLimit) {
                index = indexOf(temp);
                break;
            }
        }
        if (index > -1) {
            do {
                removeLast();
            } while(size() > index);
        }
        if (DEBUG) {
            printList();
        }
        if (this.size() == 0) {
            table.remove(principal);
        }
        if (DEBUG) {
            printList();
        }
    }
    private void printList() {
        Object[] total = toArray();
        for (int i = 0; i < total.length; i++) {
            System.out.println("object " + i + ": " + ((AuthTime)total[i]).kerberosTime + "/"
                               + ((AuthTime)total[i]).cusec);
        }
    }
}
