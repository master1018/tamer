public class BinarySearch {
    public static int binarySearch(String data, char value) {
        int low = 0, high = data.length() - 1;
        while (low <= high) {
            int mid = (low + high) >> 1;
            char target = data.charAt(mid);
            if (value == target)
                return mid;
            else if (value < target)
                high = mid - 1;
            else
                low = mid + 1;
        }
        return -1;
    }
    public static int binarySearchRange(String data, char c) {
        char value = 0;
        int low = 0, mid = -1, high = data.length() - 1;
        while (low <= high) {
            mid = (low + high) >> 1;
            value = data.charAt(mid);
            if (c > value)
                low = mid + 1;
            else if (c == value)
                return mid;
            else
                high = mid - 1;
        }
        return mid - (c < value ? 1 : 0);
    }
}
