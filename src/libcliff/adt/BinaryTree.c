interface typedef struct binary_tree_node {
    struct binary_tree_node * parent;
    struct binary_tree_node * l;
    struct binary_tree_node * r;
    void * value;
} BinaryTreeNode;

static int max(int i, int j) {
    return i >= j ? i : j;
}

interface int BinaryTreeNode_depth(BinaryTreeNode * asRoot) {
    if (asRoot == NULL) {
        return 0;
    }
    return max(BinaryTreeNode_depth(asRoot->l), BinaryTreeNode_depth(asRoot->r)) + 1;
}

interface BinaryTreeNode * findRoot(BinaryTreeNode * node) {
    while (node->parent != NULL) {
        node = node->parent;
    }
    return node;
}

/**
 * return the delinked node
 */
interface BinaryTreeNode * BinaryTreeNode_linkL(BinaryTreeNode * asThis,
        BinaryTreeNode * node) {
    assert(asThis != NULL);
    BinaryTreeNode * delinked = asThis->l;
    if ((asThis->l = node) != NULL) {
        node->parent = asThis;
    }
    if (delinked != NULL) {
        delinked->parent = NULL;
    }
    return delinked;
}

/**
 * return the delinked node
 */
interface BinaryTreeNode * BinaryTreeNode_linkR(BinaryTreeNode * asThis,
        BinaryTreeNode * node) {
    assert(asThis != NULL);
    BinaryTreeNode * delinked = asThis->r;
    if ((asThis->r = node) != NULL) {
        node->parent = asThis;
    }
    if (delinked != NULL) {
        delinked->parent = NULL;
    }
    return delinked;
}

BinaryTreeNode * BinaryTreeNode_pick(void * data) {
    BinaryTreeNode * asThis = mem_pick(sizeof(BinaryTreeNode));
    asThis->value = data;
    return asThis;
}

void BinaryTreeNode_drop(BinaryTreeNode * asThis) {
    if (asThis == NULL) {
        return;
    }
    free(asThis);
    asThis == NULL;
}

/**
 * ignore 'value': potential memory leak
 */
void BinaryTree_drop(BinaryTreeNode * asThis) {
    if (asThis == NULL) {
        return;
    }

    if (asThis->l != NULL) {
        BinaryTree_drop(asThis->l);
        asThis->l = NULL;
    }
    if (asThis->r != NULL) {
        BinaryTree_drop(asThis->r);
        asThis->r = NULL;
    }
    free(asThis);
    asThis = NULL;
}
