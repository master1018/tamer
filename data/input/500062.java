class HistoryAdapter extends BaseAdapter {
    private Vector<HistoryEntry> mEntries;
    private LayoutInflater mInflater;
    private Logic mEval;
    HistoryAdapter(Context context, History history, Logic evaluator) {
        mEntries = history.mEntries;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEval = evaluator;
    }
    public int getCount() {
        return mEntries.size() - 1;
    }
    public Object getItem(int position) {
        return mEntries.elementAt(position);
    }
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.history_item, parent, false);
        } else {
            view = convertView;
        }
        TextView expr   = (TextView) view.findViewById(R.id.historyExpr);
        TextView result = (TextView) view.findViewById(R.id.historyResult);
        HistoryEntry entry = mEntries.elementAt(position);
        String base = entry.getBase();
        expr.setText(entry.getBase());
        try {
            String res = mEval.evaluate(base);
            result.setText("= " + res);
        } catch (SyntaxException e) {
            result.setText("");
        }
        return view;
    }
}
