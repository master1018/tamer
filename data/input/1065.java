public class GRTServant {
    private final int port;
    private Hashtable senders;
    private Hashtable receivers;
    public GRTServant(int port) {
        this.port = port;
        senders = new Hashtable();
        receivers = new Hashtable();
    }
    public void sendData(String host, String data) {
        getServentOut(host).sendData(data);
    }
    public void connect(String host) {
        getServentIn(host);
        getServentOut(host);
    }
    public synchronized OutRadio getServentOut(String host) {
        OutRadio o = (OutRadio) senders.get(host);
        if (o == null) {
            o = new OutRadio(host, port);
            o.connect();
            senders.put(host, o);
        }
        return o;
    }
    public synchronized InRadio getServentIn(String host) {
        InRadio i = (InRadio) receivers.get(host);
        if (i == null) {
            i = new InRadio(host, port);
            i.connect();
            i.start();
            receivers.put(host, i);
        }
        return i;
    }
    public void addSocketListenerOut(String host, SocketListener s) {
        getServentOut(host).addSocketListener(s);
    }
    public void removeSocketListenerOut(String host, SocketListener s) {
        getServentOut(host).removeSocketListener(s);
    }
    public void addSocketListenerIn(String host, SocketListener s) {
        getServentIn(host).addSocketListener(s);
    }
    public void removeSocketListenerIn(String host, SocketListener s) {
        getServentIn(host).removeSocketListener(s);
    }
    public void addSocketListenerOut(SocketListener s) {
        Enumeration out = senders.elements();
        while (out.hasMoreElements()) {
            ((OutRadio) out.nextElement()).addSocketListener(s);
        }
    }
    public void removeSocketListenerOut(SocketListener s) {
        Enumeration out = senders.elements();
        while (out.hasMoreElements()) {
            ((OutRadio) out.nextElement()).removeSocketListener(s);
        }
    }
    public void addSocketListenerIn(SocketListener s) {
        Enumeration in = senders.elements();
        while (in.hasMoreElements()) {
            ((InRadio) in.nextElement()).addSocketListener(s);
        }
    }
    public void removeSocketListenerIn(SocketListener s) {
        Enumeration in = senders.elements();
        while (in.hasMoreElements()) {
            ((InRadio) in.nextElement()).removeSocketListener(s);
        }
    }
}
