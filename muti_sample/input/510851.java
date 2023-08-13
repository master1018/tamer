public class MusicPicker extends ListActivity
        implements View.OnClickListener, MediaPlayer.OnCompletionListener,
        MusicUtils.Defs {
    static final boolean DBG = false;
    static final String TAG = "MusicPicker";
    static final String LIST_STATE_KEY = "liststate";
    static final String FOCUS_KEY = "focused";
    static final String SORT_MODE_KEY = "sortMode";
    static final int MY_QUERY_TOKEN = 42;
    static final int TRACK_MENU = Menu.FIRST;
    static final int ALBUM_MENU = Menu.FIRST+1;
    static final int ARTIST_MENU = Menu.FIRST+2;
    static final String[] CURSOR_COLS = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.TITLE_KEY,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK
    };
    static StringBuilder sFormatBuilder = new StringBuilder();
    static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    static final Object[] sTimeArgs = new Object[5];
    Uri mBaseUri;
    TrackListAdapter mAdapter;
    QueryHandler mQueryHandler;
    Parcelable mListState = null;
    boolean mListHasFocus;
    Cursor mCursor;
    int mSortMode = -1;
    String mSortOrder;
    View mProgressContainer;
    View mListContainer;
    boolean mListShown;
    View mOkayButton;
    View mCancelButton;
    long mSelectedId = -1;
    Uri mSelectedUri;
    long mPlayingId = -1;
    MediaPlayer mMediaPlayer;
    class TrackListAdapter extends SimpleCursorAdapter
            implements SectionIndexer {
        final ListView mListView;
        private final StringBuilder mBuilder = new StringBuilder();
        private final String mUnknownArtist;
        private final String mUnknownAlbum;
        private int mIdIdx;
        private int mTitleIdx;
        private int mArtistIdx;
        private int mAlbumIdx;
        private int mDurationIdx;
        private boolean mLoading = true;
        private int mIndexerSortMode;
        private MusicAlphabetIndexer mIndexer;
        class ViewHolder {
            TextView line1;
            TextView line2;
            TextView duration;
            RadioButton radio;
            ImageView play_indicator;
            CharArrayBuffer buffer1;
            char [] buffer2;
        }
        TrackListAdapter(Context context, ListView listView, int layout,
                String[] from, int[] to) {
            super(context, layout, null, from, to);
            mListView = listView;
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
            mUnknownAlbum = context.getString(R.string.unknown_album_name);
        }
        public void setLoading(boolean loading) {
            mLoading = loading;
        }
        @Override
        public boolean isEmpty() {
            if (mLoading) {
                return false;
            } else {
                return super.isEmpty();
            }
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder vh = new ViewHolder();
            vh.line1 = (TextView) v.findViewById(R.id.line1);
            vh.line2 = (TextView) v.findViewById(R.id.line2);
            vh.duration = (TextView) v.findViewById(R.id.duration);
            vh.radio = (RadioButton) v.findViewById(R.id.radio);
            vh.play_indicator = (ImageView) v.findViewById(R.id.play_indicator);
            vh.buffer1 = new CharArrayBuffer(100);
            vh.buffer2 = new char[200];
            v.setTag(vh);
            return v;
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder vh = (ViewHolder) view.getTag();
            cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
            vh.line1.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);
            int secs = cursor.getInt(mDurationIdx) / 1000;
            if (secs == 0) {
                vh.duration.setText("");
            } else {
                vh.duration.setText(MusicUtils.makeTimeString(context, secs));
            }
            final StringBuilder builder = mBuilder;
            builder.delete(0, builder.length());
            String name = cursor.getString(mAlbumIdx);
            if (name == null || name.equals("<unknown>")) {
                builder.append(mUnknownAlbum);
            } else {
                builder.append(name);
            }
            builder.append('\n');
            name = cursor.getString(mArtistIdx);
            if (name == null || name.equals("<unknown>")) {
                builder.append(mUnknownArtist);
            } else {
                builder.append(name);
            }
            int len = builder.length();
            if (vh.buffer2.length < len) {
                vh.buffer2 = new char[len];
            }
            builder.getChars(0, len, vh.buffer2, 0);
            vh.line2.setText(vh.buffer2, 0, len);
            final long id = cursor.getLong(mIdIdx);
            vh.radio.setChecked(id == mSelectedId);
            if (DBG) Log.v(TAG, "Binding id=" + id + " sel=" + mSelectedId
                    + " playing=" + mPlayingId + " cursor=" + cursor);
            ImageView iv = vh.play_indicator;
            if (id == mPlayingId) {
                iv.setImageResource(R.drawable.indicator_ic_mp_playing_list);
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
        }
        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            if (DBG) Log.v(TAG, "Setting cursor to: " + cursor
                    + " from: " + MusicPicker.this.mCursor);
            MusicPicker.this.mCursor = cursor;
            if (cursor != null) {
                mIdIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                mTitleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                mArtistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                mAlbumIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                mDurationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                if (mIndexerSortMode != mSortMode || mIndexer == null) {
                    mIndexerSortMode = mSortMode;
                    int idx = mTitleIdx;
                    switch (mIndexerSortMode) {
                        case ARTIST_MENU:
                            idx = mArtistIdx;
                            break;
                        case ALBUM_MENU:
                            idx = mAlbumIdx;
                            break;
                    }
                    mIndexer = new MusicAlphabetIndexer(cursor, idx,
                            getResources().getString(R.string.fast_scroll_alphabet));
                } else {
                    mIndexer.setCursor(cursor);
                }
            }
            makeListShown();
        }
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (DBG) Log.v(TAG, "Getting new cursor...");
            return doQuery(true, constraint.toString());
        }
        public int getPositionForSection(int section) {
            Cursor cursor = getCursor();
            if (cursor == null) {
                return 0;
            }
            return mIndexer.getPositionForSection(section);
        }
        public int getSectionForPosition(int position) {
            return 0;
        }
        public Object[] getSections() {
            if (mIndexer != null) {
                return mIndexer.getSections();
            }
            return null;
        }
    }
    private final class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (!isFinishing()) {
                mAdapter.setLoading(false);
                mAdapter.changeCursor(cursor);
                setProgressBarIndeterminateVisibility(false);
                if (mListState != null) {
                    getListView().onRestoreInstanceState(mListState);
                    if (mListHasFocus) {
                        getListView().requestFocus();
                    }
                    mListHasFocus = false;
                    mListState = null;
                }
            } else {
                cursor.close();
            }
        }
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        int sortMode = TRACK_MENU;
        if (icicle == null) {
            mSelectedUri = getIntent().getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        } else {
            mSelectedUri = (Uri)icicle.getParcelable(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
            mListState = icicle.getParcelable(LIST_STATE_KEY);
            mListHasFocus = icicle.getBoolean(FOCUS_KEY);
            sortMode = icicle.getInt(SORT_MODE_KEY, sortMode);
        }
        if (Intent.ACTION_GET_CONTENT.equals(getIntent().getAction())) {
            mBaseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            mBaseUri = getIntent().getData();
            if (mBaseUri == null) {
                Log.w("MusicPicker", "No data URI given to PICK action");
                finish();
                return;
            }
        }
        setContentView(R.layout.music_picker);
        mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
        final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        mAdapter = new TrackListAdapter(this, listView,
                R.layout.music_picker_item, new String[] {},
                new int[] {});
        setListAdapter(mAdapter);
        listView.setTextFilterEnabled(true);
        listView.setSaveEnabled(false);
        mQueryHandler = new QueryHandler(this);
        mProgressContainer = findViewById(R.id.progressContainer);
        mListContainer = findViewById(R.id.listContainer);
        mOkayButton = findViewById(R.id.okayButton);
        mOkayButton.setOnClickListener(this);
        mCancelButton = findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(this);
        if (mSelectedUri != null) {
            Uri.Builder builder = mSelectedUri.buildUpon();
            String path = mSelectedUri.getEncodedPath();
            int idx = path.lastIndexOf('/');
            if (idx >= 0) {
                path = path.substring(0, idx);
            }
            builder.encodedPath(path);
            Uri baseSelectedUri = builder.build();
            if (DBG) Log.v(TAG, "Selected Uri: " + mSelectedUri);
            if (DBG) Log.v(TAG, "Selected base Uri: " + baseSelectedUri);
            if (DBG) Log.v(TAG, "Base Uri: " + mBaseUri);
            if (baseSelectedUri.equals(mBaseUri)) {
                mSelectedId = ContentUris.parseId(mSelectedUri);
            }
        }
        setSortMode(sortMode);
    }
    @Override public void onRestart() {
        super.onRestart();
        doQuery(false, null);
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (setSortMode(item.getItemId())) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, TRACK_MENU, Menu.NONE, R.string.sort_by_track);
        menu.add(Menu.NONE, ALBUM_MENU, Menu.NONE, R.string.sort_by_album);
        menu.add(Menu.NONE, ARTIST_MENU, Menu.NONE, R.string.sort_by_artist);
        return true;
    }
    @Override protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        icicle.putParcelable(LIST_STATE_KEY, getListView().onSaveInstanceState());
        icicle.putBoolean(FOCUS_KEY, getListView().hasFocus());
        icicle.putInt(SORT_MODE_KEY, mSortMode);
    }
    @Override public void onPause() {
        super.onPause();
        stopMediaPlayer();
    }
    @Override public void onStop() {
        super.onStop();
        mAdapter.setLoading(true);
        mAdapter.changeCursor(null);
    }
    boolean setSortMode(int sortMode) {
        if (sortMode != mSortMode) {
            switch (sortMode) {
                case TRACK_MENU:
                    mSortMode = sortMode;
                    mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
                    doQuery(false, null);
                    return true;
                case ALBUM_MENU:
                    mSortMode = sortMode;
                    mSortOrder = MediaStore.Audio.Media.ALBUM_KEY + " ASC, "
                            + MediaStore.Audio.Media.TRACK + " ASC, "
                            + MediaStore.Audio.Media.TITLE_KEY + " ASC";
                    doQuery(false, null);
                    return true;
                case ARTIST_MENU:
                    mSortMode = sortMode;
                    mSortOrder = MediaStore.Audio.Media.ARTIST_KEY + " ASC, "
                            + MediaStore.Audio.Media.ALBUM_KEY + " ASC, "
                            + MediaStore.Audio.Media.TRACK + " ASC, "
                            + MediaStore.Audio.Media.TITLE_KEY + " ASC";
                    doQuery(false, null);
                    return true;
            }
        }
        return false;
    }
    void makeListShown() {
        if (!mListShown) {
            mListShown = true;
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    this, android.R.anim.fade_out));
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    this, android.R.anim.fade_in));
            mListContainer.setVisibility(View.VISIBLE);
        }
    }
    Cursor doQuery(boolean sync, String filterstring) {
        mQueryHandler.cancelOperation(MY_QUERY_TOKEN);
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        String [] keywords = null;
        if (filterstring != null) {
            String [] searchWords = filterstring.split(" ");
            keywords = new String[searchWords.length];
            Collator col = Collator.getInstance();
            col.setStrength(Collator.PRIMARY);
            for (int i = 0; i < searchWords.length; i++) {
                String key = MediaStore.Audio.keyFor(searchWords[i]);
                key = key.replace("\\", "\\\\");
                key = key.replace("%", "\\%");
                key = key.replace("_", "\\_");
                keywords[i] = '%' + key + '%';
            }
            for (int i = 0; i < searchWords.length; i++) {
                where.append(" AND ");
                where.append(MediaStore.Audio.Media.ARTIST_KEY + "||");
                where.append(MediaStore.Audio.Media.ALBUM_KEY + "||");
                where.append(MediaStore.Audio.Media.TITLE_KEY + " LIKE ? ESCAPE '\\'");
            }
        }
        if (sync) {
            try {
                return getContentResolver().query(mBaseUri, CURSOR_COLS,
                        where.toString(), keywords, mSortOrder);
            } catch (UnsupportedOperationException ex) {
            }
        } else {
            mAdapter.setLoading(true);
            setProgressBarIndeterminateVisibility(true);
            mQueryHandler.startQuery(MY_QUERY_TOKEN, null, mBaseUri, CURSOR_COLS,
                    where.toString(), keywords, mSortOrder);
        }
        return null;
    }
    @Override protected void onListItemClick(ListView l, View v, int position,
            long id) {
        mCursor.moveToPosition(position);
        if (DBG) Log.v(TAG, "Click on " + position + " (id=" + id
                + ", cursid="
                + mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID))
                + ") in cursor " + mCursor
                + " adapter=" + l.getAdapter());
        setSelected(mCursor);
    }
    void setSelected(Cursor c) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        long newId = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
        mSelectedUri = ContentUris.withAppendedId(uri, newId);
        mSelectedId = newId;
        if (newId != mPlayingId || mMediaPlayer == null) {
            stopMediaPlayer();
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(this, mSelectedUri);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mPlayingId = newId;
                getListView().invalidateViews();
            } catch (IOException e) {
                Log.w("MusicPicker", "Unable to play track", e);
            }
        } else if (mMediaPlayer != null) {
            stopMediaPlayer();
            getListView().invalidateViews();
        }
    }
    public void onCompletion(MediaPlayer mp) {
        if (mMediaPlayer == mp) {
            mp.stop();
            mp.release();
            mMediaPlayer = null;
            mPlayingId = -1;
            getListView().invalidateViews();
        }
    }
    void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPlayingId = -1;
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okayButton:
                if (mSelectedId >= 0) {
                    setResult(RESULT_OK, new Intent().setData(mSelectedUri));
                    finish();
                }
                break;
            case R.id.cancelButton:
                finish();
                break;
        }
    }
}
