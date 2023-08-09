abstract class CoverageOptionsFactory
{
    public static CoverageOptions create (final Properties properties)
    {
        final boolean excludeSyntheticMethods =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_EXCLUDE_SYNTHETIC_METHODS,
                                                        InstrProcessor.DEFAULT_EXCLUDE_SYNTHETIC_METHODS));
        final boolean excludeBridgeMethods =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_EXCLUDE_BRIDGE_METHODS,
                                                        InstrProcessor.DEFAULT_EXCLUDE_BRIDGE_METHODS));
        final boolean doSUIDCompensaton =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_DO_SUID_COMPENSATION,
                                                        InstrProcessor.DEFAULT_DO_SUID_COMPENSATION));
        return new CoverageOptions (excludeSyntheticMethods, excludeBridgeMethods, doSUIDCompensaton);
    }
    public static CoverageOptions create (final IProperties properties)
    {
        final boolean excludeSyntheticMethods =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_EXCLUDE_SYNTHETIC_METHODS,
                                                        InstrProcessor.DEFAULT_EXCLUDE_SYNTHETIC_METHODS));
        final boolean excludeBridgeMethods =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_EXCLUDE_BRIDGE_METHODS,
                                                        InstrProcessor.DEFAULT_EXCLUDE_BRIDGE_METHODS));
        final boolean doSUIDCompensaton =
            Property.toBoolean (properties.getProperty (InstrProcessor.PROPERTY_DO_SUID_COMPENSATION,
                                                        InstrProcessor.DEFAULT_DO_SUID_COMPENSATION));
        return new CoverageOptions (excludeSyntheticMethods, excludeBridgeMethods, doSUIDCompensaton);
    }
    private CoverageOptionsFactory () {} 
} 
