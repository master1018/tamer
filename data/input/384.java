public class MiniMeeRegistry {
    private List<PreservationActionService> services = new ArrayList<PreservationActionService>();
    public MiniMeeRegistry() {
    }
    public List<PreservationActionService> findServices(FormatInfo sourceFormat, FormatInfo targetFormat) {
        ArrayList<PreservationActionService> matching = new ArrayList<PreservationActionService>();
        for (PreservationActionService service : services) {
            boolean doesSourceMatch = false;
            for (FormatInfo info : service.getSourceFormats()) {
                if (doMatch(sourceFormat, info)) doesSourceMatch = true;
            }
            if (doesSourceMatch) {
                if (doMatch(targetFormat, service.getTargetFormat())) matching.add(service);
            }
        }
        return matching;
    }
    private boolean doMatch(FormatInfo srcFormat, FormatInfo regInfo) {
        if (srcFormat == null || regInfo == null) {
            return true;
        }
        return (isEmptyOrNull(srcFormat.getPuid()) || isEmptyOrNull(regInfo.getPuid()) || srcFormat.getPuid().equals(regInfo.getPuid())) && (isEmptyOrNull(srcFormat.getName()) || isEmptyOrNull(regInfo.getName()) || regInfo.getName().toUpperCase().contains(srcFormat.getName().toUpperCase()) || srcFormat.getName().toUpperCase().contains(regInfo.getName().toUpperCase())) && (isEmptyOrNull(srcFormat.getDefaultExtension()) || isEmptyOrNull(regInfo.getDefaultExtension()) || srcFormat.getDefaultExtension().toUpperCase().equals(regInfo.getDefaultExtension().toUpperCase()));
    }
    private static boolean isEmptyOrNull(String value) {
        return (value == null) || ("".equals(value));
    }
    public void reloadFrom(String configFile) throws PlatoServiceException {
        Digester d = new Digester();
        d.setValidating(false);
        StrictErrorHandler errorHandler = new StrictErrorHandler();
        d.setErrorHandler(errorHandler);
        d.setClassLoader(PreservationActionServiceFactory.class.getClassLoader());
        services.clear();
        d.push(services);
        d.addFactoryCreate("*/preservationActionService", PreservationActionServiceFactory.class);
        d.addSetNext("*/preservationActionService", "add");
        d.addCallMethod("*/preservationActionService/name", "setName", 0);
        d.addCallMethod("*/preservationActionService/description", "setDescription", 0);
        d.addCallMethod("*/preservationActionService/descriptor", "setDescriptor", 0);
        d.addObjectCreate("*/preservationActionService/sourceFormats", ArrayList.class);
        d.addSetNext("*/preservationActionService/sourceFormats", "setSourceFormats");
        d.addObjectCreate("*/preservationActionService/sourceFormats/format", FormatInfo.class);
        d.addBeanPropertySetter("*/format/puid", "puid");
        d.addBeanPropertySetter("*/format/name", "name");
        d.addBeanPropertySetter("*/format/extension", "defaultExtension");
        d.addSetNext("*/preservationActionService/sourceFormats/format", "add");
        d.addObjectCreate("*/preservationActionService/targetFormat", FormatInfo.class);
        d.addBeanPropertySetter("*/targetFormat/puid", "puid");
        d.addBeanPropertySetter("*/targetFormat/name", "name");
        d.addBeanPropertySetter("*/targetFormat/extension", "defaultExtension");
        d.addSetNext("*/preservationActionService/targetFormat", "setTargetFormat");
        d.addCallMethod("*/preservationActionService/url", "setUrl", 0);
        d.addObjectCreate("*/preservationActionService/externalInfo", ArrayList.class);
        d.addSetNext("*/preservationActionService/externalInfo", "setExternalInfo");
        d.addCallMethod("*/preservationActionService/externalInfo/url", "add", 0);
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            d.parse(config);
        } catch (IOException e) {
            throw new PlatoServiceException("Could not read registry data.", e);
        } catch (SAXException e) {
            throw new PlatoServiceException("Could not read registry data.", e);
        }
    }
    public void reload() throws PlatoServiceException {
        String configFile = "data/services/miniMEE-actions.xml";
        reloadFrom(configFile);
    }
    public String getToolIdentifier(String url) {
        ToolRegistry toolRegistry = ToolRegistry.getInstance();
        ToolConfig toolConfig = toolRegistry.getToolConfig(ToolRegistry.getToolKey(url));
        if (toolConfig == null) {
            return "";
        }
        return toolConfig.getTool().getIdentifier();
    }
    public String getToolParameters(String url) {
        ToolRegistry toolRegistry = ToolRegistry.getInstance();
        ToolConfig toolConfig = toolRegistry.getToolConfig(ToolRegistry.getToolKey(url));
        return toolConfig.getParams();
    }
    public static void main(String[] args) {
        try {
            MiniMeeRegistry registry = new MiniMeeRegistry();
            registry.reloadFrom(args[0]);
            List<PreservationActionService> services = registry.findServices(null, null);
            for (PreservationActionService service : services) {
                System.out.println(service.getName());
            }
        } catch (PlatoServiceException e) {
            e.printStackTrace();
        }
    }
}
