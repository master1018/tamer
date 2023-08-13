    abstract class Factory
    {
        public static IItemAttribute getAttribute (final int attributeID, final int unitsID)
        {
            if ($assert.ENABLED) $assert.ASSERT (attributeID >= ATTRIBUTE_NAME_ID && attributeID <= ATTRIBUTE_LINE_COVERAGE_ID, "invalid attribute ID: " + attributeID);
            if ($assert.ENABLED) $assert.ASSERT (unitsID >= UNITS_COUNT && unitsID <= UNITS_INSTR, "invalid units ID: " + unitsID);
            return ATTRIBUTES [unitsID][attributeID];
        }
        public static IItemAttribute [] getAttributes (final int unitsID)
        {
            if ($assert.ENABLED) $assert.ASSERT (unitsID >= UNITS_COUNT && unitsID <= UNITS_INSTR, "invalid units ID: " + unitsID);
            return (IItemAttribute []) ATTRIBUTES [unitsID].clone ();
        }
        private static abstract class Attribute implements IItemAttribute
        {
            public String getName ()
            {
                return m_name;
            }
             protected Attribute (final String name)
            {
                if (name == null) throw new IllegalArgumentException ("null input: name");
                m_name = name;
            }
            private final String m_name;
        } 
        private static final class NameAttribute extends Attribute
                                                 implements IItemAttribute
        {
            public Comparator comparator ()
            {
                return m_comparator;
            }  
            public void format (final IItem item, final StringBuffer appendTo)
            {
                appendTo.append (item.getName ());
            }
            public boolean passes (final IItem item, final int criterion)
            {
                return true; 
            }
            private static final class NameComparator implements Comparator
            {
                public int compare (final Object l, final Object g)
                {
                    final IItem il = (IItem) l;
                    final IItem ig = (IItem) g;
                    return il.getName ().compareTo (ig.getName ());
                }
            } 
            NameAttribute (final String name)
            {
                super (name);
                m_comparator = new NameComparator ();
            }
            private final Comparator m_comparator;
        } 
        private static final class FractionAttribute extends Attribute
                                                     implements IItemAttribute
        {
            public Comparator comparator ()
            {
                return m_comparator;
            }  
            public void format (final IItem item, final StringBuffer appendTo)
            {
                final int n = item.getAggregate (m_numeratorAggregateID);
                final double n_scaled = (double) n / m_scale;
                final int d = item.getAggregate (m_denominatorAggregateID);
                final int appendToStart = appendTo.length ();
                if (d == 0)
                    m_format.format (1.0F, appendTo, m_fieldPosition);
                else
                    m_format.format (n_scaled / d, appendTo, m_fieldPosition);
                final int iLimit = Math.max (1, 5 - appendTo.length () + appendToStart);
                for (int i = 0; i < iLimit; ++ i) appendTo.append (' ');
                appendTo.append ('(');
                m_nFormat.format (n_scaled, appendTo, m_fieldPosition);
                appendTo.append ('/');
                appendTo.append (d);
                appendTo.append (')');
            }
            public boolean passes (final IItem item, final int criterion)
            {
                final int n = item.getAggregate (m_numeratorAggregateID);
                final int d = item.getAggregate (m_denominatorAggregateID);
                return ((double) n) * IItem.PRECISION >=  ((double) d) * m_scale * criterion; 
            }
            private final class FractionComparator implements Comparator
            {
                public int compare (final Object l, final Object g)
                {
                    final IItem il = (IItem) l;
                    final IItem ig = (IItem) g;
                    final double nil = il.getAggregate (m_numeratorAggregateID);
                    final double dil = il.getAggregate (m_denominatorAggregateID);
                    final double nig = ig.getAggregate (m_numeratorAggregateID);
                    final double dig = ig.getAggregate (m_denominatorAggregateID);
                    final double diff = nil * dig - nig * dil; 
                    return diff > 0.0 ? +1 : (diff < 0.0 ? -1 : 0);
                }
            } 
            FractionAttribute (final String name, final int numeratorAggregateID, final int denominatorAggregateID, final int scale, final int nFractionDigits)
            {
                super (name);
                if ($assert.ENABLED) $assert.ASSERT (scale != 0, "scale: " + scale);
                m_numeratorAggregateID = numeratorAggregateID;
                m_denominatorAggregateID = denominatorAggregateID; 
                m_scale = scale;
                m_format = (DecimalFormat) NumberFormat.getPercentInstance (); 
                m_fieldPosition = new FieldPosition (DecimalFormat.INTEGER_FIELD);
                m_format.setMaximumFractionDigits (0);
                m_nFormat = (DecimalFormat) NumberFormat.getInstance (); 
                m_nFormat.setGroupingUsed (false);
                m_nFormat.setMaximumFractionDigits (nFractionDigits);
                m_comparator = new FractionComparator ();
            }
            final int m_numeratorAggregateID, m_denominatorAggregateID;
            private final int m_scale;
            private final DecimalFormat m_format, m_nFormat;
            private final FieldPosition m_fieldPosition;
            private final Comparator m_comparator; 
        } 
        private Factory () {}
        private static final IItemAttribute [][] ATTRIBUTES; 
        static
        {
            final IItemAttribute nameAttribute = new NameAttribute ("name");
            final IItemAttribute classCoverageAttribute = new FractionAttribute ("class, %", IItem.COVERAGE_CLASS_COUNT, IItem.TOTAL_CLASS_COUNT, 1, 0);
            final IItemAttribute methodCoverageAttribute = new FractionAttribute ("method, %", IItem.COVERAGE_METHOD_COUNT, IItem.TOTAL_METHOD_COUNT, 1, 0);
            ATTRIBUTES = new IItemAttribute [][]
            {
                {
                    nameAttribute,
                    classCoverageAttribute,
                    methodCoverageAttribute,
                    new FractionAttribute ("block, %", IItem.COVERAGE_BLOCK_COUNT, IItem.TOTAL_BLOCK_COUNT, 1, 0),
                    new FractionAttribute ("line, %", IItem.COVERAGE_LINE_COUNT, IItem.TOTAL_LINE_COUNT, IItem.PRECISION, 1),
                },
                {
                    nameAttribute,
                    classCoverageAttribute,
                    methodCoverageAttribute,
                    new FractionAttribute ("block, %", IItem.COVERAGE_BLOCK_INSTR, IItem.TOTAL_BLOCK_INSTR, 1, 0),
                    new FractionAttribute ("line, %", IItem.COVERAGE_LINE_INSTR, IItem.TOTAL_LINE_COUNT, IItem.PRECISION, 1),
                },
            };
        }
    } 
} 
