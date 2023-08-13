public class Sequence {
    public static final float PPQ                                                       = 0.0f;
    public static final float SMPTE_24                                          = 24.0f;
    public static final float SMPTE_25                                          = 25.0f;
    public static final float SMPTE_30DROP                                      = 29.97f;
    public static final float SMPTE_30                                          = 30.0f;
    protected float divisionType;
    protected int resolution;
    protected Vector<Track> tracks = new Vector<Track>();
    public Sequence(float divisionType, int resolution) throws InvalidMidiDataException {
        if (divisionType == PPQ)
            this.divisionType = PPQ;
        else if (divisionType == SMPTE_24)
            this.divisionType = SMPTE_24;
        else if (divisionType == SMPTE_25)
            this.divisionType = SMPTE_25;
        else if (divisionType == SMPTE_30DROP)
            this.divisionType = SMPTE_30DROP;
        else if (divisionType == SMPTE_30)
            this.divisionType = SMPTE_30;
        else throw new InvalidMidiDataException("Unsupported division type: " + divisionType);
        this.resolution = resolution;
    }
    public Sequence(float divisionType, int resolution, int numTracks) throws InvalidMidiDataException {
        if (divisionType == PPQ)
            this.divisionType = PPQ;
        else if (divisionType == SMPTE_24)
            this.divisionType = SMPTE_24;
        else if (divisionType == SMPTE_25)
            this.divisionType = SMPTE_25;
        else if (divisionType == SMPTE_30DROP)
            this.divisionType = SMPTE_30DROP;
        else if (divisionType == SMPTE_30)
            this.divisionType = SMPTE_30;
        else throw new InvalidMidiDataException("Unsupported division type: " + divisionType);
        this.resolution = resolution;
        for (int i = 0; i < numTracks; i++) {
            tracks.addElement(new Track());
        }
    }
    public float getDivisionType() {
        return divisionType;
    }
    public int getResolution() {
        return resolution;
    }
    public Track createTrack() {
        Track track = new Track();
        tracks.addElement(track);
        return track;
    }
    public boolean deleteTrack(Track track) {
        synchronized(tracks) {
            return tracks.removeElement(track);
        }
    }
    public Track[] getTracks() {
        return (Track[]) tracks.toArray(new Track[tracks.size()]);
    }
    public long getMicrosecondLength() {
        return com.sun.media.sound.MidiUtils.tick2microsecond(this, getTickLength(), null);
    }
    public long getTickLength() {
        long length = 0;
        synchronized(tracks) {
            for(int i=0; i<tracks.size(); i++ ) {
                long temp = ((Track)tracks.elementAt(i)).ticks();
                if( temp>length ) {
                    length = temp;
                }
            }
            return length;
        }
    }
    public Patch[] getPatchList() {
        return new Patch[0];
    }
}
