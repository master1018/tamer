public class Addr2Line {
    private static final HashMap<String, Addr2Line> sProcessCache =
            new HashMap<String, Addr2Line>();
    private static final byte[] sCrLf = {
        '\n'
    };
    private String mLibrary;
    private Process mProcess;
    private BufferedReader mResultReader;
    private BufferedOutputStream mAddressWriter;
    public static Addr2Line getProcess(final String library) {
        if (library != null) {
            synchronized (sProcessCache) {
                Addr2Line process = sProcessCache.get(library);
                if (process == null) {
                    process = new Addr2Line(library);
                    boolean status = process.start();
                    if (status) {
                        sProcessCache.put(library, process);
                    } else {
                        process = null;
                    }
                }
                return process;
            }
        }
        return null;
    }
    private Addr2Line(final String library) {
        mLibrary = library;
    }
    private boolean start() {
        String symbols = System.getenv("ANDROID_SYMBOLS");
        if (symbols == null) {
            symbols = DdmUiPreferences.getSymbolDirectory();
        }
        String[] command = new String[5];
        command[0] = DdmUiPreferences.getAddr2Line();
        command[1] = "-C";
        command[2] = "-f";
        command[3] = "-e";
        command[4] = symbols + mLibrary;
        try {
            mProcess = Runtime.getRuntime().exec(command);
            if (mProcess != null) {
                InputStreamReader is = new InputStreamReader(mProcess
                        .getInputStream());
                mResultReader = new BufferedReader(is);
                mAddressWriter = new BufferedOutputStream(mProcess
                        .getOutputStream());
                if (mResultReader == null || mAddressWriter == null) {
                    mProcess.destroy();
                    mProcess = null;
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            String msg = String.format(
                    "Error while trying to start %1$s process for library %2$s",
                    DdmUiPreferences.getAddr2Line(), mLibrary);
            Log.e("ddm-Addr2Line", msg);
            if (mProcess != null) {
                mProcess.destroy();
                mProcess = null;
            }
        }
        return false;
    }
    public void stop() {
        synchronized (sProcessCache) {
            if (mProcess != null) {
                sProcessCache.remove(mLibrary);
                mProcess.destroy();
                mProcess = null;
            }
        }
    }
    public static void stopAll() {
        synchronized (sProcessCache) {
            Collection<Addr2Line> col = sProcessCache.values();
            for (Addr2Line a2l : col) {
                a2l.stop();
            }
        }
    }
    public NativeStackCallInfo getAddress(long addr) {
        synchronized (sProcessCache) {
            if (mProcess != null) {
                String tmp = Long.toString(addr, 16);
                try {
                    mAddressWriter.write(tmp.getBytes());
                    mAddressWriter.write(sCrLf);
                    mAddressWriter.flush();
                    String method = mResultReader.readLine();
                    String source = mResultReader.readLine();
                    if (method != null && source != null) {
                        return new NativeStackCallInfo(mLibrary, method, source);
                    }
                } catch (IOException e) {
                    Log.e("ddms",
                            "Error while trying to get information for addr: "
                                    + tmp + " in library: " + mLibrary);
                }
            }
        }
        return null;
    }
}
