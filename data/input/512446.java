class DropFeedback {
    private static final int TOP    = 0;
    private static final int LEFT   = 1;
    private static final int BOTTOM = 2;
    private static final int RIGHT  = 3;
    private static final int MAX_DIR = RIGHT;
    private static final int sOppositeDirection[] = { BOTTOM, RIGHT, TOP, LEFT };
    private static final UiElementEditPart sTempClosests[] = new UiElementEditPart[4];
    private static final int sTempMinDists[] = new int[4];
    private static class RelativeInfo {
        final UiElementEditPart targetParts[] = new UiElementEditPart[2];
        int direction;
        int anchorIndex;
    }
    private static final RelativeInfo sRelativeInfo = new RelativeInfo();
    private static final UiElementEditPart sTempTwoParts[] = new UiElementEditPart[2];
    private DropFeedback() {
    }
    static void addElementToXml(UiElementEditPart parentPart,
            ElementDescriptor descriptor, Point where,
            UiEditorActions actions) {
        String layoutXmlName = getXmlLocalName(parentPart);
        RelativeInfo info = null;
        UiElementEditPart sibling = null;
        if (LayoutConstants.LINEAR_LAYOUT.equals(layoutXmlName)) {
            sibling = findLinearTarget(parentPart, where)[1];
        } else if (LayoutConstants.RELATIVE_LAYOUT.equals(layoutXmlName)) {
            info = findRelativeTarget(parentPart, where, sRelativeInfo);
            if (info != null) {
                sibling = info.targetParts[info.anchorIndex];
                sibling = getNextUiSibling(sibling);
            }
        }
        if (actions != null) {
            UiElementNode uiSibling = sibling != null ? sibling.getUiNode() : null;
            UiElementNode uiParent = parentPart.getUiNode();
            UiElementNode uiNode = actions.addElement(uiParent, uiSibling, descriptor,
                    false );
            if (LayoutConstants.ABSOLUTE_LAYOUT.equals(layoutXmlName)) {
                adjustAbsoluteAttributes(uiNode, where);
            } else if (LayoutConstants.RELATIVE_LAYOUT.equals(layoutXmlName)) {
                adustRelativeAttributes(uiNode, info);
            }
        }
    }
    static HighlightInfo computeDropFeedback(UiLayoutEditPart parentPart,
            HighlightInfo highlightInfo,
            Point where) {
        String layoutType = getXmlLocalName(parentPart);
        if (LayoutConstants.ABSOLUTE_LAYOUT.equals(layoutType)) {
            highlightInfo.anchorPoint = where;
        } else if (LayoutConstants.LINEAR_LAYOUT.equals(layoutType)) {
            boolean isVertical = isVertical(parentPart);
            highlightInfo.childParts = findLinearTarget(parentPart, where);
            computeLinearLine(parentPart, isVertical, highlightInfo);
        } else if (LayoutConstants.RELATIVE_LAYOUT.equals(layoutType)) {
            RelativeInfo info = findRelativeTarget(parentPart, where, sRelativeInfo);
            if (info != null) {
                highlightInfo.childParts = sRelativeInfo.targetParts;
                computeRelativeLine(parentPart, info, highlightInfo);
            }
        }
        return highlightInfo;
    }
    private static UiElementEditPart getNextUiSibling(UiElementEditPart part) {
        if (part != null) {
            UiElementNode uiNode = part.getUiNode();
            if (uiNode != null) {
                uiNode = uiNode.getUiNextSibling();
            }
            if (uiNode != null) {
                for (Object childPart : part.getParent().getChildren()) {
                    if (childPart instanceof UiElementEditPart &&
                            ((UiElementEditPart) childPart).getUiNode() == uiNode) {
                        return (UiElementEditPart) childPart;
                    }
                }
            }
        }
        return null;
    }
    private static String getXmlLocalName(UiElementEditPart editPart) {
        UiElementNode uiNode = editPart.getUiNode();
        if (uiNode != null) {
            ElementDescriptor desc = uiNode.getDescriptor();
            if (desc != null) {
                return desc.getXmlLocalName();
            }
        }
        return null;
    }
    private static void adjustAbsoluteAttributes(final UiElementNode uiNode, final Point where) {
        if (where == null) {
            return;
        }
        uiNode.getEditor().editXmlModel(new Runnable() {
            public void run() {
                uiNode.setAttributeValue(LayoutConstants.ATTR_LAYOUT_X,
                        String.format(LayoutConstants.VALUE_N_DIP, where.x),
                        false );
                uiNode.setAttributeValue(LayoutConstants.ATTR_LAYOUT_Y,
                        String.format(LayoutConstants.VALUE_N_DIP, where.y),
                        false );
                uiNode.commitDirtyAttributesToXml();
            }
        });
    }
    private static void adustRelativeAttributes(final UiElementNode uiNode, RelativeInfo info) {
        if (uiNode == null || info == null) {
            return;
        }
        final UiElementEditPart anchorPart = info.targetParts[info.anchorIndex];  
        final int direction = info.direction;
        uiNode.getEditor().editXmlModel(new Runnable() {
            public void run() {
                HashMap<String, String> map = new HashMap<String, String>();
                UiElementNode anchorUiNode = anchorPart != null ? anchorPart.getUiNode() : null;
                String anchorId = anchorUiNode != null
                                    ? anchorUiNode.getAttributeValue("id")          
                                    : null;
                if (anchorId == null) {
                    anchorId = DescriptorsUtils.getFreeWidgetId(anchorUiNode);
                    anchorUiNode.setAttributeValue("id", anchorId, true ); 
                }
                if (anchorId != null) {
                    switch(direction) {
                    case TOP:
                        map.put(LayoutConstants.ATTR_LAYOUT_ABOVE, anchorId);
                        break;
                    case BOTTOM:
                        map.put(LayoutConstants.ATTR_LAYOUT_BELOW, anchorId);
                        break;
                    case LEFT:
                        map.put(LayoutConstants.ATTR_LAYOUT_TO_LEFT_OF, anchorId);
                        break;
                    case RIGHT:
                        map.put(LayoutConstants.ATTR_LAYOUT_TO_RIGHT_OF, anchorId);
                        break;
                    }
                    switch(direction) {
                    case TOP:
                    case BOTTOM:
                        map.put(LayoutConstants.ATTR_LAYOUT_CENTER_HORIZONTAL,
                                anchorUiNode.getAttributeValue(
                                        LayoutConstants.ATTR_LAYOUT_CENTER_HORIZONTAL));
                        map.put(LayoutConstants.ATTR_LAYOUT_TO_LEFT_OF,
                                anchorUiNode.getAttributeValue(
                                        LayoutConstants.ATTR_LAYOUT_TO_LEFT_OF));
                        map.put(LayoutConstants.ATTR_LAYOUT_TO_RIGHT_OF,
                                anchorUiNode.getAttributeValue(
                                        LayoutConstants.ATTR_LAYOUT_TO_RIGHT_OF));
                        break;
                    case LEFT:
                    case RIGHT:
                        map.put(LayoutConstants.ATTR_LAYOUT_CENTER_VERTICAL,
                                anchorUiNode.getAttributeValue(
                                        LayoutConstants.ATTR_LAYOUT_CENTER_VERTICAL));
                        map.put(LayoutConstants.ATTR_LAYOUT_ALIGN_BASELINE,
                                anchorUiNode.getAttributeValue(
                                        LayoutConstants.ATTR_LAYOUT_ALIGN_BASELINE));
                        map.put(LayoutConstants.ATTR_LAYOUT_ABOVE,
                                anchorUiNode.getAttributeValue(LayoutConstants.ATTR_LAYOUT_ABOVE));
                        map.put(LayoutConstants.ATTR_LAYOUT_BELOW,
                                anchorUiNode.getAttributeValue(LayoutConstants.ATTR_LAYOUT_BELOW));
                        break;
                    }
                } else {
                    switch(direction) {
                    case TOP:
                        map.put(LayoutConstants.ATTR_LAYOUT_ALIGN_PARENT_TOP,
                                LayoutConstants.VALUE_TRUE);
                        break;
                    case BOTTOM:
                        map.put(LayoutConstants.ATTR_LAYOUT_ALIGN_PARENT_BOTTOM,
                                LayoutConstants.VALUE_TRUE);
                        break;
                    case LEFT:
                        map.put(LayoutConstants.ATTR_LAYOUT_ALIGN_PARENT_LEFT,
                                LayoutConstants.VALUE_TRUE);
                        break;
                    case RIGHT:
                        map.put(LayoutConstants.ATTR_LAYOUT_ALIGN_PARENT_RIGHT,
                                LayoutConstants.VALUE_TRUE);
                        break;
                    }
                }
                for (Entry<String, String> entry : map.entrySet()) {
                    uiNode.setAttributeValue(entry.getKey(), entry.getValue(), true );
                }
                uiNode.commitDirtyAttributesToXml();
            }
        });
    }
    private static UiElementEditPart[] findLinearTarget(UiElementEditPart parent, Point point) {
        boolean isVertical = isVertical(parent);
        int target = isVertical ? point.y : point.x;
        UiElementEditPart prev = null;
        UiElementEditPart next = null;
        for (Object child : parent.getChildren()) {
            if (child instanceof UiElementEditPart) {
                UiElementEditPart childPart = (UiElementEditPart) child;
                Point p = childPart.getBounds().getCenter();
                int middle = isVertical ? p.y : p.x;
                if (target < middle) {
                    next = childPart;
                    break;
                }
                prev = childPart;
            }
        }
        sTempTwoParts[0] = prev;
        sTempTwoParts[1] = next;
        return sTempTwoParts;
    }
    private static void computeLinearLine(UiLayoutEditPart parentPart,
            boolean isVertical, HighlightInfo highlightInfo) {
        Rectangle r = parentPart.getBounds();
        if (isVertical) {
            Point p = null;
            UiElementEditPart part = highlightInfo.childParts[0];
            if (part != null) {
                p = part.getBounds().getBottom();
            } else {
                part = highlightInfo.childParts[1];
                if (part != null) {
                    p = part.getBounds().getTop();
                }
            }
            if (p != null) {
                highlightInfo.tempPoints[0].setLocation(0, p.y);
                highlightInfo.tempPoints[1].setLocation(r.width, p.y);
                highlightInfo.linePoints = highlightInfo.tempPoints;
                highlightInfo.anchorPoint = p.setLocation(r.width / 2, p.y);
            }
        } else {
            Point p = null;
            UiElementEditPart part = highlightInfo.childParts[0];
            if (part != null) {
                p = part.getBounds().getRight();
            } else {
                part = highlightInfo.childParts[1];
                if (part != null) {
                    p = part.getBounds().getLeft();
                }
            }
            if (p != null) {
                highlightInfo.tempPoints[0].setLocation(p.x, 0);
                highlightInfo.tempPoints[1].setLocation(p.x, r.height);
                highlightInfo.linePoints = highlightInfo.tempPoints;
                highlightInfo.anchorPoint = p.setLocation(p.x, r.height / 2);
            }
        }
    }
    private static boolean isVertical(UiElementEditPart parent) {
        String orientation = parent.getStringAttr("orientation");     
        boolean isVertical = "vertical".equals(orientation) ||        
                             "1".equals(orientation);                 
        return isVertical;
    }
    private static RelativeInfo findRelativeTarget(UiElementEditPart parent,
            Point point,
            RelativeInfo outInfo) {
        for (int i = 0; i < 4; i++) {
            sTempMinDists[i] = Integer.MAX_VALUE;
            sTempClosests[i] = null;
        }
        for (Object child : parent.getChildren()) {
            if (child instanceof UiElementEditPart) {
                UiElementEditPart childPart = (UiElementEditPart) child;
                Rectangle r = childPart.getBounds();
                if (r.contains(point)) {
                    float rx = ((float)(point.x - r.x) / (float)r.width ) - 0.5f;
                    float ry = ((float)(point.y - r.y) / (float)r.height) - 0.5f;
                    int index = 0;
                    if (Math.abs(rx) >= Math.abs(ry)) {
                        if (rx < 0) {
                            outInfo.direction = LEFT;
                            index = 1;
                        } else {
                            outInfo.direction = RIGHT;
                        }
                    } else {
                        if (ry < 0) {
                            outInfo.direction = TOP;
                            index = 1;
                        } else {
                            outInfo.direction = BOTTOM;
                        }
                    }
                    outInfo.anchorIndex = index;
                    outInfo.targetParts[index] = childPart;
                    outInfo.targetParts[1 - index] = findClosestPart(childPart,
                            outInfo.direction);
                    return outInfo;
                }
                computeClosest(point, childPart, sTempClosests, sTempMinDists, TOP);
                computeClosest(point, childPart, sTempClosests, sTempMinDists, LEFT);
                computeClosest(point, childPart, sTempClosests, sTempMinDists, BOTTOM);
                computeClosest(point, childPart, sTempClosests, sTempMinDists, RIGHT);
            }
        }
        UiElementEditPart closest = null;
        int minDist = Integer.MAX_VALUE;
        int minDir = -1;
        for (int i = 0; i <= MAX_DIR; i++) {
            if (sTempClosests[i] != null && sTempMinDists[i] < minDist) {
                closest = sTempClosests[i];
                minDist = sTempMinDists[i];
                minDir = i;
            }
        }
        if (closest != null) {
            int index = 0;
            switch(minDir) {
            case TOP:
            case LEFT:
                index = 0;
                break;
            case BOTTOM:
            case RIGHT:
                index = 1;
                break;
            }
            outInfo.anchorIndex = index;
            outInfo.targetParts[index] = closest;
            outInfo.targetParts[1 - index] = findClosestPart(closest, sOppositeDirection[minDir]);
            outInfo.direction = sOppositeDirection[minDir];
            return outInfo;
        }
        return null;
    }
    private static void computeRelativeLine(UiLayoutEditPart parentPart,
            RelativeInfo relInfo,
            HighlightInfo highlightInfo) {
        UiElementEditPart[] parts = relInfo.targetParts;
        int dir = relInfo.direction;
        int index = relInfo.anchorIndex;
        UiElementEditPart part = parts[index];
        if (part == null) {
            dir = sOppositeDirection[dir];
            part = parts[1 - index];
        }
        if (part == null) {
            return;
        }
        Rectangle r = part.getBounds();
        Point p = null;
        switch(dir) {
        case TOP:
            p = r.getTop();
            break;
        case BOTTOM:
            p = r.getBottom();
            break;
        case LEFT:
            p = r.getLeft();
            break;
        case RIGHT:
            p = r.getRight();
            break;
        }
        highlightInfo.anchorPoint = p;
        r = parentPart.getBounds();
        switch(dir) {
        case TOP:
        case BOTTOM:
            highlightInfo.tempPoints[0].setLocation(0, p.y);
            highlightInfo.tempPoints[1].setLocation(r.width, p.y);
            highlightInfo.linePoints = highlightInfo.tempPoints;
            highlightInfo.anchorPoint = p;
            break;
        case LEFT:
        case RIGHT:
            highlightInfo.tempPoints[0].setLocation(p.x, 0);
            highlightInfo.tempPoints[1].setLocation(p.x, r.height);
            highlightInfo.linePoints = highlightInfo.tempPoints;
            highlightInfo.anchorPoint = p;
            break;
        }
    }
    private static void computeClosest(Point refPoint,
            UiElementEditPart compareToPart,
            UiElementEditPart[] currClosests,
            int[] currMinDists,
            int direction) {
        Rectangle r = compareToPart.getBounds();
        Point p = null;
        boolean usable = false;
        switch(direction) {
        case TOP:
            p = r.getBottom();
            usable = p.y <= refPoint.y;
            break;
        case BOTTOM:
            p = r.getTop();
            usable = p.y >= refPoint.y;
            break;
        case LEFT:
            p = r.getRight();
            usable = p.x <= refPoint.x;
            break;
        case RIGHT:
            p = r.getLeft();
            usable = p.x >= refPoint.x;
            break;
        }
        if (usable) {
            int d = p.getDistance2(refPoint);
            if (d < currMinDists[direction]) {
                currMinDists[direction] = d;
                currClosests[direction] = compareToPart;
            }
        }
    }
    private static UiElementEditPart findClosestPart(UiElementEditPart referencePart,
            int direction) {
        if (referencePart == null || referencePart.getParent() == null) {
            return null;
        }
        Rectangle r = referencePart.getBounds();
        Point ref = null;
        switch(direction) {
        case TOP:
            ref = r.getTop();
            break;
        case BOTTOM:
            ref = r.getBottom();
            break;
        case LEFT:
            ref = r.getLeft();
            break;
        case RIGHT:
            ref = r.getRight();
            break;
        }
        int minDist = Integer.MAX_VALUE;
        UiElementEditPart closestPart = null;
        for (Object childPart : referencePart.getParent().getChildren()) {
            if (childPart != referencePart && childPart instanceof UiElementEditPart) {
                r = ((UiElementEditPart) childPart).getBounds();
                Point p = null;
                boolean usable = false;
                switch(direction) {
                case TOP:
                    p = r.getBottom();
                    usable = p.y <= ref.y;
                    break;
                case BOTTOM:
                    p = r.getTop();
                    usable = p.y >= ref.y;
                    break;
                case LEFT:
                    p = r.getRight();
                    usable = p.x <= ref.x;
                    break;
                case RIGHT:
                    p = r.getLeft();
                    usable = p.x >= ref.x;
                    break;
                }
                if (usable) {
                    int d = p.getDistance2(ref);
                    if (d < minDist) {
                        minDist = d;
                        closestPart = (UiElementEditPart) childPart;
                    }
                }
            }
        }
        return closestPart;
    }
}
