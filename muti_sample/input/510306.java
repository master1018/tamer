public class TutorialEN implements OnTouchListener {
    private List<Bubble> mBubbles = new ArrayList<Bubble>();
    private View mInputView;
    private OpenWnnEN mIme;
    private int[] mLocation = new int[2];
    private static final int MSG_SHOW_BUBBLE = 0;
    private int mBubbleIndex;
    private boolean mEnableKeyTouch = false;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_BUBBLE:
                    Bubble bubba = (Bubble) msg.obj;
                    bubba.show(mLocation[0], mLocation[1]);
                    break;
            }
        }
    };
    class Bubble {
        Drawable bubbleBackground;
        int x;
        int y;
        int width;
        int gravity;
        CharSequence text;
        boolean dismissOnTouch;
        boolean dismissOnClose;
        PopupWindow window;
        TextView textView;
        View inputView;
        Bubble(Context context, View inputView,
                int backgroundResource, int bx, int by, int description, int guide) {
            CharSequence text = context.getResources().getText(description);
            init(context, inputView, backgroundResource, bx, by, text, guide, false);
        }
        Bubble(Context context, View inputView, int backgroundResource, int bx, int by,
               CharSequence description, int guide, boolean leftAlign) {
            init(context, inputView, backgroundResource, bx, by, description, guide, leftAlign);
        }
        void init(Context context, View inputView, int backgroundResource,
                  int bx, int by, CharSequence description, int guide, boolean leftAlign) {
            bubbleBackground = context.getResources().getDrawable(backgroundResource);
            x = bx;
            y = by;
            width = (int) (inputView.getWidth() * 0.9);
            this.gravity = Gravity.TOP | Gravity.LEFT;
            text = new SpannableStringBuilder()
                .append(description)
                .append("\n") 
                .append(context.getResources().getText(guide));
            this.dismissOnTouch = true;
            this.dismissOnClose = false;
            this.inputView = inputView;
            window = new PopupWindow(context);
            window.setBackgroundDrawable(null);
            LayoutInflater inflate =
                (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            textView = (TextView) inflate.inflate(R.layout.bubble_text, null);
            textView.setBackgroundDrawable(bubbleBackground);
            textView.setText(text);
            if (leftAlign) {
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            }
            window.setContentView(textView);
            window.setFocusable(false);
            window.setTouchable(true);
            window.setOutsideTouchable(false);
        }
        private int chooseSize(PopupWindow pop, View parentView, CharSequence text, TextView tv) {
            int wid = tv.getPaddingLeft() + tv.getPaddingRight();
            int ht = tv.getPaddingTop() + tv.getPaddingBottom();
            int cap = width - wid;
            Layout l = new StaticLayout(text, tv.getPaint(), cap,
                                        Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            float max = 0;
            for (int i = 0; i < l.getLineCount(); i++) {
                max = Math.max(max, l.getLineWidth(i));
            }
            pop.setWidth(width);
            pop.setHeight(ht + l.getHeight());
            return l.getHeight();
        }
        void show(int offx, int offy) {
            int textHeight = chooseSize(window, inputView, text, textView);
            offy -= textView.getPaddingTop() + textHeight;
            if (inputView.getVisibility() == View.VISIBLE 
                    && inputView.getWindowVisibility() == View.VISIBLE) {
                try {
                    if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) offy -= window.getHeight();
                    if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) offx -= window.getWidth();
                    textView.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View view, MotionEvent me) {
                            boolean ret = !mEnableKeyTouch;
                            switch (me.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (mBubbleIndex >= mBubbles.size()) {
                                    mInputView.setOnTouchListener(null);
                                } else {
                                    TutorialEN.this.next();
                                }
                                break;
                            default:
                                break;
                            }
                            return ret;
                        }
                    });
                    window.showAtLocation(inputView, Gravity.NO_GRAVITY, x + offx, y + offy);
                } catch (Exception e) {
                }
            }
        }
        void hide() {
            if (window.isShowing()) {
                textView.setOnTouchListener(null);
                window.dismiss();
            }
        }
        boolean isShowing() {
            return window.isShowing();
        }
    }
    public TutorialEN(OpenWnnEN ime, View inputView, DefaultSoftKeyboardEN inputManager) {
        mInputView = inputView;
        mIme = ime;
        Context context = inputView.getContext();
        int inputWidth = inputView.getWidth();
        Resources r = inputView.getContext().getResources();
        final int x = inputWidth / 20;
        r.getDimensionPixelOffset(R.dimen.bubble_pointer_offset);
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        Bubble button;
        spannable.clear();
        spannable.append(r.getText(R.string.tip_en_to_open_keyboard));
        button = new Bubble(context, inputView, 
                R.drawable.dialog_bubble, x, 0, 
                spannable, R.string.touch_to_continue, false);
        mBubbles.add(button);
        spannable.clear();
        spannable.append(r.getText(R.string.tip_en_to_close_keyboard));
        setSpan(spannable, "\u2190", R.drawable.tutorial_back);
        button = new Bubble(context, inputView, 
                R.drawable.dialog_bubble, x, 0, 
                spannable, R.string.touch_to_continue, false);
        mBubbles.add(button);
        button = new Bubble(context, inputView, 
                R.drawable.dialog_bubble, x, 0, 
                R.string.tip_en_end_of_tutorial, R.string.touch_to_finish);
        mBubbles.add(button);
    }
    private void setSpan(SpannableStringBuilder spannable, String marker, int imageResourceId) {
        String text = spannable.toString();
        int target = text.indexOf(marker);
        while (0 <= target) {
            ImageSpan span = new ImageSpan(mIme, imageResourceId,
                    DynamicDrawableSpan.ALIGN_BOTTOM);
            spannable.setSpan(span, target, target + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
            target = text.indexOf(marker, target + 1);
        }
    }
    public void start() {
        mInputView.getLocationInWindow(mLocation);
        mBubbleIndex = -1;
        mInputView.setOnTouchListener(this);
        next();
    }
    boolean next() {
        if (mBubbleIndex >= 0) {
            if (!mBubbles.get(mBubbleIndex).isShowing()) {
                return true;
            }
            for (int i = 0; i <= mBubbleIndex; i++) {
                mBubbles.get(i).hide();
            }
        }
        mBubbleIndex++;
        if (mBubbleIndex >= mBubbles.size()) {
            mEnableKeyTouch = true;
            mIme.sendDownUpKeyEvents(-1);
            mIme.tutorialDone();
            return false;
        }
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MSG_SHOW_BUBBLE, mBubbles.get(mBubbleIndex)), 500);
        return true;
    }
    void hide() {
        for (int i = 0; i < mBubbles.size(); i++) {
            mBubbles.get(i).hide();
        }
        mInputView.setOnTouchListener(null);
    }
    public boolean close() {
        mHandler.removeMessages(MSG_SHOW_BUBBLE);
        hide();
        return true;
    }
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = !mEnableKeyTouch;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mBubbleIndex >= mBubbles.size()) {
                mInputView.setOnTouchListener(null);
            }
        }
        return ret;
    }
}
