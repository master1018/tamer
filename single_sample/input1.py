class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def inorder(root, nums):
    global len
    if root is None:
        return
    inorder(root.left, nums)
    nums[0][len[0]] = root.val
    len[0] += 1
    if len[0] == max_size[0]:
        max_size[0] <<= 1
        nums[0] = nums[0] + [0] * max_size[0]
        length += len[0]
        res += nums[0]
    inorder(root.right, nums)
    
def next(self) -> int:
        nd=self.findmd()
        self.cur=nd
        return nd.val

def findTwoSwapped(nums):
    index1, index2 = -1, -1
    for i in range(len[0] - 1):
        if nums[i + 1] < nums[i]:
            index2 = i + 1
            if index1 == -1:
                index1 = i
            else:
                break
    x, y = nums[index1], nums[index2]
    return [x, y]

def recover(r, count, x, y):
    if r is not None:
        if r.val == x or r.val == y:
            r.val = y if r.val == x else x
            count[0] -= 1
            if count[0] == 0:
                return
        recover(r.left, count, x, y)
        recover(r.right, count, x, y)
        
def next(self) -> int:
    while True:
        node, color = self.stack.pop()
        if color == 1: return node.val
        if node.right: self.stack.append((node.right, 0))
        self.stack.append((node, 1))
        if node.left: self.stack.append((node.left, 0))

def recoverTree(root):
    global len, max_size
    len = [0]
    max_size = [1]
    nums = [0] * max_size[0]
    inorder(root, [nums])
    swapped = findTwoSwapped(nums)
    recover(root, [2], swapped[0], swapped[1])

def serialize(self, root):
    if not root: return "[]"
    queue = collections.deque()
    queue.append(root)
    res = []
    while queue:
        node = queue.popleft()
        if node:
            res.append(str(node.val))
            queue.append(node.left)
            queue.append(node.right)
        else: res.append("null")
    return '[' + ','.join(res) + ']'
