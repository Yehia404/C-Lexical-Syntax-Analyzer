import java.util.List;

public class StringTreeAsTreeForTreeLayout {
    StringTreeNode root;
    public StringTreeAsTreeForTreeLayout(StringTreeNode root) {
        //super(root);
        this.root = root;
    }
    public StringTreeNode getParent(StringTreeNode node) {
        return node.getParent();
    }
    public List<StringTreeNode> getChildrenList(StringTreeNode parentNode) {
        return parentNode.getChildren();
    }
}
