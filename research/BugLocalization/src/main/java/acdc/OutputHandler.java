package acdc;
import javax.swing.tree.DefaultMutableTreeNode;

public interface OutputHandler
{
	void writeOutput(String outputName, DefaultMutableTreeNode root);
}
