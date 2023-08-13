    abstract class _ANTVersion
    {
        static final boolean _ANT_1_5_PLUS; 
        private _ANTVersion () {   }
        static
        {
            boolean temp = true;
            try
            {
                final Method m = FileSet.class.getMethod ("setFile", new Class [] { File.class });
                final int modifiers = m.getModifiers ();
                if ((modifiers & Modifier.STATIC) != 0)
                    temp = false;
            }
            catch (NoSuchMethodException nsme)
            {
                temp = false;
            }
            catch (SecurityException se)
            {
                temp = false;
            }
            catch (Throwable t)
            {
                t.printStackTrace (System.out);
                temp = false;
            }
            _ANT_1_5_PLUS = temp;
        }
    } 
} 
