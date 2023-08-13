public abstract class LauncherActivity extends ListActivity {
    Intent mIntent;
    PackageManager mPackageManager;
    IconResizer mIconResizer;
    public static class ListItem {
        public ResolveInfo resolveInfo;
        public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String className;
        public Bundle extras;
        ListItem(PackageManager pm, ResolveInfo resolveInfo, IconResizer resizer) {
            this.resolveInfo = resolveInfo;
            label = resolveInfo.loadLabel(pm);
            ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null) ci = resolveInfo.serviceInfo;
            if (label == null && ci != null) {
                label = resolveInfo.activityInfo.name;
            }
            if (resizer != null) {
                icon = resizer.createIconThumbnail(resolveInfo.loadIcon(pm));
            }
            packageName = ci.applicationInfo.packageName;
            className = ci.name;
        }
        public ListItem() {
        }
    }
    private class ActivityAdapter extends BaseAdapter implements Filterable {
        private final Object lock = new Object();
        private ArrayList<ListItem> mOriginalValues;
        protected final IconResizer mIconResizer;
        protected final LayoutInflater mInflater;
        protected List<ListItem> mActivitiesList;
        private Filter mFilter;
        public ActivityAdapter(IconResizer resizer) {
            mIconResizer = resizer;
            mInflater = (LayoutInflater) LauncherActivity.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mActivitiesList = makeListItems();
        }
        public Intent intentForPosition(int position) {
            if (mActivitiesList == null) {
                return null;
            }
            Intent intent = new Intent(mIntent);
            ListItem item = mActivitiesList.get(position);
            intent.setClassName(item.packageName, item.className);
            if (item.extras != null) {
                intent.putExtras(item.extras);
            }
            return intent;
        }
        public ListItem itemForPosition(int position) {
            if (mActivitiesList == null) {
                return null;
            }
            return mActivitiesList.get(position);
        }
        public int getCount() {
            return mActivitiesList != null ? mActivitiesList.size() : 0;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(
                        com.android.internal.R.layout.activity_list_item_2, parent, false);
            } else {
                view = convertView;
            }
            bindView(view, mActivitiesList.get(position));
            return view;
        }
        private void bindView(View view, ListItem item) {
            TextView text = (TextView) view;
            text.setText(item.label);
            if (item.icon == null) {
                item.icon = mIconResizer.createIconThumbnail(
                        item.resolveInfo.loadIcon(getPackageManager()));
            }
            text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
        }
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }
        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (mOriginalValues == null) {
                    synchronized (lock) {
                        mOriginalValues = new ArrayList<ListItem>(mActivitiesList);
                    }
                }
                if (prefix == null || prefix.length() == 0) {
                    synchronized (lock) {
                        ArrayList<ListItem> list = new ArrayList<ListItem>(mOriginalValues);
                        results.values = list;
                        results.count = list.size();
                    }
                } else {
                    final String prefixString = prefix.toString().toLowerCase();
                    ArrayList<ListItem> values = mOriginalValues;
                    int count = values.size();
                    ArrayList<ListItem> newValues = new ArrayList<ListItem>(count);
                    for (int i = 0; i < count; i++) {
                        ListItem item = values.get(i);
                        String[] words = item.label.toString().toLowerCase().split(" ");
                        int wordCount = words.length;
                        for (int k = 0; k < wordCount; k++) {
                            final String word = words[k];
                            if (word.startsWith(prefixString)) {
                                newValues.add(item);
                                break;
                            }
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mActivitiesList = (List<ListItem>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
    public class IconResizer {
        private int mIconWidth = -1;
        private int mIconHeight = -1;
        private final Rect mOldBounds = new Rect();
        private Canvas mCanvas = new Canvas();
        public IconResizer() {
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                    Paint.FILTER_BITMAP_FLAG));
            final Resources resources = LauncherActivity.this.getResources();
            mIconWidth = mIconHeight = (int) resources.getDimension(
                    android.R.dimen.app_icon_size);
        }
        public Drawable createIconThumbnail(Drawable icon) {
            int width = mIconWidth;
            int height = mIconHeight;
            final int iconWidth = icon.getIntrinsicWidth();
            final int iconHeight = icon.getIntrinsicHeight();
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            }
            if (width > 0 && height > 0) {
                if (width < iconWidth || height < iconHeight) {
                    final float ratio = (float) iconWidth / iconHeight;
                    if (iconWidth > iconHeight) {
                        height = (int) (width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int) (height * ratio);
                    }
                    final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
                                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                    final Canvas canvas = mCanvas;
                    canvas.setBitmap(thumb);
                    mOldBounds.set(icon.getBounds());
                    final int x = (mIconWidth - width) / 2;
                    final int y = (mIconHeight - height) / 2;
                    icon.setBounds(x, y, x + width, y + height);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = new BitmapDrawable(getResources(), thumb);
                } else if (iconWidth < width && iconHeight < height) {
                    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                    final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                    final Canvas canvas = mCanvas;
                    canvas.setBitmap(thumb);
                    mOldBounds.set(icon.getBounds());
                    final int x = (width - iconWidth) / 2;
                    final int y = (height - iconHeight) / 2;
                    icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = new BitmapDrawable(getResources(), thumb);
                }
            }
            return icon;
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPackageManager = getPackageManager();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        onSetContentView();
        mIconResizer = new IconResizer();
        mIntent = new Intent(getTargetIntent());
        mIntent.setComponent(null);
        mAdapter = new ActivityAdapter(mIconResizer);
        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(true);
        setProgressBarIndeterminateVisibility(false);
    }
    protected void onSetContentView() {
        setContentView(com.android.internal.R.layout.activity_list);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = intentForPosition(position);
        startActivity(intent);
    }
    protected Intent intentForPosition(int position) {
        ActivityAdapter adapter = (ActivityAdapter) mAdapter;
        return adapter.intentForPosition(position);
    }
    protected ListItem itemForPosition(int position) {
        ActivityAdapter adapter = (ActivityAdapter) mAdapter;
        return adapter.itemForPosition(position);
    }
    protected Intent getTargetIntent() {
        return new Intent();
    }
    protected List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return mPackageManager.queryIntentActivities(queryIntent,  0);
    }
    public List<ListItem> makeListItems() {
        List<ResolveInfo> list = onQueryPackageManager(mIntent);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));
        ArrayList<ListItem> result = new ArrayList<ListItem>(list.size());
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            result.add(new ListItem(mPackageManager, resolveInfo, null));
        }
        return result;
    }
}
