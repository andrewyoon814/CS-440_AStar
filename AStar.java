import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

public class AStar{

	public static void main(String[] args){


		// Get file name from user
		System.out.println("What is the name of the file?");
		Scanner scanner = new Scanner(System.in);
		String fileName = scanner.nextLine();

		
		//get start coordinates
		// All coordinates are decremented bc an array's range is from 0 to n-1.
		System.out.println("What X coordinate of start?");
		int startX = scanner.nextInt();	
		startX--;	
		System.out.println("What Y coordinate of start?");
		int startY = scanner.nextInt();	
		startY--;	

		
		//get start coordinates
		System.out.println("What X coordinate of goal?");
		int goalX = scanner.nextInt();	
		goalX--;	
		System.out.println("What Y coordinate of goal?");
		int goalY = scanner.nextInt();	
		goalY--;

		
		scanner.close();

		//Create file object
		File mazeFile = new File(fileName);
		
		
		//check if such file exists or not.
		if( !mazeFile.exists() ){
			System.out.println(fileName + " was not found.");
			return;
		}

		
		//Call methods that gets number of columns and rows
		int col = getColumns(fileName);
		int row = getRows(fileName);

		
		//method to read file and set the maze array
		//0's are obstacles and 1's are possible paths
		Node[][] maze = readAndSet(fileName, row, col);

		
		//sets heuristic value for all nodes for easy access later on
		setHeuristic(maze, goalX, goalY, row, col);

		
		//will calculate path 
		calculatePath(maze, startX, startY, goalX, goalY);

		
		//Beginning of the End... recreate the path into a .txt file
		//Felt lazy so I will print from End to Start.

		//tmp traversal node...starts off on the end
		Node trav = maze[goalY][goalX];
		
		//step counter
		int steps = 0;

		try{

			PrintWriter writer = new PrintWriter("AStar_Path.txt", "UTF-8");

		
			//write file name, start and goal coordinates
			writer.println("File Name is: " + fileName);
			writer.println("Start coordinate is: {" + startX + "," + startY + "}");
			writer.println("Goal coordinate is: {" + goalX + "," + goalY + "}");

		
			writer.println("End");

			while(trav != maze[startY][startX]){
				
				steps++;

				writer.println("{" + trav.x + "," + trav.y + "}");

				trav = maze[trav.parenty][trav.parentx];
			}

		
			//Print the start node
			writer.println("{" + trav.x + "," + trav.y + "}");
			writer.println("Start");

			writer.println("Total steps are: " + steps);

			writer.close();
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return;
	}


	private static void calculatePath(Node[][] maze, int startX, int startY, int goalX, int goalY){

		//set up of comparator function and open and closed q's
		Comparator<Node> queueComparator = new NodeComparator();
		PriorityQueue<Node> open = new PriorityQueue<Node>(10, queueComparator);
		PriorityQueue<Node> closed = new PriorityQueue<Node>(10, queueComparator);

		//add start state to open list
		maze[startY][startX].fVal = 0;
		maze[startY][startX].gVal = 0;

		open.add(maze[startY][startX]);

		while( open.size() != 0 ){
			
			//pop from open and add to close
			Node current = open.remove();
			closed.add(current);

			//check if current is goal
			if( current == maze[goalY][goalX] ){
				return;
			}

			//get neighbors
			Node[] neighbors = getNeighbors(maze, current.y, current.x);

			int count;

			//go through all neighbors
			for(count = 0; neighbors[count] != null; count++){

				//if its an obstacle... skip
				if(neighbors[count].val == 0){
					continue;
				} 

				//if its in closed...skip
				if(closed.contains(neighbors[count]) ){
					continue;
				}

				//if not in open list, add it in to list
				if(open.contains(neighbors[count]) == false){
					neighbors[count].parentx = current.x;
					neighbors[count].parenty = current.y;
					neighbors[count].gVal= current.gVal + 1;
					neighbors[count].fVal = neighbors[count].gVal + neighbors[count].hVal;
					
					open.add(neighbors[count]);

				}else{

				}

			}
		}
		
	}


	// Method returns a node array of neighbors
	private static Node[] getNeighbors(Node[][] maze, int col, int row){
	
		Node[] neighbors = new Node[6];
		int count = 0;

		//left
		if(col != 0){

			neighbors[count] = maze[col - 1][row];
			count++;

		}

		//right
		if(col != 100){

			neighbors[count] = maze[col + 1][row];
			count++;			
		}

		//down
		if(row!= 100){

			neighbors[count] = maze[col][row + 1];
			count++;
		}

		//up
		if(row != 0){
			neighbors[count] = maze[col][row-1];
			count++;
		}		
		return neighbors;
	}

	

	private static void setHeuristic(Node[][]maze, int goalX, int goalY, int row, int col){

		double h = 0;
		double x = 0;
		double y = 0;

		for( int tmpCol = 0; tmpCol != col; tmpCol++){
			for(int tmpRow = 0; tmpRow != row; tmpRow++){

				//Calculates the euclidian distance from current box to goal box
				x = Math.abs(goalX - maze[tmpCol][tmpRow].x)*Math.abs(goalX - maze[tmpCol][tmpRow].x);
				y = Math.abs(goalY - maze[tmpCol][tmpRow].y)*Math.abs(goalY - maze[tmpCol][tmpRow].y);
				h = Math.sqrt(x + y);

				//sets the h value according to value got on top
				maze[tmpCol][tmpRow].hVal = h;

			}

		}
		return;

	}
	
	private static Node[][] readAndSet(String fileName, int row, int col){

		Node[][]maze = new Node[col][row];
		try{

			BufferedReader fileInput = new BufferedReader(new FileReader(fileName));
	    	
			//set up of needed variables
	    	String s = null;
	    	char[] tmpArray = null;
	    	int tmpRow = 0; 
	    	int tmpCol = 0;


	    	//Go through each column of the maze in the .txt file
	    	while( tmpCol != col ){

	    		//read individual like and turn into char array
	    		s = fileInput.readLine();
	    		tmpArray = s.toCharArray();

	    		//Goes throuh the rows
	    		while(tmpRow != row){
	    			//Initialize 2d array node objects
	    			maze[tmpCol][tmpRow] = new Node();
	    			
	    			//determing if free or closed
	    			if( tmpArray[tmpRow] == '.'){

	    				// if free set to 1 and set coordinate values
	    				maze[tmpCol][tmpRow].val = 1;
	    				maze[tmpCol][tmpRow].x = tmpRow;
	    				maze[tmpCol][tmpRow].y = tmpCol;
	    			}else{

	    				//if obstacle set to 0 and set coordinate values
	    				maze[tmpCol][tmpRow].val = 0;
	    				maze[tmpCol][tmpRow].x = tmpRow;
	    				maze[tmpCol][tmpRow].y = tmpCol;
	  				}

	  					    			System.out.print(maze[tmpCol][tmpRow].val);
	    			tmpRow++;
	    		}

	    		System.out.println(" ");
	    		//up the counter for the col and reset row variable back to 0
	    		tmpCol++;  
	    		tmpRow = 0;

	    	}
    	}catch(IOException ex){
    		ex.printStackTrace();
    	}	

    	return maze;

	}


	private static int getColumns(String fileName){

		int col = 0; 

		try{
			//set up file reader
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			//read and clean
			String thisLine = br.readLine();
			br.close();

			col = thisLine.length();

			return col;
		}catch(IOException ex){
			ex.printStackTrace();
		}		

		return col;
	}

	
	private static int getRows(String fileName){

		int rows = 0;

		try{

			//set up file reader
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		
			//increment for amount of rows
			while( br.readLine() != null){
				rows++;
			}

			//clean up
			br.close();
			return rows;

		}catch(IOException ex){
				ex.printStackTrace();
		}

		return rows;
	}
}


//Node class will make life alot easier.
//Each node represents a box in the maze
//allows for easy access to heuristic, and coordinate 
class Node{

	int val;  //is it an obstacle or free space? 0 or 1
	double hVal;   // heuristic value
	double gVal;  
	double fVal;
	int x;     // x-coordinate
	int y;     // y coordinate
	int parentx;
	int parenty;

}

//Comparator function for primary queue 
class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node x, Node y){

		if(x.fVal > y.fVal){
			return 1;
		}else if(x.fVal < y.fVal){
			return -1;
		}
		return 0;
	}

}