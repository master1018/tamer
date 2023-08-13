public class MainFrame extends JFrame {
    private ActionMap actionsMap;
    private JMenuItem saveMenuItem;
    private ImageEditorPanel imageEditor;
    public MainFrame(String path) throws HeadlessException {
        super("Draw 9-patch");
        buildActions();
        buildMenuBar();
        buildContent();
        if (path == null) {
            showOpenFilePanel();
        } else {
            try {
                File file = new File(path);
                BufferedImage img = GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
                showImageEditor(img, file.getAbsolutePath());
            } catch (Exception ex) {
                showOpenFilePanel();
            }
        }
        setSize(1024, 600);
    }
    private void buildActions() {
        actionsMap = new ActionMap();
        actionsMap.put(OpenAction.ACTION_NAME, new OpenAction(this));
        actionsMap.put(SaveAction.ACTION_NAME, new SaveAction(this));
        actionsMap.put(ExitAction.ACTION_NAME, new ExitAction(this));
    }
    private void buildMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();
        openMenuItem.setAction(actionsMap.get(OpenAction.ACTION_NAME));
        fileMenu.add(openMenuItem);
        saveMenuItem.setAction(actionsMap.get(SaveAction.ACTION_NAME));
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);
        exitMenuItem.setAction(actionsMap.get(ExitAction.ACTION_NAME));
        fileMenu.add(exitMenuItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    private void buildContent() {
        setContentPane(new GradientPanel());
    }
    private void showOpenFilePanel() {
        add(new OpenFilePanel(this));
    }
    public SwingWorker<?, ?> open(File file) {
        if (file == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new PngFileFilter());
            int choice = chooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                return new OpenTask(chooser.getSelectedFile());
            } else {
                return null;
            }
        } else {
            return new OpenTask(file);
        }
    }
    void showImageEditor(BufferedImage image, String name) {
        getContentPane().removeAll();
        imageEditor = new ImageEditorPanel(this, image, name);
        add(imageEditor);
        saveMenuItem.setEnabled(true);
        validate();
        repaint();
    }
    public SwingWorker<?, ?> save() {
        if (imageEditor == null) {
            return null;
        }
        File file = imageEditor.chooseSaveFile();
        return file != null ? new SaveTask(file) : null;
    }
    private class SaveTask extends SwingWorker<Boolean, Void> {
        private final File file;
        SaveTask(File file) {
            this.file = file;
        }
        protected Boolean doInBackground() throws Exception {
            try {
                ImageIO.write(imageEditor.getImage(), "PNG", file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }
    private class OpenTask extends SwingWorker<BufferedImage, Void> {
        private final File file;
        OpenTask(File file) {
            this.file = file;
        }
        protected BufferedImage doInBackground() throws Exception {
            return GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
        }
        @Override
        protected void done() {
            try {
                showImageEditor(get(), file.getAbsolutePath());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
