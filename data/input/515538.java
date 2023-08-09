    abstract class Factory
    {
        public static ItemComparator create (final int [] attributeIDsWithDir, final int unitsID)
        {
            if (attributeIDsWithDir == null)
                throw new IllegalArgumentException ("null input: attributeIDsWithDir");
            if (attributeIDsWithDir.length == 0)
                return NULL_COMPARATOR;
            final Comparator [] comparators = new Comparator [attributeIDsWithDir.length >> 1];
            for (int a = 0; a < attributeIDsWithDir.length; a += 2)
            {
                final int attributeID = attributeIDsWithDir [a];
                final Comparator comparator = IItemAttribute.Factory.getAttribute (attributeID, unitsID).comparator ();
                comparators [a >> 1] = attributeIDsWithDir [a + 1] < 0 ? new ReverseComparator (comparator) : comparator;
            }
            return new CompositeComparator (comparators);
        }
        private static final class NullComparator implements ItemComparator
        {
            public int compare (final Object l, final Object g)
            {
                return 0; 
            }
        } 
        private static final class ReverseComparator implements ItemComparator
        {
            public int compare (final Object l, final Object g)
            {
                return m_comparator.compare (g, l); 
            }
            ReverseComparator (final Comparator comparator)
            {
                m_comparator = comparator;
            }
            private final Comparator m_comparator;
        } 
        private static final class CompositeComparator implements ItemComparator
        {
            public int compare (final Object l, final Object g)
            {
                for (int c = 0; c < m_comparators.length; ++ c)
                {
                    final int diff = m_comparators [c].compare (l, g);
                    if (diff != 0) return diff;
                }
                return 0;
            }
            CompositeComparator (final Comparator [] comparators)
            {
                m_comparators = comparators;
            }
            private final Comparator [] m_comparators;
        } 
    } 
} 
