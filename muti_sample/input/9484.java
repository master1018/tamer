public class AppletEventMulticaster implements AppletListener {
    private final AppletListener a, b;
    public AppletEventMulticaster(AppletListener a, AppletListener b) {
        this.a = a; this.b = b;
    }
    public void appletStateChanged(AppletEvent e) {
        a.appletStateChanged(e);
        b.appletStateChanged(e);
    }
    public static AppletListener add(AppletListener a, AppletListener b) {
        return addInternal(a, b);
    }
    public static AppletListener remove(AppletListener l, AppletListener oldl) {
        return removeInternal(l, oldl);
    }
    private static AppletListener addInternal(AppletListener a, AppletListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new AppletEventMulticaster(a, b);
    }
    protected AppletListener remove(AppletListener oldl) {
        if (oldl == a)  return b;
        if (oldl == b)  return a;
        AppletListener a2 = removeInternal(a, oldl);
        AppletListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;        
        }
        return addInternal(a2, b2);
    }
    private static AppletListener removeInternal(AppletListener l, AppletListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof AppletEventMulticaster) {
            return ((AppletEventMulticaster)l).remove(oldl);
        } else {
            return l;           
        }
    }
}
