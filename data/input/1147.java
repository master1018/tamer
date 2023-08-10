public class XmlPermissionResources implements Permissions {
    public static final String XML_PERMISSION_ERRID = "permission.invalid_xml_format";
    public static final String PERMISSION_NOT_FOUND_ERRID = "permission.not_found";
    public static final String XML_URL_PROP = "permissions.xml_url";
    private static final Log LOG = LogFactory.getLog(XmlPermissionResources.class);
    private HashMap mPermissionsForPrincipal = new HashMap();
    private HashMap mWildcardPermissionsForPrincipal = new HashMap();
    private ResourceProvider resourceProvider;
    public XmlPermissionResources() {
    }
    public void setResourceProvider(ResourceProvider inProvider) {
        resourceProvider = inProvider;
        if (resourceProvider != null) {
            init();
        }
    }
    public boolean checkPermission(String resourceName) {
        Iterator principals = getSubject().getPrincipals().iterator();
        while (principals.hasNext()) {
            Principal p = (Principal) principals.next();
            if (checkPermission(resourceName, p.getName())) {
                return true;
            }
        }
        return false;
    }
    protected Subject getSubject() {
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (subject == null) {
            String m = "Subject could not be retrieved from AccessController.";
            LOG.error(m);
            throw new RuntimeException(m);
        }
        return subject;
    }
    protected boolean checkPermission(String resourceName, String principalName) {
        Set permissionSet = (Set) mPermissionsForPrincipal.get(principalName);
        if (permissionSet != null) {
            if (permissionSet.contains(resourceName)) {
                return true;
            }
        }
        permissionSet = (Set) mWildcardPermissionsForPrincipal.get(principalName);
        if (permissionSet != null) {
            Iterator iter = permissionSet.iterator();
            while (iter.hasNext()) {
                String p = (String) iter.next();
                if (p.equals("") || resourceName.startsWith(p)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void init() {
        Properties properties = resourceProvider.getProperties();
        String permissionsUrl = properties.getProperty(XML_URL_PROP, "permissions.xml");
        if (permissionsUrl.startsWith("/")) {
            permissionsUrl = permissionsUrl.substring(1);
        }
        try {
            InputStream in = resourceProvider.getInputStream(permissionsUrl, null, null);
            if (in == null) {
                String msg = "[" + PERMISSION_NOT_FOUND_ERRID + "] " + "Permissions XML file not found: " + permissionsUrl;
                LOG.warn(msg);
                throw new RuntimeException(msg);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document document = parser.parse(in);
            NodeList grants = document.getElementsByTagName("grant");
            int numGrants = grants.getLength();
            for (int i = 0; i < numGrants; i++) {
                Element grant = (Element) grants.item(i);
                String principal = grant.getAttribute("principal");
                if (principal == null || principal.equals("")) {
                    String m = "[" + XML_PERMISSION_ERRID + "] " + "Principal attribute not defined";
                    LOG.error(m);
                    throw new RuntimeException(m);
                }
                NodeList permissions = grant.getElementsByTagName("permission");
                int numPermissions = permissions.getLength();
                for (int j = 0; j < numPermissions; j++) {
                    Node permission = permissions.item(j);
                    if (permission != null) {
                        String permissionText = permission.getFirstChild().getNodeValue();
                        if (permissionText.endsWith("*")) {
                            Set permissionSet = (Set) mWildcardPermissionsForPrincipal.get(principal);
                            if (permissionSet == null) {
                                permissionSet = new HashSet();
                                mWildcardPermissionsForPrincipal.put(principal, permissionSet);
                            }
                            permissionSet.add(permissionText.substring(0, permissionText.length() - 1));
                        } else {
                            Set permissionSet = (Set) mPermissionsForPrincipal.get(principal);
                            if (permissionSet == null) {
                                permissionSet = new HashSet();
                                mPermissionsForPrincipal.put(principal, permissionSet);
                            }
                            permissionSet.add(permissionText);
                        }
                    }
                }
            }
            in.close();
        } catch (ParserConfigurationException e) {
            String m = "[" + XML_PERMISSION_ERRID + "] " + e.getMessage();
            LOG.error(m, e);
            throw new RuntimeException(m);
        } catch (SAXException e) {
            String m = "[" + XML_PERMISSION_ERRID + "] " + e.getMessage();
            LOG.error(m, e);
            throw new RuntimeException(m);
        } catch (IOException e) {
            String m = "[" + XML_PERMISSION_ERRID + "] " + e.getMessage();
            LOG.error(m, e);
            throw new RuntimeException(m);
        }
    }
}
