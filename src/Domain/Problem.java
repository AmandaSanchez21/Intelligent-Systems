package Domain;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import persistance.FileHandler;
import persistance.InputExceptions;

public class Problem {
	
	private static final char[] CONSTS_HEX = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };

	public static void main (String[]args) throws IOException, InputExceptions {
		State t = new State(); 
		FileHandler.readFile(t);
		State newState = t; 
		int [][]field = t.getField();
		boolean optimization;
		
		
		System.out.println();
		
		for (int i1=0; i1<field.length; i1++) {
			for(int j=0; j<field.length; j++) {
				System.out.print(field[i1][j]);
			}
			System.out.println();
		}
		List<Action>actions = State.successor(t);
		List<Movement> movements = State.moveTractor(t);
		System.out.println();
		System.out.println("Possible movements: ");
		
		for (int i=0; i<movements.size(); i++) {
			movements.get(i).printMove();
		}
		
		System.out.println();
		System.out.println();
		
		System.out.println("Possible actions: ");
		
		/*for(int i=0; i<actions.size(); i++) {
			System.out.println("[(" + actions.get(i).getNext_move().getX() + "," + actions.get(i).getNext_move().getY() + ")"  + " N:" +
		actions.get(i).getSand_n() + " S:" + actions.get(i).getSand_s() + " E:" + actions.get(i).getSand_e() + " W:" + actions.get(i).getSand_w() + "]");
		}*/
		
		
		String strategy;
		Scanner sc = new Scanner(System.in);
		System.out.println("Please, enter the strategy\nBFS, DFS, DLS, ILS, UCS or A*");
		strategy = sc.nextLine();
		strategy = strategy.toUpperCase();
		
		while (!strategy.equals("BFS") && !strategy.equals("DFS") && !strategy.equals("DLS") && !strategy.equals("ILS") && !strategy.equals("UCS") && !strategy.equals("A*")) {
			System.out.println("Please, enter a correct strategy \nBFS, DFS, DLS, ILS, UCS or A*");
			strategy = sc.nextLine().toUpperCase();
		}
		
		System.out.println("Now, enter the depth desired.");
		int depth = sc.nextInt();
		
		while(depth<=0) {
			System.out.println("Please, enter a valid depth");
			depth = sc.nextInt();
		}
		
		System.out.println("Do you want to use optimization? (Y/N)");
		String opt = sc.next().toUpperCase();
		
		while(!opt.equals("Y") && !opt.equals("N")) {
			System.out.println("Please, enter a valid option (Y/N)");
			opt = sc.next().toUpperCase();
		}
		
		if (opt.equals("Y")) {
			optimization = true;
		} else {
			optimization = false;
		}
		
		long time = System.currentTimeMillis();
		List<Node> solution = boundedSearch(t, strategy, depth, optimization);
		long time1 = System.currentTimeMillis();
		long total = time1 - time;
		System.out.println(total);
		printSolution(solution);
	
	}
	
	
	public static List<Node> boundedSearch(State st, String strategy, int max_depth, boolean opt) throws IOException {
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		Node initial_node = new Node(st, 0, 0, null); //Initial node with the initial state, cost and depth are 0 and there's no reference to a parent
		initial_node.selectValueNode(strategy);
		frontier.offer(initial_node);
		Node current_node = new Node();
		
		Hashtable<String, Integer> visited = new Hashtable<String, Integer>();
		visited.put(encriptaEnMD5(initial_node.getState()), initial_node.getValue());
		
		boolean solution = false;

		
		while(!frontier.isEmpty() && solution==false) {
			current_node = frontier.poll(); //Removes the node which is in the head of the queue
			
			
			if(isGoal(current_node.getState())) {
				solution = true;
			} else {
				List<Action> actions = State.successor(current_node.getState()); //Generates all the possible actions
				List<Node> nodes = createNodeList(actions, current_node, max_depth, strategy, current_node.getState()); //Creates a node list where each node has a new state, 
																														//generated after applying the corresponding action
				
				if(opt) {
					checkVisited(nodes,visited);
				}
				
				
				for(int i=0; i<nodes.size(); i++) {
					frontier.offer(nodes.get(i));
				}	 
			}
		}
		
		/*for(int i=0; i<frontier.size(); i++) {
			System.out.print(frontier.remove().getValue());
		}
		System.out.println();*/
		
		if(solution) {
			return createSolution(current_node);
		} else {
			System.out.println("No solution");
			return null;	
		}
	}
	
	
	
	/*public static void checkVisited(List<Node> nodes, List<State> visited) {
		for (int i=0; i<nodes.size(); i++) {
			State st = nodes.get(i).getState();
			int [][] field = st.getField();
			for (int j=0; j<visited.size(); j++) {
				int [][] field1 = visited.get(j).getField();
				for(int x=0; x<field.length; x++) {
					for(int z=0; z<field.length; z++) {
						if(field[x][z]==field1[x][z]) {
							if(st.getValue() < visited.get(j).getValue()) {
								visited.get(j).setValue(st.getValue());
								nodes.remove(i);
							}
						}
					}
				}
			}
		}
	}*/
	
	
	public static void checkVisited (List<Node> nodes, Hashtable<String, Integer> visited) {
		for (int i=0; i<nodes.size(); i++) {
			if (visited.containsKey(encriptaEnMD5(nodes.get(i).getState()))) {
				if(nodes.get(i).getValue() < visited.get(encriptaEnMD5(nodes.get(i).getState()))) {
					visited.replace((encriptaEnMD5(nodes.get(i).getState())), nodes.get(i).getValue());
					nodes.remove(i);
				}
			}
		}
	}


	public static boolean isGoal (State st) {
		int [][] field = st.getField();
		
		for (int i=0; i<field.length; i++) {
			for (int j=0; j<field.length; j++) {
				if(field[i][j] != st.getK()) {
					return false;
				}
			}
		}	
		return true;
	}
	
	public static State applyAction (State st, Action ac) {
		
		int [][] newField = st.getField();
		int pos = newField[st.getX()][st.getY()]; //Amount of sand in the current position 
		int movedSand = ac.getSand_e() + ac.getSand_n() + ac.getSand_s() + ac.getSand_w(); //Totally amount of sand to move
		int newPos = pos - movedSand;
		newField[st.getX()][st.getY()] = newPos; //Remaining sand in the current position 
		/* If there is sand to move to the North, East, West or South && if it is possible to move to the North, East, West or South*/
		if(ac.getSand_n() > 0 && st.getX()+1 >= 0) {
			newField[st.getX()-1][st.getY()] += ac.getSand_n(); //Update the amount of sand of the field
		} 
		if(ac.getSand_s() > 0 && st.getX()+1 < newField.length) {
			newField[st.getX()+1][st.getY()] += ac.getSand_s();
		} 
		if(ac.getSand_w() > 0 && st.getY()-1 >= 0) {
			newField[st.getX()][st.getY()-1] += ac.getSand_w();
		} 
		if(ac.getSand_e() > 0 && st.getY()+1 < newField.length) {
			newField[st.getX()][st.getY()+1] += ac.getSand_e();
		}
	
		State newState = State.copyState(st, ac, newField);

		return newState;
	}
	
	public static List<Node> createNodeList (List<Action> actions, Node cn, int depth, String strategy, State st) {
		List <Node> nodes = new ArrayList<Node>();
		int new_depth = cn.getDepth() +1;
		if(cn.getDepth() + 1 <= depth) {
			for(int i=0; i<actions.size(); i++) {
				State c_state = applyAction(st, actions.get(i));
				Node aux = new Node(c_state, cn.getCost()+State.cost(actions.get(i)), new_depth, cn);
				aux.selectValueNode(strategy);
				aux.getState().setValue(aux.getValue());
				aux.setAction(actions.get(i));
				nodes.add(aux);
			}
		}
		
		return nodes;
	}
	
	
	public static List<Node> createSolution(Node current_node) {
		List <Node> solution = new ArrayList<Node>();
		
		while (current_node != null) {
			solution.add(current_node);
			current_node=current_node.getFather();
		}
		
		return solution;
	}
	
	/*public static void checkVisited (List<Node> nodes, Hashtable<State, Integer> visited) {
		for (int i=0; i<nodes.size(); i++) {
			State st = nodes.get(i).getState();
			
			if(visited.containsKey(st)) {
				if(nodes.get(i).getValue() < visited.get()
			}
		}
	}*/
	
	public static void printSolution (List<Node>solution) {
		int cost=0;
		for (int i=solution.size()-1; i>=0; i--) {
			Node node = solution.get(i);

			if(i==solution.size()-1) {
				System.out.println("Initial Field: ");
				node.getState().printField();
				System.out.println();
			} else {
				cost += State.cost(node.getAction());
				State st = node.getState();
				
				/*System.out.println(node.getAction().getNext_move().getX());
				System.out.println(node.getAction().getNext_move().getY());
				int north = st.getX()-1, south = st.getX()+1, east = st.getY()+1, west = st.getY()-1;
				System.out.println("Action: [(" + solution.get(i).getAction().getSand_n() + ", (" + st.getX() + "," + st.getY() + ")) ," 
						+ "(" + solution.get(i).getAction().getSand_s() + ", (" + st.getX()+ "," + st.getY() + "))"
						+ "(" + solution.get(i).getAction().getSand_w() + ", (" + st.getX() + "," + west + "))"
						+ "(" + solution.get(i).getAction().getSand_e() + ", (" + st.getX() + "," + east + "))]");*/
				
				System.out.println(node.getAction().toString() + node.getAction().getNext_move().printMove() + " Cost of Action: " + State.cost(node.getAction()) + " Total cost " + cost);
				
				int [][] field = st.getField();
				
				for(int j=0; j<field.length; j++) {
					for(int x=0; x<field.length; x++) {
						System.out.print(field[j][x]);
					}
					System.out.println();
				}
				System.out.println();
			}
		}
		
		System.out.println("The total cost is " + cost + " and the total depth is " + solution.get(0).getDepth());
	}
	
	
	
	
	
	public static String encriptaEnMD5(State st) {
		try {
			int message = st.getK() + st.getMax() + st.getN_cols() + st.getN_rows() + st.getX() + st.getY();
			for (int i=0; i<st.getField().length; i++) {
				for(int j=0; j<st.getField().length; j++) {
					message += st.getField()[i][j];
				}
			}
			MessageDigest msgd = MessageDigest.getInstance("MD5");
			byte[] bytes = msgd.digest(intToByteArray(message));
			StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
			for (int i = 0; i < bytes.length; i++) {
				int bajo = (int) (bytes[i] & 0x0f);
				int alto = (int) ((bytes[i] & 0xf0) >> 4);
				strbCadenaMD5.append(CONSTS_HEX[alto]);
				strbCadenaMD5.append(CONSTS_HEX[bajo]);
			}
			return strbCadenaMD5.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
}