public class WebViewEventSender implements EventSender {
    private static final String LOGTAG = "WebViewEventSender";
    WebViewEventSender(WebView webView) {
        mWebView = webView;
        mTouchPoints = new Vector();
    }
	public void resetMouse() {
		mouseX = mouseY = 0;
	}
	public void enableDOMUIEventLogging(int DOMNode) {
	}
	public void fireKeyboardEventsToElement(int DOMNode) {
	}
	public void keyDown(String character, String[] withModifiers) {
        Log.e("EventSender", "KeyDown: " + character + "("
                + character.getBytes()[0] + ") Modifiers: "
                + Arrays.toString(withModifiers));
        KeyEvent modifier = null;
        if (withModifiers != null && withModifiers.length > 0) {
            for (int i = 0; i < withModifiers.length; i++) {
                int keyCode = modifierMapper(withModifiers[i]);
                modifier = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                mWebView.onKeyDown(modifier.getKeyCode(), modifier);
            }
        }
        int keyCode = keyMapper(character.toLowerCase().toCharArray()[0]);
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mWebView.onKeyDown(event.getKeyCode(), event);
    }
	public void keyDown(String character) {
        keyDown(character, null);
	}
	public void leapForward(int milliseconds) {
	}
	public void mouseClick() {
		mouseDown();
		mouseUp();
	}
	public void mouseDown() {
	}
	public void mouseMoveTo(int X, int Y) {
		if (X > mouseX) {
                    KeyEvent event = new KeyEvent(
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
                    mWebView.onKeyDown(event.getKeyCode(), event);
                    mWebView.onKeyUp(event.getKeyCode(), event);
		} else if ( X < mouseX ) {
                    KeyEvent event = new KeyEvent(
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT);
                    mWebView.onKeyDown(event.getKeyCode(), event);
                    mWebView.onKeyUp(event.getKeyCode(), event);
		}
		if (Y > mouseY) {
                    KeyEvent event = new KeyEvent(
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
                    mWebView.onKeyDown(event.getKeyCode(), event);
                    mWebView.onKeyUp(event.getKeyCode(), event);
		} else if (Y < mouseY ) {
                    KeyEvent event = new KeyEvent(
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
                    mWebView.onKeyDown(event.getKeyCode(), event);
                    mWebView.onKeyUp(event.getKeyCode(), event);
		}
		mouseX= X;
		mouseY= Y;
	}
	public void mouseUp() {
	}
	static int keyMapper(char c) {
		if (c >= '0' && c<= '9') {
			int offset = c - '0';
			return KeyEvent.KEYCODE_0 + offset;
		}
		if (c >= 'a' && c <= 'z') {
			int offset = c - 'a';
			return KeyEvent.KEYCODE_A + offset;
		}
		switch (c) {
		case '*':
			return KeyEvent.KEYCODE_STAR;
		case '#':
			return KeyEvent.KEYCODE_POUND;
		case ',':
			return KeyEvent.KEYCODE_COMMA;
		case '.':
			return KeyEvent.KEYCODE_PERIOD;
		case '\t':
			return KeyEvent.KEYCODE_TAB;
		case ' ':
			return KeyEvent.KEYCODE_SPACE;
		case '\n':
			return KeyEvent.KEYCODE_ENTER;
		case '\b':
        case 0x7F:
			return KeyEvent.KEYCODE_DEL;
		case '~':
			return KeyEvent.KEYCODE_GRAVE;
		case '-':
			return KeyEvent.KEYCODE_MINUS;
		case '=':
			return KeyEvent.KEYCODE_EQUALS;
		case '(':
			return KeyEvent.KEYCODE_LEFT_BRACKET;
		case ')':
			return KeyEvent.KEYCODE_RIGHT_BRACKET;
		case '\\':
			return KeyEvent.KEYCODE_BACKSLASH;
		case ';':
			return KeyEvent.KEYCODE_SEMICOLON;
		case '\'':
			return KeyEvent.KEYCODE_APOSTROPHE;
		case '/':
			return KeyEvent.KEYCODE_SLASH;
		default:
			break;
		}
		return c;
	}
	static int modifierMapper(String modifier) {
		if (modifier.equals("ctrlKey")) {
			return KeyEvent.KEYCODE_ALT_LEFT;
		} else if (modifier.equals("shiftKey")) {
			return KeyEvent.KEYCODE_SHIFT_LEFT;
		} else if (modifier.equals("altKey")) {
			return KeyEvent.KEYCODE_SYM;
		} else if (modifier.equals("metaKey")) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}
		return KeyEvent.KEYCODE_UNKNOWN;
	}
    public void touchStart() {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        tp.setDownTime(SystemClock.uptimeMillis());
        MotionEvent event = MotionEvent.obtain(tp.downTime(), tp.downTime(),
                MotionEvent.ACTION_DOWN, tp.getX(), tp.getY(), mTouchMetaState);
        mWebView.onTouchEvent(event);
    }
    public void touchMove() {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        if (!tp.hasMoved()) {
            return;
        }
        MotionEvent event = MotionEvent.obtain(tp.downTime(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, tp.getX(), tp.getY(), mTouchMetaState);
        mWebView.onTouchEvent(event);
        tp.setMoved(false);
    }
    public void touchEnd() {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        MotionEvent event = MotionEvent.obtain(tp.downTime(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, tp.getX(), tp.getY(), mTouchMetaState);
        mWebView.onTouchEvent(event);
        if (tp.isReleased()) {
            mTouchPoints.remove(0);
        }
    }
    public void touchCancel() {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        if (tp.cancelled()) {
            MotionEvent event = MotionEvent.obtain(tp.downTime(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_CANCEL, tp.getX(), tp.getY(), mTouchMetaState);
            mWebView.onTouchEvent(event);
        }
    }
    public void cancelTouchPoint(int id) {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        tp.cancel();
    }
    public void addTouchPoint(int x, int y) {
        mTouchPoints.add(new TouchPoint(contentsToWindowX(x), contentsToWindowY(y)));
        if (mTouchPoints.size() > 1) {
            Log.w(LOGTAG, "Adding more than one touch point, but multi touch is not supported!");
        }
    }
    public void updateTouchPoint(int id, int x, int y) {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        tp.update(contentsToWindowX(x), contentsToWindowY(y));
        tp.setMoved(true);
    }
    public void setTouchModifier(String modifier, boolean enabled) {
        int mask = 0;
        if ("alt".equals(modifier.toLowerCase())) {
            mask = KeyEvent.META_ALT_ON;
        } else if ("shift".equals(modifier.toLowerCase())) {
            mask = KeyEvent.META_SHIFT_ON;
        } else if ("ctrl".equals(modifier.toLowerCase())) {
            mask = KeyEvent.META_SYM_ON;
        }
        if (enabled) {
            mTouchMetaState |= mask;
        } else {
            mTouchMetaState &= ~mask;
        }
    }
    public void releaseTouchPoint(int id) {
        TouchPoint tp = mTouchPoints.get(0);
        if (tp == null) {
            return;
        }
        tp.release();
    }
    public void clearTouchPoints() {
        mTouchPoints.clear();
    }
    public void clearTouchMetaState() {
        mTouchMetaState = 0;
    }
    private int contentsToWindowX(int x) {
        return (int) (x * mWebView.getScale()) - mWebView.getScrollX();
    }
    private int contentsToWindowY(int y) {
        return (int) (y * mWebView.getScale()) - mWebView.getScrollY();
    }
    private WebView mWebView = null;
    private int mouseX;
    private int mouseY;
    private class TouchPoint {
        private int mX;
        private int mY;
        private long mDownTime;
        private boolean mReleased;
        private boolean mMoved;
        private boolean mCancelled;
        public TouchPoint(int x, int y) {
            mX = x;
            mY = y;
            mReleased = false;
            mMoved = false;
            mCancelled = false;
        }
        public void setDownTime(long downTime) { mDownTime = downTime; }
        public long downTime() { return mDownTime; }
        public void cancel() { mCancelled = true; }
        public boolean cancelled() { return mCancelled; }
        public void release() { mReleased = true; }
        public boolean isReleased() { return mReleased; }
        public void setMoved(boolean moved) { mMoved = moved; }
        public boolean hasMoved() { return mMoved; }
        public int getX() { return mX; }
        public int getY() { return mY; }
        public void update(int x, int y) {
            mX = x;
            mY = y;
        }
    };
    private Vector<TouchPoint> mTouchPoints;
    private int mTouchMetaState;
}
