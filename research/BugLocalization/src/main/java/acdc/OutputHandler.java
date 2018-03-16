package acdc;
import javax.swing.tree.DefaultMutableTreeNode;

interface OutputHandler
{
	void writeOutput(String outputName, DefaultMutableTreeNode root);
}
