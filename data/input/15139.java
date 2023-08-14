public class AuthCacheImpl implements AuthCache {
    HashMap hashtable;
    public AuthCacheImpl () {
        hashtable = new HashMap ();
    }
    public void setMap (HashMap map) {
        hashtable = map;
    }
    public synchronized void put (String pkey, AuthCacheValue value) {
        LinkedList list = (LinkedList) hashtable.get (pkey);
        String skey = value.getPath();
        if (list == null) {
            list = new LinkedList ();
            hashtable.put (pkey, list);
        }
        ListIterator iter = list.listIterator();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo)iter.next();
            if (inf.path == null || inf.path.startsWith (skey)) {
                iter.remove ();
            }
        }
        iter.add (value);
    }
    public synchronized AuthCacheValue get (String pkey, String skey) {
        AuthenticationInfo result = null;
        LinkedList list = (LinkedList) hashtable.get (pkey);
        if (list == null || list.size() == 0) {
            return null;
        }
        if (skey == null) {
            return (AuthenticationInfo)list.get (0);
        }
        ListIterator iter = list.listIterator();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo)iter.next();
            if (skey.startsWith (inf.path)) {
                return inf;
            }
        }
        return null;
    }
    public synchronized void remove (String pkey, AuthCacheValue entry) {
        LinkedList list = (LinkedList) hashtable.get (pkey);
        if (list == null) {
            return;
        }
        if (entry == null) {
            list.clear();
            return;
        }
        ListIterator iter = list.listIterator ();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo)iter.next();
            if (entry.equals(inf)) {
                iter.remove ();
            }
        }
    }
}
