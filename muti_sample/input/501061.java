    abstract class _JREVersion
    {
        static final boolean _JRE_1_2_PLUS; 
        static final boolean _JRE_1_3_PLUS; 
        static final boolean _JRE_1_4_PLUS; 
        static final boolean _JRE_SUN_SIGNAL_COMPATIBLE; 
        private _JREVersion () {} 
        static
        {
            _JRE_1_2_PLUS = ((SecurityManager.class.getModifiers () & 0x0400) == 0);
            boolean temp = false;            
            if (_JRE_1_2_PLUS)
            {
                try
                {
                    StrictMath.abs (1.0);
                    temp = true;
                }
                catch (Error ignore) {}
            }
            _JRE_1_3_PLUS = temp;
            if (temp)
            {
                temp = false;
                try
                {
                    " ".subSequence (0, 0);
                    temp = true;
                }
                catch (NoSuchMethodError ignore) {}
            }
            _JRE_1_4_PLUS = temp;
            temp = false;
            _JRE_SUN_SIGNAL_COMPATIBLE = temp;
        }
    } 
} 
