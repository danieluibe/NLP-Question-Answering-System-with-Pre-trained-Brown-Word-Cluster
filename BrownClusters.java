import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class BrownClusters {
	HashMap<String, ArrayList<String>> clusters;
	static String file = "C:/Cornell/cs4740/project4/dep-brown-data-master/en/standard/paths_1000_min50";
	public BrownClusters(){
		
		clusters = new HashMap<String, ArrayList<String>>();
		readFile(file);
	}
	
	
	
	public void readFile(String str){
		File file = new File(str);
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			while ((s = br.readLine()) != null) {
				result.append(System.lineSeparator() + s);
				String[] string = s.split("\t");
//				System.out.println("path:" + string[0] + ", word:" + string[1]);
				if (clusters.get(string[0]) != null){
					clusters.get(string[0]).add(string[1].toLowerCase());
				} else{
					ArrayList<String> arr = new ArrayList<String>();
					arr.add(string[1].toLowerCase());
					clusters.put(string[0], arr);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println(result.toString());
	}
	
	public ArrayList<String> getClusterOnlyDownTree(String word){
		return getCluster(word, 0);
	}

	public ArrayList<String>  getCluster(String word, int searchUp){
		ArrayList<String> list = new ArrayList<String>();
		String key = "";
		//inefficient sue me
		Iterator<Entry<String, ArrayList<String>>> it = clusters.entrySet().iterator();
		
	    while (it.hasNext()) {
	        Map.Entry<String,ArrayList<String>> pair = (Map.Entry<String,ArrayList<String>>)it.next();
//	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        if (pair.getValue().contains(word.toLowerCase())){
	        	key = (String) pair.getKey();
	        	break;
	        }
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	    String newKey = key.substring(0, key.length()-searchUp);
	    recursiveHelper(newKey,list, searchUp);
	    //if the word does not exist in word cluster, just return a null arraylist
	    ArrayList<String> listA = new ArrayList<String>(); 
	    listA.add(".");
	    listA.add("]");
	    listA.add("!");
	    listA.add("?");
	    listA.add("a");
	    listA.add("the");
	    if( listA.containsAll(list) && list.containsAll(listA)){
	    	list.clear();
	    }
	    
	   
		return list;
	}
	
	private void recursiveHelper(String key, ArrayList<String> words, int itrLeft){
		ArrayList<String> val = clusters.get(key);
		if (val != null){
			words.addAll(val);
		}
		if (itrLeft > 0){
			recursiveHelper(key+"0",words, itrLeft - 1);
			recursiveHelper(key+"1",words, itrLeft - 1);
		}
	}
	
	
	public static void main(String[] args){
		BrownClusters b = new BrownClusters();
		String word = "friday";
		System.out.println(word + " -> " + b.getClusterOnlyDownTree(word));

		String word2 = "bleeze";
		System.out.println(word2 + " -> " + b.getCluster(word2,3));
		
		String words = "says";
		System.out.println(words + " -> " + b.getClusterOnlyDownTree(words));
		
		
	}
}
