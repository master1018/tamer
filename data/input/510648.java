public class StateSet {
    public static final int[] WILD_CARD = new int[0];
    public static boolean isWildCard(int[] stateSetOrSpec) {
        return stateSetOrSpec.length == 0 || stateSetOrSpec[0] == 0;
    }
    public static boolean stateSetMatches(int[] stateSpec, int[] stateSet) {
        if (stateSet == null) {
            return (stateSpec == null || isWildCard(stateSpec));
        }
        int stateSpecSize = stateSpec.length;
        int stateSetSize = stateSet.length;
        for (int i = 0; i < stateSpecSize; i++) {
            int stateSpecState = stateSpec[i];
            if (stateSpecState == 0) {
                return true;
            }
            final boolean mustMatch;
            if (stateSpecState > 0) {
                mustMatch = true;
            } else {
                mustMatch = false;
                stateSpecState = -stateSpecState;
            }
            boolean found = false;
            for (int j = 0; j < stateSetSize; j++) {
                final int state = stateSet[j];
                if (state == 0) {
                    if (mustMatch) {
                        return false;
                    } else {
                        break;
                    }
                }
                if (state == stateSpecState) {
                    if (mustMatch) {
                        found = true;
                        break;
                    } else {
                        return false;
                    }
                }
            }
            if (mustMatch && !found) {
                return false;
            }
        }
        return true;
    }
    public static boolean stateSetMatches(int[] stateSpec, int state) {
        int stateSpecSize = stateSpec.length;
        for (int i = 0; i < stateSpecSize; i++) {
            int stateSpecState = stateSpec[i];
            if (stateSpecState == 0) {
                return true;
            }
            if (stateSpecState > 0) {
                if (state != stateSpecState) {
                   return false;
                }
            } else {
                if (state == -stateSpecState) {
                    return false;
                }
            }
        }
        return true;
    }
    public static int[] trimStateSet(int[] states, int newSize) {
        if (states.length == newSize) {
            return states;
        }
        int[] trimmedStates = new int[newSize];
        System.arraycopy(states, 0, trimmedStates, 0, newSize);
        return trimmedStates;
    }
    public static String dump(int[] states) {
        StringBuilder sb = new StringBuilder();
        int count = states.length;
        for (int i = 0; i < count; i++) {
            switch (states[i]) {
            case R.attr.state_window_focused:
                sb.append("W ");
                break;
            case R.attr.state_pressed:
                sb.append("P ");
                break;
            case R.attr.state_selected:
                sb.append("S ");
                break;
            case R.attr.state_focused:
                sb.append("F ");
                break;
            case R.attr.state_enabled:
                sb.append("E ");
                break;
            }
        }
        return sb.toString();
    }
}
