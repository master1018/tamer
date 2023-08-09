public class SoftAudioPusher implements Runnable {
    private volatile boolean active = false;
    private SourceDataLine sourceDataLine = null;
    private Thread audiothread;
    private AudioInputStream ais;
    private byte[] buffer;
    public SoftAudioPusher(SourceDataLine sourceDataLine, AudioInputStream ais,
            int workbuffersizer) {
        this.ais = ais;
        this.buffer = new byte[workbuffersizer];
        this.sourceDataLine = sourceDataLine;
    }
    public synchronized void start() {
        if (active)
            return;
        active = true;
        audiothread = new Thread(this);
        audiothread.setDaemon(true);
        audiothread.setPriority(Thread.MAX_PRIORITY);
        audiothread.start();
    }
    public synchronized void stop() {
        if (!active)
            return;
        active = false;
        try {
            audiothread.join();
        } catch (InterruptedException e) {
        }
    }
    public void run() {
        byte[] buffer = SoftAudioPusher.this.buffer;
        AudioInputStream ais = SoftAudioPusher.this.ais;
        SourceDataLine sourceDataLine = SoftAudioPusher.this.sourceDataLine;
        try {
            while (active) {
                int count = ais.read(buffer);
                if(count < 0) break;
                sourceDataLine.write(buffer, 0, count);
            }
        } catch (IOException e) {
            active = false;
        }
    }
}
