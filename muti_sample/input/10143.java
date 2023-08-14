public final class SaveAsAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        GraphDocument doc = new GraphDocument();
        for (Node n : activatedNodes) {
            Group group = n.getLookup().lookup(Group.class);
            doc.addGroup(group);
        }
        save(doc);
    }
    public static void save(GraphDocument doc) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ImportAction.getFileFilter());
        fc.setCurrentDirectory(new File(Settings.get().get(Settings.DIRECTORY, Settings.DIRECTORY_DEFAULT)));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }
            File dir = file;
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            Settings.get().put(Settings.DIRECTORY, dir.getAbsolutePath());
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(file));
                Printer p = new Printer();
                p.export(writer, doc);
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    protected int mode() {
        return CookieAction.MODE_SOME;
    }
    public String getName() {
        return NbBundle.getMessage(SaveAsAction.class, "CTL_SaveAsAction");
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/coordinator/images/save.gif";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    protected boolean enable(Node[] nodes) {
        int cnt = 0;
        for (Node n : nodes) {
            cnt += n.getLookup().lookupAll(Group.class).size();
        }
        return cnt > 0;
    }
}
