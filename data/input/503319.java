public abstract class AbstractInputMethodService extends Service
        implements KeyEvent.Callback {
    private InputMethod mInputMethod;
    final KeyEvent.DispatcherState mDispatcherState
            = new KeyEvent.DispatcherState();
    public abstract class AbstractInputMethodImpl implements InputMethod {
        public void createSession(SessionCallback callback) {
            callback.sessionCreated(onCreateInputMethodSessionInterface());
        }
        public void setSessionEnabled(InputMethodSession session, boolean enabled) {
            ((AbstractInputMethodSessionImpl)session).setEnabled(enabled);
        }
        public void revokeSession(InputMethodSession session) {
            ((AbstractInputMethodSessionImpl)session).revokeSelf();
        }
    }
    public abstract class AbstractInputMethodSessionImpl implements InputMethodSession {
        boolean mEnabled = true;
        boolean mRevoked;
        public boolean isEnabled() {
            return mEnabled;
        }
        public boolean isRevoked() {
            return mRevoked;
        }
        public void setEnabled(boolean enabled) {
            if (!mRevoked) {
                mEnabled = enabled;
            }
        }
        public void revokeSelf() {
            mRevoked = true;
            mEnabled = false;
        }
        public void dispatchKeyEvent(int seq, KeyEvent event, EventCallback callback) {
            boolean handled = event.dispatch(AbstractInputMethodService.this,
                    mDispatcherState, this);
            if (callback != null) {
                callback.finishedEvent(seq, handled);
            }
        }
        public void dispatchTrackballEvent(int seq, MotionEvent event, EventCallback callback) {
            boolean handled = onTrackballEvent(event);
            if (callback != null) {
                callback.finishedEvent(seq, handled);
            }
        }
    }
    public KeyEvent.DispatcherState getKeyDispatcherState() {
        return mDispatcherState;
    }
    public abstract AbstractInputMethodImpl onCreateInputMethodInterface();
    public abstract AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface();
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }
    @Override
    final public IBinder onBind(Intent intent) {
        if (mInputMethod == null) {
            mInputMethod = onCreateInputMethodInterface();
        }
        return new IInputMethodWrapper(this, mInputMethod);
    }
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }
}
