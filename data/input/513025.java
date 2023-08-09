public class TableLayout7 extends Activity {
    private boolean mShortcutsCollapsed;
    private boolean mCheckmarksCollapsed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_7);
        final TableLayout table = (TableLayout) findViewById(R.id.menu);
        Button button = (Button) findViewById(R.id.toggle1);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mShortcutsCollapsed = !mShortcutsCollapsed;
                table.setColumnCollapsed(2, mShortcutsCollapsed);
            }
        });
        button = (Button) findViewById(R.id.toggle2);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mCheckmarksCollapsed = !mCheckmarksCollapsed;
                table.setColumnCollapsed(0, mCheckmarksCollapsed);
            }
        });
        mCheckmarksCollapsed = table.isColumnCollapsed(0);
        mShortcutsCollapsed = table.isColumnCollapsed(2);
        appendRow(table);
    }
    private void appendRow(TableLayout table) {
        TableRow row = new TableRow(this);
        TextView label = new TextView(this);
        label.setText(R.string.table_layout_7_quit);
        label.setPadding(3, 3, 3, 3);
        TextView shortcut = new TextView(this);
        shortcut.setText(R.string.table_layout_7_ctrlq);
        shortcut.setPadding(3, 3, 3, 3);
        shortcut.setGravity(Gravity.RIGHT | Gravity.TOP);
        row.addView(label, new TableRow.LayoutParams(1));
        row.addView(shortcut, new TableRow.LayoutParams());
        table.addView(row, new TableLayout.LayoutParams());
    }
}
