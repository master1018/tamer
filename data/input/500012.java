class Solution {
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> ans = new ArrayList<>();
        int n = words.length;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < n; ) {
            list.clear();
            list.add(words[i]);
            int cur = words[i++].length();
            while (i < n && cur + 1 + words[i].length() <= maxWidth) {
                cur += 1 + words[i].length();
                list.add(words[i++]);
            }
            if (i == n) {
                StringBuilder sb = new StringBuilder(list.get(0));
                for (int k = 1; k < list.size(); k++) {
                    sb.append(" ").append(list.get(k));
                }
                while (sb.length() < maxWidth) sb.append(" ");
                ans.add(sb.toString());
                break;
            }
            int cnt = list.size();
            if (cnt == 1) {
                String str = list.get(0);
                while (str.length() != maxWidth) str += " ";
                ans.add(str);
                continue;
            }
            int wordWidth = cur - (cnt - 1);
            int spaceWidth = maxWidth - wordWidth;
            int spaceItemWidth = spaceWidth / (cnt - 1);
            String spaceItem = "";
            for (int k = 0; k < spaceItemWidth; k++) spaceItem += " ";
            StringBuilder sb = new StringBuilder();
            for (int k = 0, sum = 0; k < cnt; k++) {
                String item = list.get(k);
                sb.append(item);
                if (k == cnt - 1) break;
                sb.append(spaceItem);
                sum += spaceItemWidth;
                int remain = cnt - k - 1 - 1;
                if (remain * spaceItemWidth + sum < spaceWidth) {
                    sb.append(" ");
                    sum++;
                }
            }
            ans.add(sb.toString());
        }
        return ans;
    }
}
