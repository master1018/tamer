public class SaveFileNameOverrideTest extends Applet implements ActionListener {
    private final static String clickDirName = "Directory for double click";
    private final static String dirPath = ".";
    private Button showBtn;
    private FileDialog fd;
    public void init() {
        this.setLayout(new GridLayout(1, 1));
        fd = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
        showBtn = new Button("Show File Dialog");
        showBtn.addActionListener(this);
        add(showBtn);
        File tmpDir = new File(dirPath + File.separator + clickDirName);
        tmpDir.mkdir();
        String[] instructions = {
                "1) Click on 'Show File Dialog' button. A file dialog will come up.",
                "2) Double-click on '" + clickDirName + "' and click OK.",
                "3) See result of the test below"
        };
        Sysout.createDialogWithInstructions(instructions);
    }
    public void start() {
        setSize(200, 200);
        show();
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showBtn) {
            fd.setFile("input");
            fd.setDirectory(dirPath);
            fd.setVisible(true);
            String output = fd.getFile();
            if ("input".equals(output)) {
                Sysout.println("TEST PASSED");
            } else {
                Sysout.println("TEST FAILED (output file - " + output + ")");
            }
        }
    }
}
