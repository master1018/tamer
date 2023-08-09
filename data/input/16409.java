public class SecurityDialogTest extends Frame implements ActionListener {
    Button nativeDlg, setSecurity;
        boolean isNative = true;
    public SecurityDialogTest() {
        nativeDlg = new Button("Print Dialog");
        nativeDlg.addActionListener(this);
        setSecurity = new Button("Toggle Dialog");
        setSecurity.addActionListener(this);
        add("South", nativeDlg);
        add("North", setSecurity);
        setSize(300, 300);
        setVisible(true);
    }
    public static void main(String args[]) {
        System.out.println("Native dialog is the default");
        SecurityDialogTest test = new SecurityDialogTest();
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setSecurity) {
            if (isNative) {
                isNative = false;
                System.out.println("Common dialog is the default");
            } else {
                isNative = true;
                System.out.println("Native dialog is the default");
            }
            return;
        }
        JobAttributes  ja = new JobAttributes();
        PageAttributes pa = new PageAttributes();
        if (isNative) {
            ja.setDialog(JobAttributes.DialogType.NATIVE);
        } else {
            ja.setDialog(JobAttributes.DialogType.COMMON);
        }
        ja.setDestination(JobAttributes.DestinationType.FILE);
        ja.setFileName("mohan.ps");
        PrintJob pjob = getToolkit().getPrintJob(this, null, ja, pa);
        if (pjob != null) {
            Graphics pg = pjob.getGraphics();
            System.out.println("PJOB: " + pjob);
            if (pg != null) {
                System.out.println("Printer Graphics: " + pg);
                this.printAll(pg);
                pg.dispose();
            } else {
                System.out.println("Printer Graphics is null");
            }
            pjob.end();
            System.out.println("DONE");
        } else {
            System.out.println("PJOB is null");
        }
    }
}
