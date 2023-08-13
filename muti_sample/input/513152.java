public class KmlParser {
    private final static String NS_KML_2 = "http:
    private final static String NODE_PLACEMARK = "Placemark"; 
    private final static String NODE_NAME = "name"; 
    private final static String NODE_COORDINATES = "coordinates"; 
    private final static Pattern sLocationPattern = Pattern.compile("([^,]+),([^,]+)(?:,([^,]+))?");
    private static SAXParserFactory sParserFactory;
    static {
        sParserFactory = SAXParserFactory.newInstance();
        sParserFactory.setNamespaceAware(true);
    }
    private String mFileName;
    private KmlHandler mHandler;
    private static class KmlHandler extends DefaultHandler {
        List<WayPoint> mWayPoints;
        WayPoint mCurrentWayPoint;
        final StringBuilder mStringAccumulator = new StringBuilder();
        boolean mSuccess = true;
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            try {
                if (uri.startsWith(NS_KML_2)) {
                    if (NODE_PLACEMARK.equals(localName)) {
                        if (mWayPoints == null) {
                            mWayPoints = new ArrayList<WayPoint>();
                        }
                        mWayPoints.add(mCurrentWayPoint = new WayPoint());
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
            if (uri.startsWith(NS_KML_2)) {
                if (NODE_PLACEMARK.equals(localName)) {
                    mCurrentWayPoint = null;
                } else if (NODE_NAME.equals(localName)) {
                    if (mCurrentWayPoint != null) {
                        mCurrentWayPoint.setName(mStringAccumulator.toString());
                    }
                } else if (NODE_COORDINATES.equals(localName)) {
                    if (mCurrentWayPoint != null) {
                        parseLocation(mCurrentWayPoint, mStringAccumulator.toString());
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
        private void parseLocation(LocationPoint locationNode, String location) {
            Matcher m = sLocationPattern.matcher(location);
            if (m.matches()) {
                try {
                    double longitude = Double.parseDouble(m.group(1));
                    double latitude = Double.parseDouble(m.group(2));
                    locationNode.setLocation(longitude, latitude);
                    if (m.groupCount() == 3) {
                        locationNode.setElevation(Double.parseDouble(m.group(3)));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        WayPoint[] getWayPoints() {
            if (mWayPoints != null) {
                return mWayPoints.toArray(new WayPoint[mWayPoints.size()]);
            }
            return null;
        }
        boolean getSuccess() {
            return mSuccess;
        }
    }
    public KmlParser(String fileName) {
        mFileName = fileName;
    }
    public boolean parse() {
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            mHandler = new KmlHandler();
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
}
