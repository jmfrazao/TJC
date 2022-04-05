package play;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;
import reader.NonBlockingReader;

public class ShortBetrayed extends Strategy{

	private List<GameNode> getReversePath(GameNode current) {		
		try {
			GameNode n = current.getAncestor();
			List<GameNode> l =  getReversePath(n);
			l.add(current);
			return l;
		} catch (GameNodeDoesNotExistException e) {
			List<GameNode> l = new ArrayList<GameNode>();
			l.add(current);
			return l;
		}
	}
	
	private Boolean cumputeStrategy(List<GameNode> listP1, 
			List<GameNode> listP2) throws GameNodeDoesNotExistException {
	
		Set<String> oponentMoves = new HashSet<String>();
		String p1play = "";
		String p2play = "";
		
		//When we played as Player1 we are going to check what were the moves
		//of our opponent as player2.
		for(GameNode n: listP1) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer2()) {
				p2play = n.getLabel();
			}
		}
		
		//When we played as Player2 we are going to check what were the moves
		//of our opponent as player1.
		for(GameNode n: listP2) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer1()) {
				p1play = n.getLabel();
			}
		}
		
		if(p1play.compareTo("1:1:Defect") == 0|| p2play.compareTo("2:1:Defect")== 0) {
			return true;
		}
		
		else
			return false;
		
						
	}
	@Override
	public void execute() throws InterruptedException {
		// TODO Auto-generated method stub

		SecureRandom random = new SecureRandom();

		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}

		GameNode finalP1 = null;
		GameNode finalP2 = null;
		
		Boolean goRogue = false;
		int[] stratValues = {1,0,1,0};
		int counter = 0;
	
		while(true) {
			
			PlayStrategy strat = this.getStrategyRequest();
			if(strat == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
			while(! playComplete ) {
				if(strat.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(strat.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}

				if(strat.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(strat.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}

				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = strat.keyIterator();
				
				if(finalP1 != null && finalP2 != null && !goRogue) {
					List<GameNode> listP1 = getReversePath(finalP1);
					List<GameNode> listP2 = getReversePath(finalP2);
					try {
						goRogue = this.cumputeStrategy(listP1, listP2);
					} catch( GameNodeDoesNotExistException e ) {
						System.err.println("PANIC: Strategy structure does not match the game.");
					}
					if(counter >= 19)
						goRogue = true;
					if(goRogue) {
						stratValues[0] = 0;
						stratValues[1] = 1;
						stratValues[2] = 0;
						stratValues[3] = 1;
						
						
					}
				}
				
				
				for(int i = 0; i<stratValues.length; i++) {
					if(!keys.hasNext()) {
						System.err.println("PANIC: Strategy structure does not match the game.");
						return;
					}
					strat.put(keys.next(), (double) stratValues[i]);
				}
				try{
					this.provideStrategy(strat);
					playComplete = true;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());;
					e.printStackTrace(System.err);
				} 
				counter++;
				
			}
		}

	}
	

}
