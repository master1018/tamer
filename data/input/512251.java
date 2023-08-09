public class GeomagneticField {
    private float mX;
    private float mY;
    private float mZ;
    private float mGcLatitudeRad;
    private float mGcLongitudeRad;
    private float mGcRadiusKm;
    static private final float EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f;
    static private final float EARTH_SEMI_MINOR_AXIS_KM = 6356.7523142f;
    static private final float EARTH_REFERENCE_RADIUS_KM = 6371.2f;
    static private final float[][] G_COEFF = new float[][] {
        { 0.0f },
        { -29496.6f, -1586.3f },
        { -2396.6f, 3026.1f, 1668.6f },
        { 1340.1f, -2326.2f, 1231.9f, 634.0f },
        { 912.6f, 808.9f, 166.7f, -357.1f, 89.4f },
        { -230.9f, 357.2f, 200.3f, -141.1f, -163.0f, -7.8f },
        { 72.8f, 68.6f, 76.0f, -141.4f, -22.8f, 13.2f, -77.9f },
        { 80.5f, -75.1f, -4.7f, 45.3f, 13.9f, 10.4f, 1.7f, 4.9f },
        { 24.4f, 8.1f, -14.5f, -5.6f, -19.3f, 11.5f, 10.9f, -14.1f, -3.7f },
        { 5.4f, 9.4f, 3.4f, -5.2f, 3.1f, -12.4f, -0.7f, 8.4f, -8.5f, -10.1f },
        { -2.0f, -6.3f, 0.9f, -1.1f, -0.2f, 2.5f, -0.3f, 2.2f, 3.1f, -1.0f, -2.8f },
        { 3.0f, -1.5f, -2.1f, 1.7f, -0.5f, 0.5f, -0.8f, 0.4f, 1.8f, 0.1f, 0.7f, 3.8f },
        { -2.2f, -0.2f, 0.3f, 1.0f, -0.6f, 0.9f, -0.1f, 0.5f, -0.4f, -0.4f, 0.2f, -0.8f, 0.0f } };
    static private final float[][] H_COEFF = new float[][] {
        { 0.0f },
        { 0.0f, 4944.4f },
        { 0.0f, -2707.7f, -576.1f },
        { 0.0f, -160.2f, 251.9f, -536.6f },
        { 0.0f, 286.4f, -211.2f, 164.3f, -309.1f },
        { 0.0f, 44.6f, 188.9f, -118.2f, 0.0f, 100.9f },
        { 0.0f, -20.8f, 44.1f, 61.5f, -66.3f, 3.1f, 55.0f },
        { 0.0f, -57.9f, -21.1f, 6.5f, 24.9f, 7.0f, -27.7f, -3.3f },
        { 0.0f, 11.0f, -20.0f, 11.9f, -17.4f, 16.7f, 7.0f, -10.8f, 1.7f },
        { 0.0f, -20.5f, 11.5f, 12.8f, -7.2f, -7.4f, 8.0f, 2.1f, -6.1f, 7.0f },
        { 0.0f, 2.8f, -0.1f, 4.7f, 4.4f, -7.2f, -1.0f, -3.9f, -2.0f, -2.0f, -8.3f },
        { 0.0f, 0.2f, 1.7f, -0.6f, -1.8f, 0.9f, -0.4f, -2.5f, -1.3f, -2.1f, -1.9f, -1.8f },
        { 0.0f, -0.9f, 0.3f, 2.1f, -2.5f, 0.5f, 0.6f, 0.0f, 0.1f, 0.3f, -0.9f, -0.2f, 0.9f } };
    static private final float[][] DELTA_G = new float[][] {
        { 0.0f },
        { 11.6f, 16.5f },
        { -12.1f, -4.4f, 1.9f },
        { 0.4f, -4.1f, -2.9f, -7.7f },
        { -1.8f, 2.3f, -8.7f, 4.6f, -2.1f },
        { -1.0f, 0.6f, -1.8f, -1.0f, 0.9f, 1.0f },
        { -0.2f, -0.2f, -0.1f, 2.0f, -1.7f, -0.3f, 1.7f },
        { 0.1f, -0.1f, -0.6f, 1.3f, 0.4f, 0.3f, -0.7f, 0.6f },
        { -0.1f, 0.1f, -0.6f, 0.2f, -0.2f, 0.3f, 0.3f, -0.6f, 0.2f },
        { 0.0f, -0.1f, 0.0f, 0.3f, -0.4f, -0.3f, 0.1f, -0.1f, -0.4f, -0.2f },
        { 0.0f, 0.0f, -0.1f, 0.2f, 0.0f, -0.1f, -0.2f, 0.0f, -0.1f, -0.2f, -0.2f },
        { 0.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f, 0.0f },
        { 0.0f, 0.0f, 0.1f, 0.1f, -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f, 0.1f } };
    static private final float[][] DELTA_H = new float[][] {
        { 0.0f },
        { 0.0f, -25.9f },
        { 0.0f, -22.5f, -11.8f },
        { 0.0f, 7.3f, -3.9f, -2.6f },
        { 0.0f, 1.1f, 2.7f, 3.9f, -0.8f },
        { 0.0f, 0.4f, 1.8f, 1.2f, 4.0f, -0.6f },
        { 0.0f, -0.2f, -2.1f, -0.4f, -0.6f, 0.5f, 0.9f },
        { 0.0f, 0.7f, 0.3f, -0.1f, -0.1f, -0.8f, -0.3f, 0.3f },
        { 0.0f, -0.1f, 0.2f, 0.4f, 0.4f, 0.1f, -0.1f, 0.4f, 0.3f },
        { 0.0f, 0.0f, -0.2f, 0.0f, -0.1f, 0.1f, 0.0f, -0.2f, 0.3f, 0.2f },
        { 0.0f, 0.1f, -0.1f, 0.0f, -0.1f, -0.1f, 0.0f, -0.1f, -0.2f, 0.0f, -0.1f },
        { 0.0f, 0.0f, 0.1f, 0.0f, 0.1f, 0.0f, 0.1f, 0.0f, -0.1f, -0.1f, 0.0f, -0.1f },
        { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f } };
    static private final long BASE_TIME =
        new GregorianCalendar(2010, 1, 1).getTimeInMillis();
    static private final float[][] SCHMIDT_QUASI_NORM_FACTORS =
        computeSchmidtQuasiNormFactors(G_COEFF.length);
    public GeomagneticField(float gdLatitudeDeg,
                            float gdLongitudeDeg,
                            float altitudeMeters,
                            long timeMillis) {
        final int MAX_N = G_COEFF.length; 
        gdLatitudeDeg = Math.min(90.0f - 1e-5f,
                                 Math.max(-90.0f + 1e-5f, gdLatitudeDeg));
        computeGeocentricCoordinates(gdLatitudeDeg,
                                     gdLongitudeDeg,
                                     altitudeMeters);
        assert G_COEFF.length == H_COEFF.length;
        LegendreTable legendre =
            new LegendreTable(MAX_N - 1,
                              (float) (Math.PI / 2.0 - mGcLatitudeRad));
        float[] relativeRadiusPower = new float[MAX_N + 2];
        relativeRadiusPower[0] = 1.0f;
        relativeRadiusPower[1] = EARTH_REFERENCE_RADIUS_KM / mGcRadiusKm;
        for (int i = 2; i < relativeRadiusPower.length; ++i) {
            relativeRadiusPower[i] = relativeRadiusPower[i - 1] *
                relativeRadiusPower[1];
        }
        float[] sinMLon = new float[MAX_N];
        float[] cosMLon = new float[MAX_N];
        sinMLon[0] = 0.0f;
        cosMLon[0] = 1.0f;
        sinMLon[1] = (float) Math.sin(mGcLongitudeRad);
        cosMLon[1] = (float) Math.cos(mGcLongitudeRad);
        for (int m = 2; m < MAX_N; ++m) {
            int x = m >> 1;
            sinMLon[m] = sinMLon[m-x] * cosMLon[x] + cosMLon[m-x] * sinMLon[x];
            cosMLon[m] = cosMLon[m-x] * cosMLon[x] - sinMLon[m-x] * sinMLon[x];
        }
        float inverseCosLatitude = 1.0f / (float) Math.cos(mGcLatitudeRad);
        float yearsSinceBase =
            (timeMillis - BASE_TIME) / (365f * 24f * 60f * 60f * 1000f);
        float gcX = 0.0f;  
        float gcY = 0.0f;  
        float gcZ = 0.0f;  
        for (int n = 1; n < MAX_N; n++) {
            for (int m = 0; m <= n; m++) {
                float g = G_COEFF[n][m] + yearsSinceBase * DELTA_G[n][m];
                float h = H_COEFF[n][m] + yearsSinceBase * DELTA_H[n][m];
                gcX += relativeRadiusPower[n+2]
                    * (g * cosMLon[m] + h * sinMLon[m])
                    * legendre.mPDeriv[n][m]
                    * SCHMIDT_QUASI_NORM_FACTORS[n][m];
                gcY += relativeRadiusPower[n+2] * m
                    * (g * sinMLon[m] - h * cosMLon[m])
                    * legendre.mP[n][m]
                    * SCHMIDT_QUASI_NORM_FACTORS[n][m]
                    * inverseCosLatitude;
                gcZ -= (n + 1) * relativeRadiusPower[n+2]
                    * (g * cosMLon[m] + h * sinMLon[m])
                    * legendre.mP[n][m]
                    * SCHMIDT_QUASI_NORM_FACTORS[n][m];
            }
        }
        double latDiffRad = Math.toRadians(gdLatitudeDeg) - mGcLatitudeRad;
        mX = (float) (gcX * Math.cos(latDiffRad)
                      + gcZ * Math.sin(latDiffRad));
        mY = gcY;
        mZ = (float) (- gcX * Math.sin(latDiffRad)
                      + gcZ * Math.cos(latDiffRad));
    }
    public float getX() {
        return mX;
    }
    public float getY() {
        return mY;
    }
    public float getZ() {
        return mZ;
    }
    public float getDeclination() {
        return (float) Math.toDegrees(Math.atan2(mY, mX));
    }
    public float getInclination() {
        return (float) Math.toDegrees(Math.atan2(mZ,
                                                 getHorizontalStrength()));
    }
    public float getHorizontalStrength() {
        return (float) Math.sqrt(mX * mX + mY * mY);
    }
    public float getFieldStrength() {
        return (float) Math.sqrt(mX * mX + mY * mY + mZ * mZ);
    }
    private void computeGeocentricCoordinates(float gdLatitudeDeg,
                                              float gdLongitudeDeg,
                                              float altitudeMeters) {
        float altitudeKm = altitudeMeters / 1000.0f;
        float a2 = EARTH_SEMI_MAJOR_AXIS_KM * EARTH_SEMI_MAJOR_AXIS_KM;
        float b2 = EARTH_SEMI_MINOR_AXIS_KM * EARTH_SEMI_MINOR_AXIS_KM;
        double gdLatRad = Math.toRadians(gdLatitudeDeg);
        float clat = (float) Math.cos(gdLatRad);
        float slat = (float) Math.sin(gdLatRad);
        float tlat = slat / clat;
        float latRad =
            (float) Math.sqrt(a2 * clat * clat + b2 * slat * slat);
        mGcLatitudeRad = (float) Math.atan(tlat * (latRad * altitudeKm + b2)
                                           / (latRad * altitudeKm + a2));
        mGcLongitudeRad = (float) Math.toRadians(gdLongitudeDeg);
        float radSq = altitudeKm * altitudeKm
            + 2 * altitudeKm * (float) Math.sqrt(a2 * clat * clat +
                                                 b2 * slat * slat)
            + (a2 * a2 * clat * clat + b2 * b2 * slat * slat)
            / (a2 * clat * clat + b2 * slat * slat);
        mGcRadiusKm = (float) Math.sqrt(radSq);
    }
    static private class LegendreTable {
        public final float[][] mP;
        public final float[][] mPDeriv;
        public LegendreTable(int maxN, float thetaRad) {
            float cos = (float) Math.cos(thetaRad);
            float sin = (float) Math.sin(thetaRad);
            mP = new float[maxN + 1][];
            mPDeriv = new float[maxN + 1][];
            mP[0] = new float[] { 1.0f };
            mPDeriv[0] = new float[] { 0.0f };
            for (int n = 1; n <= maxN; n++) {
            	mP[n] = new float[n + 1];
                mPDeriv[n] = new float[n + 1];
                for (int m = 0; m <= n; m++) {
                    if (n == m) {
                        mP[n][m] = sin * mP[n - 1][m - 1];
                        mPDeriv[n][m] = cos * mP[n - 1][m - 1]
                            + sin * mPDeriv[n - 1][m - 1];
                    } else if (n == 1 || m == n - 1) {
                        mP[n][m] = cos * mP[n - 1][m];
                        mPDeriv[n][m] = -sin * mP[n - 1][m]
                            + cos * mPDeriv[n - 1][m];
                    } else {
                        assert n > 1 && m < n - 1;
                        float k = ((n - 1) * (n - 1) - m * m)
                            / (float) ((2 * n - 1) * (2 * n - 3));
                        mP[n][m] = cos * mP[n - 1][m] - k * mP[n - 2][m];
                        mPDeriv[n][m] = -sin * mP[n - 1][m]
                            + cos * mPDeriv[n - 1][m] - k * mPDeriv[n - 2][m];
                    }
                }
            }
        }
    }
    private static float[][] computeSchmidtQuasiNormFactors(int maxN) {
        float[][] schmidtQuasiNorm = new float[maxN + 1][];
        schmidtQuasiNorm[0] = new float[] { 1.0f };
        for (int n = 1; n <= maxN; n++) {
            schmidtQuasiNorm[n] = new float[n + 1];
            schmidtQuasiNorm[n][0] =
                schmidtQuasiNorm[n - 1][0] * (2 * n - 1) / (float) n;
            for (int m = 1; m <= n; m++) {
                schmidtQuasiNorm[n][m] = schmidtQuasiNorm[n][m - 1]
                    * (float) Math.sqrt((n - m + 1) * (m == 1 ? 2 : 1)
                                / (float) (n + m));
            }
        }
        return schmidtQuasiNorm;
    }
}
