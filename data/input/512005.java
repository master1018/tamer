public class StreamTokenizerTest extends TestCase {
    @MediumTest
    public void testStreamTokenizer() throws Exception {
        String str = "Testing 12345 \n alpha \r\n omega";
        String strb = "-3.8 'BLIND mice' \r sEe  they run";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(strb);
        StreamTokenizer a = new StreamTokenizer(aa);
        StreamTokenizer b = new StreamTokenizer(ba);
        assertEquals(1, a.lineno());
        assertEquals(StreamTokenizer.TT_WORD, a.nextToken());
        assertEquals("Token[Testing], line 1", a.toString());
        assertEquals(StreamTokenizer.TT_NUMBER, a.nextToken());
        assertEquals("Token[n=12345.0], line 1", a.toString());
        assertEquals(StreamTokenizer.TT_WORD, a.nextToken());
        assertEquals("Token[alpha], line 2", a.toString());
        assertEquals(StreamTokenizer.TT_WORD, a.nextToken());
        assertEquals("Token[omega], line 3", a.toString());
        assertEquals(StreamTokenizer.TT_EOF, a.nextToken());
        assertEquals("Token[EOF], line 3", a.toString());
        b.commentChar('u');
        b.eolIsSignificant(true);
        b.lowerCaseMode(true);
        b.ordinaryChar('y');
        b.slashStarComments(true);
        assertEquals(StreamTokenizer.TT_NUMBER, b.nextToken());
        assertEquals(-3.8, b.nval);
        assertEquals("Token[n=-3.8], line 1", b.toString());
        assertEquals(39, b.nextToken()); 
        assertEquals("Token[BLIND mice], line 1", b.toString());
        assertEquals(10, b.nextToken()); 
        assertEquals("Token[EOL], line 2", b.toString());
        assertEquals(StreamTokenizer.TT_WORD, b.nextToken());
        assertEquals("Token[see], line 2", b.toString());
        assertEquals(StreamTokenizer.TT_WORD, b.nextToken());
        assertEquals("Token[the], line 2", b.toString());
        assertEquals(121, b.nextToken()); 
        assertEquals("Token['y'], line 2", b.toString());
        assertEquals(StreamTokenizer.TT_WORD, b.nextToken());
        assertEquals("Token[r], line 2", b.toString());
        assertEquals(StreamTokenizer.TT_EOF, b.nextToken());
        assertEquals("Token[EOF], line 2", b.toString());
        byte[] data = new byte[]{(byte) '-'};
        StreamTokenizer tokenizer = new StreamTokenizer(new ByteArrayInputStream(data));
        tokenizer.nextToken();
        String result = tokenizer.toString();
        assertEquals("Token['-'], line 1", result);
        byte[] data2 = new byte[]{(byte) '"',
                (byte) 'H',
                (byte) 'e',
                (byte) 'l',
                (byte) 'l',
                (byte) 'o',
                (byte) '"'};
        StreamTokenizer tokenizer2 = new StreamTokenizer(new ByteArrayInputStream(data2));
        tokenizer2.nextToken();
        result = tokenizer2.toString();
        assertEquals("Token[Hello], line 1", result);
    }
}
