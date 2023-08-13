final class UCharacterDirection implements UCharacterEnums.ECharacterDirection {
    private UCharacterDirection()
    {
    }
    public static String toString(int dir) {
        switch(dir)
            {
            case LEFT_TO_RIGHT :
                return "Left-to-Right";
            case RIGHT_TO_LEFT :
                return "Right-to-Left";
            case EUROPEAN_NUMBER :
                return "European Number";
            case EUROPEAN_NUMBER_SEPARATOR :
                return "European Number Separator";
            case EUROPEAN_NUMBER_TERMINATOR :
                return "European Number Terminator";
            case ARABIC_NUMBER :
                return "Arabic Number";
            case COMMON_NUMBER_SEPARATOR :
                return "Common Number Separator";
            case BLOCK_SEPARATOR :
                return "Paragraph Separator";
            case SEGMENT_SEPARATOR :
                return "Segment Separator";
            case WHITE_SPACE_NEUTRAL :
                return "Whitespace";
            case OTHER_NEUTRAL :
                return "Other Neutrals";
            case LEFT_TO_RIGHT_EMBEDDING :
                return "Left-to-Right Embedding";
            case LEFT_TO_RIGHT_OVERRIDE :
                return "Left-to-Right Override";
            case RIGHT_TO_LEFT_ARABIC :
                return "Right-to-Left Arabic";
            case RIGHT_TO_LEFT_EMBEDDING :
                return "Right-to-Left Embedding";
            case RIGHT_TO_LEFT_OVERRIDE :
                return "Right-to-Left Override";
            case POP_DIRECTIONAL_FORMAT :
                return "Pop Directional Format";
            case DIR_NON_SPACING_MARK :
                return "Non-Spacing Mark";
            case BOUNDARY_NEUTRAL :
                return "Boundary Neutral";
            }
        return "Unassigned";
    }
}
