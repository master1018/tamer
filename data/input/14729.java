class T5078378 {
    public static boolean contains(List l, Object o) {
        return Collections.binarySearch(l, o) > -1;
    }
}
