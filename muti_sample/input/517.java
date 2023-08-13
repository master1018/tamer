public class BufferClassLoader extends SecureClassLoader
{
    private final NamedBuffer[] fBuffers;
    public
    BufferClassLoader(  ClassLoader     parent,
                        NamedBuffer[]   buffers)
        {
        super(parent);
        fBuffers = buffers;     
        }
    protected Class
    findClass(String name)
        throws ClassNotFoundException
        {
        for ( int x = 0; x < fBuffers.length; x++ )
            {
            if ( fBuffers[x].getName().equals(name) )
                {
                byte[] buffer = fBuffers[x].getBuffer();
                return defineClass( name,
                                    buffer,
                                    0,
                                    buffer.length,
                                    (CodeSource) null);
                }
            }
        throw new ClassNotFoundException(name);
        }
}
