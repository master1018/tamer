public class ListOfEditTexts extends Activity {
    private int mLinesPerEditText = 12;
    private ListView mListView;
    private LinearLayout mLinearLayout;
    public ListView getListView() {
        return mListView;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Button buttonAbove = new Button(this);
        buttonAbove.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        buttonAbove.setText("button above list");
        mLinearLayout.addView(buttonAbove);
        mListView = new ListView(this);
        mListView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setDrawSelectorOnTop(false);
        mListView.setItemsCanFocus(true);
        mListView.setLayoutParams((new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f)));
        List<String> bodies = Lists.newArrayList(
                getBody("zero hello, my name is android"),
                getBody("one i'm a paranoid android"),
                getBody("two i robot.  huh huh."),
                getBody("three not the g-phone!"));
        mListView.setAdapter(new MyAdapter(this, bodies));
        mLinearLayout.addView(mListView);
        Button buttonBelow = new Button(this);
        buttonBelow.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        buttonBelow.setText("button below list");
        mLinearLayout.addView(buttonBelow);
        setContentView(mLinearLayout);
    }
    String getBody(String line) {
        StringBuilder sb = new StringBuilder((line.length() + 5) * mLinesPerEditText);
        for (int i = 0; i < mLinesPerEditText; i++) {
            sb.append(i + 1).append(' ').append(line);
            if (i < mLinesPerEditText - 1) {
                sb.append('\n'); 
            }
        }
        return sb.toString();
    }
    private static class MyAdapter extends ArrayAdapter<String> {
        public MyAdapter(Context context, List<String> bodies) {
            super(context, 0, bodies);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String body = getItem(position);
            if (convertView != null) {
                ((EditText) convertView).setText(body);
                return convertView;                
            }
            EditText editText = new EditText(getContext());
            editText.setText(body);
            return editText;
        }
    }
}
