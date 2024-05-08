import java.util.List;

public class StringTreeAsTreeForTreeLayout extends AbstractTreeForTreeLayout<StringTreeNode> {
    public StringTreeAsTreeForTreeLayout(StringTreeNode root) {
        super(root);
    }
    public StringTreeNode getParent(StringTreeNode node) {
        return node.getParent();
    }
    public List<StringTreeNode> getChildrenList(StringTreeNode parentNode) {
        return parentNode.getChildren();
    }
}

