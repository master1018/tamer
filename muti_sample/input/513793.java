public class ActiveTabsPage extends LinearLayout {
    private final BrowserActivity   mBrowserActivity;
    private final LayoutInflater    mFactory;
    private final TabControl        mControl;
    private final TabsListAdapter   mAdapter;
    private final ListView          mListView;
    public ActiveTabsPage(BrowserActivity context, TabControl control) {
        super(context);
        mBrowserActivity = context;
        mControl = control;
        mFactory = LayoutInflater.from(context);
        mFactory.inflate(R.layout.active_tabs, this);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new TabsListAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (mControl.canCreateNewTab()) {
                        position--;
                    }
                    boolean needToAttach = false;
                    if (position == -1) {
                        mBrowserActivity.openTabToHomePage();
                    } else {
                        needToAttach = !mBrowserActivity.switchToTab(position);
                    }
                    mBrowserActivity.removeActiveTabPage(needToAttach);
                }
        });
    }
    private static class CloseHolder extends ImageView {
        public CloseHolder(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        public void setPressed(boolean pressed) {
            if (pressed && ((View) getParent()).isPressed()) {
                return;
            }
            super.setPressed(pressed);
        }
    }
    private class TabsListAdapter extends BaseAdapter {
        private boolean mNotified = true;
        private int mReturnedCount;
        private Handler mHandler = new Handler();
        public int getCount() {
            int count = mControl.getTabCount();
            if (mControl.canCreateNewTab()) {
                count++;
            }
            if (!mNotified && count != mReturnedCount) {
                notifyChange();
                return mReturnedCount;
            }
            mReturnedCount = count;
            mNotified = false;
            return count;
        }
        public Object getItem(int position) {
            return null;
        }
        public long getItemId(int position) {
            return position;
        }
        public int getViewTypeCount() {
            return 2;
        }
        public int getItemViewType(int position) {
            if (mControl.canCreateNewTab()) {
                position--;
            }
            return position == -1 ? IGNORE_ITEM_VIEW_TYPE : 1;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            final int tabCount = mControl.getTabCount();
            if (mControl.canCreateNewTab()) {
                position--;
            }
            if (convertView == null) {
                convertView = mFactory.inflate(position == -1 ?
                        R.layout.tab_view_add_tab : R.layout.tab_view, null);
            }
            if (position != -1) {
                TextView title =
                        (TextView) convertView.findViewById(R.id.title);
                TextView url = (TextView) convertView.findViewById(R.id.url);
                ImageView favicon =
                        (ImageView) convertView.findViewById(R.id.favicon);
                View close = convertView.findViewById(R.id.close);
                Tab tab = mControl.getTab(position);
                tab.populatePickerData();
                title.setText(tab.getTitle());
                url.setText(tab.getUrl());
                Bitmap icon = tab.getFavicon();
                if (icon != null) {
                    favicon.setImageBitmap(icon);
                } else {
                    favicon.setImageResource(R.drawable.app_web_browser_sm);
                }
                final int closePosition = position;
                close.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            mBrowserActivity.closeTab(
                                    mControl.getTab(closePosition));
                            if (tabCount == 1) {
                                mBrowserActivity.openTabToHomePage();
                                mBrowserActivity.removeActiveTabPage(false);
                            } else {
                                mNotified = true;
                                notifyDataSetChanged();
                            }
                        }
                });
            }
            return convertView;
        }
        void notifyChange() {
            mHandler.post(new Runnable() {
                public void run() {
                    mNotified = true;
                    notifyDataSetChanged();
                }
            });
        }
    }
}
