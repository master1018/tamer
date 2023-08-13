public class TargetPanel extends Panel implements DropTargetListener{
    private java.util.List <File> content = new ArrayList<File>();
    private Frame frame;
    public TargetPanel (Frame frame)
    {
        this.frame = frame;
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(200, 200));
        setDropTarget(new DropTarget(this, this));
    }
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }
    public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }
    public void dragExit(DropTargetEvent dte) {
    }
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                content = (java.util.List)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                repaint();
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dtde.dropComplete(true);
            boolean listsAreEqual = true;
             for (int i = 0; i < content.size(); i++) {
                if(!FileListTransferable.files[i].getName().equals(content.get(i).getName())) {
                    listsAreEqual = false;
                }
            }
            if (listsAreEqual) {
                System.err.println(InterprocessMessages.EXECUTION_IS_SUCCESSFULL);
                System.exit(0);
            }
        }
        dtde.rejectDrop();
        System.err.println(InterprocessMessages.FILES_ON_TARGET_ARE_CORRUPTED);
        System.exit(1);
    }
    public void paint(Graphics g) {
        g.setColor(Color.YELLOW);
        int i = 0;
        for (Iterator <File> iterator = content.iterator(); iterator.hasNext();i++) {
            g.drawString(iterator.next().getName(), 5, g.getFontMetrics().getAscent()*i+20);
        }
    }
}
