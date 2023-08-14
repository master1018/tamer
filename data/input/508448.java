public class InstallerActivity extends Activity {
    private static final int DATA_ROOT_DIRECTORY_REQUEST_CODE = 42;
    private String rootDirectory = "";
    private InstallerActivity self;
    private static boolean sInstallationSuccess = false;
    private static boolean sIsInstalling = false;
    private final static Object sInstallerStateLock = new Object();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        Intent getRootDirectoryIntent = new Intent();
        getRootDirectoryIntent.setClassName("com.svox.pico", "com.svox.pico.CheckVoiceData");
        startActivityForResult(getRootDirectoryIntent, DATA_ROOT_DIRECTORY_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DATA_ROOT_DIRECTORY_REQUEST_CODE) {
            rootDirectory = data.getStringExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY);
            synchronized (sInstallerStateLock) {
                if (!sIsInstalling && !sInstallationSuccess) {
                    sIsInstalling = true;
                    runInstaller();
                }
            }
        }
    }
    private void runInstaller(){
        try {
            Resources res = getResources();
            AssetFileDescriptor langPackFd = res
                    .openRawResourceFd(R.raw.svoxlangpack);
            InputStream stream = langPackFd.createInputStream();
            (new Thread(new unzipper(stream))).start();
        } catch (IOException e) {
            Log.e("PicoLangInstaller", "Unable to open langpack resource.");
            e.printStackTrace();
        }
        setContentView(R.layout.installing);
    }
    private boolean unzipLangPack(InputStream stream) {
        FileOutputStream out;
        byte buf[] = new byte[16384];
        try {
            ZipInputStream zis = new ZipInputStream(stream);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    File newDir = new File(rootDirectory + entry.getName());
                    newDir.mkdir();
                } else {
                    String name = entry.getName();
                    File outputFile = new File(rootDirectory + name);
                    String outputPath = outputFile.getCanonicalPath();
                    name = outputPath
                            .substring(outputPath.lastIndexOf("/") + 1);
                    outputPath = outputPath.substring(0, outputPath
                            .lastIndexOf("/"));
                    File outputDir = new File(outputPath);
                    outputDir.mkdirs();
                    outputFile = new File(outputPath, name);
                    outputFile.createNewFile();
                    out = new FileOutputStream(outputFile);
                    int numread = 0;
                    do {
                        numread = zis.read(buf);
                        if (numread <= 0) {
                            break;
                        } else {
                            out.write(buf, 0, numread);
                        }
                    } while (true);
                    out.close();
                }
                entry = zis.getNextEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private class unzipper implements Runnable {
        public InputStream stream;
        public unzipper(InputStream is) {
            stream = is;
        }
        public void run() {
            boolean result = unzipLangPack(stream);
            synchronized (sInstallerStateLock) {
                sInstallationSuccess = result;
                sIsInstalling = false;
            }
            if (sInstallationSuccess) {
                Intent installCompleteIntent =
                        new Intent(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED);
                installCompleteIntent.putExtra(TextToSpeech.Engine.EXTRA_TTS_DATA_INSTALLED,
                        TextToSpeech.SUCCESS);
                self.sendBroadcast(installCompleteIntent);
            } else {
                if (self.isFinishing()) {
                    Intent installCompleteIntent =
                        new Intent(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED);
                    installCompleteIntent.putExtra(TextToSpeech.Engine.EXTRA_TTS_DATA_INSTALLED,
                            TextToSpeech.ERROR);
                    self.sendBroadcast(installCompleteIntent);
                } else {
                    runOnUiThread(new retryDisplayer());
                }
            }
        }
    }
    public class retryDisplayer implements Runnable {
        public void run() {
            setContentView(R.layout.retry);
            Button retryButton = (Button) findViewById(R.id.retryButton);
            retryButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    synchronized (sInstallerStateLock) {
                        if (!sIsInstalling) {
                            sIsInstalling = true;
                            runInstaller();
                        }
                    }
                }
            });
        }
    }
}
