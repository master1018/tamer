public class LinearLayout8 extends Activity {
    private LinearLayout mLinearLayout;
    public static final int VERTICAL_ID = Menu.FIRST;
    public static final int HORIZONTAL_ID = Menu.FIRST + 1;
    public static final int TOP_ID = Menu.FIRST + 2;
    public static final int MIDDLE_ID = Menu.FIRST + 3;
    public static final int BOTTOM_ID = Menu.FIRST + 4;
    public static final int LEFT_ID = Menu.FIRST + 5;
    public static final int CENTER_ID = Menu.FIRST + 6;
    public static final int RIGHT_ID = Menu.FIRST + 7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_8);
        mLinearLayout = (LinearLayout)findViewById(R.id.layout);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, VERTICAL_ID, 0, R.string.linear_layout_8_vertical);
        menu.add(0, HORIZONTAL_ID, 0, R.string.linear_layout_8_horizontal);
        menu.add(0, TOP_ID, 0, R.string.linear_layout_8_top);
        menu.add(0, MIDDLE_ID, 0, R.string.linear_layout_8_middle);
        menu.add(0, BOTTOM_ID, 0, R.string.linear_layout_8_bottom);
        menu.add(0, LEFT_ID, 0, R.string.linear_layout_8_left);
        menu.add(0, CENTER_ID, 0, R.string.linear_layout_8_center);
        menu.add(0, RIGHT_ID, 0, R.string.linear_layout_8_right);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case VERTICAL_ID:
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            return true;
        case HORIZONTAL_ID:
            mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            return true;
        case TOP_ID:
            mLinearLayout.setVerticalGravity(Gravity.TOP);
            return true;
        case MIDDLE_ID:
            mLinearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
            return true;
        case BOTTOM_ID:
            mLinearLayout.setVerticalGravity(Gravity.BOTTOM);
            return true;
        case LEFT_ID:
            mLinearLayout.setHorizontalGravity(Gravity.LEFT);
            return true;
        case CENTER_ID:
            mLinearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            return true;
        case RIGHT_ID:
            mLinearLayout.setHorizontalGravity(Gravity.RIGHT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
