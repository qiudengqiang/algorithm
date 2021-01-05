package me.techbird.leetcode.api.map;


import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class TreeMap<K, V> implements Map<K, V> {
    private static final boolean BLACK = true;
    private static final boolean RED = false;
    private int size;
    private Node<K, V> root;
    private Comparator<K> comparator;

    public TreeMap() {
        this(null);
    }

    public TreeMap(Comparator<K> comparator) {
        this.comparator = comparator;
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public V put(K key, V value) {
        keyNotNullElement(key);
        if (root == null) {
            root = new Node<>(key, value, null);
            size++;
            afterAdd(root);
            return null;
        }
        Node<K, V> parent = root;
        Node<K, V> node = root;
        int cmp = 0;
        while (node != null) {
            cmp = compare(key, node.key);
            parent = node;
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {//相等
                node.key = key;
                V oldVal = node.value;
                node.value = value;
                return oldVal;
            }
        }
        Node<K, V> newNode = new Node<>(key, value, parent);
        if (cmp > 0) {
            parent.right = newNode;
        } else {
            parent.left = newNode;
        }
        size++;
        afterAdd(newNode);
        return null;
    }

    private void afterAdd(Node<K, V> node) {
        Node<K, V> parent = node.parent;
        // 添加的是根节点 或者 上溢达到了根节点
        if (parent == null) {
            black(node);
            return;
        }

        // 如果父节点是黑色，满足性质
        if (isBlack(parent)) return;

        Node<K, V> uncle = parent.sibling();
        Node<K, V> grand = red(parent.parent);
        if (isRed(uncle)) { // 叔父节点是红色，4种情况，红黑红
            black(parent);
            black(uncle);
            //[B树节点上溢]
            afterAdd(grand);
            return;
        }

        //叔父节点不是红色
        if (parent.isLeftChild()) {//L
            if (node.isLeftChild()) {//LL
                black(parent);
            } else {//LR
                black(node);
                rotateLeft(parent);
            }
            rotateRight(grand);
        } else {//R
            if (node.isLeftChild()) {//RL
                black(node);
                rotateRight(parent);
            } else {//RR
                black(parent);
            }
            rotateLeft(grand);
        }
    }

    @Override
    public V get(K key) {
        Node<K, V> node = node(key);
        return node != null ? node.value : null;
    }

    @Override
    public V remove(K key) {
        return remove(node(key));
    }

    private V remove(Node<K, V> node) {
        if (node == null) return null;
        size--;

        V oldVal = node.value;

        if (node.hasTwoChild()) {//node是度为2的节点
            // 找到后继节点
            Node<K, V> s = successor(node);
            //用后继节点分值覆盖度为2的节点的值
            node.key = s.key;
            node.value = s.value;
            // 删除后继节点
            node = s;
        }

        // 删除node节点（Node节点的度必为1或者0）
        Node<K, V> replacement = node.left != null ? node.left : node.right;
        if (replacement != null) {//node是度为1的节点
            replacement.parent = node.parent;

            if (node.parent == null) {//node是度为1的节点并且是根节点
                root = replacement;
            } else if (node == node.parent.left) {
                node.parent.left = replacement;
            } else {
                node.parent.right = replacement;
            }
            afterRemove(node, replacement);
        } else if (node.parent == null) {//node是叶子节点并且是根节点
            root = null;
            afterRemove(node, null);
        } else {//node是叶子节点，但不是根节点
            if (node == node.parent.left) {
                node.parent.left = null;
            } else {
                node.parent.right = null;
            }
            afterRemove(node, null);
        }

        return oldVal;
    }

    private void afterRemove(Node<K, V> node, Node<K, V> replacement) {
        // 若删除的节点是红色
        if (isRed(node)) return;

        // 替代节点是红色节点
        if (isRed(replacement)) {
            black(replacement);
            return;
        }

        Node<K, V> parent = node.parent;
        // 删除的根节点
        if (parent == null) return;

        // 删除的是黑色叶子节点【下溢】
        // 判断被删除的节点是左还是右
        boolean left = parent.left == null || node.isLeftChild();
        Node<K, V> sibling = left ? parent.right : parent.left;

        if (left) {//被删的是左边的节点
            if (isRed(sibling)) {
                black(sibling);
                red(parent);
                rotateLeft(parent);
                // 更换兄弟
                sibling = parent.right;
            }

            // 兄弟节点必然是黑色
            if (isBlack(sibling.left) && isBlack(sibling.right)) {
                //兄弟没有1个红色节点的子节点，父节点要向下跟兄弟节点合并
                boolean blackParent = isBlack(parent);
                red(sibling);
                black(parent);
                if (blackParent) {
                    afterRemove(parent, null);
                }
            } else {//兄弟节点至少有1个红色子节点，向兄弟节点借元素
                //兄弟节点左边是黑色，兄弟要先旋转
                if (isBlack(sibling.right)) {
                    rotateRight(sibling);
                    sibling = parent.right;
                }

                color(sibling, colorOf(parent));
                black(sibling.right);
                black(parent);
                rotateLeft(parent);
            }
        } else {//被删的是右边的节点
            if (isRed(sibling)) {
                black(sibling);
                red(parent);
                rotateRight(parent);
                // 更换兄弟
                sibling = parent.left;
            }

            // 兄弟节点必然是黑色
            if (isBlack(sibling.left) && isBlack(sibling.right)) {
                //兄弟没有1个红色节点的子节点，父节点要向下跟兄弟节点合并
                boolean blackParent = isBlack(parent);
                red(sibling);
                black(parent);
                if (blackParent) {
                    afterRemove(parent, null);
                }
            } else {//兄弟节点至少有1个红色子节点，向兄弟节点借元素
                //兄弟节点左边是黑色，兄弟要先旋转
                if (isBlack(sibling.left)) {
                    rotateLeft(sibling);
                    sibling = parent.left;
                }

                color(sibling, colorOf(parent));
                black(sibling.left);
                black(parent);
                rotateRight(parent);
            }

        }
    }

    @Override
    public boolean containsKey(K key) {
        return node(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        if (root == null) return false;
        Queue<Node<K, V>> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            Node<K, V> node = queue.poll();
            if (valEquals(node.value, value)) return true;
            if (node.left != null) {
                queue.offer(node.left);
            }
            if (node.right != null) {
                queue.offer(node.right);
            }
        }
        return false;
    }


    @Override
    public void traversal(Visitor<K, V> visitor) {
        if (visitor == null) return;
        inorderTraversal(root, visitor);
    }

    private void inorderTraversal(Node<K, V> root, Visitor<K, V> visitor) {
        if (root == null || visitor.stop) return;

        inorderTraversal(root.left, visitor);
        if (visitor.stop) return;
        visitor.visit(root.key, root.value);
        inorderTraversal(root.right, visitor);
    }

    private boolean valEquals(V v1, V v2) {
        return v1 == null ? v2 == null : v1.equals(v2);
    }

    private Node<K, V> node(K key) {
        Node<K, V> node = root;
        while (node != null) {
            int cmp = compare(key, node.key);
            if (cmp == 0) return node;
            if (cmp > 0) {
                node = node.right;
            } else {
                node = node.left;
            }
        }
        return null;
    }

    private Node<K, V> predecessor(Node<K, V> node) {
        if (node == null) return null;

        //前驱在左子树当中(left.right.right.right...)
        Node<K, V> p = node.left;
        if (p != null) {
            while (p.right != null) {
                p = p.right;
            }
            return p;
        }

        //从父节点、祖父节点当中寻找前驱节点
        while (node.parent != null && node == node.parent.left) {
            node = node.parent;
        }

        //node.parent == null
        //node == node.parent.right
        return node.parent;
    }


    private Node<K, V> successor(Node<K, V> node) {
        if (node == null) return null;

        //前驱在左子树当中(left.right.right.right...)
        Node<K, V> p = node.right;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }

        //从父节点、祖父节点当中寻找前驱节点
        while (node.parent != null && node == node.parent.right) {
            node = node.parent;
        }

        //node.parent == null
        //node == node.parent.left
        return node.parent;
    }

    protected void rotateLeft(Node<K, V> grand) {
        Node<K, V> parent = grand.right;
        Node<K, V> child = parent.left;
        grand.right = child;
        parent.left = grand;
        afterRotate(grand, parent, child);
    }

    protected void rotateRight(Node<K, V> grand) {
        Node<K, V> parent = grand.left;
        Node<K, V> child = parent.right;
        grand.left = child;
        parent.right = grand;
        afterRotate(grand, parent, child);
    }

    protected void afterRotate(Node<K, V> grand, Node<K, V> parent, Node<K, V> child) {
        //让parent成为子树的根节点
        parent.parent = grand.parent;

        if (grand.isLeftChild()) {
            grand.parent.left = parent;
        } else if (grand.isRightChild()) {
            grand.parent.right = parent;
        } else {//grand是root节点
            root = parent;
        }
        // 更新child的parent
        if (child != null) {
            child.parent = grand;
        }
        // 更新grand的parent
        grand.parent = parent;
    }

    private int compare(K e1, K e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        }
        return ((Comparable<K>) e1).compareTo(e2);
    }

    private void keyNotNullElement(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key not be null");
        }
    }

    private Node<K, V> color(Node<K, V> node, boolean color) {
        if (node == null) return null;
        node.color = color;
        return node;
    }

    private Node<K, V> red(Node<K, V> node) {
        return color(node, RED);
    }

    private Node<K, V> black(Node<K, V> node) {
        return color(node, BLACK);
    }

    private boolean colorOf(Node<K, V> node) {
        return node == null ? BLACK : node.color;
    }

    private boolean isBlack(Node<K, V> node) {
        return colorOf(node) == BLACK;
    }

    private boolean isRed(Node<K, V> node) {
        return colorOf(node) == RED;
    }


    private static class Node<K, V> {
        boolean color = RED;
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;
        Node<K, V> parent;

        public Node(K key, V value, Node<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public boolean isLeaf() {
            return this.right == null && this.left == null;
        }

        public boolean hasTwoChild() {
            return this.left != null && this.right != null;
        }

        public boolean isLeftChild() {
            return parent != null && this == parent.left;
        }

        public boolean isRightChild() {
            return parent != null && this == parent.right;
        }

        public Node<K, V> sibling() {
            if (isLeftChild()) {
                return parent.right;
            }
            if (isRightChild()) {
                return parent.left;
            }
            return null;
        }
    }

}
