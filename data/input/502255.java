public class WebsiteSettingsActivity extends ListActivity {
    private String LOGTAG = "WebsiteSettingsActivity";
    private static String sMBStored = null;
    private SiteAdapter mAdapter = null;
    class Site {
        private String mOrigin;
        private String mTitle;
        private Bitmap mIcon;
        private int mFeatures;
        private final static int FEATURE_WEB_STORAGE = 0;
        private final static int FEATURE_GEOLOCATION = 1;
        private final static int FEATURE_COUNT = 2;
        public Site(String origin) {
            mOrigin = origin;
            mTitle = null;
            mIcon = null;
            mFeatures = 0;
        }
        public void addFeature(int feature) {
            mFeatures |= (1 << feature);
        }
        public void removeFeature(int feature) {
            mFeatures &= ~(1 << feature);
        }
        public boolean hasFeature(int feature) {
            return (mFeatures & (1 << feature)) != 0;
        }
        public int getFeatureCount() {
            int count = 0;
            for (int i = 0; i < FEATURE_COUNT; ++i) {
                count += hasFeature(i) ? 1 : 0;
            }
            return count;
        }
        public int getFeatureByIndex(int n) {
            int j = -1;
            for (int i = 0; i < FEATURE_COUNT; ++i) {
                j += hasFeature(i) ? 1 : 0;
                if (j == n) {
                    return i;
                }
            }
            return -1;
        }
        public String getOrigin() {
            return mOrigin;
        }
        public void setTitle(String title) {
            mTitle = title;
        }
        public void setIcon(Bitmap icon) {
            mIcon = icon;
        }
        public Bitmap getIcon() {
            return mIcon;
        }
        public String getPrettyOrigin() {
            return mTitle == null ? null : hideHttp(mOrigin);
        }
        public String getPrettyTitle() {
            return mTitle == null ? hideHttp(mOrigin) : mTitle;
        }
        private String hideHttp(String str) {
            Uri uri = Uri.parse(str);
            return "http".equals(uri.getScheme()) ?  str.substring(7) : str;
        }
    }
    class SiteAdapter extends ArrayAdapter<Site>
            implements AdapterView.OnItemClickListener {
        private int mResource;
        private LayoutInflater mInflater;
        private Bitmap mDefaultIcon;
        private Bitmap mUsageEmptyIcon;
        private Bitmap mUsageLowIcon;
        private Bitmap mUsageHighIcon;
        private Bitmap mLocationAllowedIcon;
        private Bitmap mLocationDisallowedIcon;
        private Site mCurrentSite;
        public SiteAdapter(Context context, int rsc) {
            super(context, rsc);
            mResource = rsc;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDefaultIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.app_web_browser_sm);
            mUsageEmptyIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_list_data_off);
            mUsageLowIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_list_data_small);
            mUsageHighIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_list_data_large);
            mLocationAllowedIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_list_gps_on);
            mLocationDisallowedIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_list_gps_denied);
            askForOrigins();
        }
        private void addFeatureToSite(Map sites, String origin, int feature) {
            Site site = null;
            if (sites.containsKey(origin)) {
                site = (Site) sites.get(origin);
            } else {
                site = new Site(origin);
                sites.put(origin, site);
            }
            site.addFeature(feature);
        }
        public void askForOrigins() {
            WebStorage.getInstance().getOrigins(new ValueCallback<Map>() {
                public void onReceiveValue(Map origins) {
                    Map sites = new HashMap<String, Site>();
                    if (origins != null) {
                        Iterator<String> iter = origins.keySet().iterator();
                        while (iter.hasNext()) {
                            addFeatureToSite(sites, iter.next(), Site.FEATURE_WEB_STORAGE);
                        }
                    }
                    askForGeolocation(sites);
                }
            });
        }
        public void askForGeolocation(final Map sites) {
            GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String> >() {
                public void onReceiveValue(Set<String> origins) {
                    if (origins != null) {
                        Iterator<String> iter = origins.iterator();
                        while (iter.hasNext()) {
                            addFeatureToSite(sites, iter.next(), Site.FEATURE_GEOLOCATION);
                        }
                    }
                    populateIcons(sites);
                    populateOrigins(sites);
                }
            });
        }
        public void populateIcons(Map sites) {
            HashMap hosts = new HashMap<String, Set<Site> >();
            Set keys = sites.keySet();
            Iterator<String> originIter = keys.iterator();
            while (originIter.hasNext()) {
                String origin = originIter.next();
                Site site = (Site) sites.get(origin);
                String host = Uri.parse(origin).getHost();
                Set hostSites = null;
                if (hosts.containsKey(host)) {
                    hostSites = (Set) hosts.get(host);
                } else {
                    hostSites = new HashSet<Site>();
                    hosts.put(host, hostSites);
                }
                hostSites.add(site);
            }
            Cursor c = getContext().getContentResolver().query(Browser.BOOKMARKS_URI,
                    new String[] { Browser.BookmarkColumns.URL, Browser.BookmarkColumns.TITLE,
                    Browser.BookmarkColumns.FAVICON }, "bookmark = 1", null, null);
            if ((c != null) && c.moveToFirst()) {
                int urlIndex = c.getColumnIndex(Browser.BookmarkColumns.URL);
                int titleIndex = c.getColumnIndex(Browser.BookmarkColumns.TITLE);
                int faviconIndex = c.getColumnIndex(Browser.BookmarkColumns.FAVICON);
                do {
                    String url = c.getString(urlIndex);
                    String host = Uri.parse(url).getHost();
                    if (hosts.containsKey(host)) {
                        String title = c.getString(titleIndex);
                        Bitmap bmp = null;
                        byte[] data = c.getBlob(faviconIndex);
                        if (data != null) {
                            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        }
                        Set matchingSites = (Set) hosts.get(host);
                        Iterator<Site> sitesIter = matchingSites.iterator();
                        while (sitesIter.hasNext()) {
                            Site site = sitesIter.next();
                            if (url.equals(site.getOrigin()) ||
                                    (new String(site.getOrigin()+"/")).equals(url)) {
                                site.setTitle(title);
                            }
                            if (bmp != null) {
                                site.setIcon(bmp);
                            }
                        }
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        public void populateOrigins(Map sites) {
            clear();
            Set keys = sites.keySet();
            Iterator<String> originIter = keys.iterator();
            while (originIter.hasNext()) {
                String origin = originIter.next();
                Site site = (Site) sites.get(origin);
                add(site);
            }
            notifyDataSetChanged();
            if (getCount() == 0) {
                finish(); 
            }
        }
        public int getCount() {
            if (mCurrentSite == null) {
                return super.getCount();
            }
            return mCurrentSite.getFeatureCount();
        }
        public String sizeValueToString(long bytes) {
            if (bytes <= 0) {
                Log.e(LOGTAG, "sizeValueToString called with non-positive value: " + bytes);
                return "0";
            }
            float megabytes = (float) bytes / (1024.0F * 1024.0F);
            int truncated = (int) Math.ceil(megabytes * 10.0F);
            float result = (float) (truncated / 10.0F);
            return String.valueOf(result);
        }
        public boolean backKeyPressed() {
            if (mCurrentSite != null) {
                mCurrentSite = null;
                askForOrigins();
                return true;
            }
            return false;
        }
        public void setIconForUsage(ImageView usageIcon, long usageInBytes) {
            float usageInMegabytes = (float) usageInBytes / (1024.0F * 1024.0F);
            if (usageInMegabytes <= 0.1) {
                usageIcon.setImageBitmap(mUsageEmptyIcon);
            } else if (usageInMegabytes > 0.1 && usageInMegabytes <= 5) {
                usageIcon.setImageBitmap(mUsageLowIcon);
            } else if (usageInMegabytes > 5) {
                usageIcon.setImageBitmap(mUsageHighIcon);
            }
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            final TextView title;
            final TextView subtitle;
            final ImageView icon;
            final ImageView usageIcon;
            final ImageView locationIcon;
            final ImageView featureIcon;
            if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);
            } else {
                view = convertView;
            }
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            icon = (ImageView) view.findViewById(R.id.icon);
            featureIcon = (ImageView) view.findViewById(R.id.feature_icon);
            usageIcon = (ImageView) view.findViewById(R.id.usage_icon);
            locationIcon = (ImageView) view.findViewById(R.id.location_icon);
            usageIcon.setVisibility(View.GONE);
            locationIcon.setVisibility(View.GONE);
            if (mCurrentSite == null) {
                setTitle(getString(R.string.pref_extras_website_settings));
                Site site = getItem(position);
                title.setText(site.getPrettyTitle());
                String subtitleText = site.getPrettyOrigin();
                if (subtitleText != null) {
                    title.setMaxLines(1);
                    title.setSingleLine(true);
                    subtitle.setVisibility(View.VISIBLE);
                    subtitle.setText(subtitleText);
                } else {
                    subtitle.setVisibility(View.GONE);
                    title.setMaxLines(2);
                    title.setSingleLine(false);
                }
                icon.setVisibility(View.VISIBLE);
                usageIcon.setVisibility(View.INVISIBLE);
                locationIcon.setVisibility(View.INVISIBLE);
                featureIcon.setVisibility(View.GONE);
                Bitmap bmp = site.getIcon();
                if (bmp == null) {
                    bmp = mDefaultIcon;
                }
                icon.setImageBitmap(bmp);
                view.setTag(site);
                String origin = site.getOrigin();
                if (site.hasFeature(Site.FEATURE_WEB_STORAGE)) {
                    WebStorage.getInstance().getUsageForOrigin(origin, new ValueCallback<Long>() {
                        public void onReceiveValue(Long value) {
                            if (value != null) {
                                setIconForUsage(usageIcon, value.longValue());
                                usageIcon.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                if (site.hasFeature(Site.FEATURE_GEOLOCATION)) {
                    locationIcon.setVisibility(View.VISIBLE);
                    GeolocationPermissions.getInstance().getAllowed(origin, new ValueCallback<Boolean>() {
                        public void onReceiveValue(Boolean allowed) {
                            if (allowed != null) {
                                if (allowed.booleanValue()) {
                                    locationIcon.setImageBitmap(mLocationAllowedIcon);
                                } else {
                                    locationIcon.setImageBitmap(mLocationDisallowedIcon);
                                }
                            }
                        }
                    });
                }
            } else {
                icon.setVisibility(View.GONE);
                locationIcon.setVisibility(View.GONE);
                usageIcon.setVisibility(View.GONE);
                featureIcon.setVisibility(View.VISIBLE);
                setTitle(mCurrentSite.getPrettyTitle());
                String origin = mCurrentSite.getOrigin();
                switch (mCurrentSite.getFeatureByIndex(position)) {
                    case Site.FEATURE_WEB_STORAGE:
                        WebStorage.getInstance().getUsageForOrigin(origin, new ValueCallback<Long>() {
                            public void onReceiveValue(Long value) {
                                if (value != null) {
                                    String usage = sizeValueToString(value.longValue()) + " " + sMBStored;
                                    title.setText(R.string.webstorage_clear_data_title);
                                    subtitle.setText(usage);
                                    subtitle.setVisibility(View.VISIBLE);
                                    setIconForUsage(featureIcon, value.longValue());
                                }
                            }
                        });
                        break;
                    case Site.FEATURE_GEOLOCATION:
                        title.setText(R.string.geolocation_settings_page_title);
                        GeolocationPermissions.getInstance().getAllowed(origin, new ValueCallback<Boolean>() {
                            public void onReceiveValue(Boolean allowed) {
                                if (allowed != null) {
                                    if (allowed.booleanValue()) {
                                        subtitle.setText(R.string.geolocation_settings_page_summary_allowed);
                                        featureIcon.setImageBitmap(mLocationAllowedIcon);
                                    } else {
                                        subtitle.setText(R.string.geolocation_settings_page_summary_not_allowed);
                                        featureIcon.setImageBitmap(mLocationDisallowedIcon);
                                    }
                                    subtitle.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        break;
                }
            }
            return view;
        }
        public void onItemClick(AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {
            if (mCurrentSite != null) {
                switch (mCurrentSite.getFeatureByIndex(position)) {
                    case Site.FEATURE_WEB_STORAGE:
                        new AlertDialog.Builder(getContext())
                            .setTitle(R.string.webstorage_clear_data_dialog_title)
                            .setMessage(R.string.webstorage_clear_data_dialog_message)
                            .setPositiveButton(R.string.webstorage_clear_data_dialog_ok_button,
                                               new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dlg, int which) {
                                    WebStorage.getInstance().deleteOrigin(mCurrentSite.getOrigin());
                                    mCurrentSite.removeFeature(Site.FEATURE_WEB_STORAGE);
                                    if (mCurrentSite.getFeatureCount() == 0) {
                                        mCurrentSite = null;
                                    }
                                    askForOrigins();
                                    notifyDataSetChanged();
                                }})
                            .setNegativeButton(R.string.webstorage_clear_data_dialog_cancel_button, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                        break;
                    case Site.FEATURE_GEOLOCATION:
                        new AlertDialog.Builder(getContext())
                            .setTitle(R.string.geolocation_settings_page_dialog_title)
                            .setMessage(R.string.geolocation_settings_page_dialog_message)
                            .setPositiveButton(R.string.geolocation_settings_page_dialog_ok_button,
                                               new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dlg, int which) {
                                    GeolocationPermissions.getInstance().clear(mCurrentSite.getOrigin());
                                    mCurrentSite.removeFeature(Site.FEATURE_GEOLOCATION);
                                    if (mCurrentSite.getFeatureCount() == 0) {
                                        mCurrentSite = null;
                                    }
                                    askForOrigins();
                                    notifyDataSetChanged();
                                }})
                            .setNegativeButton(R.string.geolocation_settings_page_dialog_cancel_button, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                        break;
                }
            } else {
                mCurrentSite = (Site) view.getTag();
                notifyDataSetChanged();
            }
        }
        public Site currentSite() {
            return mCurrentSite;
        }
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if ((mAdapter != null) && (mAdapter.backKeyPressed())){
                return true; 
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (sMBStored == null) {
            sMBStored = getString(R.string.webstorage_origin_summary_mb_stored);
        }
        mAdapter = new SiteAdapter(this, R.layout.website_settings_row);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(mAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.websitesettings, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return  mAdapter.currentSite() == null && mAdapter.getCount() > 0;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.website_settings_menu_clear_all:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.website_settings_clear_all_dialog_title)
                        .setMessage(R.string.website_settings_clear_all_dialog_message)
                        .setPositiveButton(R.string.website_settings_clear_all_dialog_ok_button,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int which) {
                                        WebStorage.getInstance().deleteAllData();
                                        GeolocationPermissions.getInstance().clearAll();
                                        WebStorageSizeManager.resetLastOutOfSpaceNotificationTime();
                                        mAdapter.askForOrigins();
                                        finish();
                                    }})
                        .setNegativeButton(R.string.website_settings_clear_all_dialog_cancel_button, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }
        return false;
    }
}
