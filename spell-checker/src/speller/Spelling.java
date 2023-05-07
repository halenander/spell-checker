package speller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
/*
 * CS 245 Assignment 2
 * @author Haley Lenander
 */
public class Spelling {
    /*
     * Node class for a binary trie
     */
    static class TrieNode {
        double val;
        char key;
        String storedWord;
        TrieNode[] children = new TrieNode[26];
        int numChildren = 0;

        TrieNode(double val, char key) {
            this.val = val;
            this.key = key;
            this.numChildren = 0;
        }

        TrieNode(double val, char key, TrieNode[] children, int numChildren, String storedWord) {
            this.val = val;
            this.key = key;
            this.numChildren = numChildren;
            this.children = children;
            this.storedWord = storedWord;
        }
    }

    /*
     * The root of the trie
     */
    TrieNode root;
    
    static HashMap<String, String> misspellings = new HashMap<String, String>();

    /*
     * Default constructor
     */
    public Spelling() {
        root = new TrieNode(0, '\0');
    }

    /**
     * Insert an item to the trie
     *
     * @param freq value to insert, string to insert
     */
    public void insert(String word, double freq) {
        root = insert(word, root, freq);
    }

    /**
     * Function override of the insert function
     *
     * @param freq value to add
     * @param word string to add to tree by each char
     * @param node current node
     * @return root of the trie
     */
    private TrieNode insert(String word, TrieNode node, double freq) {
        //if root doesn't have children yet
        int i = 0;
        while (i < word.length()) {
            if (node.numChildren==0){
                node.children[0] = new TrieNode(0, word.charAt(i));
                node.numChildren++;
            }
            for (int j = 0; j < node.numChildren; j++) {
                //if char matches
                if (node.children[j].key == word.charAt(i)){
                    //if end of word set frequency value and string
                    if((i+1) == word.length()){
                        node.children[j].val = freq;
                        node.children[j].storedWord = word;
                        return root;
                    }
                    i++;
                    //change node
                    node = node.children[j];
                    //go back to beginning of for loop
                    j= -1;
                }
                //if char isnt in array add it
                else if((j+1) == node.numChildren) {
                    node.children[node.numChildren] = new TrieNode(0, word.charAt(i));
                    //if end of word, set freq value and string
                    if ((i + 1) == word.length()) {
                        node.children[node.numChildren].val = freq;
                        node.children[node.numChildren].storedWord = word;
                        node.numChildren++;
                        return root;
                    }
                    //increase numchildren
                    node.numChildren++;
                    i++;
                    //change node
                    node = node.children[node.numChildren-1];
                    //go back to beginning of for loop
                    j=-1;
                }
            }
        }
        return root;
    }

    public List<List<String>> suggest(String token, int count) {
        List<String> holder = new ArrayList<String>();
        Set<List<String>> sugList = new LinkedHashSet<List<String>>();
        //run for each prefix til the length of the string
        for (int i = 0; i < token.length(); i++) {
            //get arraylist of suggestions and add to list of lists
            holder = check(token.substring(0, i + 1), count, token.length());
            //if returned arraylist doesnt have count # of elements, replace it with previous arraylist
            if (holder.size() < count) {
                //sugList.add(sugList.get(i-1));
            } else {
                sugList.add(holder);
            }
        }
        List<List<String>> arrList = new ArrayList<List<String>>(sugList);
        return arrList;
    }

    public ArrayList<String> check(String preFix, int count, int wordLength){
        ArrayList<String> strList = new ArrayList<>();
        TrieNode[] trieArr = new TrieNode[count];
        //initialize array of trie nodes
        for (int i = 0; i < count; i++) {
            trieArr[i] = new TrieNode(0, '\0');
        }
        TrieNode node = root;
        //go through trie til you get through the preFix
        for (int i = 0; i < preFix.length(); i++) {
            //run through all the children for each branch
            for (int j = 0; j < node.numChildren; j++) {
                if(preFix.charAt(i)== node.children[j].key){
                    //change node
                    node = node.children[j];
                    //exit loop
                    j = node.numChildren;
                }
            }
        }
        //call recursive function to generate most freq nodes with prefix
        trieArr = recurCheck(trieArr, node, wordLength);
        //turn arr of nodes to list of string
        for (int i = 0; i < trieArr.length; i++) {
            //only add to arraylist if storedword isn't null (i.e. if there was a matching node with the prefix)
            if(trieArr[i].storedWord != null){
                strList.add(trieArr[i].storedWord);
            }
        }
        return strList;
    }

