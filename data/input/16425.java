class TargetFileListFrame extends Frame implements DropTargetListener {
    private List list = new List(URIListBetweenJVMsTest.VISIBLE_RAWS_IN_LIST);
    private int expectationTransferredFilesNumber;
    private DataFlavor dropFlavor;
    TargetFileListFrame(Point location, int expectationTransferredFilesNumber) {
        try {
            dropFlavor = new DataFlavor("text/uri-list;class=java.io.Reader");
        } catch (Exception ex) {
        }
        this.expectationTransferredFilesNumber = expectationTransferredFilesNumber;
        initGUI(location);
        setDropTarget(new DropTarget(list, DnDConstants.ACTION_COPY,
                this));
    }
    private void initGUI(Point location) {
        this.setLocation(location);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                TargetFileListFrame.this.dispose();
            }
        });
        this.add(new Panel().add(list));
        this.pack();
        this.setVisible(true);
    }
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }
    public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }
    public void dragExit(DropTargetEvent dte) {}
    public void drop(DropTargetDropEvent dtde) {
        list.removeAll();
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        java.util.List<File> fileList = extractListOfFiles(dtde);
        for (File file:fileList) {
            list.add(file.getName());
        }
        if (fileList.size() != expectationTransferredFilesNumber)
        {
            System.err.println("ERROR: Expected file number:"
                    + expectationTransferredFilesNumber
                    + "; Received file number: "
                    + fileList.size());
            TargetFileListFrame.this.dispose();
            System.exit(InterprocessMessages.WRONG_FILES_NUMBER_ON_TARGET);
        }
        TargetFileListFrame.this.dispose();
    }
    private java.util.List<File> extractListOfFiles(DropTargetDropEvent dtde) {
        BufferedReader reader = null;
        ArrayList<File> files = new ArrayList<File>();
        try {
            reader = new BufferedReader((Reader)dtde.getTransferable().
                    getTransferData(dropFlavor));
            String line;
            while ((line = reader.readLine()) != null) {
                files.add(new File(new URI(line)));
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return files;
    }
    Point getDropTargetPoint() {
       return new Point((int)list.getLocationOnScreen().getX()+(list.getWidth()/2),
                (int)list.getLocationOnScreen().getY()+(list.getHeight()/2));
    }
}
