public class ActivityPicker extends AlertActivity implements
        DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private PickAdapter mAdapter;
    private Intent mBaseIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        Parcelable parcel = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (parcel instanceof Intent) {
            mBaseIntent = (Intent) parcel;
        } else {
            mBaseIntent = new Intent(Intent.ACTION_MAIN, null);
            mBaseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        AlertController.AlertParams params = mAlertParams;
        params.mOnClickListener = this;
        params.mOnCancelListener = this;
        if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            params.mTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
        } else {
            params.mTitle = getTitle();
        }
        List<PickAdapter.Item> items = getItems();
        mAdapter = new PickAdapter(this, items);
        params.mAdapter = mAdapter;
        setupAlert();
    }
    public void onClick(DialogInterface dialog, int which) {
        Intent intent = getIntentForPosition(which);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    public void onCancel(DialogInterface dialog) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
    protected Intent getIntentForPosition(int position) {
        PickAdapter.Item item = (PickAdapter.Item) mAdapter.getItem(position);
        return item.getIntent(mBaseIntent);
    }
    protected List<PickAdapter.Item> getItems() {
        PackageManager packageManager = getPackageManager();
        List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
        final Intent intent = getIntent();
        ArrayList<String> labels =
            intent.getStringArrayListExtra(Intent.EXTRA_SHORTCUT_NAME);
        ArrayList<ShortcutIconResource> icons =
            intent.getParcelableArrayListExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        if (labels != null && icons != null && labels.size() == icons.size()) {
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                Drawable icon = null;
                try {
                    ShortcutIconResource iconResource = icons.get(i);
                    Resources res = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    icon = res.getDrawable(res.getIdentifier(
                            iconResource.resourceName, null, null));
                } catch (NameNotFoundException e) {
                }
                items.add(new PickAdapter.Item(this, label, icon));
            }
        }
        if (mBaseIntent != null) {
            putIntentItems(mBaseIntent, items);
        }
        return items;
    }
    protected void putIntentItems(Intent baseIntent, List<PickAdapter.Item> items) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(baseIntent,
                0 );
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(packageManager));
        final int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            items.add(new PickAdapter.Item(this, packageManager, resolveInfo));
        }
    }
    protected static class PickAdapter extends BaseAdapter {
        public static class Item {
            protected static IconResizer sResizer;
            protected IconResizer getResizer(Context context) {
                if (sResizer == null) {
                    final Resources resources = context.getResources();
                    int size = (int) resources.getDimension(android.R.dimen.app_icon_size);
                    sResizer = new IconResizer(size, size, resources.getDisplayMetrics());
                }
                return sResizer;
            }
            CharSequence label;
            Drawable icon;
            String packageName;
            String className;
            Bundle extras;
            Item(Context context, CharSequence label, Drawable icon) {
                this.label = label;
                this.icon = getResizer(context).createIconThumbnail(icon);
            }
            Item(Context context, PackageManager pm, ResolveInfo resolveInfo) {
                label = resolveInfo.loadLabel(pm);
                if (label == null && resolveInfo.activityInfo != null) {
                    label = resolveInfo.activityInfo.name;
                }
                icon = getResizer(context).createIconThumbnail(resolveInfo.loadIcon(pm));
                packageName = resolveInfo.activityInfo.applicationInfo.packageName;
                className = resolveInfo.activityInfo.name;
            }
            Intent getIntent(Intent baseIntent) {
                Intent intent = new Intent(baseIntent);
                if (packageName != null && className != null) {
                    intent.setClassName(packageName, className);
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                } else {
                    intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
                }
                return intent;
            }
        }
        private final LayoutInflater mInflater;
        private final List<Item> mItems;
        public PickAdapter(Context context, List<Item> items) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItems = items;
        }
        public int getCount() {
            return mItems.size();
        }
        public Object getItem(int position) {
            return mItems.get(position);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.pick_item, parent, false);
            }
            Item item = (Item) getItem(position);
            TextView textView = (TextView) convertView;
            textView.setText(item.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
            return convertView;
        }
    }
    private static class IconResizer {
        private final int mIconWidth;
        private final int mIconHeight;
        private final DisplayMetrics mMetrics;
        private final Rect mOldBounds = new Rect();
        private final Canvas mCanvas = new Canvas();
        public IconResizer(int width, int height, DisplayMetrics metrics) {
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                    Paint.FILTER_BITMAP_FLAG));
            mMetrics = metrics;
            mIconWidth = width;
            mIconHeight = height; 
        }
        public Drawable createIconThumbnail(Drawable icon) {
            int width = mIconWidth;
            int height = mIconHeight;
            if (icon == null) {
                return new EmptyDrawable(width, height);
            }
            try {
                if (icon instanceof PaintDrawable) {
                    PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                } else if (icon instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                        bitmapDrawable.setTargetDensity(mMetrics);
                    }
                }
                int iconWidth = icon.getIntrinsicWidth();
                int iconHeight = icon.getIntrinsicHeight();
                if (iconWidth > 0 && iconHeight > 0) {
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
                        icon = new BitmapDrawable(thumb);
                        ((BitmapDrawable) icon).setTargetDensity(mMetrics);
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
                        icon = new BitmapDrawable(thumb);
                        ((BitmapDrawable) icon).setTargetDensity(mMetrics);
                    }
                }
            } catch (Throwable t) {
                icon = new EmptyDrawable(width, height);
            }
            return icon;
        }
    }
    private static class EmptyDrawable extends Drawable {
        private final int mWidth;
        private final int mHeight;
        EmptyDrawable(int width, int height) {
            mWidth = width;
            mHeight = height;
        }
        @Override
        public int getIntrinsicWidth() {
            return mWidth;
        }
        @Override
        public int getIntrinsicHeight() {
            return mHeight;
        }
        @Override
        public int getMinimumWidth() {
            return mWidth;
        }
        @Override
        public int getMinimumHeight() {
            return mHeight;
        }
        @Override
        public void draw(Canvas canvas) {
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }    
}
