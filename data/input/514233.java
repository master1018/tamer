    abstract class Factory
    {
        public static IOptsParser create (final String metadataResourceName, final ClassLoader loader,
                                          final String msgPrefix, final String [] usageOpts)
        {
            return new OptsParser (metadataResourceName, loader, msgPrefix, usageOpts);
        }
    } 
} 
