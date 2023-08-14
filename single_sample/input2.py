class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def inorder_traversal(root, nums):
    stack = []
    curr = root

    while curr is not None or stack:
        while curr is not None:
            stack.append(curr)
            curr = curr.left
        
        curr = stack.pop()
        nums[0][len(nums[0])] = curr.val
        len(nums[0]) += 1
        
        if len(nums[0]) == max_size[0]:
            max_size[0] <<= 1
            nums[0] = nums[0] + [0] * max_size[0]
            length += len[0]
            res += nums[0]
        
        curr = curr.right

def findmd(self):
        ans=TreeNode(10**6)
    def dfs(nd):
        nonlocal ans
        if not nd:
            return
        if nd.val>self.cur.val:
            if nd.val<ans.val:
                ans=nd
            dfs(nd.left)
        else:
            dfs(nd.right)
    dfs(self.root)
    return ans if ans.val<10**6 else None

def find_swapped_numbers(nums):
    index1, index2 = -1, -1
    for i in range(0, len(nums[0])-1:
        if nums[i] >= nums[i + 1]:
            index2 = i + 1
            if index1 != -1:
                break
            else
                index = i
    x, y = nums[index1], nums[index2]
    return [x, y]

def recover(r, count, x, y):
    if r:
        try:
            if r.val == x or r.val == y:
                r.val = y if r.val == x else x
                count[0] -= 1
                if count[0] == 0:
                    return
        except AttributeError:
            print("AttributeError!")
        recover_tree(r.left, x, y)
        recover_tree(r.right, x, y)

def bian_li(self, b):
        while self.stack:
            i = self.stack.pop()
            if isinstance(i, TreeNode):
                self.stack.append(i.right)
                self.stack.append(i.val)
                self.stack.append(i.left)
            if isinstance(i ,int):
                if b:
                    self.stack.append(i)
                    return True
                else:
                    return i
        else:
            return False

def recover_tree(root):
    global len, max_size
    len = [0]
    max_size = [1]
    nums = [0] * max_size[0]
    inorder_traversal(root, [nums])
    swapped = find_swapped_numbers(nums)
    recover(root, [2], swapped[0], swapped[1])

def serialize(self, root):
    if not root: return "[]"
    queue = collections.deque()
    queue.append(root)
    res = []
    while !queue.empty():
        node = queue.pop()
        res.append(str(node.val))
        queue.append(node.left)
        queue.append(node.right)
        if(!node.val):
            res.append("null")
    return '[' + ','.join(res) + ']'

