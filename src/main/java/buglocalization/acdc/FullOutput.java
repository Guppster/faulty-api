package buglocalization.acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

public class FullOutput extends Pattern
{
    private final String systemName;

    public FullOutput(DefaultMutableTreeNode _root, String _systemName)
    {
        super(_root);
        systemName = _systemName;
    }

    public void execute()
    {
        // Create an extra root here since OutputHandler ignores the root of the tree
        Node newDummy = new Node(systemName, "Dummy");
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(newDummy);
        newDummy.setTreeNode(newRoot);

        Vector rootChildren = nodeChildren(root);
        for (Object aRootChildren : rootChildren)
        {
            Node n = (Node) aRootChildren;
            DefaultMutableTreeNode curr = n.getTreeNode();
            newRoot.add(curr);
        }
        root.add(newRoot);
    }
}