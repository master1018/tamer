public class SaveDialogTitleTest {
    public static void main(String args[]) {
        System.out.print("Once the dialog appears, press OK and the ");
        System.out.print("Save to File dialog should appear and it ");
        System.out.println("must have a window title else the test fails.");
        Toolkit tk = Toolkit.getDefaultToolkit();
        JobAttributes jobAttributes = new JobAttributes();
        jobAttributes.setDestination(JobAttributes.DestinationType.FILE);
        PrintJob printJob =
            tk.getPrintJob(new Frame(), "Save Title Test",
                           jobAttributes, null);
        if (printJob != null) { 
          printJob.end();
        }
        System.exit(0); 
    }
}
