package org.codemonkey.javareflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.codemonkey.javareflection.ExternalClassLoader;
import org.junit.Test;

/**
 * Junit test for {@link ExternalClassLoader}.
 */
public class ExternalClassLoaderTest {

    /**
	 * Test for {@link ExternalClassLoader#ExternalClassLoader()}.
	 */
    @Test
    public void testConstructor() {
        new ExternalClassLoader();
    }

    /**
	 * Test for ExternalClassLoader setters/getters.
	 */
    @Test
    public void testSettersGetters() {
        ExternalClassLoader loader = new ExternalClassLoader();
        assertNull(loader.getBasepath());
        loader.setBasepath("test");
        assertEquals("test", loader.getBasepath());
    }
}
