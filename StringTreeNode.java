import java.util.ArrayList;
import java.util.List;

public class StringTreeNode {
     private StringTreeNode Parent;
     private String text;
     private List<StringTreeNode> children;
    public StringTreeNode(StringTreeNode parent, String text){
        this.Parent = parent;
        this.text = text;
        children = new ArrayList<>();
    }

    public StringTreeNode getParent() {
        return Parent;
    }

    public void setParent(StringTreeNode parent) {
        Parent = parent;
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
