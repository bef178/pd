interface typedef struct binary_tree_node {
	struct binary_tree_node * p;
	struct binary_tree_node * l;
	struct binary_tree_node * r;
	void * e;
} binaryTreeNode_t;

static int max(int i, int j) {
	return i >= j ? i : j;
}

interface int binaryTreeNode_depth(binaryTreeNode_t * asRoot) {
	if (asRoot == NULL) {
		return 0;
	}
	return max(binaryTreeNode_depth(asRoot->l), binaryTreeNode_depth(asRoot->r)) + 1;
}

interface binaryTreeNode_t * findRoot(binaryTreeNode_t * node) {
	while (node->p != NULL) {
		node = node->p;
	}
	return node;
}

/**
 * return the delinked node
 */
interface binaryTreeNode_t * linkL(binaryTreeNode_t * asThis,
		binaryTreeNode_t * node) {
	assert(asThis != NULL);
	binaryTreeNode_t * delinked = asThis->l;
	if ((asThis->l = node) != NULL) {
		node->p = asThis;
	}
	if (delinked != NULL) {
		delinked->p = NULL;
	}
	return delinked;
}

/**
 * return the delinked node
 */
interface binaryTreeNode_t * linkR(binaryTreeNode_t * asThis,
		binaryTreeNode_t * node) {
	assert(asThis != NULL);
	binaryTreeNode_t * delinked = asThis->r;
	if ((asThis->r = node) != NULL) {
		node->p = asThis;
	}
	if (delinked != NULL) {
		delinked->p = NULL;
	}
	return delinked;
}

binaryTreeNode_t * binaryTreeNode_pick(void * data) {
	binaryTreeNode_t * asThis = mem_pick(sizeof(binaryTreeNode_t));
	asThis->e = data;
	return asThis;
}

/**
 * returns the data element in node
 */
void binaryTreeNode_drop(binaryTreeNode_t * asThis) {
	if (asThis == NULL) {
		return;
	}
	free(asThis);
	asThis == NULL;
}

/**
 * ignore 'e': potential memory leak
 */
void binaryTree_drop(binaryTreeNode_t * asThis) {
	if (asThis == NULL) {
		return;
	}

	if (asThis->l != NULL) {
		binaryTree_drop(asThis->l);
		asThis->l = NULL;
	}
	if (asThis->r != NULL) {
		binaryTree_drop(asThis->r);
		asThis->r = NULL;
	}
	free(asThis);
	asThis = NULL;
}
