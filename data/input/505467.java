public class GalResult {
    public int total;
    public ArrayList<GalData> galData = new ArrayList<GalData>();
    public GalResult() {
    }
    public void addGalData(long id, String displayName, String emailAddress) {
        galData.add(new GalData(id, displayName, emailAddress));
    }
    public static class GalData {
        final long _id;
        final String displayName;
        final String emailAddress;
        private GalData(long id, String _displayName, String _emailAddress) {
            _id = id;
            displayName = _displayName;
            emailAddress = _emailAddress;
        }
    }
}
