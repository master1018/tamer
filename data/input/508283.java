public class RegistrantList
{
    ArrayList   registrants = new ArrayList();      
    public synchronized void
    add(Handler h, int what, Object obj)
    {
        add(new Registrant(h, what, obj));
    }
    public synchronized void
    addUnique(Handler h, int what, Object obj)
    {
        remove(h);
        add(new Registrant(h, what, obj));        
    }
    public synchronized void
    add(Registrant r)
    {
        removeCleared();
        registrants.add(r);
    }
    public synchronized void
    removeCleared()
    {
        for (int i = registrants.size() - 1; i >= 0 ; i--) {
            Registrant  r = (Registrant) registrants.get(i);
            if (r.refH == null) {
                registrants.remove(i);
            }
        }
    }
    public synchronized int
    size()
    {
        return registrants.size();
    }
    public synchronized Object
    get(int index)
    {
        return registrants.get(index);
    }
    private synchronized void
    internalNotifyRegistrants (Object result, Throwable exception)
    {
       for (int i = 0, s = registrants.size(); i < s ; i++) {
            Registrant  r = (Registrant) registrants.get(i);
            r.internalNotifyRegistrant(result, exception);
       }
    }
    public  void
    notifyRegistrants()
    {
        internalNotifyRegistrants(null, null);
    }
    public  void
    notifyException(Throwable exception)
    {
        internalNotifyRegistrants (null, exception);
    }
    public  void
    notifyResult(Object result)
    {
        internalNotifyRegistrants (result, null);
    }
    public  void
    notifyRegistrants(AsyncResult ar)
    {
        internalNotifyRegistrants(ar.result, ar.exception);
    }
    public synchronized void
    remove(Handler h)
    {
        for (int i = 0, s = registrants.size() ; i < s ; i++) {
            Registrant  r = (Registrant) registrants.get(i);
            Handler     rh;
            rh = r.getHandler();
            if (rh == null || rh == h) {
                r.clear();
            }
        }
        removeCleared();
    }
}
