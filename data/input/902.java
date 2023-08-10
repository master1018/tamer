public final class ImageRegistry {
    private static Map<String, ImageProvider> providers = new LinkedHashMap<String, ImageProvider>();
    static {
        for (final ImageEncoding encoding : ImageEncoding.values()) {
            registerProvider(encoding.getMimeType(), encoding.getProvider());
        }
    }
    public static void registerProvider(final String mimeType, final ImageProvider decoder) {
        providers.put(mimeType, decoder);
    }
    public static ImageDecoder getImageProvider(final String mimeType) {
        if (providers.containsKey(mimeType)) {
            return providers.get(mimeType).newDecoder();
        } else {
            throw new IllegalArgumentException();
        }
    }
    private ImageRegistry() {
    }
}
