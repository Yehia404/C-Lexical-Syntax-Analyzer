import java.util.List;

public class StringTreeNode {
    private StringTreeNode parent;
    private String text;
    private List<StringTreeNode> children;

    public StringTreeNode getParent() {
        return parent;
    }

    public void setParent(StringTreeNode parent) {
        this.parent = parent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<StringTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<StringTreeNode> children) {
        this.children = children;
    }
}
