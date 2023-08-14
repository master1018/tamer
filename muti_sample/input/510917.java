public class ResolverActivity extends AlertActivity implements
        DialogInterface.OnClickListener, CheckBox.OnCheckedChangeListener {
    private ResolveListAdapter mAdapter;
    private CheckBox mAlwaysCheck;
    private TextView mClearDefaultHint;
    private PackageManager mPm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, new Intent(getIntent()),
                getResources().getText(com.android.internal.R.string.whichApplication),
                null, true);
    }
    protected void onCreate(Bundle savedInstanceState, Intent intent,
            CharSequence title, Intent[] initialIntents, boolean alwaysUseOption) {
        super.onCreate(savedInstanceState);
        mPm = getPackageManager();
        intent.setComponent(null);
        AlertController.AlertParams ap = mAlertParams;
        ap.mTitle = title;
        ap.mOnClickListener = this;
        if (alwaysUseOption) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ap.mView = inflater.inflate(R.layout.always_use_checkbox, null);
            mAlwaysCheck = (CheckBox)ap.mView.findViewById(com.android.internal.R.id.alwaysUse);
            mAlwaysCheck.setText(R.string.alwaysUse);
            mAlwaysCheck.setOnCheckedChangeListener(this);
            mClearDefaultHint = (TextView)ap.mView.findViewById(
                                                        com.android.internal.R.id.clearDefaultHint);
            mClearDefaultHint.setVisibility(View.GONE);
        }
        mAdapter = new ResolveListAdapter(this, intent, initialIntents);
        if (mAdapter.getCount() > 1) {
            ap.mAdapter = mAdapter;
        } else if (mAdapter.getCount() == 1) {
            startActivity(mAdapter.intentForPosition(0));
            finish();
            return;
        } else {
            ap.mMessage = getResources().getText(com.android.internal.R.string.noApplications);
        }
        setupAlert();
    }
    public void onClick(DialogInterface dialog, int which) {
        ResolveInfo ri = mAdapter.resolveInfoForPosition(which);
        Intent intent = mAdapter.intentForPosition(which);
        if ((mAlwaysCheck != null) && mAlwaysCheck.isChecked()) {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            int cat = ri.match&IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w("ResolverActivity", e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                                && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());
                    Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(),
                                        port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }
            if (filter != null) {
                final int N = mAdapter.mList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo r = mAdapter.mList.get(i).ri;
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                getPackageManager().addPreferredActivity(filter, bestMatch, set,
                        intent.getComponent());
            }
        }
        if (intent != null) {
            startActivity(intent);
        }
        finish();
    }
    private final class DisplayResolveInfo {
        ResolveInfo ri;
        CharSequence displayLabel;
        Drawable displayIcon;
        CharSequence extendedInfo;
        Intent origIntent;
        DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                CharSequence pInfo, Intent pOrigIntent) {
            ri = pri;
            displayLabel = pLabel;
            extendedInfo = pInfo;
            origIntent = pOrigIntent;
        }
    }
    private final class ResolveListAdapter extends BaseAdapter {
        private final Intent mIntent;
        private final LayoutInflater mInflater;
        private List<DisplayResolveInfo> mList;
        public ResolveListAdapter(Context context, Intent intent,
                Intent[] initialIntents) {
            mIntent = new Intent(intent);
            mIntent.setComponent(null);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            List<ResolveInfo> rList = mPm.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY
                    | (mAlwaysCheck != null ? PackageManager.GET_RESOLVED_FILTER : 0));
            int N;
            if ((rList != null) && ((N = rList.size()) > 0)) {
                ResolveInfo r0 = rList.get(0);
                for (int i=1; i<N; i++) {
                    ResolveInfo ri = rList.get(i);
                    if (Config.LOGV) Log.v(
                        "ResolveListActivity",
                        r0.activityInfo.name + "=" +
                        r0.priority + "/" + r0.isDefault + " vs " +
                        ri.activityInfo.name + "=" +
                        ri.priority + "/" + ri.isDefault);
                   if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                        while (i < N) {
                            rList.remove(i);
                            N--;
                        }
                    }
                }
                if (N > 1) {
                    ResolveInfo.DisplayNameComparator rComparator =
                            new ResolveInfo.DisplayNameComparator(mPm);
                    Collections.sort(rList, rComparator);
                }
                mList = new ArrayList<DisplayResolveInfo>();
                if (initialIntents != null) {
                    for (int i=0; i<initialIntents.length; i++) {
                        Intent ii = initialIntents[i];
                        if (ii == null) {
                            continue;
                        }
                        ActivityInfo ai = ii.resolveActivityInfo(
                                getPackageManager(), 0);
                        if (ai == null) {
                            Log.w("ResolverActivity", "No activity found for "
                                    + ii);
                            continue;
                        }
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent)ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
                        }
                        mList.add(new DisplayResolveInfo(ri,
                                ri.loadLabel(getPackageManager()), null, ii));
                    }
                }
                r0 = rList.get(0);
                int start = 0;
                CharSequence r0Label =  r0.loadLabel(mPm);
                for (int i = 1; i < N; i++) {
                    if (r0Label == null) {
                        r0Label = r0.activityInfo.packageName;
                    }
                    ResolveInfo ri = rList.get(i);
                    CharSequence riLabel = ri.loadLabel(mPm);
                    if (riLabel == null) {
                        riLabel = ri.activityInfo.packageName;
                    }
                    if (riLabel.equals(r0Label)) {
                        continue;
                    }
                    processGroup(rList, start, (i-1), r0, r0Label);
                    r0 = ri;
                    r0Label = riLabel;
                    start = i;
                }
                processGroup(rList, start, (N-1), r0, r0Label);
            }
        }
        private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro,
                CharSequence roLabel) {
            int num = end - start+1;
            if (num == 1) {
                mList.add(new DisplayResolveInfo(ro, roLabel, null, null));
            } else {
                boolean usePkg = false;
                CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(mPm);
                if (startApp == null) {
                    usePkg = true;
                }
                if (!usePkg) {
                    HashSet<CharSequence> duplicates =
                        new HashSet<CharSequence>();
                    duplicates.add(startApp);
                    for (int j = start+1; j <= end ; j++) {
                        ResolveInfo jRi = rList.get(j);
                        CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
                        if ( (jApp == null) || (duplicates.contains(jApp))) {
                            usePkg = true;
                            break;
                        } else {
                            duplicates.add(jApp);
                        }
                    }
                    duplicates.clear();
                }
                for (int k = start; k <= end; k++) {
                    ResolveInfo add = rList.get(k);
                    if (usePkg) {
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.packageName, null));
                    } else {
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.applicationInfo.loadLabel(mPm), null));
                    }
                }
            }
        }
        public ResolveInfo resolveInfoForPosition(int position) {
            if (mList == null) {
                return null;
            }
            return mList.get(position).ri;
        }
        public Intent intentForPosition(int position) {
            if (mList == null) {
                return null;
            }
            DisplayResolveInfo dri = mList.get(position);
            Intent intent = new Intent(dri.origIntent != null
                    ? dri.origIntent : mIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                    |Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            ActivityInfo ai = dri.ri.activityInfo;
            intent.setComponent(new ComponentName(
                    ai.applicationInfo.packageName, ai.name));
            return intent;
        }
        public int getCount() {
            return mList != null ? mList.size() : 0;
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
                        com.android.internal.R.layout.resolve_list_item, parent, false);
            } else {
                view = convertView;
            }
            bindView(view, mList.get(position));
            return view;
        }
        private final void bindView(View view, DisplayResolveInfo info) {
            TextView text = (TextView)view.findViewById(com.android.internal.R.id.text1);
            TextView text2 = (TextView)view.findViewById(com.android.internal.R.id.text2);
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            text.setText(info.displayLabel);
            if (info.extendedInfo != null) {
                text2.setVisibility(View.VISIBLE);
                text2.setText(info.extendedInfo);
            } else {
                text2.setVisibility(View.GONE);
            }
            if (info.displayIcon == null) {
                info.displayIcon = info.ri.loadIcon(mPm);
            }
            icon.setImageDrawable(info.displayIcon);
        }
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mClearDefaultHint == null) return;
        if(isChecked) {
            mClearDefaultHint.setVisibility(View.VISIBLE);
        } else {
            mClearDefaultHint.setVisibility(View.GONE);
        }
    }
}
