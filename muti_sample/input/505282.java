public class GridDelete extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(1001)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.20)
                .setVerticalSpacing(20);
    }
    @Override
    protected ListAdapter createAdapter() {
        return new DeleteAdapter(getInitialNumItems());
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            GridView g = getGridView();
            ((DeleteAdapter)g.getAdapter()).deletePosition(g.getSelectedItemPosition());
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    private class DeleteAdapter extends BaseAdapter {
        private ArrayList<Integer> mData;
        public DeleteAdapter(int initialNumItems) {
            super();
            mData = new ArrayList<Integer>(initialNumItems);
            int i;
            for (i=0; i<initialNumItems; ++i) {
                mData.add(new Integer(10000 + i));
            }
        }
        public void deletePosition(int selectedItemPosition) {
            if (selectedItemPosition >=0 && selectedItemPosition < mData.size()) {
                mData.remove(selectedItemPosition);
                notifyDataSetChanged();
            }
        }
        public int getCount() {
            return mData.size();
        }
        public Object getItem(int position) {
            return mData.get(position);
        }
        public long getItemId(int position) {
            return mData.get(position);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            int desiredHeight = getDesiredItemHeight();
            return createView(mData.get(position), parent, desiredHeight);
        }
    }
}
