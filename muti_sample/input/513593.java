public class LinearLayoutGrid extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.linear_layout_grid);
    }
    public ViewGroup getRootView() {
        return (ViewGroup) findViewById(R.id.layout);
    }
    public Button getButtonAt(int column, int row) {
        if (row < 0 || row > 2) {
            throw new IllegalArgumentException("row out of range");
        }
        if (column < 0 || column > 2) {
            throw new IllegalArgumentException("column out of range");
        }
        return (Button) getColumn(column).getChildAt(row);
    }
    private LinearLayout getColumn(int column) {
        switch (column) {
            case 0:
                return (LinearLayout) findViewById(R.id.column1);
            case 1:
                return (LinearLayout) findViewById(R.id.column2);
            case 2:
                return (LinearLayout) findViewById(R.id.column3);
            default:
                throw new IllegalArgumentException("column out of range");
        }
    }
}
