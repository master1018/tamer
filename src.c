#include <iostream>
#include <set>
using namespace std;

struct NODE {
    int k1;
    int k2;
    int m1;
    int m2;
};

set<NODE> arr;
bool operator<(const NODE &a, const NODE &b)
{
    return a.k1 < b.k1;
}
int main()
{
    for (int x1 = 0; x1 < 20; x1++)
        for (int y1 = 0; y1 < 21; y1++)
            for (int x2 = 0; x2 < 20; x2++)
                for (int y2 = 0; y2 < 21; y2++)
                {
                    if (x1 == x2 || y1 == y2) continue;
                    NODE node;
                    node.k1 = y2 - y1;
                    node.k2 = x2 - x1;
                    node.m1 = y1 * (x2 - x1) - x1 * (y2 - y1);
                    node.m2 = x2 - x1;
                    arr.insert(node);
                }
    cout << arr.size() + 20 + 21 << endl;
}