package P2Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AstarAgent extends Agent {
	//	Things to do:
	//	move into the space next to the town hall, not the town hall

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public class MapLocation
    {
        public int x, y;

        //ADDED A COUPLE FIELDS HERE
        public MapLocation cameFrom;

        public float cost;

        public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        {
            this.x = x;
            this.y = y;
            this.cameFrom = cameFrom;
            this.cost = cost;
        }
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs
    //	define legal directions
    private int [][] expand = {{1,0}, {0,1}, {0,-1}, {-1,0}, {-1,1}, {1,1}, {-1,-1}, {1,-1}}; // diagonal moves are allowed too

    public AstarAgent(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();


        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
        		if(shouldReplanPath(newstate, statehistory, path)) {
        			System.out.println("replanning path...");
        			long planStartTime = System.nanoTime();
            		path = findPath(newstate);
            		planTime = System.nanoTime() - planStartTime;
            		totalPlanTime += planTime;
        		}
        		nextLoc = path.pop();
        		System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
    		// if the 2nd player (the ai enemy blocker agent) has moved last turn
    		// note to grader: if I forget to add it to the readme, I feel like state.getUnitId(unitId) should have some method to get it's player number value in the state.
    		if (!history.getCommandFeedback(state.getPlayerNumbers()[1], state.getTurnNumber() - 1).isEmpty()) {
    			//	the footman should replan its path
    			return true;
    		}
    		// if not it should return false
    		return false;

				//UnitView blocker = state.getUnits(1)[0];
				//int blockerX = blocker.getXPosition();
				//int blockerY = blocker.getYPosition();

				//while (!currentPath.empty()) {
					//MapLocation nextMove = currentPath.pop();
					//if (nextMove.x == blockerX && nextMove.y == blockerY)
						//return true;
				//}

				//return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }


    /*
     *	This is a custom comparator for the priority queue that will be our openList
     *	having our openList be a priority queue rather than a list, lets us explore nodes with
     *	the least estimated cost first.
     */
    public static Comparator<MapLocation> funcValComparator = new Comparator<MapLocation>() {

			@Override
			public int compare(MapLocation m1, MapLocation m2) {
				//	originally m1.cost - m2.cost but m2 - m1 performs much better for planning and execution time (but with much worse paths)

				return (int)(m1.cost - m2.cost);
			}


    };

    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {
      //Stack that keeps track of the map locations in the path.
      Stack<MapLocation> path = new Stack<MapLocation>();
  		//Priority Queue used to keep track of the map locations that are open. uses the funValComparator when determine the priority.
      Queue <MapLocation> openList = new PriorityQueue<MapLocation>(1,funcValComparator);
      MapLocation[][] closeList = new MapLocation[xExtent][yExtent];
      //	if enemyFootmanLoc is provided, give it's location a high cost on the close List
      if (enemyFootmanLoc != null) {
      		closeList[enemyFootmanLoc.x][enemyFootmanLoc.y] = enemyFootmanLoc;
      }
  		//Insert the cost of the trees to 1000.
      for (MapLocation tree: resourceLocations) {
      		tree.cost = 1000;
      		closeList[tree.x][tree.y] = tree;
      }
      openList.add(start);
      while (!openList.isEmpty()) {
      		//	Pick the location to expand from
      		MapLocation current = openList.remove();
      		//	Add to list of already expanded upon locations
      		closeList[current.x][current.y] = current;
      		//	iterate over possible adjacent locations to discover
      		for (int[] direction: expand) {
      			int i = current.x + direction[0];
      			int j = current.y + direction[1];
  				if (-1 < i && i < xExtent && -1 < j && j < yExtent) { //	Check for invalid positions like [-1,5]
  					//	if node already added to the closeList
  					if (closeList[i][j] != null) {
  	    			continue; // skip it
  	    		}
  					//	if target node found
      			if (goal.x == i && goal.y == j) {
      				//	iterate from point before the goal to node after the start location
      				while (current.cameFrom != null) {
      					//	add the current node to the path
      					path.add(current);
      					//	set the current node to the node that we came from
      					current = current.cameFrom;
      				}
      				//	returns successful path
      				return path;
      			}
      			//	calculate heuristic of adjacent location
      			float heuristic = Math.max(Math.abs(goal.x - i), Math.abs(goal.y - j));
    				//	calculate current node heuristic
    				float heuristicCurrent = Math.max(Math.abs(goal.x - current.x), Math.abs(goal.y - current.y));
    				//	calculate adjacent cost as the current cost + 1 + the difference in heuristic values
    				float funcVal = current.cost + 1 - heuristicCurrent + heuristic;
    				// add the neighbor to the priority queue of nodes to expand
    				openList.add(new MapLocation (i, j, current, funcVal));
    				// add the current node to the closeList
    				closeList[current.x][current.y] = current;
      		}
      	}
      }
      //	openList will empty when no possibilities are found and reach this point
      //	The program will then print out that no paths have been found and then terminate
      System.out.println("No valid path found");
      System.exit(0);
      return path;
    }

    /**
     * Primitive actions take a direction (e.g. NORTH, NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}
