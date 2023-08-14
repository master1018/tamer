public  class City implements Comparable<City> {
    private static Map<String,City> cities = new HashMap<String,City>();
    private static City[] citiesByRawOffset;
    private String name;
    private String timeZoneID;
    private TimeZone timeZone = null;
    private int rawOffset;
    private float latitude, longitude;
    private float x, y, z;
    public static void loadCities(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int numCities = dis.readInt();
        citiesByRawOffset = new City[numCities];
        byte[] buf = new byte[24];
        for (int i = 0; i < numCities; i++) {
            String name = dis.readUTF();
            String tzid = dis.readUTF();
            dis.read(buf);
            int rawOffset =
                       (buf[ 0] << 24) |       ((buf[ 1] & 0xff) << 16) |
                      ((buf[ 2] & 0xff) << 8) | (buf[ 3] & 0xff);
            int ilat = (buf[ 4] << 24) |       ((buf[ 5] & 0xff) << 16) |
                      ((buf[ 6] & 0xff) << 8) | (buf[ 7] & 0xff);
            int ilon = (buf[ 8] << 24) |       ((buf[ 9] & 0xff) << 16) |
                      ((buf[10] & 0xff) << 8) | (buf[11] & 0xff);
            int icx =  (buf[12] << 24) |       ((buf[13] & 0xff) << 16) |
                      ((buf[14] & 0xff) << 8) | (buf[15] & 0xff);
            int icy =  (buf[16] << 24) |       ((buf[17] & 0xff) << 16) |
                      ((buf[18] & 0xff) << 8) | (buf[19] & 0xff);
            int icz =  (buf[20] << 24) |       ((buf[21] & 0xff) << 16) |
                      ((buf[22] & 0xff) << 8) | (buf[23] & 0xff);
            float latitude = Float.intBitsToFloat(ilat);
            float longitude = Float.intBitsToFloat(ilon);
            float cx = Float.intBitsToFloat(icx);
            float cy = Float.intBitsToFloat(icy);
            float cz = Float.intBitsToFloat(icz);
            City city = new City(name, tzid, rawOffset,
                                 latitude, longitude, cx, cy, cz);
            cities.put(name, city);
            citiesByRawOffset[i] = city;
        }
    }
    public static City[] getCitiesByName() {
        City[] ocities = new City[cities.size()];
        Iterator<City> iter = cities.values().iterator();
        int idx = 0;
        while (iter.hasNext()) {
            ocities[idx++] = iter.next();
        }
        Arrays.sort(ocities);
        return ocities;
    }
    public static City[] getCitiesByOffset() {
        City[] ocities = new City[cities.size()];
        Iterator<City> iter = cities.values().iterator();
        int idx = 0;
        while (iter.hasNext()) {
            ocities[idx++] = iter.next();
        }
        Arrays.sort(ocities, new Comparator() {
                public int compare(Object o1, Object o2) {
                    long now = System.currentTimeMillis();
                    City c1 = (City)o1;
                    City c2 = (City)o2;
                    TimeZone tz1 = c1.getTimeZone();
                    TimeZone tz2 = c2.getTimeZone();
                    int off1 = tz1.getOffset(now);
                    int off2 = tz2.getOffset(now);
                    if (off1 == off2) {
                        float dlat = c2.getLatitude() - c1.getLatitude();
                        if (dlat < 0.0f) return -1;
                        if (dlat > 0.0f) return 1;
                        return 0;
                    }
                    return off1 - off2;
                }
            });
        return ocities;
    }
    public static City[] getCitiesByRawOffset() {
        return citiesByRawOffset;
    }
    public static Iterator<City> iterator() {
        return cities.values().iterator();
    }
    public static int numCities() {
        return cities.size();
    }
    public City(String name, String timeZoneID,
                int rawOffset,
                float latitude, float longitude,
                float x, float y, float z) {
        this.name = name;
        this.timeZoneID = timeZoneID;
        this.rawOffset = rawOffset;
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public String getName() {
        return name;
    }
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone(timeZoneID);
        }
        return timeZone;
    }
    public float getLongitude() {
        return longitude;
    }
    public float getLatitude() {
        return latitude;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }
    public float getRawOffset() {
        return rawOffset / 3600000.0f;
    }
    public int getRawOffsetMillis() {
        return rawOffset;
    }
    public float getOffset() {
        long now = System.currentTimeMillis();
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone(timeZoneID);
        }
        return timeZone.getOffset(now) / 3600000.0f;
    }
    public int compareTo(City o) {
        return name.compareTo(o.name);
    }
}
