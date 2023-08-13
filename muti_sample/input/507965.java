public final class MockGeolocation {
    private static MockGeolocation sMockGeolocation;
    public void setPosition(double latitude, double longitude, double accuracy) {
        nativeSetPosition(latitude, longitude, accuracy);
    }
    public void setError(int code, String message) {
        nativeSetError(code, message);
    }
    public static MockGeolocation getInstance() {
      if (sMockGeolocation == null) {
          sMockGeolocation = new MockGeolocation();
      }
      return sMockGeolocation;
    }
    private static native void nativeSetPosition(double latitude, double longitude, double accuracy);
    private static native void nativeSetError(int code, String message);
}
