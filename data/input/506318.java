public abstract class SimpleCursorTreeAdapter extends ResourceCursorTreeAdapter {
    private int[] mGroupFrom;
    private int[] mGroupTo;
    private int[] mChildFrom;
    private int[] mChildTo;
    private ViewBinder mViewBinder;
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout,
            int lastChildLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout,
                lastChildLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, String[] groupFrom, int[] groupTo,
            int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int groupLayout,
            String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom,
            int[] childTo) {
        super(context, cursor, groupLayout, childLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }
    private void init(String[] groupFromNames, int[] groupTo, String[] childFromNames,
            int[] childTo) {
        mGroupTo = groupTo;
        mChildTo = childTo;
        initGroupFromColumns(groupFromNames);
        if (getGroupCount() > 0) {
            MyCursorHelper tmpCursorHelper = getChildrenCursorHelper(0, true);
            if (tmpCursorHelper != null) {
                initChildrenFromColumns(childFromNames, tmpCursorHelper.getCursor());
                deactivateChildrenCursorHelper(0);
            }
        }
    }
    private void initFromColumns(Cursor cursor, String[] fromColumnNames, int[] fromColumns) {
        for (int i = fromColumnNames.length - 1; i >= 0; i--) {
            fromColumns[i] = cursor.getColumnIndexOrThrow(fromColumnNames[i]);
        }
    }
    private void initGroupFromColumns(String[] groupFromNames) {
        mGroupFrom = new int[groupFromNames.length];
        initFromColumns(mGroupCursorHelper.getCursor(), groupFromNames, mGroupFrom);
    }
    private void initChildrenFromColumns(String[] childFromNames, Cursor childCursor) {
        mChildFrom = new int[childFromNames.length];
        initFromColumns(childCursor, childFromNames, mChildFrom);
    }
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }
    private void bindView(View view, Context context, Cursor cursor, int[] from, int[] to) {
        final ViewBinder binder = mViewBinder;
        for (int i = 0; i < to.length; i++) {
            View v = view.findViewById(to[i]);
            if (v != null) {
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[i]);
                }
                if (!bound) {
                    String text = cursor.getString(from[i]);
                    if (text == null) {
                        text = "";
                    }
                    if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        setViewImage((ImageView) v, text);
                    } else {
                        throw new IllegalStateException("SimpleCursorTreeAdapter can bind values" +
                                " only to TextView and ImageView!");
                    }
                }
            }
        }
    }
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        bindView(view, context, cursor, mChildFrom, mChildTo);
    }
    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        bindView(view, context, cursor, mGroupFrom, mGroupTo);
    }
    protected void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }
    public void setViewText(TextView v, String text) {
        v.setText(text);
    }
    public static interface ViewBinder {
        boolean setViewValue(View view, Cursor cursor, int columnIndex);
    }
}
