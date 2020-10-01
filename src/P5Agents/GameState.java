package P5Agents;

import edu.cwru.sepia.environment.model.state.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;

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

	public int unitCount;

	public int requiredGold;

	public int requiredWood;

	public int currentGold;

	public int currentWood;

	public int food;

	public double cost;

	public StripsAction actionToGetHere;

	public Integer previousHash;

	//[peasantId][x][y][-1 if carrying gold, 0 if not carrying anything, 1 if carrying wood], peasant carries 100 resource amounts at a time
	public ArrayList<Integer[]> peasant;

	//[townhallId][x][y][goldAmount][woodAmount]
	public ArrayList<Integer[]> townhall;

	//[goldMineId][x][y][remainingGold]
	public ArrayList<Integer[]> gold;

	//[woodMineId][x][y][remainingWood]
	public ArrayList<Integer[]> tree;


	public static Comparator<Integer[]> peasantComparator = new Comparator<Integer[]>() {

		@Override
		public int compare(Integer[] p1, Integer[] p2) {
			return (p1[0] - p2[0]); // some computation given fields of the StripsAction
		}


	};

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
        this.unitCount = 0;
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
            unitCount++;
        }
        this.food = state.getSupplyCap(playernum);

        this.gold = new ArrayList<Integer[]>();
        this.tree = new ArrayList<Integer[]>();
        for (ResourceView resource : state.getAllResourceNodes()) {
        		if (resource.getType().equals(Type.GOLD_MINE)) {
        			gold.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else if (resource.getType().equals(Type.TREE)) {
        			tree.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else System.err.println("Not an appropriate resource type");
        }
        this.peasant.sort(peasantComparator);
    }

    public GameState(State.StateView state) {
    		this.XExtent = state.getXExtent();
				this.YExtent = state.getYExtent();
				this.playernum = state.getPlayerNumbers()[0];   //There is only one player
				this.currentGold = state.getResourceAmount(playernum, ResourceType.GOLD);
				this.currentWood = state.getResourceAmount(playernum, ResourceType.WOOD);

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
        this.food = state.getSupplyCap(playernum);
        this.gold = new ArrayList<Integer[]>();
        this.tree = new ArrayList<Integer[]>();
        for (ResourceView resource : state.getAllResourceNodes()) {
        		if (resource.getType().equals(Type.GOLD_MINE)) {
        			gold.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else if (resource.getType().equals(Type.TREE)) {
        			tree.add(new Integer[]{resource.getID(),resource.getXPosition(),resource.getYPosition(),resource.getAmountRemaining()});
        		} else System.err.println("Not an appropriate resource type");
        }
        this.peasant.sort(peasantComparator);
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
        this.food = state.food;
        this.unitCount = state.unitCount;
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
        this.peasant.sort(peasantComparator);
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        if (requiredGold == currentGold && requiredWood == currentWood)
        		return true;
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {                                         //REWRITE THIS
        List<GameState> res = new ArrayList<GameState>();
        //ArrayList<Integer[]> posToMove = posToMove();

        //System.out.println("Pos To Move " + posToMove.size());

        for (int i = 0; i < peasant.size(); i++) {
        		ArrayList<Integer[][]> peasantCom = new ArrayList<Integer[][]>();
        		ArrayList<Integer[]> data = new ArrayList<Integer[]>();
        		for (int j = 0; j < i+1; j++)
        			data.add(null);
        		combinationUtil(peasant,peasant.size(),i+1,0,data,0,peasantCom);
        		ArrayList<Integer[][]> posToMove = posToMove(i + 1);
        		for (Integer[][] peasants : peasantCom) {
        			for (Integer[][] pos : posToMove) {
        				HashMap<Integer[],Position> move = new HashMap<Integer[],Position>();
        				for (int j = 0; j < peasants.length; j++) {
        					Integer[] toMove = pos[j];
        					move.put(new Integer[] {peasants[j][0], peasants[j][1], peasants[j][2]}, new Position(toMove[0],toMove[1]));
        				}
        				Move action = new Move(move);
        				if (action.preconditionsMet(this))
        					res.add(action.apply(this));
        			}

        		}

        }
       /*for (int i = 0; i < XExtent; i++)
        		for (int j = 0; j < YExtent; j++)
        			for (Integer[] peasant : peasant){
        				Move action = new Move(peasant[0],i,j);
        				if (action.preconditionsMet(this))
        					res.add(action.apply(this));
        			} */
        	//Generate all HarvestGold actions
        for (Integer[] mine : gold) {
        		ArrayList<Integer[]> idle = harvestable();
        		for (int i = 0; i < idle.size(); i++) {
        			ArrayList<Integer[][]> peasantCom = new ArrayList<Integer[][]>();
        			ArrayList<Integer[]> data = new ArrayList<Integer[]>();
            		for (int j = 0; j < i+1; j++)
            			data.add(null);
            		combinationUtil(idle,idle.size(),i+1,0,data,0,peasantCom);
            		for (Integer[][] peasants : peasantCom) {
            			ArrayList<Integer[]> peasantIds = new ArrayList<Integer[]>();
            			for (Integer[] peasant : peasants)
						peasantIds.add(new Integer[] {peasant[0],peasant[1],peasant[2]});
            			HarvestGold action = new HarvestGold(peasantIds,mine[0]);
            			//System.out.println("Gold " + action.preconditionsMet(this));
            			if (action.preconditionsMet(this))
            				res.add(action.apply(this));
            		}
        		}
        }
        //Generate all HarvestWood actions
        for (Integer[] mine : tree) {
        		ArrayList<Integer[]> idle = harvestable();
        		for (int i = 0; i < idle.size(); i++) {
        			ArrayList<Integer[][]> peasantCom = new ArrayList<Integer[][]>();
        			ArrayList<Integer[]> data = new ArrayList<Integer[]>();
            		for (int j = 0; j < i+1; j++)
            			data.add(null);
        			combinationUtil(idle,idle.size(),i+1,0,data,0,peasantCom);
        			for (Integer[][] peasants : peasantCom) {
        				ArrayList<Integer[]> peasantIds = new ArrayList<Integer[]>();
        				for (Integer[] peasant : peasants)
        					peasantIds.add(new Integer[] {peasant[0],peasant[1],peasant[2]});
        				HarvestWood action = new HarvestWood(peasantIds,mine[0]);
        				//System.out.println("Wood " + action.preconditionsMet(this));
        				if (action.preconditionsMet(this))
        					res.add(action.apply(this));
        			}
        		}
        }
        //Generate all Deposit actions
        for (Integer[] townhall : townhall) {
        		ArrayList<Integer[]> idle = depositable();
        		for (int i = 0; i < idle.size(); i++) {
        			ArrayList<Integer[][]> peasantCom = new ArrayList<Integer[][]>();
        			ArrayList<Integer[]> data = new ArrayList<Integer[]>();
            		for (int j = 0; j < i+1; j++)
            			data.add(null);
        			combinationUtil(idle,idle.size(),i+1,0,data,0,peasantCom);
        			for (Integer[][] peasants : peasantCom) {
        				ArrayList<Integer[]> peasantIds = new ArrayList<Integer[]>();
        				for (Integer[] peasant : peasants)
        					peasantIds.add(new Integer[] {peasant[0],peasant[1],peasant[2]});
        				Deposit action = new Deposit(peasantIds,townhall[0]);
        				//System.out.println("Deposit " + action.preconditionsMet(this));
        				if (action.preconditionsMet(this))
        					res.add(action.apply(this));
        			}
        		}
        }
        //Generate a buildPeasant action
        for (Integer[] townhall : townhall) {
        		BuildPeasant action = new BuildPeasant(townhall[0]);
        		System.out.println("Build Peasant " + action.preconditionsMet(this));
        		if (action.preconditionsMet(this))
        			res.add(action.apply(this));
        }
        return res;
    }

    /**
    Attempt at a heuristic to allow for running on large map.
    public double heuristic() {
        if (heuristic != null){
         return heuristic;
        }
        if (isGoal()) {
            heuristic = 0.0;
            return 0;
        }
        int leftoverWood = this.requiredWood - this.totalWood;
        int leftoverGold = this.requiredGold - this.totalGold;
        int woodCollect = (int)(leftoverWood*1.0/peasants.size() + 0.5);
        int goldCollect = (int)(leftoverGold*1.0/peasants.size() + 0.5);
        double Heuristic = Double.MIN_VALUE;
        for (Peasant peasant : peasants.values()) {
            int collectWood = Math.min(leftoverWood, woodCollect);
            int collectGold = Math.min(leftoverGold, goldCollect);
            Heuristic = Math.max(getSimplePeasantHeuristic(peasant, collectWood, collectGold), Heuristic);
            leftoverWood -= collectWood;
            leftoverGold -= collectGold;
        }
        heuristic = Heuristic;
        return heuristic;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {                                          //HEURISTIC NEEDS TO ACCOUNT FOR LONG TERM EFFECT
    		double res = 0;

    		if (requiredGold >= currentGold)
    			res += Math.abs(requiredGold - currentGold)*10/peasant.size();
    		else
    			res += Math.abs(requiredGold - currentGold)/peasant.size();
    		if (requiredWood >= currentWood)
    			res += Math.abs(requiredWood - currentWood)*10/peasant.size();
    		else
    			res += Math.abs(requiredWood - currentWood)/peasant.size();

    		double treeDist = 1000;

    		double goldDist = 1000;

    		double townhallDist = 1000;

    		boolean carry = false;

    		for (Integer[] peasant : peasant) {
    			// if the peasant is carrying resources
    			if (peasant[3].intValue() != 0) {
    				carry = true;
    				// find closest town hall
            		for (Integer[] townhall : townhall) {
            			double temSum = 0;
            			Position townhallPos = new Position(townhall[1],townhall[2]);
            			Position peasantPos = new Position(peasant[1],peasant[2]);
        				temSum += peasantPos.euclideanDistance(townhallPos);
            			townhallDist = Math.min(townhallDist, temSum/this.peasant.size());
            		}
      
    			} else {
            		for (Integer[] gold : gold) {
            			if (gold[3].intValue() == 0)
            				continue;
            			double temSum = 0;
            			Position goldPos = new Position(gold[1],gold[2]);
            			Position peasantPos = new Position(peasant[1],peasant[2]);
        				temSum += peasantPos.euclideanDistance(goldPos);
            			goldDist = Math.min(goldDist, temSum/this.peasant.size());
            		} 
            		
        			for (Integer[] tree : tree) {
            			if (tree[3].intValue() == 0)
            				continue;
            			double temSum = 0;
            			Position treePos = new Position(tree[1],tree[2]);
            			Position peasantPos = new Position(peasant[1],peasant[2]);
        				temSum += peasantPos.euclideanDistance(treePos);
            			treeDist = Math.min(treeDist, temSum/this.peasant.size());
        			}   				
    			}
    		}

    		double dist = 0;

    		if (carry) {
    			dist += townhallDist;
    		} else if (currentGold < requiredGold && currentWood < requiredWood) {
    			dist += Math.min(goldDist,treeDist);
    		} else if (currentGold >= requiredGold) {
    			dist += treeDist;
    		} else if (currentWood >= requiredWood) {
    			dist += goldDist;
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


    //EQUALS() AND TOSTRING() NEED TO BE FIXED BECAUSE WE HAVE MULTIPLE PEASANTS NOW.
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
					if (peasant.size() == state.peasant.size()) {
						for (int i = 0; i < peasant.size(); i++) {
							Integer[] peasant1 = peasant.get(i);
							Integer[] peasant2 = state.peasant.get(i);
							if (peasant1[1].intValue() != peasant2[1].intValue() || peasant1[2].intValue() != peasant2[2].intValue() || peasant1[3].intValue() != peasant2[3].intValue())
								return false;
						}
					} else
						return false;
					/*for (Integer[] peasant1 : peasant)
						for (Integer[] peasant2 : state.peasant)
							if (peasant1[0].intValue() == peasant2[0].intValue())
								if (peasant1[1].intValue() != peasant2[1].intValue() || peasant1[2].intValue() != peasant2[2].intValue() || peasant1[3].intValue() != peasant2[3].intValue())
									return false;*/
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
    			str.append(Integer.toString(peasant[0]) + " " + Integer.toString(peasant[1]) + " " + Integer.toString(peasant[2]) + " " + Integer.toString(peasant[3]) + " ");
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

    public void combinationUtil(ArrayList<Integer[]> arr, int n, int r, int index, ArrayList<Integer[]> data, int i, ArrayList<Integer[][]> res) {
    	// Current combination is ready to be printed, print it
    		if (index == r) {
    			Integer[][] tem = new Integer[r][];
    			for (int j=0; j<r; j++)
    				tem[j] = data.get(j);
    			res.add(tem);
    			return;
    		}

    		// When no more elements are there to put in data[]
    		if (i >= n)
    			return;

    		// current is included, put next at next location
    		data.set(index, arr.get(i));
    		combinationUtil(arr, n, r, index+1, data, i+1, res);

    		// current is excluded, replace it with next (Note that
    		// i+1 is passed, but index is not changed)
    		combinationUtil(arr, n, r, index, data, i+1, res);

    }

    public ArrayList<Integer[]> harvestable() {
    		ArrayList<Integer[]> res = new ArrayList<Integer[]>();
    		for (Integer[] peasant : peasant) {
    			//System.out.println("CArGO " + peasant[1] + " " + peasant[2] + " " + peasant[3]);
    			if (peasant[3].intValue() == 0)
    				res.add(peasant);
    		}
    		return res;
    }

    public ArrayList<Integer[]> depositable() {
    		ArrayList<Integer[]> res = new ArrayList<Integer[]>();
		for (Integer[] peasant : peasant)
			if (peasant[3].intValue() != 0)
				res.add(peasant);
		return res;
    }

    public ArrayList<Integer[][]> posToMove(int numPeasant) {
    		ArrayList<Integer[][]> res = new ArrayList<Integer[][]>();
    		int n = numPeasant;
    		for (Integer[] tree : tree) {
    			if (tree[3] < 100*n)
    				continue;
    			Integer[][] pos = new Integer[n][];
        		Position treePos = new Position(tree[1],tree[2]);
        		List<Position> adjacent = treePos.getAdjacentPositions();
        		for (int i = 0 ; i < n ; i++)
        			pos[i] = new Integer[] {adjacent.get(i).x,adjacent.get(i).y};
        		res.add(pos);
        }
        for (Integer[] gold : gold) {
        		if (gold[3] < 100*n)
				continue;
        		Integer[][] pos = new Integer[n][];
    			Position goldPos = new Position(gold[1],gold[2]);
    			List<Position> adjacent = goldPos.getAdjacentPositions();
        		for (int i = 0 ; i < n ; i++)
        			pos[i] = new Integer[] {adjacent.get(i).x,adjacent.get(i).y};
        		res.add(pos);
        }
        for (Integer[] townhall : townhall) {
        		Integer[][] pos = new Integer[n][];
        		Position townhallPos = new Position(townhall[1],townhall[2]);
        		List<Position> adjacent = townhallPos.getAdjacentPositions();
        		for (int i = 0 ; i < n ; i++)
        			pos[i] = new Integer[] {adjacent.get(i).x,adjacent.get(i).y};
        		res.add(pos);
        }
    		return res;
    }
}
