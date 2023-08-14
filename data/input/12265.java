public class AsynchInvoke implements Runnable {
    private RequestImpl _req;
    private ORB         _orb;
    private boolean     _notifyORB;
    public AsynchInvoke (ORB o, RequestImpl reqToInvokeOn, boolean n)
    {
        _orb = o;
        _req = reqToInvokeOn;
        _notifyORB = n;
    };
    public void run()
    {
        _req.doInvocation();
        synchronized (_req)
            {
                _req.gotResponse = true;
                _req.notify();
            }
        if (_notifyORB == true) {
            _orb.notifyORB() ;
        }
    }
};
