public final class BingMapsApiUrls {
    public static final String API_URLS_FILE = "GoogleMapsApiUrls.properties";
    private static final Logger LOG = Logger.getLogger(BingMapsApiUrls.class.getCanonicalName());
    private static final Properties googleApiUrls = new Properties();
    static {
        try {
            googleApiUrls.load(BingMapsApiUrls.class.getResourceAsStream(API_URLS_FILE));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "An error occurred while loading urls.", e);
        }
    }
    public static final String GEOCODE_URL = googleApiUrls.getProperty("com.googleapis.maps.services.geocode");
    public static final String DIRECTIONS_URL = googleApiUrls.getProperty("com.googleapis.maps.services.directions");
    public static final String ELEVATION_URL = googleApiUrls.getProperty("com.googleapis.maps.services.elevation");
    public static final String PLACE_URL = googleApiUrls.getProperty("com.googleapis.maps.services.place");
    private BingMapsApiUrls() {
    }
    public static class GoogleMapsApiUrlBuilder {
        private static final char API_URLS_PLACEHOLDER_START = '{';
        private static final char API_URLS_PLACEHOLDER_END = '}';
        private static final String QUERY_PARAMETERS_PLACEHOLDER = "queryParameters";
        private String urlFormat;
        private Map<String, Collection<String>> parametersMap = new HashMap<String, Collection<String>>();
        public GoogleMapsApiUrlBuilder(String urlFormat) {
            this(urlFormat, ApplicationConstants.DEFAULT_API_VERSION);
        }
        public GoogleMapsApiUrlBuilder(String urlFormat, String apiVersion) {
            this.urlFormat = urlFormat;
        }
        public GoogleMapsApiUrlBuilder withParameter(String name, String value) {
            if (value != null && value.length() > 0) {
                Collection<String> values = parametersMap.get(name);
                if (values == null) {
                    values = new ArrayList<String>();
                    parametersMap.put(name, values);
                }
                values.add(encodeUrl(value));
            }
            return this;
        }
        public GoogleMapsApiUrlBuilder withParameterSuffix(String name, String suffix) {
            if (suffix != null && suffix.length() > 0) {
                Collection<String> values = parametersMap.get(name);
                if (values != null) {
                    List<String> updatedValues = new ArrayList<String>(values.size());
                    for (String value : values) {
                        updatedValues.add(encodeUrl(suffix) + value);
                    }
                    parametersMap.put(name, updatedValues);
                }
            }
            return this;
        }
        public GoogleMapsApiUrlBuilder withParameters(String name, Collection<String> values) {
            List<String> encodedValues = new ArrayList<String>(values.size());
            for (String value : values) {
                encodedValues.add(encodeUrl(value));
            }
            parametersMap.put(name, encodedValues);
            return this;
        }
        public GoogleMapsApiUrlBuilder withParameterEnumSet(String name, Set<? extends ValueEnum> enumSet) {
            Set<String> values = new HashSet<String>(enumSet.size());
            for (ValueEnum fieldEnum : enumSet) {
                values.add(encodeUrl(fieldEnum.value()));
            }
            parametersMap.put(name, values);
            return this;
        }
        public GoogleMapsApiUrlBuilder withParameterEnum(String name, ValueEnum value) {
            withParameter(name, value.value());
            return this;
        }
        public GoogleMapsApiUrlBuilder withParameterEnumMap(Map<? extends ValueEnum, String> enumMap) {
            for (ValueEnum parameter : enumMap.keySet()) {
                withParameter(parameter.value(), enumMap.get(parameter));
            }
            return this;
        }
        public String buildUrl() {
            StringBuilder urlBuilder = new StringBuilder();
            StringBuilder placeHolderBuilder = new StringBuilder();
            boolean placeHolderFlag = false;
            for (int i = 0; i < urlFormat.length(); i++) {
                if (urlFormat.charAt(i) == API_URLS_PLACEHOLDER_START) {
                    placeHolderBuilder = new StringBuilder();
                    placeHolderFlag = true;
                } else if (placeHolderFlag && urlFormat.charAt(i) == API_URLS_PLACEHOLDER_END) {
                    String placeHolder = placeHolderBuilder.toString();
                    if (QUERY_PARAMETERS_PLACEHOLDER.equals(placeHolder)) {
                        StringBuilder builder = new StringBuilder();
                        if (!parametersMap.isEmpty()) {
                            Iterator<String> iter = parametersMap.keySet().iterator();
                            while (iter.hasNext()) {
                                String name = iter.next();
                                Collection<String> parameterValues = parametersMap.get(name);
                                Iterator<String> iterParam = parameterValues.iterator();
                                while (iterParam.hasNext()) {
                                    builder.append(name);
                                    builder.append("=");
                                    builder.append(iterParam.next());
                                    if (iterParam.hasNext()) {
                                        builder.append("&");
                                    }
                                }
                                if (iter.hasNext()) {
                                    builder.append("&");
                                }
                            }
                        }
                        urlBuilder.append(builder.toString());
                    } else {
                        urlBuilder.append(API_URLS_PLACEHOLDER_START);
                        urlBuilder.append(placeHolder);
                        urlBuilder.append(API_URLS_PLACEHOLDER_END);
                    }
                    placeHolderFlag = false;
                } else if (placeHolderFlag) {
                    placeHolderBuilder.append(urlFormat.charAt(i));
                } else {
                    urlBuilder.append(urlFormat.charAt(i));
                }
            }
            return urlBuilder.toString();
        }
        private static String encodeUrl(String original) {
            try {
                return URLEncoder.encode(original, ApplicationConstants.CONTENT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return original;
            }
        }
    }
}