    public TrieNode[] recurCheck(TrieNode[] trieArr, TrieNode node, int wordLength){
        //semi-base case
        if(node.val > 0){

            int min = checkMin(trieArr, node.val);
            //if freq is higher than lowest freq in triearr, replace value
            if(min != -1){
                trieArr[min] = node;
            }
            //if this is the last node in the path, return array (base case)
            if (node.numChildren == 0 || node.storedWord.length() > wordLength){
                return trieArr;
            }
            //otherwise continue calling
            else{
                for (int i = 0; i < node.numChildren; i++) {
                    trieArr = recurCheck(trieArr, node.children[i], wordLength);
                }
            }
        }
        //call function for each child for the node
        else{
            for (int i = 0; i < node.numChildren; i++) {
                trieArr = recurCheck(trieArr, node.children[i], wordLength);
            }
        }
        return trieArr;
    }

    //helper function to replace node with lowest frequency with new node (with higher frequency)
    private int checkMin(TrieNode[] max, double value){
        double min = max[0].val;
        int index = 0;
        //find min value in max arr
        for (int i = 0; i < max.length; i++) {
            if (max[i].val < min){
                min = max[i].val;
                index = i;
            }
        }
        //compare min value with input value, if larger return index, else return -1
        if(value > min){
            return index;
        }
        else{
            return -1;
        }
    }


    //function to take line from file and send word, freq value to insert to trie
    public void addToTrie(String line){
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                insert(rowScanner.next(),Double.parseDouble(rowScanner.next()));
            }
        }
        catch(Exception e){
            System.out.print("line not found");
        }
    }

    //function to read in misspelling.cvc file and keep track of how many suggested words match the correction
    public int misSpelled(String line, int count){
        String correct;
        String incorrect;
        int total = 0;
        boolean isCorrect = false;
        List<List<String>> sugList = new ArrayList<List<String>>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                correct = rowScanner.next();
                incorrect= rowScanner.next();
                sugList= suggest(incorrect,count);
                isCorrect = false;
                //run through returned list of lists to see if any of the string match the correct string
                for (int i = 0; i < sugList.size(); i++) {
                    List<String> temp = new ArrayList<String>(sugList.get(i));
                    for (int j = 0; j < count; j++) {
                        //if any of the string in the list matches the correct string, set isCorrect to true (to show match found)
                        if (temp.get(j).equals(correct)) {
                            isCorrect= true;
                        }
                    }
                }
                //increase total if match found
                if(isCorrect){
                    total++;
                }
            }
        }
        catch(Exception e){
            System.out.print("line not found");
        }
        return total;
    }


    public static void main(String[] args) {
        Spelling newTrie = new Spelling();
        Random rand = new Random();
        
        //read in common words file
        try (Scanner scanner = new Scanner(new File("unigram_freq.csv"));) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                newTrie.addToTrie(scanner.nextLine());
            }
            scanner.close();
        }
        catch(FileNotFoundException e){
            System.out.println("File not found.");
        }
        
        //read in misspellings file
        try (Scanner scan = new Scanner(new File("misspelling.csv"));) {
            scan.nextLine();
            while (scan.hasNextLine()) {
                try (Scanner rowScanner = new Scanner(scan.nextLine())) {
                    rowScanner.useDelimiter(",");
                    while (rowScanner.hasNext()) {
                    	String correct = rowScanner.next();
                        misspellings.put(rowScanner.next(), correct);
                    }
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
        	System.out.println("File not found.");
		}
        
        String correct = "";
        String alternate = "";
        
        for (String word: args) {
        	if (misspellings.containsKey(word)) {
        		correct += misspellings.get(word);
        		alternate += misspellings.get(word);
        	}
        	else {
				List<List<String>> suggestions = newTrie.suggest(word, 5);
	        	if (suggestions.get(suggestions.size()-1).contains(word)) {
	        		correct+= word;
	        		alternate += word;
	        	}
	        	else {
	        		String[] choices = {suggestions.get(suggestions.size()-1).get(0), suggestions.get(suggestions.size()-2).get(0), suggestions.get(suggestions.size()-1).get(1), suggestions.get(suggestions.size()-2).get(1)};
	        		correct += choices[rand.nextInt(3)];
	        		alternate += choices[rand.nextInt(3)];
	
	        	}
        	}
        	correct += " ";
        	alternate += " ";
        	
        }
        System.out.println(correct);
        System.out.println(alternate);
        
        Server server = new Server(8080);
		try {
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setDirectoriesListed(true);
			resourceHandler.setResourceBase("./src/main/resources/spider");

			ServletHandler servletHandler = new ServletHandler();
			servletHandler.addServletWithMapping(speller.BulmaServlet.class, "/home");

			HandlerList handlers = new HandlerList();
			handlers.addHandler(resourceHandler);
			handlers.addHandler(servletHandler);
			server.setHandler(handlers);
			server.start();
			server.join();
		} catch (Exception E) {
			System.out.print("Error when attempting to start server.");
		}
    }


}
