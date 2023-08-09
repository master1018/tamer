final class XDragAndDropProtocols {
    private final static List dragProtocols;
    private final static List dropProtocols;
    public static final String XDnD = "XDnD";
    public static final String MotifDnD = "MotifDnD";
    static {
        XDragSourceProtocolListener dragSourceProtocolListener =
            XDragSourceContextPeer.getXDragSourceProtocolListener();
        XDropTargetProtocolListener dropTargetProtocolListener =
            XDropTargetContextPeer.getXDropTargetProtocolListener();
        List tDragSourceProtocols = new ArrayList();
        XDragSourceProtocol xdndDragSourceProtocol =
            XDnDDragSourceProtocol.createInstance(dragSourceProtocolListener);
        tDragSourceProtocols.add(xdndDragSourceProtocol);
        XDragSourceProtocol motifdndDragSourceProtocol =
            MotifDnDDragSourceProtocol.createInstance(dragSourceProtocolListener);
        tDragSourceProtocols.add(motifdndDragSourceProtocol);
        List tDropTargetProtocols = new ArrayList();
        XDropTargetProtocol xdndDropTargetProtocol =
            XDnDDropTargetProtocol.createInstance(dropTargetProtocolListener);
        tDropTargetProtocols.add(xdndDropTargetProtocol);
        XDropTargetProtocol motifdndDropTargetProtocol =
            MotifDnDDropTargetProtocol.createInstance(dropTargetProtocolListener);
        tDropTargetProtocols.add(motifdndDropTargetProtocol);
        dragProtocols = Collections.unmodifiableList(tDragSourceProtocols);
        dropProtocols = Collections.unmodifiableList(tDropTargetProtocols);
    }
    static Iterator getDragSourceProtocols() {
        return dragProtocols.iterator();
    }
    static Iterator getDropTargetProtocols() {
        return dropProtocols.iterator();
    }
    public static XDragSourceProtocol getDragSourceProtocol(String name) {
        if (name == null) {
            return null;
        }
        Iterator dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
        while (dragProtocols.hasNext()) {
            XDragSourceProtocol dragProtocol =
                (XDragSourceProtocol)dragProtocols.next();
            if (dragProtocol.getProtocolName().equals(name)) {
                return dragProtocol;
            }
        }
        return null;
    }
    public static XDropTargetProtocol getDropTargetProtocol(String name) {
        if (name == null) {
            return null;
        }
        Iterator dropProtocols = XDragAndDropProtocols.getDropTargetProtocols();
        while (dropProtocols.hasNext()) {
            XDropTargetProtocol dropProtocol =
                (XDropTargetProtocol)dropProtocols.next();
            if (dropProtocol.getProtocolName().equals(name)) {
                return dropProtocol;
            }
        }
        return null;
    }
}
