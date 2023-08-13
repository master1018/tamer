public class MidLayoutInflater extends LayoutInflater {
    private static final String[] sClassPrefixList = {
        "android.widget.",
        "android.webkit."
    };
    public MidLayoutInflater(Context context) {
        super(context);
    }
    protected MidLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }
    @Override protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : sClassPrefixList) {
            try {
                View view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return super.onCreateView(name, attrs);
    }
    public LayoutInflater cloneInContext(Context newContext) {
        return new MidLayoutInflater(this, newContext);
    }
}
