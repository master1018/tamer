public class ListOfButtons extends ListActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_with_button_above);
        getListView().setItemsCanFocus(true);
        setListAdapter(new MyAdapter(this, mLabels));
    }
    String[] mLabels = {
            "Alabama", "Alaska", "Arizona", "apple sauce!",
            "California", "Colorado", "Connecticut", "Delaware"
    };
    public String[] getLabels() {
        return mLabels;
    }
    public static class MyAdapter extends ArrayAdapter<String> {
        public MyAdapter(Context context, String[] labels) {
            super(context, 0, labels);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String label = getItem(position);
            Button button = new Button(parent.getContext());
            button.setText(label);
            return button;
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
    }
}
