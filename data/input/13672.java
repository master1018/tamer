class DnDSource extends Button implements Transferable,
        DragGestureListener,
        DragSourceListener {
    private DataFlavor df;
    private transient int dropAction;
    private final int dragOperation = DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK;
    DragSource dragSource = new DragSource();
    DnDSource(String label) {
        super(label);
        setBackground(Color.yellow);
        setForeground(Color.blue);
        df = new DataFlavor(DnDSource.class, "DnDSource");
        dragSource.createDefaultDragGestureRecognizer(
                this,
                dragOperation,
                this
        );
        dragSource.addDragSourceListener(this);
    }
    public void changeCursor(
            DragSourceContext dsc,
            int ra
    ) {
        java.awt.Cursor c = null;
        if ((ra & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK)
            c = DragSource.DefaultLinkDrop;
        else if ((ra & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE)
            c = MyCursor.MOVE;
        else if ((ra & DnDConstants.ACTION_COPY) == DnDConstants.ACTION_COPY)
            c = MyCursor.COPY;
        else
            c = MyCursor.NO_DROP;
        dsc.setCursor(c);
    }
    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("starting Drag");
        try {
            if (DragSource.isDragImageSupported()) {
                System.out.println("starting Imaged Drag");
                dge.startDrag(
                        null,
                        new ImageGenerator(50, 100, new Color(0xff, 0xff, 0xff, 0x00) ) {
                                @Override public void paint(Graphics gr) {
                                    gr.translate(width/2, height/2);
                                    ((Graphics2D)gr).setStroke(new BasicStroke(3));
                                    int R = width/4+5;
                                    gr.setColor(Color.BLUE);
                                    gr.fillRect(-R, -R, 2*R, 2*R);
                                    gr.setColor(Color.CYAN);
                                    gr.drawRect(-R, -R, 2*R, 2*R);
                                    gr.translate(10, 10);
                                    R -= 5;
                                    gr.setColor(Color.RED);
                                    gr.fillOval(-R, -R, 2*R, 2*R);
                                    gr.setColor(Color.MAGENTA);
                                    gr.drawOval(-R, -R, 2*R, 2*R);
                                }
                        }.getImage(),
                        new Point(15, 40),
                        this,
                        this);
            } else {
                dge.startDrag(
                        null,
                        this,
                        this);
            }
        } catch (InvalidDnDOperationException e) {
            e.printStackTrace();
        }
    }
    public void dragEnter(DragSourceDragEvent dsde) {
        System.out.println("[Source] dragEnter");
        changeCursor(
            dsde.getDragSourceContext(),
            dsde.getUserAction() & dsde.getDropAction()
        );
    }
    public void dragOver(DragSourceDragEvent dsde) {
        System.out.println("[Source] dragOver");
        changeCursor(
            dsde.getDragSourceContext(),
            dsde.getUserAction() & dsde.getDropAction()
        );
        dropAction = dsde.getUserAction() & dsde.getDropAction();
        System.out.println("dropAction = " + dropAction);
    }
    public void dragExit(DragSourceEvent dse) {
        System.out.println("[Source] dragExit");
        changeCursor(
                dse.getDragSourceContext(),
                DnDConstants.ACTION_NONE
        );
    }
    public void dragGestureChanged(DragSourceDragEvent dsde) {
        System.out.println("[Source] dragGestureChanged");
        changeCursor(
            dsde.getDragSourceContext(),
            dsde.getUserAction() & dsde.getDropAction()
        );
        dropAction = dsde.getUserAction() & dsde.getDropAction();
        System.out.println("dropAction = " + dropAction);
    }
    public void dragDropEnd(DragSourceDropEvent dsde) {
        System.out.println("[Source] dragDropEnd");
    }
    public void dropActionChanged(DragSourceDragEvent dsde) {
        System.out.println("[Source] dropActionChanged");
        dropAction = dsde.getUserAction() & dsde.getDropAction();
        System.out.println("dropAction = " + dropAction);
    }
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{df};
    }
    public boolean isDataFlavorSupported(DataFlavor sdf) {
        return df.equals(sdf);
    }
    public Object getTransferData(DataFlavor tdf) throws UnsupportedFlavorException, IOException {
        Object copy = null;
        if( !df.equals(tdf) ){
            throw new UnsupportedFlavorException(tdf);
        }
        Container parent = getParent();
        switch (dropAction) {
            case DnDConstants.ACTION_COPY:
                try {
                    copy = this.clone();
                } catch (CloneNotSupportedException e) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(this);
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try {
                        copy = ois.readObject();
                    } catch (ClassNotFoundException cnfe) {
                    }
                }
                parent.add(this);
                return copy;
            case DnDConstants.ACTION_MOVE:
                synchronized (this) {
                    if (parent != null) {
                        parent.remove(this);
                        Label label = new Label("[empty]");
                        label.setBackground(Color.cyan);
                        label.setBounds(this.getBounds());
                        parent.add(label);
                    }
                }
                return this;
            case DnDConstants.ACTION_LINK:
                return this;
            default:
                return null;
        }
    }
}
