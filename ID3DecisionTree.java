import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ID3DecisionTree {
	private static ArrayList<String> classifications = new ArrayList<>();
	private static ArrayList<String> attributes = new ArrayList<>();
	private static ArrayList<String> computedClassifications = new ArrayList<>();
	private static ArrayList<ArrayList<String>> meta = new ArrayList<>();
	private static ArrayList<ArrayList<Pair<String, Integer>>> dataCounts = new ArrayList<ArrayList<Pair<String, Integer>>>();
	private static ArrayList<Double> informationGain = new ArrayList<Double>();
	private static String defaultClass;

	private static DecisionTree tree = new DecisionTree();
	private static ArrayList<Integer> tempData = new ArrayList<>();
	static ArrayList<Integer> indexs = new ArrayList<>();

	private static DecisionTree decisionTree;

	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		int command = 0;
		String file1;
		String file2;

		while (command != 5) {
			System.out.println("Please enter the number corresponding to the following command that you wish to run.");
			System.out.println("1 - Create decision tree.");
			System.out.println("2 - Create file of labeled examples.");
			System.out.println("3 - Print decision tree.");
			System.out.println("4 - Determine accuracy of decision tree labels.");
			System.out.println("5 - Quit.");
			command = keyboard.nextInt();
			keyboard.nextLine();

			switch (command) {
			case 1:

				System.out.println("Enter the name of the metadata file: ");
				file1 = keyboard.nextLine();
				System.out.println("Enter the name of the training data file: ");
				file2 = keyboard.nextLine();
				train(file1, file2);
				break;

			case 2:
				if (meta.isEmpty()) {
					System.out.println("Please train the system before trying to create file of labeled examples.");
					break;
				}
				System.out.println("Enter the name of the file with instances: ");
				file1 = keyboard.nextLine();
				System.out.println("Enter the name of the output file: ");
				file2 = keyboard.nextLine();
				classify(file1, file2);
				break;

			case 3:
				if (meta.isEmpty()) {
					System.out.println("Please train the system before trying to print the decision tree.");
					break;
				}
				printDecisionTree();
				break;

			case 4:
				if (meta.isEmpty()) {
					System.out.println("Please train the system before trying to determine the accuracy of labels.");
					break;
				}
				System.out.println("Enter the name of the data file: ");
				file1 = keyboard.nextLine();
				calcAccuracy(file1);
				break;
			}
		}
		keyboard.close();
	}

	private static void train(String mFile, String dFile) {
		System.out.println("Meta file name is: " + mFile);
		System.out.println("Training file name is: " + dFile);

		BufferedReader mbr = null;
		BufferedReader dbr = null;
		meta.clear();
		dataCounts.clear();
		informationGain.clear();
		tree = new DecisionTree();
		defaultClass="";
		tempData.clear();
		indexs.clear();
		
		
		String line = "";
		try {
			mbr = new BufferedReader(new FileReader(mFile));
			dbr = new BufferedReader(new FileReader(dFile));
			int count = 0;
			// stores data from meta file
			while ((line = mbr.readLine()) != null) {
				meta.add(new ArrayList<String>());
				String[] data = line.split(",|\\:");
				attributes.add(data[0]);
				for (int j = 1; j < data.length; j++)
					meta.get(count).add(data[j]);
				count++;
			}
			// sets up storage for calculations
			for (int i = 0; i < meta.size(); i++) {
				dataCounts.add(new ArrayList<Pair<String, Integer>>());
				if (i != meta.size() - 1) {
					for (int j = 0; j < meta.get(meta.size() - 1).size(); j++) {
						Pair<String, Integer> p = null;
						for (int k = 0; k < meta.get(i).size(); k++) {
							p = new Pair<String, Integer>(meta.get(i).get(k) + "/", 0);
							dataCounts.get(i).add(p);
						}
					}
				}
				if (i == meta.size() - 1) {
					for (int j = 0; j < meta.get(i).size(); j++) {
						Pair<String, Integer> p = new Pair<String, Integer>(meta.get(i).get(j), 0);
						dataCounts.get(i).add(p);
					}
				}
			}
			for (int i = 0; i < dataCounts.size() - 1; i++) {
				for (int j = 0; j < dataCounts.get(i).size(); j++) {
					Pair<String, Integer> p = new Pair<String, Integer>(
							dataCounts.get(i).get(j).getKey() + meta.get(meta.size() - 1).get(j / meta.get(i).size()),
							0);
					dataCounts.get(i).set(j, p);
				}
			}

			// stores counts from training file
			count = 0;
			while ((line = dbr.readLine()) != null) {
				String[] data = line.split(",");
				for (int j = 0; j < data.length; j++) {
					if (j != data.length - 1) {
						String checkKey = data[j] + "/" + data[data.length - 1];
						for (int i = 0; i < dataCounts.get(j).size(); i++) {

							if (dataCounts.get(j).get(i).getKey().equals(checkKey)) {
								Pair<String, Integer> p = new Pair<String, Integer>(dataCounts.get(j).get(i).getKey(),
										dataCounts.get(j).get(i).getValue() + 1);
								dataCounts.get(j).set(i, p);
							}
						}
					} else {
						String checkKey = data[j];
						for (int i = 0; i < dataCounts.get(dataCounts.size() - 1).size(); i++) {
							if (dataCounts.get(dataCounts.size() - 1).get(i).getKey().equals(checkKey)) {
								Pair<String, Integer> p = new Pair<String, Integer>(
										dataCounts.get(dataCounts.size() - 1).get(i).getKey(),
										dataCounts.get(dataCounts.size() - 1).get(i).getValue() + 1);
								dataCounts.get(dataCounts.size() - 1).set(i, p);
							}
						}
					}
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mbr != null && dbr != null) {
				try {
					mbr.close();
					dbr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		calcEntropy(dataCounts,informationGain);
//		System.out.println(informationGain);
		System.out.println("System has been trained.");

		for (int i = 0; i < dataCounts.size(); i++) {
			for (int j = 0; j < dataCounts.get(i).size(); j++) {
//				System.out.print(dataCounts.get(i).get(j).getKey() + "=");
//				System.out.print(dataCounts.get(i).get(j).getValue() + " ");
			}
//			System.out.println();
		}

		// set the default value -> change to function
		int max = 0;
		for (int i = 0; i < dataCounts.get(dataCounts.size() - 1).size(); i++) {
			if (dataCounts.get(dataCounts.size() - 1).get(i).getValue() > max) {
				max = dataCounts.get(dataCounts.size() - 1).get(i).getValue();
				defaultClass = dataCounts.get(dataCounts.size() - 1).get(i).getKey();
			}
		}
		ArrayList<ArrayList<Pair<String, Integer>>> dataCountCopy = new ArrayList<ArrayList<Pair<String, Integer>>>();
		dataCountCopy.addAll(dataCounts);
		ArrayList<Double> infoG = new ArrayList<Double>();
		infoG.addAll(informationGain);
		//populate classifications
		classifications= meta.get(meta.size()-1);
		// create tree
		tree=createTree(dataCountCopy, infoG);
	}

	private static void classify(String inF, String outF) {
		System.out.println("Input file name is: " + inF);
		System.out.println("Output file name is: " + outF);
		BufferedReader br = null;
		FileWriter fw = null;
		String line = "";
		String classification;
		try {
			br = new BufferedReader(new FileReader(inF));
			fw = new FileWriter(outF);
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				// prints everything but classification
				for (int i = 0; i < data.length - 1; i++) {
					fw.write(data[i] + ",");
				}
				// use data to get a classification
				classification = findClassification(data);
				fw.write(classification + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null && fw != null) {
				try {
					br.close();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("File with labeled examples has been created.");
	}

	private static void calcAccuracy(String inF) {
		System.out.println("Data file name is: " + inF);

		classifications.clear();
		computedClassifications.clear();

		BufferedReader br = null;
		String line = "";
		int compare = 0;
		String classify;

		try {
			br = new BufferedReader(new FileReader(inF));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				classifications.add(data[data.length - 1]);
				// classify instance
				classify = findClassification(data);
				computedClassifications.add(classify);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < classifications.size(); i++) {
			if (classifications.get(i).equals(computedClassifications.get(i)))
				compare++;
		}
		double percent;
		percent = ((double) compare / computedClassifications.size()) * 100;
		System.out.println("Accuracy has been calculated: " + compare + " computed classifications out of "
				+ computedClassifications.size() + " are correct.");
		String formatted = String.format("%.2f", percent);
		System.out.println(formatted + "% correct.");
	}

	private static DecisionTree createTree(ArrayList<ArrayList<Pair<String, Integer>>> dataCountCopy, ArrayList<Double> infoG) {
		DecisionTree tempTree= new DecisionTree();

		// find the largest gain excluding the classification gain
		double max = 0.0;
		double classGain=infoG.get(infoG.size()-1);
		int maxIndex = -1;
		for (int i = 0; i < infoG.size() - 1; i++) {
			if (infoG.get(i) > max&&classGain!=infoG.get(i)) {
				max = infoG.get(i);
				maxIndex = i;
			}
		}
		// only if there was a maxIndex assigned make this the root of the tree
		if (maxIndex >= 0) {
			indexs.add(maxIndex);
			tempData.clear();
			for(int i =0; i<dataCountCopy.get(maxIndex).size();i++){
				tempData.add(dataCountCopy.get(maxIndex).get(i).getValue());
				dataCountCopy.get(maxIndex).get(i).setValue(0);
			}
			infoG=calcEntropy(dataCountCopy, infoG);
			
			//set the root node of the tree/subtree
			Node node =new Node(attributes.get(maxIndex), false, meta.get(maxIndex));
			tempTree.setRoot(node);
		}
		
		// there are no more attributes able to be added, assign default classification and return
		else {
			Node node =new Node(defaultClass, true);
			DecisionTree temp = new DecisionTree();
			temp.setRoot(node);
			tempTree.addChild(temp);
			return temp;
		}

		// case if there is a consensus
		int nonZero = 0;
		int index=0;
		int count=0;
		// check each value in meta of current index

		for (int i = 0; i < meta.get(maxIndex).size(); i++) {
			if(tempTree.getRoot().getLabel().equals("safety")) {
				System.out.print(" " );
			}
			nonZero=0;
			index=0;
			count=0;
			// check each classification count using dataCounts[maxIndex]
			int dLength = tempData.size();
			int vLength =  meta.get(maxIndex).size();
			for (int j = i; j < dLength; j +=vLength) {
				if (tempData.get(j)!= 0) {
					nonZero++;
					index=count;
				}
				count++;
			}
			
			// if nonZero==1 then there is a consensus and the node will go to a
			// classification
			if (nonZero == 1) {
				// add classification to tree
				Node node = new Node(classifications.get(index), true);
				DecisionTree temp = new DecisionTree();
				temp.setRoot(node);
				tempTree.addChild(temp);
			}

			// this should call recursively and build a subtree that attaches to the value
			// in meta that did not have a consensus
			else {
				tempTree.addChild(createTree(dataCountCopy, infoG));
			}
		}
		return tempTree;
	}

	private static void printDecisionTree() {
		String attribute="";
		DecisionTree curr = tree;
		int index=0;
		int childIndex=0;
		
		for(int i =0 ;i<attributes.size()-1; i++) {
			System.out.println(curr.getRoot().getLabel());
			System.out.print("\t");
			curr.printChildren();
			System.out.println();
			
			for(int k = 0 ; k<curr.getChildren().size();k++) {
				if(attributes.contains(curr.getChild(k).getRoot().getLabel())) {
					curr=curr.getChild(k);
				}
			}

			//if(curr.getChildren().indexOf(0)) {};
			
		}
		
	}

	private static String findClassification(String[] data) {
		String attribute="";
		DecisionTree curr = tree;
		int index=0;
		int childIndex=0;
		
		
		while(true) {
			
			attribute= curr.getRoot().getLabel();
			index=attributes.indexOf(attribute);
			childIndex=meta.get(index).indexOf(data[index]);
			curr=curr.getChild(childIndex);
			if(meta.get(meta.size()-1).contains(curr.getRoot().getLabel())) {
				break;
			}
		}
		return curr.getRoot().getLabel();
	}

	private static Double log2(Double n) {
		double v = (double) Math.log(n) / (double) Math.log(2);
		return v;
	}

	private static ArrayList<Double> calcEntropy(ArrayList<ArrayList<Pair<String, Integer>>> c,  ArrayList<Double> infoG) {
		infoG.clear();
		for (int i = 0; i < meta.size(); i++) {
			infoG.add(0.0);
		}
		double infoGain = 0.0;
		int totalRows = 0;
		for (int i = 0; i < meta.get(meta.size() - 1).size(); i++) {
			totalRows += c.get(c.size() - 1).get(i).getValue();
		}
		for (int i = 0; i < c.get(c.size() - 1).size(); i++) {
			double val = (double) c.get(c.size() - 1).get(i).getValue() / (double) totalRows;
			infoGain += val * log2(val);
		}
		infoGain *= -1;
		infoG.set(infoG.size() - 1, infoGain);
		int h=0;
		for (int i = 0; i < c.size() - 1; i++) {
			
			infoGain = 0.0;

			for (int j = 0; j < c.get(i).size() / c.get(c.size() - 1).size(); j++) {
				double ig = 0.0;
				double divide = 0.0;

				for (int k = j; k < c.get(i).size(); k += c.get(i).size()
						/ c.get(c.size() - 1).size()) {
					divide += (double) c.get(i).get(k).getValue();
				}
//				System.out.print(divide + "/" + totalRows + " (");
				for (int k = j; k < c.get(i).size(); k += c.get(i).size()
						/ c.get(c.size() - 1).size()) {
					double val;

					if (divide == 0)
						val = 0;
					else
						val = (double) c.get(i).get(k).getValue() / (double) divide;
					if (val == 0)
						ig -= val;
					else
						ig -= (val * log2(val));
				}

				ig *= (divide / (double) totalRows);
				infoGain += ig;
			}
			infoGain = infoG.get(infoG.size() - 1) - infoGain;
			if(c.size()==meta.size()) {
				infoG.set(i, infoGain);
			}
		}
		return infoG;
	}
	
}

class Pair<K, V> {
	public K key;
	public V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public void setKey(K key) {
		this.key = Objects.requireNonNull(key);
	}

	public void setValue(V value) {
		this.value = Objects.requireNonNull(value, "no value");
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair<?, ?> p = (Pair<?, ?>) o;
		return Objects.equals(p.key, key) && Objects.equals(p.value, value);

	}

	@Override
	public int hashCode() {
		return (Objects.hash(key, value));
	}
}

class DecisionTree {


	private Node root;
	private ArrayList<DecisionTree> children = new ArrayList<>();
	public void printChildren() {
		for(int i =0; i<children.size();i++) {
			System.out.print(children.get(i).getRoot().getLabel()+" ");
		}
	}
	
	public ArrayList<DecisionTree> getChildren() {
		return children;
	}

	public DecisionTree getChild(int index) {
		return children.get(index);
	}
	
	public void addChild(DecisionTree subTree) {
		children.add(subTree);
	}
	
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}


}

class Node {
	// label the node will be printed with
	private String label;
	// this boolean will determine if values will need to be populated
	// leaf is true when the node label is a classification, false if it is an
	// attribute
	private boolean leaf;
	
	// the list of branches that will come from the node, attribute values
	private ArrayList<String> values;

	//for attribute node
	Node(String label, boolean leaf,ArrayList<String> values){
		this.label = label;
		this.leaf =leaf;
		this.values = values;
	}
	
	//for classification node
	Node(String label, boolean leaf){
		this.label=label;
		this.leaf=leaf;
	}


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public ArrayList<String> getValues() {
		return values;
	}

	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

}