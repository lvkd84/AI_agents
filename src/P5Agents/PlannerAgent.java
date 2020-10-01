package P5Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
        HashMap<Integer, GameState> closedList = new HashMap<Integer, GameState>();
        PriorityQueue<GameState> frontier = new PriorityQueue<GameState>();
        frontier.add(startState);
        while (frontier.peek() != null) {
        		GameState statee = frontier.peek();
        		while (statee.previousHash != null) {
        			System.out.println(statee.actionToGetHere);
        			statee = closedList.get(statee.previousHash);
        		}
            GameState current = frontier.remove();
            System.out.println("Gold " + current.currentGold);
            System.out.println("Wood " + current.currentWood);
            if (current.isGoal()) {
                Stack<StripsAction> currentPath = new Stack<StripsAction>();
                GameState stateptr = current;
                while (stateptr.previousHash != null) {
                    currentPath.push(stateptr.actionToGetHere);
                    stateptr = closedList.get(stateptr.previousHash);
                }
                savePlan(currentPath);
                return currentPath;
            }
            else {
                closedList.put(current.hashCode(), current);
                List<GameState> children = current.generateChildren();
                //System.out.println("Number " + children.size());
                //System.out.println("Frontier " + frontier.size());
                for (GameState child : children) {
                    if (closedList.containsKey(child.hashCode())) {
                        if (child.compareTo(closedList.get(child.hashCode())) == -1) {
                          /*closedList.get(child.hashCode()).previousHash = child.previousHash;
                            closedList.get(child.hashCode()).costToGetHere = child.costToGetHere;
                            closedList.get(child.hashCode()).actionToGetHere = child.actionToGetHere;*/
                            closedList.replace(child.hashCode(),child);
                        }
                    }
                    else if (!frontier.contains(child)) {
                        frontier.add(child);
                    }
                    else {
                        Iterator<GameState> frontierIterator = frontier.iterator();
                        boolean childReachedInFrontier = false;
                        while (frontierIterator.hasNext() && !childReachedInFrontier) {
                            GameState nodeptr = frontierIterator.next();
                            if (nodeptr.equals(child)) {
                                if (child.compareTo(nodeptr) == - 1) {
                                  /*nodeptr.previousHash = child.previousHash;
                                    nodeptr.costToGetHere = child.costToGetHere;
                                    nodeptr.actionToGetHere = child.actionToGetHere;

                                    Not sure if we changed the fields of elements of the priority queue, would the queue reorder itself?
                                    Also, is Java priority queue min queue?*/
                                    frontier.remove(nodeptr);
                                    frontier.offer(child);
                                }
                                childReachedInFrontier = true;
                            }
                        }
                        if (!childReachedInFrontier) {
                            System.err.println("Child not found in frontier");
                        }
                    }
                }
            }
        }
        System.err.println("There is no path to the goal");
        return new Stack<StripsAction>();
    }

    /*
     *	This is a custom comparator for the priority queue that will be our openList
     *	having our openList be a priority queue rather than a list, lets us explore nodes with
     *	the least estimated cost first.
     */
    public static Comparator<StripsAction> funcValComparator = new Comparator<StripsAction>() {

			@Override
			public int compare(StripsAction sa1, StripsAction sa2) {
				//return (int)(sa1 - sa2); // some computation given fields of the StripsAction
				return 0;
			}


    };

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
