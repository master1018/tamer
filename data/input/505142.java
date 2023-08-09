public class Vibrator
{
    IVibratorService mService;
    private final Binder mToken = new Binder();
    public Vibrator()
    {
        mService = IVibratorService.Stub.asInterface(
                ServiceManager.getService("vibrator"));
    }
    public void vibrate(long milliseconds)
    {
        try {
            mService.vibrate(milliseconds, mToken);
        } catch (RemoteException e) {
        }
    }
    public void vibrate(long[] pattern, int repeat)
    {
        if (repeat < pattern.length) {
            try {
                mService.vibratePattern(pattern, repeat, mToken);
            } catch (RemoteException e) {
            }
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    public void cancel()
    {
        try {
            mService.cancelVibrate(mToken);
        } catch (RemoteException e) {
        }
    }
}
