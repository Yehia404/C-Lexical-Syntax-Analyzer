import java.util.ArrayList;
import java.util.List;

public class StringTreeNode {
    private Token data;
     StringTreeNode sibling;
     StringTreeNode child;
    public StringTreeNode(Token data){
        this.data = data;
        sibling = null;
        child = null;
    }
    public Token getData() {
        return data;
    }

    public void setData(Token data) {
        this.data = data;
    }

    public StringTreeNode getChild() {
        return child;
    }

    public void setChild(StringTreeNode child) {
        this.child = child;
    }

    public StringTreeNode getSibling() {
        return sibling;
    }

    public void setSibling(StringTreeNode sibling) {
        this.sibling = sibling;
    }
    public void printTree(StringTreeNode x) {
        if (x == null) {
            return; // Base case: If the node is null, return
        }

        // Print data of the current node
        System.out.println(x.getData());

        // Recursively print the child nodes
        printTree(x.getChild());

        // Recursively print the sibling nodes
        printTree(x.getSibling());
    }

}
