#include <iostream>
using namespace std;
typedef unsigned long long int ll;
#define P (571373)
ll tree[100005 * 4] = {0};
ll lazy[100005 * 4] = {0};
ll mullLz[100005 * 4] = {0};
ll type;

void build(ll l, ll r, ll pos, ll val)
{
    if (l == r)
    {
        // 建树的时候，叶子节点的取值
        //tree[pos] = val;
        cin >> tree[pos];
        return;
    }
    ll mid = (l + r) / 2;
    build(l, mid, 2 * pos, val); // 左孩子
    build(mid + 1, r, 2 * pos + 1, val); // 右孩子
    tree[pos] = tree[2 * pos] + tree[2 * pos + 1];
}

void update(ll l, ll r, ll idx, ll pos, ll val)
{
    if (l == r)
    {
        // 更新叶子结点的方式
        // 可以根据题目进行其他更改
        tree[pos] += val;
        return;
    }
    ll mid = (l + r) / 2;
    // 左孩子递归
    if (idx <= mid) update(l, mid, idx, 2 * pos, val);
    // 右孩子递归
    else update(mid + 1, r, idx, 2 * pos + 1, val);

    tree[pos] = tree[2 * pos] + tree[2 * pos + 1];
}

ll query(ll l, ll r, ll x, ll y, ll pos)
{
    if (l >= x && r <= y)
        return tree[pos];
    ll mid = (l + r) / 2;
    ll sum = 0;
    if (mid >= x) sum += query(l, mid, x, y, 2 * pos);
    if (mid < y) sum += query(mid + 1, r, x, y, 2* pos + 1);
    return sum;
}

void push_down(ll l, ll r, ll pos)
{
    if (lazy[pos])
    {
        lazy[2 * pos] += lazy[pos];
        lazy[2 * pos + 1] += lazy[pos];
        
        ll mid = (l + r) / 2;
        tree[2 * pos] += (mid - l + 1) * lazy[pos];
        tree[2 * pos + 1] += (r - mid) * lazy[pos];
        lazy[pos] = 0; 
    }
}

void update_range(ll l, ll r, ll x, ll y, ll pos, ll val)
{
    if (x <= l && r <= y)
    {
        lazy[pos] += val;
        tree[pos] += (r - l + 1) * val;
        return ;
    }

    push_down(l, r, pos);
    ll mid = (l + r) / 2;
    if (mid >= x) update_range(l, mid, x, y, 2 * pos, val);
    if (mid < y) update_range(mid + 1, r, x, y, 2 * pos + 1, val);
    tree[pos] = tree[2 * pos] + tree[2 * pos + 1];
}

ll query_range(ll l, ll r, ll x, ll y, ll pos)
{
    if (x <= l && r <= y) return tree[pos];
    push_down(l, r, pos);
    ll mid = (l + r) / 2;
    ll sum = 0;
    if (mid >= x) sum += query_range(l, mid, x, y, 2 * pos);
    if (mid < y) sum += query_range(mid + 1, r, x, y, 2 * pos + 1);
    return sum;
}

int main()
{
    ll n, m;
    cin >> n >> m;
    build(1, n, 1, 0);
    for (ll i = 1; i <= m; i++)
    {
        ll op;
        cin >> op;
        if (op == 1)
        {
            ll x, y, k;
            cin >> x >> y >> k;
            update_range(1, n, x, y, 1, k);
        }
        else
        {
            ll x, y;
            cin >> x >> y;
            cout << query_range(1, n, x, y, 1) << endl;
        }
    }
}