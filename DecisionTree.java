import java.util.ArrayList;

public class DecisionTree {

	private ArrayList<String> tree = new ArrayList<>();
	private String root;

	public ArrayList<String> getTree() {
		return tree;
	}
	public void setTree(ArrayList<String> tree) {
		this.tree = tree;
	}
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
		tree.add(root);
	}
	
}
