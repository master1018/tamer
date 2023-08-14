public abstract class ElementParallelTimeContainerImpl extends ElementTimeContainerImpl
        implements ElementParallelTimeContainer {
    private final static String ENDSYNC_ATTRIBUTE_NAME = "endsync";
    private final static String ENDSYNC_FIRST = "first";
    private final static String ENDSYNC_LAST  = "last";
    private final static String ENDSYNC_ALL   = "all";
    private final static String ENDSYNC_MEDIA = "media";
    ElementParallelTimeContainerImpl(SMILElement element) {
        super(element);
    }
    public String getEndSync() {
        String endsync = mSmilElement.getAttribute(ENDSYNC_ATTRIBUTE_NAME);
        if ((endsync == null) || (endsync.length() == 0)) {
            setEndSync(ENDSYNC_LAST);
            return ENDSYNC_LAST;
        }
        if (ENDSYNC_FIRST.equals(endsync) || ENDSYNC_LAST.equals(endsync) ||
                ENDSYNC_ALL.equals(endsync) || ENDSYNC_MEDIA.equals(endsync)) {
            return endsync;
        }
        setEndSync(ENDSYNC_LAST);
        return ENDSYNC_LAST;
    }
    public void setEndSync(String endSync) throws DOMException {
        if (ENDSYNC_FIRST.equals(endSync) || ENDSYNC_LAST.equals(endSync) ||
                ENDSYNC_ALL.equals(endSync) || ENDSYNC_MEDIA.equals(endSync)) {
            mSmilElement.setAttribute(ENDSYNC_ATTRIBUTE_NAME, endSync);
        } else { 
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Unsupported endsync value" + endSync);
        }
    }
    @Override
    public float getDur() {
        float dur = super.getDur();
        if (dur == 0) {
            dur = getImplicitDuration();
        }
        return dur;
    }
    public float getImplicitDuration() {
        float dur = -1.0F;
        if (ENDSYNC_LAST.equals(getEndSync())) {
            NodeList children = getTimeChildren();
            for (int i = 0; i < children.getLength(); ++i) {
                ElementTime child = (ElementTime) children.item(i);
                TimeList endTimeList = child.getEnd();
                for (int j = 0; j < endTimeList.getLength(); ++j) {
                    Time endTime = endTimeList.item(j);
                    if (endTime.getTimeType() == Time.SMIL_TIME_INDEFINITE) {
                        return -1.0F;
                    }
                    if (endTime.getResolved()) {
                        float end = (float)endTime.getResolvedOffset();
                        dur = (end > dur) ? end : dur;
                    }
                }
            }
        } 
        return dur;
    }
    public NodeList getActiveChildrenAt(float instant) {
        ArrayList<Node> activeChildren = new ArrayList<Node>();
        NodeList children = getTimeChildren();
        int childrenLen = children.getLength();
        for (int i = 0; i < childrenLen; ++i) {
            double maxOffset = 0.0;
            boolean active = false;
            ElementTime child = (ElementTime) children.item(i);
            TimeList beginList = child.getBegin();
            int len = beginList.getLength();
            for (int j = 0; j < len; ++j) {
                Time begin = beginList.item(j);
                if (begin.getResolved()) {
                    double resolvedOffset = begin.getResolvedOffset() * 1000.0;
                    if ((resolvedOffset <= instant) && (resolvedOffset >= maxOffset)) {
                        maxOffset = resolvedOffset;
                        active = true;
                    }
                }
            }
            TimeList endList = child.getEnd();
            len = endList.getLength();
            for (int j = 0; j < len; ++j) {
                Time end = endList.item(j);
                if (end.getResolved()) {
                    double resolvedOffset = end.getResolvedOffset() * 1000.0;
                    if ((resolvedOffset <= instant) && (resolvedOffset >= maxOffset)) {
                        maxOffset = resolvedOffset;
                        active = false;
                    }
                }
            }
            if (active) {
                activeChildren.add((Node) child);
            }
        }
        return new NodeListImpl(activeChildren);
    }
}
