class SourceFileListFrame extends Frame implements DragGestureListener {
    private final static int SOURCE_POINT_SHIFT = 3;
    private List list = new List(URIListBetweenJVMsTest.VISIBLE_RAWS_IN_LIST);
    private File[] files;
    SourceFileListFrame() {
        super("Source File List Frame");
        extractFilesFromTheWorkingDirectory();
        initList();
        initGUI();
        new DragSource().createDefaultDragGestureRecognizer(list,
                DnDConstants.ACTION_COPY,this);
    }
    private void extractFilesFromTheWorkingDirectory() {
        files = new File(System.getProperty("java.home", "")).listFiles();
    }
    private void initList() {
        for (File currFile:files) {
            list.add(currFile.getName());
        }
    }
    private void initGUI() {
        this.addWindowListener(Util.getClosingWindowAdapter());
        this.setLocation(300,250);
        this.add(new Panel().add(list));
        this.pack();
        this.setVisible(true);
    }
    int getNextLocationX() {
        return getX()+getWidth();
    }
    int getNextLocationY() {
        return getY();
    }
    int getDragSourcePointX() {
        return (int)list.getLocationOnScreen().getX()+(list.getWidth()/2);
    }
   int getDragSourcePointY() {
        return (int)list.getLocationOnScreen().getY()+ SOURCE_POINT_SHIFT;
    }
    int getSourceFilesNumber() {
        return files.length;
    }
    public void dragGestureRecognized(DragGestureEvent dge) {
        String [] filesAsStringArray = list.getItems();
        File [] files = new File[filesAsStringArray.length];
        for (int fileNumber=0; fileNumber<filesAsStringArray.length ; fileNumber++ ) {
            files[fileNumber]=new File(filesAsStringArray[fileNumber]);
        }
        dge.startDrag(null, new FileListTransferable(Arrays.asList(files)));
    }
}
