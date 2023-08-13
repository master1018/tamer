class Persist {
    private static final int LAST_VERSION = 1;
    private static final String FILE_NAME = "calculator.data";
    private Context mContext;
    History history = new History();
    Persist(Context context) {
        this.mContext = context;
        load();
    }
    private void load() {
        try {
            InputStream is = new BufferedInputStream(mContext.openFileInput(FILE_NAME), 8192);
            DataInputStream in = new DataInputStream(is);
            int version = in.readInt();
            if (version > LAST_VERSION) {
                throw new IOException("data version " + version + "; expected " + LAST_VERSION);
            }
            history = new History(version, in);
            in.close();
        } catch (FileNotFoundException e) {
            Calculator.log("" + e);
        } catch (IOException e) {
            Calculator.log("" + e);
        }
    }
    void save() {
        try {
            OutputStream os = new BufferedOutputStream(mContext.openFileOutput(FILE_NAME, 0), 8192);
            DataOutputStream out = new DataOutputStream(os);
            out.writeInt(LAST_VERSION);
            history.write(out);
            out.close();
        } catch (IOException e) {
            Calculator.log("" + e);
        } 
    }
}
