package com.dyuproject.protostuff;

import java.util.ArrayList;
import com.dyuproject.protostuff.StringSerializer.STRING;

/**
 * Json escape test
 *
 * @author David Yu
 * @created Oct 10, 2010
 */
public class JsonEscapeTest extends AbstractTest {

    static final String ESCAPE_TARGET = "a	\r\n\f\t\b\"\\";

    public void testFoo() throws Exception {
        Bar bar = new Bar();
        bar.setSomeInt(1);
        bar.setSomeBytes(ByteString.copyFromUtf8(ESCAPE_TARGET));
        bar.setSomeString(ESCAPE_TARGET);
        ArrayList<Bar> bars = new ArrayList<Bar>();
        bars.add(bar);
        bars.add(bar);
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(ESCAPE_TARGET);
        strings.add("");
        strings.add(ESCAPE_TARGET);
        Foo foo = new Foo();
        foo.setSomeBar(bars);
        foo.setSomeString(strings);
        byte[] protostuff = ProtostuffIOUtil.toByteArray(foo, foo.cachedSchema(), buf());
        byte[] json = JsonIOUtil.toByteArray(ProtostuffIOUtil.newPipe(protostuff), Foo.getPipeSchema(), false);
        byte[] json2 = JsonXIOUtil.toByteArray(ProtostuffIOUtil.newPipe(protostuff), Foo.getPipeSchema(), false, buf());
        assertTrue(json.length == json2.length);
        String strJson = STRING.deser(json);
        String strJson2 = STRING.deser(json2);
        assertEquals(strJson, strJson2);
        System.err.println(strJson);
        System.err.println(strJson2);
    }

    public void testBar() throws Exception {
        Bar bar = new Bar();
        bar.setSomeInt(1);
        bar.setSomeBytes(ByteString.copyFromUtf8(ESCAPE_TARGET));
        bar.setSomeString(ESCAPE_TARGET);
        byte[] protostuff = ProtostuffIOUtil.toByteArray(bar, bar.cachedSchema(), buf());
        byte[] json = JsonIOUtil.toByteArray(ProtostuffIOUtil.newPipe(protostuff), Bar.getPipeSchema(), false);
        byte[] json2 = JsonXIOUtil.toByteArray(ProtostuffIOUtil.newPipe(protostuff), Bar.getPipeSchema(), false, buf());
        assertTrue(json.length == json2.length);
        String strJson = STRING.deser(json);
        String strJson2 = STRING.deser(json2);
        assertEquals(strJson, strJson2);
        System.err.println(strJson);
        System.err.println(strJson2);
    }
}
