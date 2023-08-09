public class B5086147 {
    public static final void main( String[] aaParamters ) throws Exception{
        File file = new File( "\\\\somehost\\someshare\\somefile.ext" );
        URI uri = file.toURI();
        if (!(uri.toURL().toURI().equals(uri))) {
            throw new RuntimeException("Test failed : (uri.toURL().toURI().equals(uri)) isn't hold");
        }
    }
}
