interface typedef struct binary_tree_node {
    struct binary_tree_node * parent;
    struct binary_tree_node * l;
    struct binary_tree_node * r;
    void * value;
} BinaryTreeNode_t;

static int max(int i, int j) {
    return i >= j ? i : j;
}

interface int BinaryTreeNode_depth(BinaryTreeNode_t * asRoot) {
    if (asRoot == NULL) {
        return 0;
    }
    return max(BinaryTreeNode_depth(asRoot->l), BinaryTreeNode_depth(asRoot->r)) + 1;
}

interface BinaryTreeNode_t * findRoot(BinaryTreeNode_t * node) {
    while (node->parent != NULL) {
        node = node->parent;
    }
    return node;
}

/**
 * return the delinked node
 */
interface BinaryTreeNode_t * BinaryTreeNode_linkL(BinaryTreeNode_t * asThis,
        BinaryTreeNode_t * node) {
    assert(asThis != NULL);
    BinaryTreeNode_t * delinked = asThis->l;
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
interface BinaryTreeNode_t * BinaryTreeNode_linkR(BinaryTreeNode_t * asThis,
        BinaryTreeNode_t * node) {
    assert(asThis != NULL);
    BinaryTreeNode_t * delinked = asThis->r;
    if ((asThis->r = node) != NULL) {
        node->parent = asThis;
    }
    if (delinked != NULL) {
        delinked->parent = NULL;
    }
    return delinked;
}

BinaryTreeNode_t * BinaryTreeNode_pick(void * data) {
    BinaryTreeNode_t * asThis = mem_pick(sizeof(BinaryTreeNode_t));
    asThis->value = data;
    return asThis;
}

void BinaryTreeNode_drop(BinaryTreeNode_t * asThis) {
    if (asThis == NULL) {
        return;
    }
    free(asThis);
    asThis == NULL;
}

/**
 * ignore 'value': potential memory leak
 */
void BinaryTree_drop(BinaryTreeNode_t * asThis) {
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
