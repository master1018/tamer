    abstract class Factory
    {
        public static IItemMetadata getTypeMetadata (final int typeID)
        {
            if ((typeID < TYPE_ID_ALL) || (typeID > TYPE_ID_METHOD))
                throw new IllegalArgumentException ("invalid type ID: " + typeID);
            return METADATA [typeID];
        }
        public static IItemMetadata [] getAllTypes ()
        {
            return METADATA;
        }
        private Factory () {}
        private static final IItemMetadata [] METADATA; 
        static
        {
            METADATA = new IItemMetadata []
            {
                AllItem.getTypeMetadata (),
                PackageItem.getTypeMetadata (),
                SrcFileItem.getTypeMetadata (),
                ClassItem.getTypeMetadata (),
                MethodItem.getTypeMetadata (),
            };
        }
    } 
} 
