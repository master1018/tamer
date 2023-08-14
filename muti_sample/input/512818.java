public class ListItemsExpandOnSelection extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setNumItems(10)
                .setItemScreenSizeFactor(1.0/5);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        TextView result = new ExpandWhenSelectedView(parent.getContext(), desiredHeight);
        result.setHeight(desiredHeight);
        result.setFocusable(mItemsFocusable);
        result.setText(getValueAtPosition(position));
        final AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lp);
        return result;
    }
    @Override
    public View convertView(int position, View convertView, ViewGroup parent) {
        ((ExpandWhenSelectedView)convertView).setText(getValueAtPosition(position));
        return convertView;
    }
    static private class ExpandWhenSelectedView extends TextView {
        private final int mDesiredHeight;
        public ExpandWhenSelectedView(Context context, int desiredHeight) {
            super(context);
            mDesiredHeight = desiredHeight;
        }
        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if (selected) {
                setHeight((int) (mDesiredHeight * 1.5));
            } else {
                setHeight(mDesiredHeight);
            }
        }
    }
}
