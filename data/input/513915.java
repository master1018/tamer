 class WebTextView extends AutoCompleteTextView {
    static final String LOGTAG = "webtextview";
    private WebView         mWebView;
    private boolean         mSingle;
    private int             mWidthSpec;
    private int             mHeightSpec;
    private int             mNodePointer;
    private boolean         mGotEnterDown;
    private int             mMaxLength;
    private String          mPreChange;
    private Drawable        mBackground;
    private float           mDragStartX;
    private float           mDragStartY;
    private long            mDragStartTime;
    private boolean         mDragSent;
    private boolean         mScrolled;
    private boolean         mFromWebKit;
    private boolean         mFromFocusChange;
    private boolean         mFromSetInputType;
    private boolean         mGotTouchDown;
    private boolean         mHasPerformedLongClick;
    private boolean         mInSetTextAndKeepSelection;
    private char[]          mCharacter = new char[1];
    private static final InputFilter[] NO_FILTERS = new InputFilter[0];
     WebTextView(Context context, WebView webView) {
        super(context, null, com.android.internal.R.attr.webTextViewStyle);
        mWebView = webView;
        mMaxLength = -1;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.isSystem()) {
            return super.dispatchKeyEvent(event);
        }
        boolean down = event.getAction() != KeyEvent.ACTION_UP;
        int keyCode = event.getKeyCode();
        boolean isArrowKey = false;
        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!mWebView.nativeCursorMatchesFocus()) {
                    return down ? mWebView.onKeyDown(keyCode, event) : mWebView
                            .onKeyUp(keyCode, event);
                }
                isArrowKey = true;
                break;
        }
        if (KeyEvent.KEYCODE_TAB == keyCode) {
            if (down) {
                onEditorAction(EditorInfo.IME_ACTION_NEXT);
            }
            return true;
        }
        Spannable text = (Spannable) getText();
        int oldLength = text.length();
        if (KeyEvent.KEYCODE_DEL == keyCode && 0 == oldLength) {
            sendDomEvent(event);
            return true;
        }
        if ((mSingle && KeyEvent.KEYCODE_ENTER == keyCode)) {
            if (isPopupShowing()) {
                return super.dispatchKeyEvent(event);
            }
            if (!down) {
                InputMethodManager.getInstance(mContext)
                        .hideSoftInputFromWindow(getWindowToken(), 0);
                sendDomEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                sendDomEvent(event);
            }
            return super.dispatchKeyEvent(event);
        } else if (KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
            if (isPopupShowing()) {
                return super.dispatchKeyEvent(event);
            }
            if (!mWebView.nativeCursorMatchesFocus()) {
                return down ? mWebView.onKeyDown(keyCode, event) : mWebView
                        .onKeyUp(keyCode, event);
            }
            if (!down) {
                mWebView.centerKeyPressOnTextField();
            }
            return super.dispatchKeyEvent(event);
        }
        if (getLayout() == null) {
            measure(mWidthSpec, mHeightSpec);
        }
        int oldStart = Selection.getSelectionStart(text);
        int oldEnd = Selection.getSelectionEnd(text);
        boolean maxedOut = mMaxLength != -1 && oldLength == mMaxLength;
        String oldText;
        if (maxedOut && oldEnd != oldStart) {
            oldText = text.toString();
        } else {
            oldText = "";
        }
        if (super.dispatchKeyEvent(event)) {
            if (KeyEvent.KEYCODE_ENTER == keyCode) {
                mGotEnterDown = true;
            }
            if (maxedOut && !isArrowKey && keyCode != KeyEvent.KEYCODE_DEL) {
                if (oldEnd == oldStart) {
                    return true;
                } else if (!oldText.equals(getText().toString())) {
                    Spannable span = (Spannable) getText();
                    int newStart = Selection.getSelectionStart(span);
                    int newEnd = Selection.getSelectionEnd(span);
                    mWebView.replaceTextfieldText(0, oldLength, span.toString(),
                            newStart, newEnd);
                    return true;
                }
            }
            return true;
        }
        if (mGotEnterDown && !down) {
            return true;
        }
        if (isArrowKey) {
            mWebView.resetTrackballTime();
            return down ? mWebView.onKeyDown(keyCode, event) : mWebView
                    .onKeyUp(keyCode, event);
        }
        return false;
    }
     boolean isSameTextField(int ptr) {
        return ptr == mNodePointer;
    }
    @Override public InputConnection onCreateInputConnection(
            EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        if (mWebView != null) {
            outAttrs.fieldName = mWebView.nativeFocusCandidateName() + "\\"
                    + mWebView.getUrl();
        }
        return connection;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mWebView == null || !mWebView.nativeFocusCandidateIsPassword()
                || !isSameTextField(mWebView.nativeFocusCandidatePointer())) {
            setInPassword(false);
        } else {
            super.onDraw(canvas);
        }
    }
    @Override
    public void onEditorAction(int actionCode) {
        switch (actionCode) {
        case EditorInfo.IME_ACTION_NEXT:
            if (mWebView.nativeMoveCursorToNextTextInput()) {
                mWebView.setFocusControllerInactive();
                mWebView.rebuildWebTextView();
                setDefaultSelection();
                mWebView.invalidate();
            }
            break;
        case EditorInfo.IME_ACTION_DONE:
            super.onEditorAction(actionCode);
            break;
        case EditorInfo.IME_ACTION_GO:
        case EditorInfo.IME_ACTION_SEARCH:
            InputMethodManager.getInstance(mContext)
                    .hideSoftInputFromWindow(getWindowToken(), 0);
            sendDomEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_ENTER));
            sendDomEvent(new KeyEvent(KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_ENTER));
        default:
            break;
        }
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        mFromFocusChange = true;
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        mFromFocusChange = false;
    }
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (mInSetTextAndKeepSelection) return;
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null && imm.isActive(this)) {
            Spannable sp = (Spannable) getText();
            int candStart = EditableInputConnection.getComposingSpanStart(sp);
            int candEnd = EditableInputConnection.getComposingSpanEnd(sp);
            imm.updateSelection(this, selStart, selEnd, candStart, candEnd);
        }
        if (!mFromWebKit && !mFromFocusChange && !mFromSetInputType
                && mWebView != null) {
            if (DebugFlags.WEB_TEXT_VIEW) {
                Log.v(LOGTAG, "onSelectionChanged selStart=" + selStart
                        + " selEnd=" + selEnd);
            }
            mWebView.setSelection(selStart, selEnd);
        }
    }
    @Override
    protected void onTextChanged(CharSequence s,int start,int before,int count){
        super.onTextChanged(s, start, before, count);
        String postChange = s.toString();
        if (mPreChange == null || mPreChange.equals(postChange) ||
                (mMaxLength > -1 && mPreChange.length() > mMaxLength &&
                mPreChange.substring(0, mMaxLength).equals(postChange))) {
            return;
        }
        mPreChange = postChange;
        if (0 == count) {
            if (before > 0) {
                mWebView.deleteSelection(start, start + before);
                updateCachedTextfield();
            }
            return;
        }
        TextUtils.getChars(s, start + count - 1, start + count, mCharacter, 0);
        KeyCharacterMap kmap =
                KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        KeyEvent[] events = kmap.getEvents(mCharacter);
        boolean cannotUseKeyEvents = null == events;
        int charactersFromKeyEvents = cannotUseKeyEvents ? 0 : 1;
        if (count > 1 || cannotUseKeyEvents) {
            String replace = s.subSequence(start,
                    start + count - charactersFromKeyEvents).toString();
            mWebView.replaceTextfieldText(start, start + before, replace,
                    start + count - charactersFromKeyEvents,
                    start + count - charactersFromKeyEvents);
        } else {
            if (DebugFlags.WEB_TEXT_VIEW) {
                Log.v(LOGTAG, "onTextChanged start=" + start
                        + " start + before=" + (start + before));
            }
            if (!mInSetTextAndKeepSelection) {
                mWebView.setSelection(start, start + before);
            }
        }
        if (!cannotUseKeyEvents) {
            int length = events.length;
            for (int i = 0; i < length; i++) {
                if (!KeyEvent.isModifierKey(events[i].getKeyCode())) {
                    sendDomEvent(events[i]);
                }
            }
        }
        updateCachedTextfield();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            super.onTouchEvent(event);
            mDragStartX = event.getX();
            mDragStartY = event.getY();
            mDragStartTime = event.getEventTime();
            mDragSent = false;
            mScrolled = false;
            mGotTouchDown = true;
            mHasPerformedLongClick = false;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mHasPerformedLongClick) {
                mGotTouchDown = false;
                return false;
            }
            int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            Spannable buffer = getText();
            int initialScrollX = Touch.getInitialScrollX(this, buffer);
            int initialScrollY = Touch.getInitialScrollY(this, buffer);
            super.onTouchEvent(event);
            int dx = Math.abs(mScrollX - initialScrollX);
            int dy = Math.abs(mScrollY - initialScrollY);
            int smallerSlop = slop/2;
            if (dx > smallerSlop || dy > smallerSlop) {
                if (mWebView != null) {
                    float maxScrollX = (float) Touch.getMaxScrollX(this,
                                getLayout(), mScrollY);
                    if (DebugFlags.WEB_TEXT_VIEW) {
                        Log.v(LOGTAG, "onTouchEvent x=" + mScrollX + " y="
                                + mScrollY + " maxX=" + maxScrollX);
                    }
                    mWebView.scrollFocusedTextInput(maxScrollX > 0 ?
                            mScrollX / maxScrollX : 0, mScrollY);
                }
                mScrolled = true;
                cancelLongPress();
                return true;
            }
            if (Math.abs((int) event.getX() - mDragStartX) < slop
                    && Math.abs((int) event.getY() - mDragStartY) < slop) {
                return true;
            }
            if (mWebView != null) {
                if (!mDragSent) {
                    mWebView.initiateTextFieldDrag(mDragStartX, mDragStartY,
                            mDragStartTime);
                    mDragSent = true;
                }
                boolean scrolled = mWebView.textFieldDrag(event);
                if (scrolled) {
                    mScrolled = true;
                    cancelLongPress();
                    return true;
                }
            }
            return false;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mHasPerformedLongClick) {
                mGotTouchDown = false;
                return false;
            }
            if (!mScrolled) {
                cancelLongPress();
                if (mGotTouchDown && mWebView != null) {
                    mWebView.touchUpOnTextField(event);
                }
            }
            if (mWebView != null && mDragSent) {
                mWebView.onTouchEvent(event);
            }
            mGotTouchDown = false;
            break;
        default:
            break;
        }
        return true;
    }
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (isPopupShowing()) {
            return super.onTrackballEvent(event);
        }
        if (event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }
        if (!mWebView.nativeCursorMatchesFocus()) {
            return mWebView.onTrackballEvent(event);
        }
        Spannable text = (Spannable) getText();
        MovementMethod move = getMovementMethod();
        if (move != null && getLayout() != null &&
            move.onTrackballEvent(this, text, event)) {
            return true;
        }
        return false;
    }
    @Override
    public boolean performLongClick() {
        mHasPerformedLongClick = true;
        return super.performLongClick();
    }
     void remove() {
        InputMethodManager.getInstance(mContext).hideSoftInputFromWindow(
                getWindowToken(), 0);
        mWebView.removeView(this);
        mWebView.requestFocus();
    }
     void bringIntoView() {
        if (getLayout() != null) {
            bringPointIntoView(Selection.getSelectionEnd(getText()));
        }
    }
    private void sendDomEvent(KeyEvent event) {
        mWebView.passToJavaScript(getText().toString(), event);
    }
    public void setAdapterCustom(AutoCompleteAdapter adapter) {
        if (adapter != null) {
            setInputType(getInputType()
                    | EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            adapter.setTextView(this);
        }
        super.setAdapter(adapter);
    }
    public static class AutoCompleteAdapter extends ArrayAdapter<String> {
        private TextView mTextView;
        public AutoCompleteAdapter(Context context, ArrayList<String> entries) {
            super(context, com.android.internal.R.layout
                    .search_dropdown_item_1line, entries);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv =
                    (TextView) super.getView(position, convertView, parent);
            if (tv != null && mTextView != null) {
                tv.setTextSize(mTextView.getTextSize());
            }
            return tv;
        }
        private void setTextView(TextView tv) {
            mTextView = tv;
        }
    }
     void setDefaultSelection() {
        Spannable text = (Spannable) getText();
        int selection = mSingle ? text.length() : 0;
        if (Selection.getSelectionStart(text) == selection
                && Selection.getSelectionEnd(text) == selection) {
            if (mWebView != null) {
                mWebView.setSelection(selection, selection);
            }
        } else {
            Selection.setSelection(text, selection, selection);
        }
    }
     void setInPassword(boolean inPassword) {
        if (inPassword) {
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.
                    TYPE_TEXT_VARIATION_PASSWORD);
            createBackground();
        }
        setWillNotDraw(!inPassword);
        setBackgroundDrawable(inPassword ? mBackground : null);
        setCursorVisible(inPassword);
    }
    private static class OutlineDrawable extends Drawable {
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            canvas.drawRect(bounds, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRect(bounds, paint);
        }
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
        public void setAlpha(int alpha) { }
        public void setColorFilter(ColorFilter cf) { }
    }
    private void createBackground() {
        if (mBackground != null) {
            return;
        }
        mBackground = new OutlineDrawable();
        setGravity(Gravity.CENTER_VERTICAL);
        TextPaint paint = getPaint();
        int flags = paint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG |
                Paint.ANTI_ALIAS_FLAG & ~Paint.DEV_KERN_TEXT_FLAG;
        paint.setFlags(flags);
        setTextColor(Color.BLACK);
    }
    @Override
    public void setInputType(int type) {
        mFromSetInputType = true;
        super.setInputType(type);
        mFromSetInputType = false;
    }
    private void setMaxLength(int maxLength) {
        mMaxLength = maxLength;
        if (-1 == maxLength) {
            setFilters(NO_FILTERS);
        } else {
            setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(maxLength) });
        }
    }
     void setNodePointer(int ptr) {
        mNodePointer = ptr;
    }
     void setRect(int x, int y, int width, int height) {
        LayoutParams lp = (LayoutParams) getLayoutParams();
        if (null == lp) {
            lp = new LayoutParams(width, height, x, y);
        } else {
            lp.x = x;
            lp.y = y;
            lp.width = width;
            lp.height = height;
        }
        if (getParent() == null) {
            mWebView.addView(this, lp);
        } else {
            setLayoutParams(lp);
        }
        mWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        mHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    }
     void setSelectionFromWebKit(int start, int end) {
        if (start < 0 || end < 0) return;
        Spannable text = (Spannable) getText();
        int length = text.length();
        if (start > length || end > length) return;
        mFromWebKit = true;
        Selection.setSelection(text, start, end);
        mFromWebKit = false;
    }
     void setTextAndKeepSelection(String text) {
        mPreChange = text.toString();
        Editable edit = (Editable) getText();
        int selStart = Selection.getSelectionStart(edit);
        int selEnd = Selection.getSelectionEnd(edit);
        mInSetTextAndKeepSelection = true;
        edit.replace(0, edit.length(), text);
        int newLength = edit.length();
        if (selStart > newLength) selStart = newLength;
        if (selEnd > newLength) selEnd = newLength;
        Selection.setSelection(edit, selStart, selEnd);
        mInSetTextAndKeepSelection = false;
        updateCachedTextfield();
    }
     void setType(int type) {
        if (mWebView == null) return;
        boolean single = true;
        boolean inPassword = false;
        int maxLength = -1;
        int inputType = EditorInfo.TYPE_CLASS_TEXT;
        if (mWebView.nativeFocusCandidateHasNextTextfield()) {
            inputType |= EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT;
        }
        int imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        switch (type) {
            case 0: 
                imeOptions |= EditorInfo.IME_ACTION_GO;
                break;
            case 1: 
                single = false;
                inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                        | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | EditorInfo.TYPE_CLASS_TEXT
                        | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT;
                imeOptions |= EditorInfo.IME_ACTION_NONE;
                break;
            case 2: 
                inPassword = true;
                imeOptions |= EditorInfo.IME_ACTION_GO;
                break;
            case 3: 
                imeOptions |= EditorInfo.IME_ACTION_SEARCH;
                break;
            case 4: 
                imeOptions |= EditorInfo.IME_ACTION_GO;
                break;
            case 5: 
                inputType |= EditorInfo.TYPE_CLASS_NUMBER;
                imeOptions |= EditorInfo.IME_ACTION_NEXT;
                break;
            case 6: 
                inputType |= EditorInfo.TYPE_CLASS_PHONE;
                imeOptions |= EditorInfo.IME_ACTION_NEXT;
                break;
            case 7: 
                imeOptions |= EditorInfo.IME_ACTION_GO;
                break;
            default:
                imeOptions |= EditorInfo.IME_ACTION_GO;
                break;
        }
        setHint(null);
        if (single) {
            mWebView.requestLabel(mWebView.nativeFocusCandidateFramePointer(),
                    mNodePointer);
            maxLength = mWebView.nativeFocusCandidateMaxLength();
            if (type != 2 ) {
                String name = mWebView.nativeFocusCandidateName();
                if (name != null && name.length() > 0) {
                    mWebView.requestFormData(name, mNodePointer);
                }
            }
        }
        mSingle = single;
        setMaxLength(maxLength);
        setHorizontallyScrolling(single);
        setInputType(inputType);
        setImeOptions(imeOptions);
        setInPassword(inPassword);
        AutoCompleteAdapter adapter = null;
        setAdapterCustom(adapter);
    }
     void updateCachedTextfield() {
        mWebView.updateCachedTextfield(getText().toString());
    }
    @Override
    public boolean requestRectangleOnScreen(Rect rectangle) {
        if (!mWebView.inAnimateZoom()) {
            return super.requestRectangleOnScreen(rectangle);
        }
        return false;
    }
}
