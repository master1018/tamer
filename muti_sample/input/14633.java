class DnDTarget extends Panel implements DropTargetListener {
    Color bgColor;
    Color htColor;
    DnDTarget(Color bgColor, Color htColor) {
        super();
        setLayout(new FlowLayout());
        this.bgColor = bgColor;
        this.htColor = htColor;
        setBackground(bgColor);
        setDropTarget(new DropTarget(this, this));
        add(new Label("drop here"));
    }
    boolean check(DropTargetDragEvent dtde)
    {
        if (dtde.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
            return true;
        }
        return false;
    }
    public void dragEnter(DropTargetDragEvent dtde) {
        if(check(dtde)){
            setBackground(htColor);
            repaint();
        }
    }
    public void dragOver(DropTargetDragEvent dtde) {
        check(dtde);
    }
    public void dropActionChanged(DropTargetDragEvent dtde) {
        check(dtde);
    }
    public void dragExit(DropTargetEvent e) {
        setBackground(bgColor);
        repaint();
    }
    public void dragScroll(DropTargetDragEvent e) {
        System.out.println("[Target] dragScroll");
    }
    public void drop(DropTargetDropEvent dtde) {
        System.out.println("[Target] drop");
        boolean success = false;
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        if( dtde.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor) ){
            System.out.println("[Target] DROP OK!");
            try {
                Transferable transfer = dtde.getTransferable();
                java.util.List<File> fl = (java.util.List<File>)transfer.getTransferData(DataFlavor.javaFileListFlavor);
                for(File f : fl){
                    add(new Button(f.getCanonicalPath()));
                    System.out.println("[Target] drop file:" + f.getCanonicalPath());
                }
                validate();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            setBackground(bgColor);
            repaint();
            success = true;
        }
        dtde.dropComplete(success);
    }
}
