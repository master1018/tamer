public class AboutPage extends Composite {
    private Label mLabel;
    public AboutPage(Composite parent) {
        super(parent, SWT.BORDER);
        createContents(this);
        postCreate();  
    }
    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(2, false));
        Label logo = new Label(parent, SWT.NONE);
        InputStream imageStream = this.getClass().getResourceAsStream("logo.png"); 
        if (imageStream != null) {
            Image img = new Image(parent.getShell().getDisplay(), imageStream);
            logo.setImage(img);
        }
        mLabel = new Label(parent, SWT.NONE);
        mLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        mLabel.setText(String.format(
                "Android SDK Updater.\nRevision %1$s\nCopyright (C) 2009 The Android Open Source Project.",
                getRevision()));
    }
    @Override
    protected void checkSubclass() {
    }
    private void postCreate() {
    }
    private String getRevision() {
        Properties p = new Properties();
        try{
            String toolsdir = System.getProperty(Main.TOOLSDIR);
            File sourceProp;
            if (toolsdir == null || toolsdir.length() == 0) {
                sourceProp = new File(SdkConstants.FN_SOURCE_PROP);
            } else {
                sourceProp = new File(toolsdir, SdkConstants.FN_SOURCE_PROP);
            }
            p.load(new FileInputStream(sourceProp));
            String revision = p.getProperty(Package.PROP_REVISION);
            if (revision != null) {
                return revision;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return "?";
    }
}
