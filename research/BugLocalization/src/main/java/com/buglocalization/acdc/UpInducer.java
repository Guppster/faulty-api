package com.buglocalization.acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

public class UpInducer extends Pattern
{
    public UpInducer(DefaultMutableTreeNode _root)
    {
        super(_root);
    }

    public void execute()
    {
        // Remove intermediate clusters from the tree
        Vector rootChildren = nodeChildren(root);

        for (Object aRootChildren : rootChildren)
        {
            Node parent = (Node) aRootChildren;
            DefaultMutableTreeNode tparent = parent.getTreeNode();
            Vector subTree = allNodes(tparent);
            tparent.removeAllChildren();
            for (Object aSubTree : subTree)
            {
                Node child = (Node) aSubTree;
                if (child.isFile())
                {
                    DefaultMutableTreeNode tchild = child.getTreeNode();
                    tchild.removeAllChildren();
                    tparent.add(tchild);
                }
            }
        }
    }
}
