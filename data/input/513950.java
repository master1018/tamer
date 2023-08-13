public final class GpsStatus {
    private static final int NUM_SATELLITES = 255;
    private int mTimeToFirstFix;
    private GpsSatellite mSatellites[] = new GpsSatellite[NUM_SATELLITES];
    private final class SatelliteIterator implements Iterator<GpsSatellite> {
        private GpsSatellite[] mSatellites;
        int mIndex = 0;
        SatelliteIterator(GpsSatellite[] satellites) {
            mSatellites = satellites;
        }
        public boolean hasNext() {
            for (int i = mIndex; i < mSatellites.length; i++) {
                if (mSatellites[i].mValid) {
                    return true;
                }
            }
            return false;
        }
        public GpsSatellite next() {
            while (mIndex < mSatellites.length) {
                GpsSatellite satellite = mSatellites[mIndex++];
                if (satellite.mValid) {
                    return satellite;
                }
            }
            throw new NoSuchElementException();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    private Iterable<GpsSatellite> mSatelliteList = new Iterable<GpsSatellite>() {
        public Iterator<GpsSatellite> iterator() {
            return new SatelliteIterator(mSatellites);
        }
    };
    public static final int GPS_EVENT_STARTED = 1;
    public static final int GPS_EVENT_STOPPED = 2;
    public static final int GPS_EVENT_FIRST_FIX = 3;
    public static final int GPS_EVENT_SATELLITE_STATUS = 4;
    public interface Listener {
        void onGpsStatusChanged(int event);
    }
    public interface NmeaListener {
        void onNmeaReceived(long timestamp, String nmea);
    }
    GpsStatus() {
        for (int i = 0; i < mSatellites.length; i++) {
            mSatellites[i] = new GpsSatellite(i + 1);
        }
    }
    synchronized void setStatus(int svCount, int[] prns, float[] snrs,
            float[] elevations, float[] azimuths, int ephemerisMask,
            int almanacMask, int usedInFixMask) {
        int i;
        for (i = 0; i < mSatellites.length; i++) {
            mSatellites[i].mValid = false;
        }
        for (i = 0; i < svCount; i++) {
            int prn = prns[i] - 1;
            int prnShift = (1 << prn);
            GpsSatellite satellite = mSatellites[prn];
            satellite.mValid = true;
            satellite.mSnr = snrs[i];
            satellite.mElevation = elevations[i];
            satellite.mAzimuth = azimuths[i];
            satellite.mHasEphemeris = ((ephemerisMask & prnShift) != 0);
            satellite.mHasAlmanac = ((almanacMask & prnShift) != 0);
            satellite.mUsedInFix = ((usedInFixMask & prnShift) != 0);
        }
    }
    void setStatus(GpsStatus status) {
        mTimeToFirstFix = status.getTimeToFirstFix();
        for (int i = 0; i < mSatellites.length; i++) {
            mSatellites[i].setStatus(status.mSatellites[i]);
        } 
    }
    void setTimeToFirstFix(int ttff) {
        mTimeToFirstFix = ttff;
    }
    public int getTimeToFirstFix() {
        return mTimeToFirstFix;
    }
    public Iterable<GpsSatellite> getSatellites() {
        return mSatelliteList;
    }
    public int getMaxSatellites() {
        return NUM_SATELLITES;
    }
}
