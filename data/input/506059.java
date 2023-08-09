public class VoiceDialerTester {
    private static final String TAG = "VoiceDialerTester";
    private final WavFile[] mWavFiles;
    private final File[] mWavDirs;
    private int mWavFile = -1; 
    private static class WavFile {
        final public File mFile;
        public int mRank;
        public int mTotal;
        public String mMessage;
        public WavFile(File file) {
            mFile = file;
        }
    }
    public VoiceDialerTester(File dir) {
        if (Config.LOGD) {
            Log.d(TAG, "VoiceDialerTester " + dir);
        }
        Vector<File> wavDirs = new Vector<File>();
        wavDirs.add(dir);
        Vector<File> wavFiles = new Vector<File>();
        for (int i = 0; i < wavDirs.size(); i++) {
            File d = wavDirs.get(i);
            for (File f : d.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".wav")) {
                    wavFiles.add(f);
                }
                else if (f.isDirectory()) {
                    wavDirs.add(f);
                }
            }
        }
        File[] fa = wavFiles.toArray(new File[wavFiles.size()]);
        Arrays.sort(fa);
        mWavFiles = new WavFile[fa.length];
        for (int i = 0; i < mWavFiles.length; i++) {
            mWavFiles[i] = new WavFile(fa[i]);
        }
        mWavDirs = wavDirs.toArray(new File[wavDirs.size()]);
        Arrays.sort(mWavDirs);
    }
    public File getWavFile() {
        return mWavFiles[mWavFile].mFile;
    }
    public void onRecognitionError(String msg) {
        WavFile wf = mWavFiles[mWavFile];
        wf.mRank = -1;
        wf.mTotal = -1;
        wf.mMessage = msg;
    }
    public void onRecognitionFailure(String msg) {
        WavFile wf = mWavFiles[mWavFile];
        wf.mRank = 0;
        wf.mTotal = 0;
        wf.mMessage = msg;
    }
    public void onRecognitionSuccess(Intent[] intents) {
        WavFile wf = mWavFiles[mWavFile];
        wf.mTotal = intents.length;
        String utter = wf.mFile.getName().toLowerCase().replace('_', ' ');
        utter = utter.substring(0, utter.indexOf('.')).trim();
        for (int i = 0; i < intents.length; i++) {
            String sentence =
                    intents[i].getStringExtra(RecognizerEngine.SENTENCE_EXTRA).
                    toLowerCase().trim();
            if (i == 0) {
                wf.mMessage = sentence;
                if (intents.length > 1) wf.mMessage += ", etc";
            }
            if (utter.equals(sentence)) {
                wf.mRank = i + 1;
                wf.mMessage = null;
                break;
            }
        }
    }
    public boolean stepToNextTest() {
        mWavFile++;
        return mWavFile < mWavFiles.length;
    }
    private static final String REPORT_FMT = "%6s %6s %6s %6s %6s %6s %6s %s";
    private static final String REPORT_HDR = String.format(REPORT_FMT,
            "1/1", "1/N", "M/N", "0/N", "Fail", "Error", "Total", "");
    public void report() {
        Log.d(TAG, "List of all utterances tested");
        for (WavFile wf : mWavFiles) {
            Log.d(TAG, wf.mRank + "/" + wf.mTotal + "  " + wf.mFile +
                    (wf.mMessage != null ? "  " + wf.mMessage : ""));
        }
        reportSummaryForEachFileName();
        reportSummaryForEachDir();
        Log.d(TAG, "Summary of all utterances");
        Log.d(TAG, REPORT_HDR);
        reportSummary("Total", null);
    }
    private void reportSummaryForEachFileName() {
        Set<String> set = new HashSet<String>();
        for (WavFile wf : mWavFiles) {
            set.add(wf.mFile.getName());
        }
        String[] names = set.toArray(new String[set.size()]);
        Arrays.sort(names);
        Log.d(TAG, "Summary of utternaces by filename");
        Log.d(TAG, REPORT_HDR);
        for (final String fn : names) {
            reportSummary(fn,
                    new FileFilter() {
                        public boolean accept(File file) {
                            return fn.equals(file.getName());
                        }
            });
        }
    }
    private void reportSummaryForEachDir() {
        Set<String> set = new HashSet<String>();
        for (WavFile wf : mWavFiles) {
            set.add(wf.mFile.getParent());
        }
        String[] names = set.toArray(new String[set.size()]);
        Arrays.sort(names);
        Log.d(TAG, "Summary of utterances by directory");
        Log.d(TAG, REPORT_HDR);
        for (File dir : mWavDirs) {
            final String dn = dir.getPath();
            final String dn2 = dn + "/";
            reportSummary(dn,
                    new FileFilter() {
                        public boolean accept(File file) {
                            return file.getPath().startsWith(dn2);
                        }
            });
        }
    }
    private void reportSummary(String label, FileFilter filter) {
        if (!Config.LOGD) return;
        int total = 0;
        int count11 = 0;
        int count1N = 0;
        int countMN = 0;
        int count0N = 0;
        int countFail = 0;
        int countErrors = 0;
        for (WavFile wf : mWavFiles) {
            if (filter == null || filter.accept(wf.mFile)) {
                total++;
                if (wf.mRank == 1 && wf.mTotal == 1) count11++;
                if (wf.mRank == 1 && wf.mTotal >= 1) count1N++;
                if (wf.mRank >= 1 && wf.mTotal >= 1) countMN++;
                if (wf.mRank == 0 && wf.mTotal >= 1) count0N++;
                if (wf.mRank == 0 && wf.mTotal == 0) countFail++;
                if (wf.mRank == -1 && wf.mTotal == -1) countErrors++;
            }
        }
        String line = String.format(REPORT_FMT,
                countString(count11, total),
                countString(count1N, total),
                countString(countMN, total),
                countString(count0N, total),
                countString(countFail, total),
                countString(countErrors, total),
                "" + total,
                label);
        Log.d(TAG, line);
    }
    private static String countString(int count, int total) {
        return total > 0 ? "" + (100 * count / total) + "%" : "";
    }
}