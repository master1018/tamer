public class GpxParser {
    private final static String NS_GPX = "http:
    private final static String NODE_WAYPOINT = "wpt"; 
    private final static String NODE_TRACK = "trk"; 
    private final static String NODE_TRACK_SEGMENT = "trkseg"; 
    private final static String NODE_TRACK_POINT = "trkpt"; 
    private final static String NODE_NAME = "name"; 
    private final static String NODE_TIME = "time"; 
    private final static String NODE_ELEVATION = "ele"; 
    private final static String NODE_DESCRIPTION = "desc"; 
    private final static String ATTR_LONGITUDE = "lon"; 
    private final static String ATTR_LATITUDE = "lat"; 
    private static SAXParserFactory sParserFactory;
    static {
        sParserFactory = SAXParserFactory.newInstance();
        sParserFactory.setNamespaceAware(true);
    }
    private String mFileName;
    private GpxHandler mHandler;
    private final static Pattern ISO8601_TIME =
        Pattern.compile("(\\d{4})-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d)(?:(\\.\\d+))?(Z)?"); 
    private static class GpxHandler extends DefaultHandler {
        List<WayPoint> mWayPoints;
        List<Track> mTrackList;
        Track mCurrentTrack;
        TrackPoint mCurrentTrackPoint;
        WayPoint mCurrentWayPoint;
        final StringBuilder mStringAccumulator = new StringBuilder();
        boolean mSuccess = true;
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            try {
                if (NS_GPX.equals(uri)) {
                    if (NODE_WAYPOINT.equals(localName)) {
                        if (mWayPoints == null) {
                            mWayPoints = new ArrayList<WayPoint>();
                        }
                        mWayPoints.add(mCurrentWayPoint = new WayPoint());
                        handleLocation(mCurrentWayPoint, attributes);
                    } else if (NODE_TRACK.equals(localName)) {
                        if (mTrackList == null) {
                            mTrackList = new ArrayList<Track>();
                        }
                        mTrackList.add(mCurrentTrack = new Track());
                    } else if (NODE_TRACK_SEGMENT.equals(localName)) {
                    } else if (NODE_TRACK_POINT.equals(localName)) {
                        if (mCurrentTrack != null) {
                            mCurrentTrack.addPoint(mCurrentTrackPoint = new TrackPoint());
                            handleLocation(mCurrentTrackPoint, attributes);
                        }
                    }
                }
            } finally {
                mStringAccumulator.setLength(0);
            }
        }
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mStringAccumulator.append(ch, start, length);
        }
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (NS_GPX.equals(uri)) {
                if (NODE_WAYPOINT.equals(localName)) {
                    mCurrentWayPoint = null;
                } else if (NODE_TRACK.equals(localName)) {
                    mCurrentTrack = null;
                } else if (NODE_TRACK_POINT.equals(localName)) {
                    mCurrentTrackPoint = null;
                } else if (NODE_NAME.equals(localName)) {
                    if (mCurrentTrack != null) {
                        mCurrentTrack.setName(mStringAccumulator.toString());
                    } else if (mCurrentWayPoint != null) {
                        mCurrentWayPoint.setName(mStringAccumulator.toString());
                    }
                } else if (NODE_TIME.equals(localName)) {
                    if (mCurrentTrackPoint != null) {
                        mCurrentTrackPoint.setTime(computeTime(mStringAccumulator.toString()));
                    }
                } else if (NODE_ELEVATION.equals(localName)) {
                    if (mCurrentTrackPoint != null) {
                        mCurrentTrackPoint.setElevation(
                                Double.parseDouble(mStringAccumulator.toString()));
                    } else if (mCurrentWayPoint != null) {
                        mCurrentWayPoint.setElevation(
                                Double.parseDouble(mStringAccumulator.toString()));
                    }
                } else if (NODE_DESCRIPTION.equals(localName)) {
                    if (mCurrentWayPoint != null) {
                        mCurrentWayPoint.setDescription(mStringAccumulator.toString());
                    }
                }
            }
        }
        @Override
        public void error(SAXParseException e) throws SAXException {
            mSuccess = false;
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            mSuccess = false;
        }
        private long computeTime(String timeString) {
            Matcher m = ISO8601_TIME.matcher(timeString);
            if (m.matches()) {
                try {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int date = Integer.parseInt(m.group(3));
                    int hourOfDay = Integer.parseInt(m.group(4));
                    int minute = Integer.parseInt(m.group(5));
                    int second = Integer.parseInt(m.group(6));
                    int milliseconds = 0;
                    String subSecondGroup = m.group(7);
                    if (subSecondGroup != null) {
                        milliseconds = (int)(1000 * Double.parseDouble(subSecondGroup));
                    }
                    boolean utcTime = m.group(8) != null;
                    Calendar c;
                    if (utcTime) {
                        c = Calendar.getInstance(TimeZone.getTimeZone("GMT")); 
                    } else {
                        c = Calendar.getInstance();
                    }
                    c.set(year, month, date, hourOfDay, minute, second);
                    return c.getTimeInMillis() + milliseconds;
                } catch (NumberFormatException e) {
                }
            }
            return -1;
        }
        private void handleLocation(LocationPoint locationNode, Attributes attributes) {
            try {
                double longitude = Double.parseDouble(attributes.getValue(ATTR_LONGITUDE));
                double latitude = Double.parseDouble(attributes.getValue(ATTR_LATITUDE));
                locationNode.setLocation(longitude, latitude);
            } catch (NumberFormatException e) {
            }
        }
        WayPoint[] getWayPoints() {
            if (mWayPoints != null) {
                return mWayPoints.toArray(new WayPoint[mWayPoints.size()]);
            }
            return null;
        }
        Track[] getTracks() {
            if (mTrackList != null) {
                return mTrackList.toArray(new Track[mTrackList.size()]);
            }
            return null;
        }
        boolean getSuccess() {
            return mSuccess;
        }
    }
    public final static class Track {
        private String mName;
        private String mComment;
        private List<TrackPoint> mPoints = new ArrayList<TrackPoint>();
        void setName(String name) {
            mName = name;
        }
        public String getName() {
            return mName;
        }
        void setComment(String comment) {
            mComment = comment;
        }
        public String getComment() {
            return mComment;
        }
        void addPoint(TrackPoint trackPoint) {
            mPoints.add(trackPoint);
        }
        public TrackPoint[] getPoints() {
            return mPoints.toArray(new TrackPoint[mPoints.size()]);
        }
        public long getFirstPointTime() {
            if (mPoints.size() > 0) {
                return mPoints.get(0).getTime();
            }
            return -1;
        }
        public long getLastPointTime() {
            if (mPoints.size() > 0) {
                return mPoints.get(mPoints.size()-1).getTime();
            }
            return -1;
        }
        public int getPointCount() {
            return mPoints.size();
        }
    }
    public GpxParser(String fileName) {
        mFileName = fileName;
    }
    public boolean parse() {
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            mHandler = new GpxHandler();
            parser.parse(new InputSource(new FileReader(mFileName)), mHandler);
            return mHandler.getSuccess();
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } finally {
        }
        return false;
    }
    public WayPoint[] getWayPoints() {
        if (mHandler != null) {
            return mHandler.getWayPoints();
        }
        return null;
    }
    public Track[] getTracks() {
        if (mHandler != null) {
            return mHandler.getTracks();
        }
        return null;
    }
}
