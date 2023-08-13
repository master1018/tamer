public final class DropBoxManagerService extends IDropBoxManagerService.Stub {
    private static final String TAG = "DropBoxManagerService";
    private static final int DEFAULT_AGE_SECONDS = 3 * 86400;
    private static final int DEFAULT_MAX_FILES = 1000;
    private static final int DEFAULT_QUOTA_KB = 5 * 1024;
    private static final int DEFAULT_QUOTA_PERCENT = 10;
    private static final int DEFAULT_RESERVE_PERCENT = 10;
    private static final int QUOTA_RESCAN_MILLIS = 5000;
    private static final boolean PROFILE_DUMP = false;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final File mDropBoxDir;
    private FileList mAllFiles = null;
    private HashMap<String, FileList> mFilesByTag = null;
    private StatFs mStatFs = null;
    private int mBlockSize = 0;
    private int mCachedQuotaBlocks = 0;  
    private long mCachedQuotaUptimeMillis = 0;
    private long mLastTimestamp = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCachedQuotaUptimeMillis = 0;  
            new Thread() {
                public void run() {
                    try {
                        init();
                        trimToFit();
                    } catch (IOException e) {
                        Slog.e(TAG, "Can't init", e);
                    }
                }
            }.start();
        }
    };
    public DropBoxManagerService(final Context context, File path) {
        mDropBoxDir = path;
        mContext = context;
        mContentResolver = context.getContentResolver();
        context.registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW));
        mContentResolver.registerContentObserver(
            Settings.Secure.CONTENT_URI, true,
            new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    mReceiver.onReceive(context, (Intent) null);
                }
            });
    }
    public void stop() {
        mContext.unregisterReceiver(mReceiver);
    }
    public void add(DropBoxManager.Entry entry) {
        File temp = null;
        OutputStream output = null;
        final String tag = entry.getTag();
        try {
            int flags = entry.getFlags();
            if ((flags & DropBoxManager.IS_EMPTY) != 0) throw new IllegalArgumentException();
            init();
            if (!isTagEnabled(tag)) return;
            long max = trimToFit();
            long lastTrim = System.currentTimeMillis();
            byte[] buffer = new byte[mBlockSize];
            InputStream input = entry.getInputStream();
            int read = 0;
            while (read < buffer.length) {
                int n = input.read(buffer, read, buffer.length - read);
                if (n <= 0) break;
                read += n;
            }
            temp = new File(mDropBoxDir, "drop" + Thread.currentThread().getId() + ".tmp");
            output = new FileOutputStream(temp);
            if (read == buffer.length && ((flags & DropBoxManager.IS_GZIPPED) == 0)) {
                output = new GZIPOutputStream(output);
                flags = flags | DropBoxManager.IS_GZIPPED;
            }
            do {
                output.write(buffer, 0, read);
                long now = System.currentTimeMillis();
                if (now - lastTrim > 30 * 1000) {
                    max = trimToFit();  
                    lastTrim = now;
                }
                read = input.read(buffer);
                if (read <= 0) {
                    output.close();  
                    output = null;
                } else {
                    output.flush();  
                }
                long len = temp.length();
                if (len > max) {
                    Slog.w(TAG, "Dropping: " + tag + " (" + temp.length() + " > " + max + " bytes)");
                    temp.delete();
                    temp = null;  
                    break;
                }
            } while (read > 0);
            createEntry(temp, tag, flags);
            temp = null;
        } catch (IOException e) {
            Slog.e(TAG, "Can't write: " + tag, e);
        } finally {
            try { if (output != null) output.close(); } catch (IOException e) {}
            entry.close();
            if (temp != null) temp.delete();
        }
    }
    public boolean isTagEnabled(String tag) {
        return !"disabled".equals(Settings.Secure.getString(
                mContentResolver, Settings.Secure.DROPBOX_TAG_PREFIX + tag));
    }
    public synchronized DropBoxManager.Entry getNextEntry(String tag, long millis) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.READ_LOGS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("READ_LOGS permission required");
        }
        try {
            init();
        } catch (IOException e) {
            Slog.e(TAG, "Can't init", e);
            return null;
        }
        FileList list = tag == null ? mAllFiles : mFilesByTag.get(tag);
        if (list == null) return null;
        for (EntryFile entry : list.contents.tailSet(new EntryFile(millis + 1))) {
            if (entry.tag == null) continue;
            if ((entry.flags & DropBoxManager.IS_EMPTY) != 0) {
                return new DropBoxManager.Entry(entry.tag, entry.timestampMillis);
            }
            try {
                return new DropBoxManager.Entry(
                        entry.tag, entry.timestampMillis, entry.file, entry.flags);
            } catch (IOException e) {
                Slog.e(TAG, "Can't read: " + entry.file, e);
            }
        }
        return null;
    }
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: Can't dump DropBoxManagerService");
            return;
        }
        try {
            init();
        } catch (IOException e) {
            pw.println("Can't initialize: " + e);
            Slog.e(TAG, "Can't init", e);
            return;
        }
        if (PROFILE_DUMP) Debug.startMethodTracing("/data/trace/dropbox.dump");
        StringBuilder out = new StringBuilder();
        boolean doPrint = false, doFile = false;
        ArrayList<String> searchArgs = new ArrayList<String>();
        for (int i = 0; args != null && i < args.length; i++) {
            if (args[i].equals("-p") || args[i].equals("--print")) {
                doPrint = true;
            } else if (args[i].equals("-f") || args[i].equals("--file")) {
                doFile = true;
            } else if (args[i].startsWith("-")) {
                out.append("Unknown argument: ").append(args[i]).append("\n");
            } else {
                searchArgs.add(args[i]);
            }
        }
        out.append("Drop box contents: ").append(mAllFiles.contents.size()).append(" entries\n");
        if (!searchArgs.isEmpty()) {
            out.append("Searching for:");
            for (String a : searchArgs) out.append(" ").append(a);
            out.append("\n");
        }
        int numFound = 0, numArgs = searchArgs.size();
        Time time = new Time();
        out.append("\n");
        for (EntryFile entry : mAllFiles.contents) {
            time.set(entry.timestampMillis);
            String date = time.format("%Y-%m-%d %H:%M:%S");
            boolean match = true;
            for (int i = 0; i < numArgs && match; i++) {
                String arg = searchArgs.get(i);
                match = (date.contains(arg) || arg.equals(entry.tag));
            }
            if (!match) continue;
            numFound++;
            if (doPrint) out.append("========================================\n");
            out.append(date).append(" ").append(entry.tag == null ? "(no tag)" : entry.tag);
            if (entry.file == null) {
                out.append(" (no file)\n");
                continue;
            } else if ((entry.flags & DropBoxManager.IS_EMPTY) != 0) {
                out.append(" (contents lost)\n");
                continue;
            } else {
                out.append(" (");
                if ((entry.flags & DropBoxManager.IS_GZIPPED) != 0) out.append("compressed ");
                out.append((entry.flags & DropBoxManager.IS_TEXT) != 0 ? "text" : "data");
                out.append(", ").append(entry.file.length()).append(" bytes)\n");
            }
            if (doFile || (doPrint && (entry.flags & DropBoxManager.IS_TEXT) == 0)) {
                if (!doPrint) out.append("    ");
                out.append(entry.file.getPath()).append("\n");
            }
            if ((entry.flags & DropBoxManager.IS_TEXT) != 0 && (doPrint || !doFile)) {
                DropBoxManager.Entry dbe = null;
                try {
                    dbe = new DropBoxManager.Entry(
                             entry.tag, entry.timestampMillis, entry.file, entry.flags);
                    if (doPrint) {
                        InputStreamReader r = new InputStreamReader(dbe.getInputStream());
                        char[] buf = new char[4096];
                        boolean newline = false;
                        for (;;) {
                            int n = r.read(buf);
                            if (n <= 0) break;
                            out.append(buf, 0, n);
                            newline = (buf[n - 1] == '\n');
                            if (out.length() > 65536) {
                                pw.write(out.toString());
                                out.setLength(0);
                            }
                        }
                        if (!newline) out.append("\n");
                    } else {
                        String text = dbe.getText(70);
                        boolean truncated = (text.length() == 70);
                        out.append("    ").append(text.trim().replace('\n', '/'));
                        if (truncated) out.append(" ...");
                        out.append("\n");
                    }
                } catch (IOException e) {
                    out.append("*** ").append(e.toString()).append("\n");
                    Slog.e(TAG, "Can't read: " + entry.file, e);
                } finally {
                    if (dbe != null) dbe.close();
                }
            }
            if (doPrint) out.append("\n");
        }
        if (numFound == 0) out.append("(No entries found.)\n");
        if (args == null || args.length == 0) {
            if (!doPrint) out.append("\n");
            out.append("Usage: dumpsys dropbox [--print|--file] [YYYY-mm-dd] [HH:MM:SS] [tag]\n");
        }
        pw.write(out.toString());
        if (PROFILE_DUMP) Debug.stopMethodTracing();
    }
    private static final class FileList implements Comparable<FileList> {
        public int blocks = 0;
        public final TreeSet<EntryFile> contents = new TreeSet<EntryFile>();
        public final int compareTo(FileList o) {
            if (blocks != o.blocks) return o.blocks - blocks;
            if (this == o) return 0;
            if (hashCode() < o.hashCode()) return -1;
            if (hashCode() > o.hashCode()) return 1;
            return 0;
        }
    }
    private static final class EntryFile implements Comparable<EntryFile> {
        public final String tag;
        public final long timestampMillis;
        public final int flags;
        public final File file;
        public final int blocks;
        public final int compareTo(EntryFile o) {
            if (timestampMillis < o.timestampMillis) return -1;
            if (timestampMillis > o.timestampMillis) return 1;
            if (file != null && o.file != null) return file.compareTo(o.file);
            if (o.file != null) return -1;
            if (file != null) return 1;
            if (this == o) return 0;
            if (hashCode() < o.hashCode()) return -1;
            if (hashCode() > o.hashCode()) return 1;
            return 0;
        }
        public EntryFile(File temp, File dir, String tag,long timestampMillis,
                         int flags, int blockSize) throws IOException {
            if ((flags & DropBoxManager.IS_EMPTY) != 0) throw new IllegalArgumentException();
            this.tag = tag;
            this.timestampMillis = timestampMillis;
            this.flags = flags;
            this.file = new File(dir, Uri.encode(tag) + "@" + timestampMillis +
                    ((flags & DropBoxManager.IS_TEXT) != 0 ? ".txt" : ".dat") +
                    ((flags & DropBoxManager.IS_GZIPPED) != 0 ? ".gz" : ""));
            if (!temp.renameTo(this.file)) {
                throw new IOException("Can't rename " + temp + " to " + this.file);
            }
            this.blocks = (int) ((this.file.length() + blockSize - 1) / blockSize);
        }
        public EntryFile(File dir, String tag, long timestampMillis) throws IOException {
            this.tag = tag;
            this.timestampMillis = timestampMillis;
            this.flags = DropBoxManager.IS_EMPTY;
            this.file = new File(dir, Uri.encode(tag) + "@" + timestampMillis + ".lost");
            this.blocks = 0;
            new FileOutputStream(this.file).close();
        }
        public EntryFile(File file, int blockSize) {
            this.file = file;
            this.blocks = (int) ((this.file.length() + blockSize - 1) / blockSize);
            String name = file.getName();
            int at = name.lastIndexOf('@');
            if (at < 0) {
                this.tag = null;
                this.timestampMillis = 0;
                this.flags = DropBoxManager.IS_EMPTY;
                return;
            }
            int flags = 0;
            this.tag = Uri.decode(name.substring(0, at));
            if (name.endsWith(".gz")) {
                flags |= DropBoxManager.IS_GZIPPED;
                name = name.substring(0, name.length() - 3);
            }
            if (name.endsWith(".lost")) {
                flags |= DropBoxManager.IS_EMPTY;
                name = name.substring(at + 1, name.length() - 5);
            } else if (name.endsWith(".txt")) {
                flags |= DropBoxManager.IS_TEXT;
                name = name.substring(at + 1, name.length() - 4);
            } else if (name.endsWith(".dat")) {
                name = name.substring(at + 1, name.length() - 4);
            } else {
                this.flags = DropBoxManager.IS_EMPTY;
                this.timestampMillis = 0;
                return;
            }
            this.flags = flags;
            long millis;
            try { millis = Long.valueOf(name); } catch (NumberFormatException e) { millis = 0; }
            this.timestampMillis = millis;
        }
        public EntryFile(long millis) {
            this.tag = null;
            this.timestampMillis = millis;
            this.flags = DropBoxManager.IS_EMPTY;
            this.file = null;
            this.blocks = 0;
        }
    }
    private synchronized void init() throws IOException {
        if (mStatFs == null) {
            if (!mDropBoxDir.isDirectory() && !mDropBoxDir.mkdirs()) {
                throw new IOException("Can't mkdir: " + mDropBoxDir);
            }
            try {
                mStatFs = new StatFs(mDropBoxDir.getPath());
                mBlockSize = mStatFs.getBlockSize();
            } catch (IllegalArgumentException e) {  
                throw new IOException("Can't statfs: " + mDropBoxDir);
            }
        }
        if (mAllFiles == null) {
            File[] files = mDropBoxDir.listFiles();
            if (files == null) throw new IOException("Can't list files: " + mDropBoxDir);
            mAllFiles = new FileList();
            mFilesByTag = new HashMap<String, FileList>();
            for (File file : files) {
                if (file.getName().endsWith(".tmp")) {
                    Slog.i(TAG, "Cleaning temp file: " + file);
                    file.delete();
                    continue;
                }
                EntryFile entry = new EntryFile(file, mBlockSize);
                if (entry.tag == null) {
                    Slog.w(TAG, "Unrecognized file: " + file);
                    continue;
                } else if (entry.timestampMillis == 0) {
                    Slog.w(TAG, "Invalid filename: " + file);
                    file.delete();
                    continue;
                }
                enrollEntry(entry);
            }
        }
    }
    private synchronized void enrollEntry(EntryFile entry) {
        mAllFiles.contents.add(entry);
        mAllFiles.blocks += entry.blocks;
        if (entry.tag != null && entry.file != null && entry.blocks > 0) {
            FileList tagFiles = mFilesByTag.get(entry.tag);
            if (tagFiles == null) {
                tagFiles = new FileList();
                mFilesByTag.put(entry.tag, tagFiles);
            }
            tagFiles.contents.add(entry);
            tagFiles.blocks += entry.blocks;
        }
    }
    private synchronized void createEntry(File temp, String tag, int flags) throws IOException {
        long t = System.currentTimeMillis();
        SortedSet<EntryFile> tail = mAllFiles.contents.tailSet(new EntryFile(t + 10000));
        EntryFile[] future = null;
        if (!tail.isEmpty()) {
            future = tail.toArray(new EntryFile[tail.size()]);
            tail.clear();  
        }
        if (!mAllFiles.contents.isEmpty()) {
            t = Math.max(t, mAllFiles.contents.last().timestampMillis + 1);
        }
        if (future != null) {
            for (EntryFile late : future) {
                mAllFiles.blocks -= late.blocks;
                FileList tagFiles = mFilesByTag.get(late.tag);
                if (tagFiles != null && tagFiles.contents.remove(late)) {
                    tagFiles.blocks -= late.blocks;
                }
                if ((late.flags & DropBoxManager.IS_EMPTY) == 0) {
                    enrollEntry(new EntryFile(
                            late.file, mDropBoxDir, late.tag, t++, late.flags, mBlockSize));
                } else {
                    enrollEntry(new EntryFile(mDropBoxDir, late.tag, t++));
                }
            }
        }
        if (temp == null) {
            enrollEntry(new EntryFile(mDropBoxDir, tag, t));
        } else {
            enrollEntry(new EntryFile(temp, mDropBoxDir, tag, t, flags, mBlockSize));
        }
    }
    private synchronized long trimToFit() {
        int ageSeconds = Settings.Secure.getInt(mContentResolver,
                Settings.Secure.DROPBOX_AGE_SECONDS, DEFAULT_AGE_SECONDS);
        int maxFiles = Settings.Secure.getInt(mContentResolver,
                Settings.Secure.DROPBOX_MAX_FILES, DEFAULT_MAX_FILES);
        long cutoffMillis = System.currentTimeMillis() - ageSeconds * 1000;
        while (!mAllFiles.contents.isEmpty()) {
            EntryFile entry = mAllFiles.contents.first();
            if (entry.timestampMillis > cutoffMillis && mAllFiles.contents.size() < maxFiles) break;
            FileList tag = mFilesByTag.get(entry.tag);
            if (tag != null && tag.contents.remove(entry)) tag.blocks -= entry.blocks;
            if (mAllFiles.contents.remove(entry)) mAllFiles.blocks -= entry.blocks;
            if (entry.file != null) entry.file.delete();
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis > mCachedQuotaUptimeMillis + QUOTA_RESCAN_MILLIS) {
            int quotaPercent = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.DROPBOX_QUOTA_PERCENT, DEFAULT_QUOTA_PERCENT);
            int reservePercent = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.DROPBOX_RESERVE_PERCENT, DEFAULT_RESERVE_PERCENT);
            int quotaKb = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.DROPBOX_QUOTA_KB, DEFAULT_QUOTA_KB);
            mStatFs.restat(mDropBoxDir.getPath());
            int available = mStatFs.getAvailableBlocks();
            int nonreserved = available - mStatFs.getBlockCount() * reservePercent / 100;
            int maximum = quotaKb * 1024 / mBlockSize;
            mCachedQuotaBlocks = Math.min(maximum, Math.max(0, nonreserved * quotaPercent / 100));
            mCachedQuotaUptimeMillis = uptimeMillis;
        }
        if (mAllFiles.blocks > mCachedQuotaBlocks) {
            Slog.i(TAG, "Usage (" + mAllFiles.blocks + ") > Quota (" + mCachedQuotaBlocks + ")");
            int unsqueezed = mAllFiles.blocks, squeezed = 0;
            TreeSet<FileList> tags = new TreeSet<FileList>(mFilesByTag.values());
            for (FileList tag : tags) {
                if (squeezed > 0 && tag.blocks <= (mCachedQuotaBlocks - unsqueezed) / squeezed) {
                    break;
                }
                unsqueezed -= tag.blocks;
                squeezed++;
            }
            int tagQuota = (mCachedQuotaBlocks - unsqueezed) / squeezed;
            for (FileList tag : tags) {
                if (mAllFiles.blocks < mCachedQuotaBlocks) break;
                while (tag.blocks > tagQuota && !tag.contents.isEmpty()) {
                    EntryFile entry = tag.contents.first();
                    if (tag.contents.remove(entry)) tag.blocks -= entry.blocks;
                    if (mAllFiles.contents.remove(entry)) mAllFiles.blocks -= entry.blocks;
                    try {
                        if (entry.file != null) entry.file.delete();
                        enrollEntry(new EntryFile(mDropBoxDir, entry.tag, entry.timestampMillis));
                    } catch (IOException e) {
                        Slog.e(TAG, "Can't write tombstone file", e);
                    }
                }
            }
        }
        return mCachedQuotaBlocks * mBlockSize;
    }
}
