public abstract class BaseHeapPanel extends TablePanel {
    protected byte[] mProcessedHeapData;
    private Map<Integer, ArrayList<HeapSegmentElement>> mHeapMap;
    protected boolean serializeHeapData(HeapData heapData) {
        Collection<HeapSegment> heapSegments;
        synchronized (heapData) {
            heapSegments = heapData.getHeapSegments();
            if (heapSegments != null) {
                heapData.clearHeapData();
                doSerializeHeapData(heapSegments);
                heapData.setProcessedHeapData(mProcessedHeapData);
                heapData.setProcessedHeapMap(mHeapMap);
            } else {
                byte[] pixData = heapData.getProcessedHeapData();
                if (pixData == mProcessedHeapData) {
                    return false;
                } else {
                    mProcessedHeapData = pixData;
                }
                Map<Integer, ArrayList<HeapSegmentElement>> heapMap =
                    heapData.getProcessedHeapMap();
                mHeapMap = heapMap;
            }
        }
        return true;
    }
    protected byte[] getSerializedData() {
        return mProcessedHeapData;
    }
    private void doSerializeHeapData(Collection<HeapSegment> heapData) {
        mHeapMap = new TreeMap<Integer, ArrayList<HeapSegmentElement>>();
        Iterator<HeapSegment> iterator;
        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream(4 * 1024);
        iterator = heapData.iterator();
        while (iterator.hasNext()) {
            HeapSegment hs = iterator.next();
            HeapSegmentElement e = null;
            while (true) {
                int v;
                e = hs.getNextElement(null);
                if (e == null) {
                    break;
                }
                if (e.getSolidity() == HeapSegmentElement.SOLIDITY_FREE) {
                    v = 1;
                } else {
                    v = e.getKind() + 2;
                }
                ArrayList<HeapSegmentElement> elementList = mHeapMap.get(v);
                if (elementList == null) {
                    elementList = new ArrayList<HeapSegmentElement>();
                    mHeapMap.put(v, elementList);
                }
                elementList.add(e);
                int len = e.getLength() / 8;
                while (len > 0) {
                    out.write(v);
                    --len;
                }
            }
        }
        mProcessedHeapData = out.toByteArray();
        Collection<ArrayList<HeapSegmentElement>> elementLists = mHeapMap.values();
        for (ArrayList<HeapSegmentElement> elementList : elementLists) {
            Collections.sort(elementList);
        }
    }
    protected ImageData createLinearHeapImage(byte[] pixData, int h, PaletteData palette) {
        int w = pixData.length / h;
        if (pixData.length % h != 0) {
            w++;
        }
        ImageData id = new ImageData(w, h, 8, palette);
        int x = 0;
        int y = 0;
        for (byte b : pixData) {
            if (b >= 0) {
                id.setPixel(x, y, b);
            }
            y++;
            if (y >= h) {
                y = 0;
                x++;
            }
        }
        return id;
    }
}
