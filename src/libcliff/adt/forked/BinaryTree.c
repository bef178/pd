interface typedef struct binary_tree_node {
    struct binary_tree_node * parent;
    struct binary_tree_node * l;
    struct binary_tree_node * r;
    void * value;
} BinaryTreeNode;

interface BinaryTreeNode * BinaryTreeNode_pick(void * data) {
    BinaryTreeNode * p = mem_pick(sizeof(BinaryTreeNode));
    p->value = data;
    return p;
}

interface void BinaryTreeNode_drop(BinaryTreeNode * asThis) {
    free(asThis);
}

interface BinaryTreeNode * BinaryTreeNode_linkL(BinaryTreeNode * asThis,
        BinaryTreeNode * node) {
    BinaryTreeNode * delinked = asThis->l;
    if ((asThis->l = node) != NULL) {
        node->parent = asThis;
    }
    if (delinked != NULL) {
        delinked->parent = NULL;
    }
    return delinked;
}

interface BinaryTreeNode * BinaryTreeNode_linkR(BinaryTreeNode * asThis,
        BinaryTreeNode * node) {
    BinaryTreeNode * delinked = asThis->r;
    if ((asThis->r = node) != NULL) {
        node->parent = asThis;
    }
    if (delinked != NULL) {
        delinked->parent = NULL;
    }
    return delinked;
}

interface void BinaryTree_drop(BinaryTreeNode * asRoot) {
    if (asRoot == NULL) {
        return;
    }
    if (asRoot->l != NULL) {
        BinaryTree_drop(asRoot->l);
        asRoot->l = NULL;
    }
    if (asRoot->r != NULL) {
        BinaryTree_drop(asRoot->r);
        asRoot->r = NULL;
    }
    BinaryTreeNode_drop(asRoot);
}

interface int BinaryTree_depth(BinaryTreeNode * asRoot) {
    if (asRoot == NULL) {
        return 0;
    }
    int depthL = BinaryTree_depth(asRoot->l);
    int depthR = BinaryTree_depth(asRoot->r);
    return (depthL >= depthR ? depthL : depthR) + 1;
}

interface BinaryTreeNode * BinaryTree_findRoot(BinaryTreeNode * node) {
    while (node->parent != NULL) {
        node = node->parent;
    }
    return node;
}
