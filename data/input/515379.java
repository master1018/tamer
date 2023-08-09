public class RepoSources {
    private static final String KEY_COUNT = "count";
    private static final String KEY_SRC = "src";
    private static final String SRC_FILENAME = "repositories.cfg"; 
    private ArrayList<RepoSource> mSources = new ArrayList<RepoSource>();
    public RepoSources() {
    }
    public void add(RepoSource source) {
        mSources.add(source);
    }
    public void remove(RepoSource source) {
        mSources.remove(source);
    }
    public RepoSource[] getSources() {
        return mSources.toArray(new RepoSource[mSources.size()]);
    }
    public void loadUserSources(ISdkLog log) {
        for (Iterator<RepoSource> it = mSources.iterator(); it.hasNext(); ) {
            RepoSource s = it.next();
            if (s.isUserSource()) {
                it.remove();
            }
        }
        FileInputStream fis = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SRC_FILENAME);
            if (f.exists()) {
                fis = new FileInputStream(f);
                Properties props = new Properties();
                props.load(fis);
                int count = Integer.parseInt(props.getProperty(KEY_COUNT, "0"));
                for (int i = 0; i < count; i++) {
                    String url = props.getProperty(String.format("%s%02d", KEY_SRC, i));  
                    if (url != null) {
                        RepoSource s = new RepoSource(url, true );
                        if (!hasSource(s)) {
                            mSources.add(s);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error(e, null);
        } catch (AndroidLocationException e) {
            log.error(e, null);
        } catch (IOException e) {
            log.error(e, null);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public boolean hasSource(RepoSource source) {
        for (RepoSource s : mSources) {
            if (s.equals(source)) {
                return true;
            }
        }
        return false;
    }
    public void saveUserSources(ISdkLog log) {
        FileOutputStream fos = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SRC_FILENAME);
            fos = new FileOutputStream(f);
            Properties props = new Properties();
            int count = 0;
            for (RepoSource s : mSources) {
                if (s.isUserSource()) {
                    count++;
                    props.setProperty(String.format("%s%02d", KEY_SRC, count), s.getUrl());  
                }
            }
            props.setProperty(KEY_COUNT, Integer.toString(count));
            props.store( fos, "## User Sources for Android tool");  
        } catch (AndroidLocationException e) {
            log.error(e, null);
        } catch (IOException e) {
            log.error(e, null);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
