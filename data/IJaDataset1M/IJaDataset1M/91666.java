package org.eclipse.babel.runtime.pluginXmlParsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.babel.runtime.external.ITranslatableText;
import org.eclipse.babel.runtime.external.NonTranslatableText;
import org.eclipse.babel.runtime.external.TranslatableResourceBundle;
import org.eclipse.babel.runtime.external.TranslatableText;
import org.eclipse.core.internal.registry.osgi.EclipseBundleListener;
import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.internal.runtime.DevClassPathHelper;
import org.eclipse.core.internal.runtime.ResourceTranslator;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PluginXmlRegistry {

    private static PluginXmlRegistry theInstance = new PluginXmlRegistry();

    private Map<Bundle, LocalizableContribution> registry = new HashMap<Bundle, LocalizableContribution>();

    private SAXParserFactory theXMLParserFactory = null;

    public static PluginXmlRegistry getInstance() {
        return theInstance;
    }

    static ITranslatableText translate(TranslatableResourceBundle translationBundle, String originalText) {
        String trimmedText = originalText.trim();
        if (trimmedText.length() == 0) return new NonTranslatableText(trimmedText);
        if (trimmedText.charAt(0) != '%') return new NonTranslatableText(trimmedText);
        return new TranslatableText(translationBundle, trimmedText.substring(1));
    }

    /**
	 * This method is copied from EclipseBundleListener.addBundle.
	 * 
	 * @param osgiBundle
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
    public LocalizableContribution getLocalizableContribution(Bundle osgiBundle) throws ParserConfigurationException, SAXException, IOException {
        LocalizableContribution contribution = registry.get(osgiBundle);
        if (contribution != null) {
            return contribution;
        }
        String bundleResourceFile = (String) osgiBundle.getHeaders().get(Constants.BUNDLE_LOCALIZATION);
        if (bundleResourceFile == null) {
            bundleResourceFile = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
        }
        String[] nlVarients = buildNLVariants(Locale.getDefault().toString());
        PropertyResourceBundle resourceBundle = null;
        for (int i = nlVarients.length - 1; i >= 0; i--) {
            String fullPath = bundleResourceFile + ".properties" + (nlVarients[i].equals("") ? nlVarients[i] : '_' + nlVarients[i]);
            URL varientURL = osgiBundle.getEntry(bundleResourceFile + ".properties" + (nlVarients[i].equals("") ? nlVarients[i] : '_' + nlVarients[i]));
            if (varientURL == null) {
                break;
            }
            InputStream resourceStream = null;
            try {
                resourceStream = varientURL.openStream();
                resourceBundle = new MyPropertyResourceBundle(resourceStream, resourceBundle);
            } catch (IOException e) {
            } finally {
                if (resourceStream != null) {
                    try {
                        resourceStream.close();
                    } catch (IOException e3) {
                    }
                }
            }
        }
        TranslatableResourceBundle translationBundle = null;
        try {
            ResourceBundle immutableTranslationBundle = ResourceTranslator.getResourceBundle(osgiBundle);
            translationBundle = TranslatableResourceBundle.get(osgiBundle, createTempClassloader(osgiBundle), bundleResourceFile);
        } catch (MissingResourceException e) {
        }
        contribution = new LocalizableContribution(osgiBundle.getSymbolicName(), translationBundle);
        registry.put(osgiBundle, contribution);
        URL pluginManifest = EclipseBundleListener.getExtensionURL(osgiBundle, false);
        if (pluginManifest == null) return contribution;
        InputStream is;
        try {
            is = new BufferedInputStream(pluginManifest.openStream());
        } catch (IOException ex) {
            is = null;
        }
        if (is == null) return contribution;
        ExtensionsParser parser = new ExtensionsParser(this);
        try {
            parser.parseManifest(getXMLParser(), new InputSource(is), contribution, translationBundle);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
            }
        }
        return contribution;
    }

    /**
	 * Returns the parser used by the registry to parse descriptions of extension points and extensions.
	 * This method must not return <code>null</code>.
	 *
	 * Copied from RegistryStrategy.getXMLParser
	 */
    public SAXParserFactory getXMLParser() {
        if (theXMLParserFactory == null) theXMLParserFactory = SAXParserFactory.newInstance();
        return theXMLParserFactory;
    }

    private String[] buildNLVariants(String nl) {
        ArrayList<String> result = new ArrayList<String>();
        int lastSeparator;
        while ((lastSeparator = nl.lastIndexOf('_')) != -1) {
            result.add(nl);
            if (lastSeparator != -1) {
                nl = nl.substring(0, lastSeparator);
            }
        }
        result.add(nl);
        result.add("");
        return (String[]) result.toArray(new String[result.size()]);
    }

    private static ClassLoader createTempClassloader(Bundle b) {
        ArrayList<URL> classpath = new ArrayList<URL>();
        addClasspathEntries(b, classpath);
        addBundleRoot(b, classpath);
        addDevEntries(b, classpath);
        addFragments(b, classpath);
        URL[] urls = new URL[classpath.size()];
        return new URLClassLoader(classpath.toArray(urls));
    }

    private static void addFragments(Bundle host, ArrayList<URL> classpath) {
        Activator activator = Activator.getDefault();
        if (activator == null) return;
        Bundle[] fragments = activator.getFragments(host);
        if (fragments == null) return;
        for (int i = 0; i < fragments.length; i++) {
            addClasspathEntries(fragments[i], classpath);
            addDevEntries(fragments[i], classpath);
        }
    }

    private static void addClasspathEntries(Bundle b, ArrayList<URL> classpath) {
        ManifestElement[] classpathElements;
        try {
            classpathElements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, (String) b.getHeaders("").get(Constants.BUNDLE_CLASSPATH));
            if (classpathElements == null) return;
            for (int i = 0; i < classpathElements.length; i++) {
                URL classpathEntry = b.getEntry(classpathElements[i].getValue());
                if (classpathEntry != null) classpath.add(classpathEntry);
            }
        } catch (BundleException e) {
        }
    }

    private static void addBundleRoot(Bundle b, ArrayList<URL> classpath) {
        classpath.add(b.getEntry("/"));
    }

    private static void addDevEntries(Bundle b, ArrayList<URL> classpath) {
        if (!DevClassPathHelper.inDevelopmentMode()) return;
        String[] binaryPaths = DevClassPathHelper.getDevClassPath(b.getSymbolicName());
        for (int i = 0; i < binaryPaths.length; i++) {
            URL classpathEntry = b.getEntry(binaryPaths[i]);
            if (classpathEntry != null) classpath.add(classpathEntry);
        }
    }
}
