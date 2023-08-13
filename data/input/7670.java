final public class MWMConstants {
    private MWMConstants(){}
    static final int MWM_HINTS_FUNCTIONS=       (1  << 0);
    static final int MWM_HINTS_DECORATIONS=     (1  << 1);
    static final int MWM_HINTS_INPUT_MODE=      (1  << 2);
    static final int MWM_HINTS_STATUS=          (1  << 3);
    static final int MWM_FUNC_ALL=              (1  << 0);
    static final int MWM_FUNC_RESIZE=           (1  << 1);
    static final int MWM_FUNC_MOVE=             (1  << 2);
    static final int MWM_FUNC_MINIMIZE=         (1  << 3);
    static final int MWM_FUNC_MAXIMIZE=         (1  << 4);
    static final int MWM_FUNC_CLOSE=            (1  << 5);
    static final int MWM_DECOR_ALL=             (1  << 0);
    static final int MWM_DECOR_BORDER=          (1  << 1);
    static final int MWM_DECOR_RESIZEH=         (1  << 2);
    static final int MWM_DECOR_TITLE  =         (1  << 3);
    static final int MWM_DECOR_MENU     =       (1  << 4);
    static final int MWM_DECOR_MINIMIZE=        (1  << 5);
    static final int MWM_DECOR_MAXIMIZE=        (1  << 6);
    static final int MWM_INPUT_MODELESS                 =0;
    static final int MWM_INPUT_PRIMARY_APPLICATION_MODAL=1;
    static final int MWM_INPUT_SYSTEM_MODAL             =2;
    static final int MWM_INPUT_FULL_APPLICATION_MODAL   =3;
    static final int PROP_MWM_HINTS_ELEMENTS          = 5;
    final static int PROP_MOTIF_WM_INFO_ELEMENTS=       2;
    final static int PROP_MWM_INFO_ELEMENTS=            PROP_MOTIF_WM_INFO_ELEMENTS;
    static final String MWM_HINTS_ATOM_NAME = "_MOTIF_WM_HINTS";
}
