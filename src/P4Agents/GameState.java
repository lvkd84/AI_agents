package P4Agents.edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.State;

import java.util.ArrayList;
import java.util.List;
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import P4Agents.edu.cwru.sepia.agent.planner.actions.Deposit;
import P4Agents.edu.cwru.sepia.agent.planner.actions.HarvestGold;
import P4Agents.edu.cwru.sepia.agent.planner.actions.HarvestWood;
import P4Agents.edu.cwru.sepia.agent.planner.actions.Move;
import P4Agents.edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

	public int XExtent;

	public int YExtent;

	public int playernum;

	public int requiredGold;

	public int requiredWood;

	public int currentGold;

	public int currentWood;

	public double cost;

	public StripsAction actionToGetHere;

	public Integer previousHash;

	//[peasantId][x][y][-1 if carrying gold, 0 if not carrying anything, 1 if carrying wood], peasant carries 100 resource amounts at a time
	public ArrayList<Integer[]> peasant;

	//[townhallId][x][y][goldAmount][woodAmount]
	public ArrayList<Integer[]> townhall;

	//[goldMineId][x][y][remainingGold]
	public ArrayList<Integer[]> gold;

	//[woodMintId][x][y][remainingWood]
	public ArrayList<Integer[]> tree;

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    		this.XExtent = state.getXExtent();
    		this.YExtent = state.getYExtent();
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.playernum = playernum;
        this.currentGold = 0;
        this.currentWood = 0;
        this.cost = 0.0;

        this.peasant = new ArrayList<Integer[]>();
        this.townhall = new ArrayList<Integer[]>();
        for(int unitId : state.getUnitIds(playernum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhall.add(new Integer[]{unitId, unit.getXPosition(), unit.getYPosition(), 0, 0}); //How to get gold? wood? Assume: 0, 0 at start.
            } else if(unitType.equals("peasant")) {
                peasant.add(new Integer[]{unitId, unit.getXPosition(), unit.getYPosition(), 0});
            }
        }

        if (buildPeasants) {
        		//In this assignment, I am assuming we always have a peasant
        }

        this.gold = new ArrayList<Integer[]>();
        this.tree = new ArrayList<Integer[]>();
        for (ResourceView resource : state.getAllResourceNodes()) {
        		if (resource.getType().equals(Type.GOLD_MINE)) {
        			gold.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else if (resource.getType().equals(Type.TREE)) {
        			tree.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else System.err.println("Not an appropriate resource type");
        }
    }

    public GameState(GameState state, double actionCost, boolean buildPeasants, StripsAction actionToGetHere) {
    		this.XExtent = state.XExtent;
    		this.YExtent = state.YExtent;
    		this.requiredGold = state.requiredGold;
        this.requiredWood = state.requiredWood;
        this.playernum = state.playernum;
        this.currentGold = state.currentGold;
        this.currentWood = state.currentWood;
        this.cost = state.cost + actionCost;
				this.previousHash = state.hashCode();
				this.actionToGetHere = actionToGetHere; //May causes bugs

        this.peasant = new ArrayList<Integer[]>();
        this.townhall = new ArrayList<Integer[]>();
        for (Integer[] peasant : state.peasant) {
    			this.peasant.add(new Integer[]{peasant[0], peasant[1], peasant[2], peasant[3]});
        }
        for (Integer[] townhall : state.townhall) {
    			this.townhall.add(new Integer[]{townhall[0], townhall[1], townhall[2], townhall[3], townhall[4]});
        }

        if (buildPeasants) {
    			//In this assignment, I am assuming we always have a peasant
        }

        this.gold = new ArrayList<Integer[]>();
        this.tree = new ArrayList<Integer[]>();
        for (Integer[] goldNode : state.gold) {
        		this.gold.add(new Integer[]{goldNode[0], goldNode[1], goldNode[2], goldNode[3]});
        }
        for (Integer[] treeNode : state.tree) {
        		this.tree.add(new Integer[]{treeNode[0], treeNode[1], treeNode[2], treeNode[3]});
        }
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        if (requiredGold <= currentGold && requiredWood <= currentWood)
        		return true;
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
			List<GameState> res = new ArrayList<GameState>();
			//Generate all Move actions - The commented code is used for large scenario.
/*    List<Position> posToMove = new ArrayList<Position>();
			for (Integer[] tree : tree) {
					Position treePos = new Position(tree[1],tree[2]);
					for (Position pos : treePos.getAdjacentPositions())
						posToMove.add(pos);
			}
			for (Integer[] gold : gold) {
				Position goldPos = new Position(gold[1],gold[2]);
				for (Position pos : goldPos.getAdjacentPositions())
					posToMove.add(pos);
			}
			for (Integer[] townhall : townhall) {
					Position townhallPos = new Position(townhall[1],townhall[2]);
					for (Position pos : townhallPos.getAdjacentPositions())
						posToMove.add(pos);
			}
			for (Position pos : posToMove) {
					for (Integer[] peasant : peasant){
						Move action = new Move(peasant[0],pos.x,pos.y);
					if (action.preconditionsMet(this))
						res.add(action.apply(this));
					}
			} */
			for (int i = 0; i < XExtent; i++)
					for (int j = 0; j < YExtent; j++)
						for (Integer[] peasant : peasant){
							Move action = new Move(peasant[0],i,j);
							if (action.preconditionsMet(this))
								res.add(action.apply(this));
						}
				//Generate all HarvestGold actions
			for (Integer[] mine : gold)
					for (Integer[] peasant : peasant) {
						HarvestGold action = new HarvestGold(peasant[0],mine[0]);
						if (action.preconditionsMet(this))
							res.add(action.apply(this));
					}
			//Generate all HarvestWood actions
			for (Integer[] mine : tree)
					for (Integer[] peasant : peasant) {
					HarvestWood action = new HarvestWood(peasant[0],mine[0]);
					if (action.preconditionsMet(this))
						res.add(action.apply(this));
					}
			//Generate all Deposit actions
			for (Integer[] townhall : townhall)
					for (Integer[] peasant : peasant) {
						Deposit action = new Deposit(peasant[0],townhall[0]);
						if (action.preconditionsMet(this))
							res.add(action.apply(this));
					}
			return res;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
			/*
if (this.isGoal()) {
	return Double.NEGATIVE_INFINITY;
}
else {
	double heuristic = 0.0;
	if((goldLeftOnMap < 0) || (woodLeftOnMap < 0)){
			return Double.POSITIVE_INFINITY;
	}
	double distanceFactor = estimatedDistance;
	if(currentGold != currentWood){
			heuristic += 5;
	}
	if (currentGold < requiredGold) {
			heuristic = heuristic + Math.abs((requiredGold - currentGold))/100 * 2 * distanceFactor/numPeasants;
			int index = 0;
			for(boolean holding : holdingGold){
					if (holding) {
							heuristic = heuristic - distanceFactor/numPeasants;
							if(atTownHall.get(index)){
									heuristic -= 1;
							}
					}
					index++;
			}
	}
	if (currentWood < requiredWood) {
			 heuristic = heuristic + Math.abs((requiredWood - currentWood))/100 * 2 * distanceFactor /numPeasants;
			 int index = 0;
			 for(boolean holding : holdingWood){
					if (holding) {
							heuristic = heuristic - distanceFactor/numPeasants;
							if(atTownHall.get(index)){
									heuristic -= 1;
							}
					}
					index++;
			}
	}
	 heuristic -= numPeasants*2;
	 heuristic -= extraFood/2;
	return heuristic;
}*/
    		double res = 0;

    		if (requiredGold == currentGold)
    			res += requiredGold - currentGold;
    		else
    			res += Math.abs((requiredGold - currentGold)*100);
    		if (requiredWood == currentWood)
    			res+= requiredWood - currentWood;
    		else
    			res += Math.abs((requiredWood - currentWood)*100);

    		double dist = 100;
    		for (Integer[] peasant : peasant) {
    			Position peasantPos = new Position(peasant[1],peasant[2]);
    		//If the peasant is carrying something -> Should return to base asap
    			if (peasant[3].intValue() != 0) {
    				for (Integer[] townhall : townhall) {
    					Position townhallPos = new Position(townhall[1],townhall[2]);
    					dist = Math.min(dist, peasantPos.euclideanDistance(townhallPos));
    				}
    			} else { //If the peasant is not carrying something -> Go collect resources asap
    		//If both Gold and Wood are not fulfilled -> Go to the closest resource node
    				if (currentGold < requiredGold && currentWood < requiredWood) {
    					for (Integer[] tree : tree) {
    						Position treePos = new Position(tree[1],tree[2]);
        					dist = Math.min(dist, peasantPos.euclideanDistance(treePos));
    					}
    					for (Integer[] gold : gold) {
    						Position goldPos = new Position(gold[1],gold[2]);
        					dist = Math.min(dist, peasantPos.euclideanDistance(goldPos));
    					}
    		//If still need Wood -> Go to the closest tree
    				} else if (currentWood < requiredWood) {
    					for (Integer[] tree : tree) {
    						Position treePos = new Position(tree[1],tree[2]);
        					dist = Math.min(dist, peasantPos.euclideanDistance(treePos));
    					}
    		//If still need Gold -> Go to the closest gold mine
    				} else {
    					for (Integer[] gold : gold) {
    						Position goldPos = new Position(gold[1],gold[2]);
        					dist = Math.min(dist, peasantPos.euclideanDistance(goldPos));
    					}
    				}
    			}
    		}
    		res += dist;
        return res;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
    		if ((this.getCost() + this.heuristic()) > (o.getCost() + o.heuristic()))
    			return 1;
    		else if ((this.getCost() + this.heuristic()) < (o.getCost() + o.heuristic()))
    			return -1;
    		else return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
			if (o instanceof GameState) {
					GameState state = (GameState) o;
					if (state.currentGold != this.currentGold || state.currentWood != this.currentWood)
						return false;
					for (Integer[] peasant1 : peasant)
						for (Integer[] peasant2 : state.peasant)
							if (peasant1[0].intValue() == peasant2[0].intValue())
								if (peasant1[1].intValue() != peasant2[1].intValue() || peasant1[2].intValue() != peasant2[2].intValue() || peasant1[3].intValue() != peasant2[3].intValue())
									return false;
					for (Integer[] townhall1 : townhall)
						for (Integer[] townhall2 : state.townhall)
							if (townhall1[0].intValue() == townhall2[0].intValue())
								if (townhall1[3].intValue() != townhall2[3].intValue() || townhall1[4].intValue() != townhall2[4].intValue())
									return false;
					for (Integer[] mine1 : gold)
						for (Integer[] mine2 : state.gold)
							if (mine1[0].intValue() == mine2[0].intValue())
								if (mine1[3].intValue() != mine2[3].intValue())
									return false;
					for (Integer[] mine1 : tree)
						for (Integer[] mine2 : state.tree)
							if (mine1[0].intValue() == mine2[0].intValue())
								if (mine1[3].intValue() != mine2[3].intValue())
									return false;
			} else
					return false;
			return true;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
    		StringBuilder str = new StringBuilder();
    		str.append("Current gold " + Integer.toString(currentGold) + " ");
    		str.append("Current wood " + Integer.toString(currentWood) + " ");
    		str.append("Peasant ");
    		for (Integer[] peasant : peasant)
    			str.append(Integer.toString(peasant[1]) + " " + Integer.toString(peasant[2]) + " " + Integer.toString(peasant[3]) + " ");
    		str.append("Townhall ");
    		for (Integer[] townhall : townhall)
    			str.append(Integer.toString(townhall[1]) + " " + Integer.toString(townhall[2]) + " " + Integer.toString(townhall[3]) + " " + Integer.toString(townhall[4]) + " ");
    		str.append("Gold ");
    		for (Integer[] gold : gold)
    			str.append(Integer.toString(gold[1]) + " " + Integer.toString(gold[2]) + " " + Integer.toString(gold[3]) + " ");
    		str.append("Wood ");
    		for (Integer[] tree : tree)
    			str.append(Integer.toString(tree[1]) + " " + Integer.toString(tree[2]) + " " + Integer.toString(tree[3]) + " ");

        return str.toString().hashCode();
    }
}
