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
		calcEntropy(dataCounts);
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

		// create tree
		createTree();
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

	private static void createTree() {
		// find the largest gain excluding the classification gain
		double max = 0.0;
		double classGain=informationGain.get(informationGain.size()-1);
		int maxIndex = -1;
		for (int i = 0; i < informationGain.size() - 1; i++) {
			if (informationGain.get(i) > max&&classGain!=informationGain.get(i)) {
				max = informationGain.get(i);
				maxIndex = i;
			}
		}
		// only if there was a maxIndex assigned
		if (maxIndex >= 0) {
			indexs.add(maxIndex);
			for(int i =0; i<dataCounts.get(maxIndex).size();i++){
				dataCounts.get(maxIndex).get(i).setValue(0);
			}
			calcEntropy(dataCounts);
		}
		// there are no more attributes able to be added assign classification and return
		else {
			
			return;
		}

		// case if there is a consensus
		int nonZero = 0;
		// check each value in meta of current index
		int savedIndex = 0;
		int savedValue = 0;

		for (int i = 0; i < meta.get(maxIndex).size() - 1; i++) {
			// check each classification count using dataCounts[maxIndex]
			for (int j = i; j < dataCounts.get(maxIndex).size(); j = j + meta.get(meta.size() - 1).size()) {
//				System.out.println("Line 402: Max Index: " + maxIndex + ", j: " + j + ", "
//						+ dataCounts.get(maxIndex).get(j).getValue());
				if (dataCounts.get(maxIndex).get(j).getValue() != 0) {
					nonZero++;
					savedIndex = maxIndex;
					savedValue = j;
				}
			}
			// if nonZero==1 then there is a consensus and the node will go to a
			// classification
			if (nonZero == 1) {
				// add classification to tree

			}

			// this should call recursively and build a subtree that attaches to the value
			// in meta that did not have a consensus
			else {
				createTree();
			}
		}

	}

	private static void printDecisionTree() {
		System.out.println("Decision Tree: ");
	}

	private static String findClassification(String[] data) {
		return "";
	}

	private static Double log2(Double n) {
		double v = (double) Math.log(n) / (double) Math.log(2);
		return v;
	}

	private static void calcEntropy(ArrayList<ArrayList<Pair<String, Integer>>> c) {
		informationGain.clear();
		for (int i = 0; i < meta.size(); i++) {
			informationGain.add(0.0);
		}
		double infoGain = 0.0;
		int totalRows = 0;
		for (int i = 0; i < meta.get(meta.size() - 1).size(); i++) {
			totalRows += dataCounts.get(dataCounts.size() - 1).get(i).getValue();
		}
		for (int i = 0; i < dataCounts.get(dataCounts.size() - 1).size(); i++) {
			double val = (double) dataCounts.get(dataCounts.size() - 1).get(i).getValue() / (double) totalRows;
			infoGain += val * log2(val);
		}
		infoGain *= -1;
		informationGain.set(informationGain.size() - 1, infoGain);
		int h=0;
		for (int i = 0; i < dataCounts.size() - 1; i++) {
			
			infoGain = 0.0;

			for (int j = 0; j < dataCounts.get(i).size() / dataCounts.get(dataCounts.size() - 1).size(); j++) {
				double ig = 0.0;
				double divide = 0.0;

				for (int k = j; k < dataCounts.get(i).size(); k += dataCounts.get(i).size()
						/ dataCounts.get(dataCounts.size() - 1).size()) {
					divide += (double) dataCounts.get(i).get(k).getValue();
				}
//				System.out.print(divide + "/" + totalRows + " (");
				for (int k = j; k < dataCounts.get(i).size(); k += dataCounts.get(i).size()
						/ dataCounts.get(dataCounts.size() - 1).size()) {
					double val;

					if (divide == 0)
						val = 0;
					else
						val = (double) dataCounts.get(i).get(k).getValue() / (double) divide;

//					System.out.print(" -" + dataCounts.get(i).get(k).getValue() + "/" + divide + " * log2("
//							+ dataCounts.get(i).get(k).getValue() + "/" + divide + ")");
					if (val == 0)
						ig -= val;
					else
						ig -= (val * log2(val));

				}

//				System.out.println(")");
				ig *= (divide / (double) totalRows);
				infoGain += ig;
//				System.out.println("info gain = " + infoGain);
			}
			infoGain = informationGain.get(informationGain.size() - 1) - infoGain;
//			System.out.println("information gain for atttribute" + (i + 1) + " = " + infoGain);
			if(dataCounts.size()==meta.size()) {
				informationGain.set(i, infoGain);
			}
//			else if (indexs.contains(h)) {
//				informationGain.set(h + 1, infoGain);
//				h++;
//			}
//			else {
//				informationGain.set(h, infoGain);
//			}
//			
//			h++;
		}
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

}

class Node {
	// label the node will be printed with
	private String label;
	// this boolean will determine if values will need to be populated
	// leaf is true when the node label is a classification, false if it is an
	// attribute
	private boolean leaf;
	// next sibling
	private Node sibling;
	// the list of branches that will come from the node, attribute values
	private ArrayList<String> values;

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

	public Node getSibling() {
		return sibling;
	}

	public void setSibling(Node sibling) {
		this.sibling = sibling;
	}

	public ArrayList<String> getValues() {
		return values;
	}

	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

}