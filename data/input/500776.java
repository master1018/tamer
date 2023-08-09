final class ClassPathCacheEntry
{
    public final byte [] m_bytes;
    public final String m_srcURL; 
    public ClassPathCacheEntry (final byte [] bytes, final String srcURL)
    {
        if ($assert.ENABLED)
        {
            $assert.ASSERT (bytes != null, "bytes = null");
            $assert.ASSERT (srcURL != null, "srcURL = null");
        }
        m_bytes = bytes;
        m_srcURL = srcURL;
    }
} 
