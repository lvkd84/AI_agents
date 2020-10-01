package P3Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.agent.minimax.GameStateChild;
import edu.cwru.sepia.agent.minimax.GameState;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class MinimaxAlphaBeta extends Agent {

    //enum Turn {
    //  Min,
    //  Max
    //}

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }



    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY)[0];
        System.out.println(bestChild.action);
        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild[] alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta) {
      if (depth <= 0){
         return new GameStateChild[]{node,node};
      }
    	  int turn = node.state.getTurn();
      GameStateChild next = null;
      GameStateChild best = null;
      Queue<GameStateChild> children = new PriorityQueue<GameStateChild>();
      if(node != null){
    	  	for (GameStateChild child : node.state.getChildren()) {// COMMENT HERE
    	  		System.out.println(child.state.getUtility());
    	  		System.out.println(child.action);
    	  	}
        children = orderChildrenWithHeuristics(node.state.getChildren());
      }
      if(turn%2 == 0){ //MAX
        double val = Double.NEGATIVE_INFINITY;
        double utilVal = Double.POSITIVE_INFINITY;
        for(GameStateChild childNode : children) {
          GameStateChild newAlphaBeta = alphaBetaSearch(childNode, depth - 1, alpha, beta)[1];
          if(newAlphaBeta != null){
            utilVal = newAlphaBeta.state.getUtility();
          }
          if(val < utilVal){
            val = newAlphaBeta.state.getUtility();
            next = childNode;
            best = newAlphaBeta;
          }
          alpha = Math.max(alpha, val);
          if(alpha >= beta){
            break;
          }
        }
      }
      else{ //MIN
        double val = Double.POSITIVE_INFINITY;
        double utilVal = Double.NEGATIVE_INFINITY;
        for(GameStateChild childNode : children){
          GameStateChild newAlphaBeta = alphaBetaSearch(childNode, depth - 1, alpha, beta)[1];
          if(newAlphaBeta != null){
            utilVal = newAlphaBeta.state.getUtility();
          }
          if(val > utilVal){
            val = newAlphaBeta.state.getUtility();
            next = childNode;
            best = newAlphaBeta;
          }
          beta = Math.min(beta, val);
          if(beta <= alpha)
            break;
        }
      }
      return new GameStateChild[]{next,best};
    }

    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */

    public Queue <GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
    		Queue <GameStateChild> orderedChildren = new PriorityQueue<GameStateChild>(1,getUtilityComparator);
    		for (GameStateChild child : children) {
    			orderedChildren.add(child);
    		}
        return orderedChildren;
    }

    /*
     *	Automatically orders elements in list by their utility functions.
     */

    public static Comparator<GameStateChild> getUtilityComparator = new Comparator<GameStateChild>() {

			@Override
			public int compare(GameStateChild c1, GameStateChild c2) {
				return (int)(c1.state.getUtility() - c2.state.getUtility());
			}


    };
}
