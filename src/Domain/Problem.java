package Domain;
import java.io.*;
import java.util.*;

import persistance.FileHandler;
import persistance.InputExceptions;

public class Problem {
	
	public static void main (String[]args) throws IOException, InputExceptions {
		State t = new State(); 
		FileHandler.readFile(t);
		State newState = t; 
		int [][]field = t.getField();
		
		System.out.println();
		
		for (int i1=0; i1<field.length; i1++) {
			for(int j=0; j<field.length; j++) {
				System.out.print(field[i1][j]);
			}
			System.out.println();
		}
		
		
		List<Movement> movements = State.moveTractor(t);
		System.out.println();
		System.out.println("Possible movements: ");
		
		for (int i=0; i<movements.size(); i++) {
			movements.get(i).printMove();
		}

		System.out.println();
		System.out.println();
		

		frontierQueue(t);
		int [][] field1 = t.getField();
		for(int i=0; i<field1.length;i++) {
			for(int j=0; j<field1.length;j++) {
				System.out.print(field1[i][j]);
			}
			System.out.println();
		}
		frontierList(t);
		
		/*System.out.println("Possible actions: ");
		for(int i=0; i<actions.size(); i++) {
			System.out.println("[(" + actions.get(i).getNext_move().getX() + "," + actions.get(i).getNext_move().getY() + ")"  + " N:" +
		actions.get(i).getSand_n() + " S:" + actions.get(i).getSand_s() + " E:" + actions.get(i).getSand_e() + " W:" + actions.get(i).getSand_w() + "]");
		}
		*/
		
		
	}	
	
	public static void frontierQueue (State t) {
		List<Action> actions = new ArrayList<Action>();
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		
		Node initialState = new Node();
		initialState.setState(t);
		frontier.offer(initialState);
		
		double max = 0;
		double min = 10000000;
		double average=0;
		double time1 = 0;
		double time2;
		double t_time = 0;
		
		List<Double> times = new ArrayList<Double>();
		
		while(!frontier.isEmpty() && !isGoal(frontier.peek().getState())) {
			time1 = System.currentTimeMillis();
			actions = State.successor(frontier.peek().getState());
			State newState = new State();
			State currentState = frontier.poll().getState();
			
			for (int i=0; i<actions.size(); i++) {
				newState = applyAction(currentState, actions.get(i));
				newState.setN_cols(currentState.getN_cols());
				newState.setN_rows(currentState.getN_rows());
				Node aux = new Node();
				aux.setState(newState);
				frontier.offer(aux);
				time2 = System.currentTimeMillis();
				t_time= time2-time1; 
				times.add(t_time);
				
				if(t_time < min) {
					min = t_time;
				} else if (t_time > max) {
					max = t_time;
				}
			}	
		}
		System.out.println();
		System.out.println("Frontier implemented with queue: ");
		System.out.println("Minimo: " + min);
		System.out.println("Maximo: " + max);
		
		for(int i=0;i<times.size(); i++) {
			average += times.get(i);
		}
		System.out.println("Average: " + average/times.size());

	}
	
	
	public static void frontierList (State t) {
		List <Action> actions = new ArrayList<Action>();
		List <Node> frontier = new ArrayList<Node>();
		
		Node initialState = new Node();
		initialState.setState(t);
		frontier.add(initialState);
		System.out.println(frontier.size());
		
		double max = 0;
		double min = 10000000;
		double average=0;
		double time1 = 0;
		double time2;
		double t_time = 0;
		
		List<Double> times = new ArrayList<Double>();
		
		while(!frontier.isEmpty() && !isGoal(frontier.get(0).getState())) {
			time1 = System.currentTimeMillis();
			actions = State.successor(frontier.get(0).getState());
			State newState = new State();
			State currentState = frontier.remove(0).getState();
			
			for (int i=0; i<actions.size(); i++) {
			
				newState = applyAction(currentState, actions.get(i));
				newState.setN_cols(currentState.getN_cols());
				newState.setN_rows(currentState.getN_rows());
				Node aux = new Node();
				aux.setState(newState);
				frontier.add(aux);
				time2 = System.currentTimeMillis();
				t_time= time2-time1; 
				//System.out.println(t_time);
				times.add(t_time);
				
				Collections.sort(frontier, new Comparator<Node>() {
					public int compare(Node n1, Node n2) {
						return new Integer(n1.getValue()).compareTo(new Integer(n2.getValue()));

					}
				});
				
				if(t_time < min) {
					min = t_time;
				} else if (t_time > max) {
					max = t_time;
				}
			}	
		}
		System.out.println();
		System.out.println("Frontier implemented with List: ");
		System.out.println("Minimo: " + min);
		System.out.println("Maximo: " + max);
		
		for(int i=0;i<times.size(); i++) {
			average += times.get(i);
		}
		System.out.println("Average: " + average/times.size());
		
		
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
		State newState = new State();
		int [][] newField = st.getField();
		int pos = newField[st.getX()][st.getY()];
		int currentSand = ac.getSand_e() + ac.getSand_n() + ac.getSand_s() + ac.getSand_w();
		int newPos = pos - currentSand;
		newField[st.getX()][st.getY()] = newPos;
		
	
		
		if(ac.getSand_n() > 0 && st.getX()-1 > 0 && newField[st.getX()-1][st.getY()] + ac.getSand_n() <= st.getMax()) {
			newField[st.getX()-1][st.getY()] += ac.getSand_n();
		} else if(ac.getSand_s() > 0 && st.getX()+1 < newField.length && newField[st.getX()+1][st.getY()] + ac.getSand_s() <= st.getMax()) {
			newField[st.getX()+1][st.getY()] += ac.getSand_s();
		} else if(ac.getSand_w() > 0 && st.getY()-1 > 0 && newField[st.getX()][st.getY()-1] + ac.getSand_w() <= st.getMax()) {
			newField[st.getX()][st.getY()-1] += ac.getSand_w();
		} else if(ac.getSand_e() > 0 && st.getY()+1 < newField.length && newField[st.getX()][st.getY()+1] + ac.getSand_e() <= st.getMax()) {
			newField[st.getX()][st.getY()+1] += ac.getSand_e();
		}
		
		newState.setField(newField);
		newState.setX(ac.getNext_move().getX());
		newState.setY(ac.getNext_move().getY());
		newState.setK(st.getK());
		newState.setMax(st.getMax());
		

		return newState;
	}

}