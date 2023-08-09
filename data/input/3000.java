final class XDropTargetEventProcessor {
    private static final XDropTargetEventProcessor theInstance =
        new XDropTargetEventProcessor();
    private static boolean active = false;
    private XDropTargetProtocol protocol = null;
    private XDropTargetEventProcessor() {}
    private boolean doProcessEvent(XEvent ev) {
        if (ev.get_type() == (int)XConstants.DestroyNotify &&
            protocol != null &&
            ev.get_xany().get_window() == protocol.getSourceWindow()) {
            protocol.cleanup();
            protocol = null;
            return false;
        }
        if (ev.get_type() == (int)XConstants.PropertyNotify) {
            XPropertyEvent xproperty = ev.get_xproperty();
            if (xproperty.get_atom() ==
                MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.getAtom()) {
                XDropTargetRegistry.getRegistry().updateEmbedderDropSite(xproperty.get_window());
            }
        }
        if (ev.get_type() != (int)XConstants.ClientMessage) {
            return false;
        }
        boolean processed = false;
        XClientMessageEvent xclient = ev.get_xclient();
        XDropTargetProtocol curProtocol = protocol;
        if (protocol != null) {
            if (protocol.getMessageType(xclient) !=
                XDropTargetProtocol.UNKNOWN_MESSAGE) {
                processed = protocol.processClientMessage(xclient);
            } else {
                protocol = null;
            }
        }
        if (protocol == null) {
            Iterator dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                if (dropTargetProtocol == curProtocol) {
                    continue;
                }
                if (dropTargetProtocol.getMessageType(xclient) ==
                    XDropTargetProtocol.UNKNOWN_MESSAGE) {
                    continue;
                }
                protocol = dropTargetProtocol;
                processed = protocol.processClientMessage(xclient);
                break;
            }
        }
        return processed;
    }
    static void reset() {
        theInstance.protocol = null;
    }
    static void activate() {
        active = true;
    }
    static boolean processEvent(XEvent ev) {
        return active ? theInstance.doProcessEvent(ev) : false;
    }
}
