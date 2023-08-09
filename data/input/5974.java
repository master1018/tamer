public final class ExportAction extends CallableSystemAction implements LookupListener {
    private final Lookup lookup;
    private final Lookup.Result<ExportCookie> result;
    public ExportAction() {
        putValue(Action.SHORT_DESCRIPTION, "Export current graph as an SVG file");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        lookup = Utilities.actionsGlobalContext();
        result = lookup.lookup(new Lookup.Template<ExportCookie>(ExportCookie.class));
        result.addLookupListener(this);
        resultChanged(null);
    }
    public void resultChanged(LookupEvent e) {
        super.setEnabled(result.allInstances().size() > 0);
    }
    public void performAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return true;
            }
            public String getDescription() {
                return "SVG files (*.svg)";
            }
        });
        fc.setCurrentDirectory(new File(Settings.get().get(Settings.DIRECTORY, Settings.DIRECTORY_DEFAULT)));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".svg");
            }
            File dir = file;
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            Settings.get().put(Settings.DIRECTORY, dir.getAbsolutePath());
            ExportCookie cookie = Utilities.actionsGlobalContext().lookup(ExportCookie.class);
            if (cookie != null) {
                cookie.export(file);
            }
        }
    }
    public String getName() {
        return NbBundle.getMessage(ExportAction.class, "CTL_ExportAction");
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/export.gif";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
