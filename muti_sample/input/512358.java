 class CanvasDropListener implements DropTargetListener {
    private final LayoutCanvas mCanvas;
    private CanvasViewInfo mCurrentView;
    private String mCurrentDragFqcn;
    private NodeProxy mTargetNode;
    private DropFeedback mFeedback;
    private NodeProxy mLeaveTargetNode;
    private DropFeedback mLeaveFeedback;
    public CanvasDropListener(LayoutCanvas canvas) {
        mCanvas = canvas;
    }
    public void dragEnter(DropTargetEvent event) {
        AdtPlugin.printErrorToConsole("DEBUG", "drag enter", event);
        checkDataType(event);
        if (event.detail == DND.DROP_DEFAULT) {
            if ((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            }
        }
        if (event.detail == DND.DROP_COPY) {
            mCurrentDragFqcn = GlobalCanvasDragInfo.getInstance().getCurrentFqcn();
            ElementDescTransfer edt = ElementDescTransfer.getInstance();
            if (edt.isSupportedType(event.currentDataType)) {
                String data = (String) edt.nativeToJava(event.currentDataType);
                if (data != null) {
                    if (mCurrentDragFqcn == null) {
                        mCurrentDragFqcn = data;
                    } else if (!mCurrentDragFqcn.equals(data)) {
                        AdtPlugin.logAndPrintError(null , "CanvasDrop",
                                "TransferType mismatch: Global=%s, drag.event=%s",
                                mCurrentDragFqcn, data);
                    }
                }
            }
            processDropEvent(event);
        } else {
            event.detail = DND.DROP_NONE;
            clearDropInfo();
        }
    }
    public void dragOperationChanged(DropTargetEvent event) {
        AdtPlugin.printErrorToConsole("DEBUG", "drag changed", event);
        checkDataType(event);
        if (event.detail == DND.DROP_DEFAULT) {
            if ((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            }
        }
        if (event.detail == DND.DROP_COPY) {
            processDropEvent(event);
        } else {
            event.detail = DND.DROP_NONE;
            clearDropInfo();
        }
    }
    public void dragLeave(DropTargetEvent event) {
        AdtPlugin.printErrorToConsole("DEBUG", "drag leave");
        mLeaveTargetNode = mTargetNode;
        mLeaveFeedback = mFeedback;
        clearDropInfo();
    }
    public void dragOver(DropTargetEvent event) {
        processDropEvent(event);
    }
    public void dropAccept(DropTargetEvent event) {
        AdtPlugin.printErrorToConsole("DEBUG", "drop accept");
        checkDataType(event);
        if (event.detail != DND.DROP_NONE) {
            processDropEvent(event);
        }
    }
    public void drop(DropTargetEvent event) {
        AdtPlugin.printErrorToConsole("DEBUG", "dropped");
        if (mTargetNode == null) {
            AdtPlugin.printErrorToConsole("DEBUG", "dropped on null targetNode");
            return;
        }
        if (mTargetNode == mLeaveTargetNode) {
            mFeedback = mLeaveFeedback;
        }
        mLeaveTargetNode = null;
        mLeaveFeedback = null;
        String viewFqcn = null;
        ElementDescTransfer edt = ElementDescTransfer.getInstance();
        if (edt.isSupportedType(event.currentDataType)) {
            if (event.data instanceof String) {
                viewFqcn = (String) event.data;
            }
        }
        if (viewFqcn == null) {
            AdtPlugin.printErrorToConsole("DEBUG", "drop missing drop data");
            return;
        }
        Point p = mCanvas.displayToCanvasPoint(event.x, event.y);
        mCanvas.getRulesEngine().callOnDropped(mTargetNode, viewFqcn, mFeedback, p);
        clearDropInfo();
    }
    public void paintFeedback(GCWrapper gCWrapper) {
        if (mTargetNode != null && mFeedback != null && mFeedback.requestPaint) {
            mFeedback.requestPaint = false;
            mCanvas.getRulesEngine().callDropFeedbackPaint(gCWrapper, mTargetNode, mFeedback);
        }
    }
    private boolean checkDataType(DropTargetEvent event) {
        ElementDescTransfer edt = ElementDescTransfer.getInstance();
        TransferData current = event.currentDataType;
        if (edt.isSupportedType(current)) {
            return true;
        }
        for (TransferData td : event.dataTypes) {
            if (td != current && edt.isSupportedType(td)) {
                event.currentDataType = td;
                return true;
            }
        }
        event.detail = DND.DROP_NONE;
        return false;
    }
    private void processDropEvent(DropTargetEvent event) {
        if (!mCanvas.isResultValid()) {
            event.detail = DND.DROP_NONE;
            clearDropInfo();
            return;
        }
        Point p = mCanvas.displayToCanvasPoint(event.x, event.y);
        int x = p.x;
        int y = p.y;
        boolean isCaptured = false;
        if (mFeedback != null) {
            Rect r = mFeedback.captureArea;
            isCaptured = r != null && r.contains(x, y);
        }
        CanvasViewInfo vi;
        if (isCaptured) {
            vi = mCurrentView;
        } else {
            vi = mCanvas.findViewInfoAt(x, y);
        }
        boolean isMove = true;
        boolean needRedraw = false;
        if (vi != mCurrentView) {
            if (vi == null) {
                callDropLeave();
                isMove = false;
                needRedraw = true;
            } else {
                DropFeedback df = null;
                NodeProxy targetNode = null;
                for (CanvasViewInfo targetVi = vi;
                        targetVi != null && df == null;
                        targetVi = targetVi.getParent()) {
                    targetNode = mCanvas.getNodeFactory().create(targetVi);
                    df = mCanvas.getRulesEngine().callOnDropEnter(targetNode, mCurrentDragFqcn);
                }
                if (df != null && targetNode != mTargetNode) {
                    callDropLeave();
                    mTargetNode = targetNode;
                    mFeedback = df;
                    isMove = false;
                }
            }
            mCurrentView = vi;
        }
        if (isMove && mTargetNode != null && mFeedback != null) {
            com.android.ide.eclipse.adt.editors.layout.gscripts.Point p2 =
                new com.android.ide.eclipse.adt.editors.layout.gscripts.Point(x, y);
            DropFeedback df = mCanvas.getRulesEngine().callOnDropMove(
                    mTargetNode, mCurrentDragFqcn, mFeedback, p2);
            if (df == null) {
                callDropLeave();
            }
        }
        if (needRedraw || (mFeedback != null && mFeedback.requestPaint)) {
            mCanvas.redraw();
        }
    }
    private void callDropLeave() {
        if (mTargetNode != null && mFeedback != null) {
            mCanvas.getRulesEngine().callOnDropLeave(mTargetNode, mCurrentDragFqcn, mFeedback);
        }
        mTargetNode = null;
        mFeedback = null;
    }
    private void clearDropInfo() {
        callDropLeave();
        mCurrentView = null;
        mCanvas.redraw();
    }
}
