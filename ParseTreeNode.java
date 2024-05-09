import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    private String type;
    private String value;
    private List<ParseTreeNode> children;
    private ParseTreeNode parent;

    public ParseTreeNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public void addChild(ParseTreeNode child) {
        child.setParent(this);
        children.add(child);
    }

    public void visualizeTree() {
        visualizeTree(this, 0);
    }

    private void visualizeTree(ParseTreeNode node, int depth) {
        StringBuilder indentation = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indentation.append("    "); // Adjust the number of spaces as needed
        }
        System.out.println(indentation.toString() + node.getType() + " " + node.getValue());

        for (ParseTreeNode child : node.getChildren()) {
            visualizeTree(child, depth + 1);
        }
    }

    public List<ParseTreeNode> getChildren() {
        return children;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public ParseTreeNode getParent() {
        return parent;
    }

    public void setParent(ParseTreeNode parent) {
        this.parent = parent;
    }
}