package usefulMethods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * 
 * Csv parser readCSVtoStringList taken from stackoverflow
 * https://stackoverflow.com/questions/40074840/reading-a-csv-file-into-a-array
 * @author Michael Lihs. Modified by Y. Oguns.
 * 
 * This class is used to read and write CSVs
 *
 */
public class CsvParser {

		public static void main(String[] args) throws IOException {
        // String fileName= "read_ex3.csv";
        // List<List<String>> csv= readCSVtoStringList(fileName);
        
    	String occupationRLI = "src\\industry\\OccupationsRLINoLabels.csv";	  
    	List<List<String>> csv= readCSVtoStringList(occupationRLI);
    	
    	
    	
        double[][] output = convertListtoArray(csv);
        
        String name = "test2";
        ArrayList<ArrayList<Double>> array = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> no1 = new ArrayList<Double>();        
        ArrayList<Double> no2 = new ArrayList<Double>();
        no1.add(1.0);
        no1.add(2.0);
        no1.add(3.0);
        no1.add(4.0);
        
        no2.add(7.0);
        no2.add(6.0);
        
        array.add(no1);
        array.add(no2);
        
        doubleNestedArrayListToCSV(name, array);
 
    }
        public static List<List<String>> readCSVtoStringList(String filepath) {

        // this gives you a 2-dimensional array of strings
        List<List<String>> lines = new ArrayList<>();
        File file= new File(filepath);
        Scanner inputStream;

        try{
            inputStream = new Scanner(file);

            while(inputStream.hasNext()){
                String line= inputStream.next();
                String[] values = line.split(",");
                // this adds the currently parsed line to the 2-dimensional string array
                lines.add(Arrays.asList(values));
            }

            inputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // the following code lets you iterate through the 2-dimensional array
        int lineNo = 1;
        for(List<String> line: lines) {
            int columnNo = 1;
            for (String value: line) {
                System.out.println("Line " + lineNo + " Column " + columnNo + ": " + value);
                columnNo++;
            }
            lineNo++;
        }

        return lines;
    }

        public static double[][] convertListtoArray(List<List<String>> inputList){
			
        	int  rows = inputList.size();
        	int columns= inputList.get(0).size();
        	
        	System.out.println(rows);
        	System.out.println(columns);
        	
        	double[][] outputArray = new double[rows][columns];
        	String currentValue;
        	// get size
        	System.out.println("columns: " + columns+ "\nrows: " + rows);
        	// for each column populate rows
        	//return null;
	
        	for(int i=0; i<rows; i++) {
        		for(int j = 0; j< columns; j++) {
        			currentValue = inputList.get(i).get(j);
        			outputArray[i][j] = Double.parseDouble(currentValue);
        			System.out.println("row: " + (i+1) + " column: " + (j+1) + 
        					" value: " + outputArray[i][j]);
        		}
        	}
        	return outputArray;
        }
        
        public static String[] getRowLabels(List<List<String>> inputList) {
			// first entry of each row
        	int  rows = inputList.size();
        	
        	String[] labels = new String[rows];
        	
        	for(int i=0; i< rows; i++) {
        		labels[i] = inputList.get(i).get(0);
        		System.out.println(labels[i]);
        	}
        	return labels;
        	
        }
        
        public static String[] getColumnLabels(List<List<String>> inputList) {
			// first row
        	int  rows = inputList.size();
        	
        	int columns = 0;
        	
        	//try this line. If error, set columns to 
        	// put in try catch loop
        	try {
        		columns = inputList.get(1).size();
        		// if no row exists, then ROW VECTOR, not grid
        	}
        	catch(IndexOutOfBoundsException e){
        		columns = inputList.get(0).size();
        		System.out.println("catch columns: " + columns);
        	}
        	        	
        	finally{
        	
        	System.out.println("finally catch: " + columns);
        	String[] labels = new String[columns];
        	
        	for(int i=0; i< columns; i++) {
        		labels[i] = inputList.get(0).get(i);
        		System.out.println(labels[i]);
        	}
        	
        	return labels;
        	}
        	
        	
        	
        }
        
        public static List<List<String>> removeLabelsFromList(List<List<String>> inputList) {
			
        	List<List<String>> outputList = new ArrayList();
        	List<List<String>> tempList = new ArrayList();
        	List<String> temp = new ArrayList();
        	//List<String> temp2 = new ArrayList();
        	
        	int columnCount;
        	tempList = inputList;
        	
        	System.out.println("pre removal size is " + tempList.size());
        	tempList.remove(0);
        	System.out.println("removed row size is " + tempList.size());
        	
        	// for each row, remove 0th element
        	for(int i = 0; i< tempList.size(); i++) {
        		temp = tempList.get(i);
        		columnCount = temp.size();
        		System.out.println("size is "+ columnCount);
            	List<String> temp2 = new ArrayList();
	
        			for(int j = 0; j < columnCount; j++) {
        				if(j !=0) {
	        				System.out.println("value is " + temp.get(j));
	        				temp2.add(temp.get(j));
        				}
        			}
    
        		outputList.add(temp2);
        	}
            return outputList;
        }
        
        public static void printStringList(List<List<String>> inputList) {
        	
        	int  rows = inputList.size();
	
        	for(int i=0; i<rows; i++) {
            	
        		int columns= inputList.get(i).size();
            	String currentValue;
        		
            	for(int j = 0; j< columns; j++) {

        			currentValue = inputList.get(i).get(j);
        			System.out.println("row: " + (i+1) + " column: " + (j+1) + 
        					" value: " + currentValue);
        		}
        	}
        }
        
        public static void printIntArray(int[][] inputArray) {
        	int  rows = inputArray.length;
        	int columns= inputArray[0].length;
        	
        	int currentValue;
	
        	for(int i=0; i<rows; i++) {
        		for(int j = 0; j< columns; j++) {
        			
        			currentValue = inputArray[i][j];
        			System.out.println("row: " + (i+1) + " column: " + (j+1) + 
        					" value: " + currentValue);
        		}
        	}
        }

        public static void printDoubleArray(double[][] inputArray) {
        	int  rows = inputArray.length;
        	int columns= inputArray[0].length;
        	
        	double currentValue;
	
        	for(int i=0; i<rows; i++) {
        		for(int j = 0; j< columns; j++) {
        			
        			currentValue = inputArray[i][j];
        			System.out.println("row: " + (i+1) + " column: " + (j+1) + 
        					" value: " + currentValue);
        		}
        	}
        }
        
       
        public static void listToCSV() {
        	
        }
        
        public static void doubleArrayListToCSV(String name, ArrayList<Double> arrayList) throws IOException {
        	File fileName = new File(name+".csv");
        	//ArrayList<Integer> arrayList = new ArrayList<>();
        	//arrayList.add(1);
        	//arrayList.add(3);
        	//arrayList.add(5);
        	
        	FileWriter fw = new FileWriter(fileName);
        	Writer testOut = new BufferedWriter(fw);
        	
        	int size = arrayList.size();
        	for(int i = 0; i < size; i++) {
        		testOut.write(arrayList.get(i).toString()); //+"\n");;
        	}
        	testOut.close();
        }
        
        public static void integerArrayListToCSV(String name, ArrayList<Integer> arrayList) throws IOException {
        	File fileName = new File(name+".csv");
        	//ArrayList<Integer> arrayList = new ArrayList<>();
        	//arrayList.add(1);
        	//arrayList.add(3);
        	//arrayList.add(5);
        	
        	FileWriter fw = new FileWriter(fileName);
        	Writer testOut = new BufferedWriter(fw);
        	
        	int size = arrayList.size();
        	for(int i = 0; i < size; i++) {
        		testOut.write(arrayList.get(i).toString() +",");
        	}
        	testOut.close();
        }
        
        public static void doubleNestedArrayListToCSV(String name, ArrayList<ArrayList<Double>> arrayList) throws IOException {
        	File fileName = new File(name+".csv");
        	//ArrayList<Integer> arrayList = new ArrayList<>();
        	//arrayList.add(1);
        	//arrayList.add(3);
        	//arrayList.add(5);
        	
        	FileWriter fw = new FileWriter(fileName);
        	Writer testOut = new BufferedWriter(fw);
        	
        	
        	int lists = arrayList.size();
        	System.out.println("Lists size: "+ lists);
        	
        	for(int col = 0; col < lists; col++) {
        	
        		int size = arrayList.get(col).size();
        	
	        	for(int i = 0; i < size; i++) {
	        		
	        		testOut.write(arrayList.get(col).get(i).toString() + ","); //+"\n");;
	        	}
	        	testOut.write( "\n"); //+"\n");
        	}
        	
        	testOut.close();
        }
}

