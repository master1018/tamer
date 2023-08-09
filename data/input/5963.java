final class LayoutComparator implements Comparator<Component>, java.io.Serializable {
    private static final int ROW_TOLERANCE = 10;
    private boolean horizontal = true;
    private boolean leftToRight = true;
    void setComponentOrientation(ComponentOrientation orientation) {
        horizontal = orientation.isHorizontal();
        leftToRight = orientation.isLeftToRight();
    }
    public int compare(Component a, Component b) {
        if (a == b) {
            return 0;
        }
        if (a.getParent() != b.getParent()) {
            LinkedList<Component> aAncestory = new LinkedList<Component>();
            for(; a != null; a = a.getParent()) {
                aAncestory.add(a);
                if (a instanceof Window) {
                    break;
                }
            }
            if (a == null) {
                throw new ClassCastException();
            }
            LinkedList<Component> bAncestory = new LinkedList<Component>();
            for(; b != null; b = b.getParent()) {
                bAncestory.add(b);
                if (b instanceof Window) {
                    break;
                }
            }
            if (b == null) {
                throw new ClassCastException();
            }
            for (ListIterator<Component>
                     aIter = aAncestory.listIterator(aAncestory.size()),
                     bIter = bAncestory.listIterator(bAncestory.size()); ;) {
                if (aIter.hasPrevious()) {
                    a = aIter.previous();
                } else {
                    return -1;
                }
                if (bIter.hasPrevious()) {
                    b = bIter.previous();
                } else {
                    return 1;
                }
                if (a != b) {
                    break;
                }
            }
        }
        int ax = a.getX(), ay = a.getY(), bx = b.getX(), by = b.getY();
        int zOrder = a.getParent().getComponentZOrder(a) - b.getParent().getComponentZOrder(b);
        if (horizontal) {
            if (leftToRight) {
                if (Math.abs(ay - by) < ROW_TOLERANCE) {
                    return (ax < bx) ? -1 : ((ax > bx) ? 1 : zOrder);
                } else {
                    return (ay < by) ? -1 : 1;
                }
            } else { 
                if (Math.abs(ay - by) < ROW_TOLERANCE) {
                    return (ax > bx) ? -1 : ((ax < bx) ? 1 : zOrder);
                } else {
                    return (ay < by) ? -1 : 1;
                }
            }
        } else { 
            if (leftToRight) {
                if (Math.abs(ax - bx) < ROW_TOLERANCE) {
                    return (ay < by) ? -1 : ((ay > by) ? 1 : zOrder);
                } else {
                    return (ax < bx) ? -1 : 1;
                }
            } else { 
                if (Math.abs(ax - bx) < ROW_TOLERANCE) {
                    return (ay < by) ? -1 : ((ay > by) ? 1 : zOrder);
                } else {
                    return (ax > bx) ? -1 : 1;
                }
            }
        }
    }
}
