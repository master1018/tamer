    public static class Standard
    implements TabStopSpan
    {
        public Standard(int where) {
            mTab = where;
        }
        public int getTabStop() {
            return mTab;
        }
        private int mTab;
    }
}
