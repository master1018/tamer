public class PduBody {
    private Vector<PduPart> mParts = null;
    private Map<String, PduPart> mPartMapByContentId = null;
    private Map<String, PduPart> mPartMapByContentLocation = null;
    private Map<String, PduPart> mPartMapByName = null;
    private Map<String, PduPart> mPartMapByFileName = null;
    public PduBody() {
        mParts = new Vector<PduPart>();
        mPartMapByContentId = new HashMap<String, PduPart>();
        mPartMapByContentLocation  = new HashMap<String, PduPart>();
        mPartMapByName = new HashMap<String, PduPart>();
        mPartMapByFileName = new HashMap<String, PduPart>();
    }
    private void putPartToMaps(PduPart part) {
        byte[] contentId = part.getContentId();
        if(null != contentId) {
            mPartMapByContentId.put(new String(contentId), part);
        }
        byte[] contentLocation = part.getContentLocation();
        if(null != contentLocation) {
            String clc = new String(contentLocation);
            mPartMapByContentLocation.put(clc, part);
        }
        byte[] name = part.getName();
        if(null != name) {
            String clc = new String(name);
            mPartMapByName.put(clc, part);
        }
        byte[] fileName = part.getFilename();
        if(null != fileName) {
            String clc = new String(fileName);
            mPartMapByFileName.put(clc, part);
        }
    }
    public boolean addPart(PduPart part) {
        if(null == part) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        return mParts.add(part);
    }
    public void addPart(int index, PduPart part) {
        if(null == part) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        mParts.add(index, part);
    }
    public PduPart removePart(int index) {
        return mParts.remove(index);
    }
    public void removeAll() {
        mParts.clear();
    }
    public PduPart getPart(int index) {
        return mParts.get(index);
    }
    public int getPartIndex(PduPart part) {
        return mParts.indexOf(part);
    }
    public int getPartsNum() {
        return mParts.size();
    }
    public PduPart getPartByContentId(String cid) {
        return mPartMapByContentId.get(cid);
    }
    public PduPart getPartByContentLocation(String contentLocation) {
        return mPartMapByContentLocation.get(contentLocation);
    }
    public PduPart getPartByName(String name) {
        return mPartMapByName.get(name);
    }
    public PduPart getPartByFileName(String filename) {
        return mPartMapByFileName.get(filename);
    }
}
