public class MultipleEnd {
    public static void main(String[] args) {
        new MultipleEnd().start();
    }
    public void start() {
        new MultipleEndFrame();
    }
}
class MultipleEndFrame extends Frame {
    public MultipleEndFrame() {
        super("MultipleEnd");
        setVisible(true);
        JobAttributes job = new JobAttributes();
        job.setDialog(JobAttributes.DialogType.NONE);
        PrintJob pj  = getToolkit().getPrintJob(this, "MultipleEnd", job, null);
        if (pj != null) {
            pj.end();
            pj.end();
        }
    }
}
