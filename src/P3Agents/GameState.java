package P3Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

	private int XExtent;

	private int YExtent;

	private int turn;

	private int archerRange;

	private int archerDmg;

	private int footmanDmg;

	private int archerHpChange;

	private int footmanHpChange;

	//list of resources
	private int[][] resource;

	//list of footmen
	//private List<UnitView> footmen = new ArrayList<UnitView>();

	//list of archers
	//private List<UnitView> archers = new ArrayList<UnitView>();

	private ArrayList<Integer[]> footmanPosAndHp;

	private ArrayList<Integer[]> archerPosAndHp;

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
    		this.footmanPosAndHp = new ArrayList<Integer[]>();
    		this.archerPosAndHp = new ArrayList<Integer[]>();

    		this.XExtent = state.getXExtent();
    		this.YExtent = state.getYExtent();
    		this.resource = new int[XExtent][YExtent];

		this.turn = state.getTurnNumber();
    		List<Integer> resourceIds = state.getAllResourceIds();
    		for (int i : resourceIds) {
    			ResourceView resourceNode = state.getResourceNode(i);
    			resource[resourceNode.getXPosition()][resourceNode.getYPosition()] = 1;
    		}

    		List<Integer> footmenIds = state.getUnitIds(0);
    		for (int i : footmenIds) {
    			UnitView footman = state.getUnit(i);
    			footmanPosAndHp.add(new Integer[]{i,footman.getXPosition(),footman.getYPosition(),footman.getHP()});
    		}

    		List<Integer> archerIds = state.getUnitIds(1);
    		for (int i : archerIds) {
    			UnitView archer = state.getUnit(i);
    			archerPosAndHp.add(new Integer[]{i,archer.getXPosition(),archer.getYPosition(),archer.getHP()});
    		}

    		footmanDmg = state.getUnit(footmenIds.get(0)).getTemplateView().getBasicAttack();
    		archerRange = state.getUnit(archerIds.get(0)).getTemplateView().getRange();
    		archerDmg = state.getUnit(archerIds.get(0)).getTemplateView().getBasicAttack();
    }

    //Another constructor for GameState that takes a GameState object as an input. This is our state tracking mechanism.
    public GameState(GameState state, Map<Integer, Action> map) {
    		//Copying the values from the last state
    		this.footmanPosAndHp = new ArrayList<Integer[]>();
		this.archerPosAndHp = new ArrayList<Integer[]>();
		this.archerHpChange = 0;

    		this.turn = state.getTurn() + 1;
    		this.XExtent = state.getXExtent();
    		this.YExtent = state.getYExtent();
    		this.archerRange = state.getArcherRange();
    		this.archerDmg = state.getArcherDmg();
    		this.footmanDmg = state.getFootmanDmg();

    		this.resource = new int[XExtent][YExtent];
    		int[][] tempResource = state.getResourceInfo();

    		for (int i = 0; i < XExtent; i++)
    			for (int j = 0; j < YExtent; j++)
    				this.resource[i][j] = tempResource[i][j];

    		//Looking at actions and applying the changes to this state.
    		if (turn%2 == 1) {
    			for (Integer[] archer : state.getArcherInfo()) {
    				Integer[] newArcher = new Integer[archer.length];
    				for (int i = 0; i < archer.length; i++)
    					newArcher[i] = new Integer(archer[i]);
    				this.archerPosAndHp.add(newArcher);
    			}
    			for (Integer[] footman : state.getFootmanInfo()) {
    				Action action = map.get(footman[0]);
    				System.out.println(action);
    				Integer[] newFootman = new Integer[footman.length];
    				for (int i = 0; i < footman.length; i++)
    					newFootman[i] = new Integer(footman[i]);
    				if (action instanceof TargetedAction) {
    					int targetId = ((TargetedAction) action).getTargetId();
    					for (Integer[] archer : archerPosAndHp)
    						if (archer[0] == targetId) {
    							archer[3] -= footmanDmg;
    							archerHpChange += footmanDmg;
    						}
    				} else if (action instanceof DirectedAction) {
    					Direction direction = ((DirectedAction) action).getDirection();
    					if (direction.equals(Direction.EAST)) {
    						//System.out.println("EAST");
    							newFootman[1]++;
    					} else if (direction.equals(Direction.WEST)) {
    						//System.out.println("WEST");
    							newFootman[1]--;
    					} else if (direction.equals(Direction.SOUTH)) {
    						//System.out.println("SOUTH");
    							newFootman[2]++;
    					} else if (direction.equals(Direction.NORTH)) {
    						//System.out.println("NORTH");
    							newFootman[2]--;
    					} else {};//System.err.println("Not an appropriate direction");
    				} else System.err.println("Not an appropriate action type");
    				this.footmanPosAndHp.add(newFootman);
    			}
    		} else if (turn%2 == 0) {
    			for (Integer[] footman : state.getFootmanInfo()) {
    				Integer[] newFootman = new Integer[footman.length];
    				for (int i = 0; i < footman.length; i++)
    					newFootman[i] = new Integer(footman[i]);
    				this.footmanPosAndHp.add(footman);
    			}
    			for (Integer[] archer : state.getArcherInfo()) {
    				Action action = map.get(archer[0]);
    				System.out.println(action);
    				Integer[] newArcher = new Integer[archer.length];
    				for (int i = 0; i < archer.length; i++)
    					newArcher[i] = new Integer(archer[i]);
    				if (action instanceof TargetedAction) {
    					int targetId = ((TargetedAction) action).getTargetId();
    					for (Integer[] footman : footmanPosAndHp)
    						if (footman[0] == targetId) {
    							footman[3] -= archerDmg;
    							footmanHpChange += archerDmg;
    						}
    				} else if (action instanceof DirectedAction) {
    					Direction direction = ((DirectedAction) action).getDirection();
    					if (direction.equals(Direction.EAST)) {
    							newArcher[1]++;
    					} else if (direction.equals(Direction.WEST)) {
    							newArcher[1]--;
    					} else if (direction.equals(Direction.SOUTH)) {
    							newArcher[2]++;
    					} else if (direction.equals(Direction.NORTH)) {
    							newArcher[2]--;
    					} else {}; //System.err.println("Not an appropriate direction");
    				} else System.err.println("Not a appropriate action type");
    				this.archerPosAndHp.add(newArcher);
    			}
    		}
	}

	/**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
    		double dist = 0;
    		double midX = 0;
    		double midY = 0;
    		double midDist = 0;

    		//Footman-archer distances
    		for (Integer[] footman : footmanPosAndHp) {
    			for (Integer[] archer : archerPosAndHp)
//              	dist += Math.max((footman[1] - archer[1]),(footman[2] - archer[2]));
    				dist += Math.sqrt((footman[1] - archer[1])*(footman[1] - archer[1]) + (footman[2] - archer[2])*(footman[2] - archer[2]));
//    			double count = 0;
//    			for (int i = footman[1]; i <= XExtent; i++) {
//    				if (i == XExtent) {
//    					stuck += count;
//    					break;
//    				}
//    				if (resource[i][footman[2]] == 1) {
//    					stuck += count;
//    					count = 0;
//    					break;
//    				} else count++;
//    			}
//    			for (int i = footman[1]; i >= -1; i--) {
//    				if (i == -1) {
//    					stuck += count;
//    					break;
//    				}
//    				if (resource[i][footman[2]] == 1) {
//    					stuck += count;
//    					count = 0;
//    					break;
//    				} else count++;
//    			}
//    			for (int i = footman[2]; i <= YExtent; i++) {
//    				if (i == YExtent) {
//    					stuck += count;
//    					break;
//    				}
//    				if (resource[footman[1]][i] == 1) {
//    					stuck += count;
//    					count = 0;
//    					break;
//    				} else count++;
//    			}
//    			for (int i = footman[2]; i >= -1; i--) {
//    				if (i == -1) {
//    					stuck += count;
//    					break;
//    				}
//    				if (resource[footman[1]][i] == 1) {
//    					stuck += count;
//    					count = 0;
//    					break;
//    				} else count++;
//    			}
    		}

    		//Trapping
    		for (Integer[] footman : footmanPosAndHp) {
    			midX += footman[1];
    			midY += footman[2];
    		}
    		midX = midX/2;
    		midY = midY/2;
    		for (Integer[] archer : archerPosAndHp) {
    				double tempMidDist = 0;
    				if ((midX - archer[1])*(midX - archer[1]) <= 0 && (midY - archer[2])*(midY - archer[2]) <= 0) {//Best scenario
    						tempMidDist = 0.1*Math.sqrt((midX - archer[1])*(midX - archer[1]) + (midY - archer[2])*(midY - archer[2]));
    				} else if ((midX - archer[1])*(midX - archer[1]) > 0 && (midY - archer[2])*(midY - archer[2]) <= 0) {//Good scenario 1
    						tempMidDist = 0.2*Math.abs((midX - archer[1])*(midX - archer[1]));
    				} else if ((midX - archer[1])*(midX - archer[1]) <= 0 && (midY - archer[2])*(midY - archer[2]) > 0) {//Good scenario 2
    						tempMidDist = 0.2*Math.abs((midY - archer[2])*(midY - archer[2]));
    				} else { //(midX - archer[1])*(midX - archer[1]) <= 0 && (midY - archer[2])*(midY - archer[2]) > 0) - Worst case
    						for (Integer[] footman : footmanPosAndHp) {
    							int max = Math.max(Math.abs(archer[1] - footman[1]),Math.abs(archer[2] - footman[2]));
    							tempMidDist = Math.max(midDist, max);
    						}
    				}
    				midDist = Math.max(midDist,tempMidDist);
    		}

    		//Cornering
    		double cornerDist = 0;
    		for (Integer[] archer : archerPosAndHp) {
    			cornerDist = Math.min(cornerDist, archer[1] + archer[2]);
    			cornerDist = Math.min(cornerDist, XExtent - 1 - archer[1] + archer[2]);
    			cornerDist = Math.min(cornerDist, archer[1] + YExtent - 1 - archer[2]);
    			cornerDist = Math.min(cornerDist, XExtent - 1 - archer[1] + YExtent - 1 - archer[2]);
    		}
        return 10*archerHpChange - 5*footmanHpChange + 5/(0.8*dist + midDist + 0.8*cornerDist);
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
    		ArrayList<Map<Integer,Action>> currentActionList;
    		Direction[] directions = new Direction[]{Direction.EAST,Direction.WEST,Direction.SOUTH,Direction.NORTH};
    		//System.out.println(turn);
				if (turn % 2 == 0) { //WHEN THIS IS OUR TURN
						//Build the list of action map. First, add 5 possible actions for the first footman to the list.
						//Then, for each action of the first footman, there are 5 possible actions for the second footman.
						//Continue until we are done with every footman.
						ArrayList<Map<Integer,Action>> actions = new ArrayList<Map<Integer,Action>>();
						for (int i = 0; i < footmanPosAndHp.size(); i++){ //Add 5 possible actions for the first footman.
								Integer[] currentUnit = footmanPosAndHp.get(i);
								ArrayList<Map<Integer,Action>> temp = new ArrayList<Map<Integer,Action>>();
								if (actions.isEmpty()) {
										for (Direction direction : directions) {
												int nextX = currentUnit[1] + direction.xComponent();
												int nextY = currentUnit[2] + direction.yComponent();
												Map<Integer,Action> map = new HashMap<Integer,Action>();
												//System.out.println(hasArcher(nextX,nextY));
												if (nextX >=0 && nextX < XExtent && nextY >= 0 && nextY < YExtent) {
														if (hasArcher(nextX,nextY) != -1) {
																//System.out.println(Action.createPrimitiveAttack(currentUnit[0], hasArcher(nextX,nextY)));
																map.put(currentUnit[0],Action.createPrimitiveAttack(currentUnit[0], hasArcher(nextX,nextY)));
																actions.add(map);
														} else {
																//System.out.println(Action.createPrimitiveMove(currentUnit[0], direction));
																if (resource[nextX][nextY] != 1 && hasFootman(nextX,nextY) == -1) {
																		map.put(currentUnit[0],Action.createPrimitiveMove(currentUnit[0], direction));
																		actions.add(map);
																}
														}

												}
										}
								} else { //Add actions for each footman after the first.
										for (Direction direction : directions) {
												int nextX = currentUnit[1] + direction.xComponent();
												int nextY = currentUnit[2] + direction.yComponent();
												for (Map<Integer,Action> map : actions) {
														Map<Integer,Action> tempMap = new HashMap<Integer,Action>();
														tempMap.putAll(map);
														System.out.println(hasArcher(nextX,nextY));
														if (nextX >=0 && nextX < XExtent && nextY >= 0 && nextY < YExtent) {
																if (hasArcher(nextX,nextY) != -1) {
																		//System.out.println(Action.createPrimitiveAttack(currentUnit[0], hasArcher(nextX,nextY)));
																		tempMap.put(currentUnit[0],Action.createPrimitiveAttack(currentUnit[0], hasArcher(nextX,nextY)));
																		temp.add(tempMap);
																} else {
																		//System.out.println(Action.createPrimitiveMove(currentUnit[0], direction));
																		if (resource[nextX][nextY] != 1 && hasFootman(nextX,nextY) == -1) {
																				tempMap.put(currentUnit[0],Action.createPrimitiveMove(currentUnit[0], direction));
																				temp.add(tempMap);
																		}
																}
														}
												}
										}
										actions = temp;
								}
						}
						currentActionList = actions;
						//turn = 1;

				} else { // WHEN THIS IS THE ENEMY TURN
					//Adding actions for enemy archers like what we do for footman.
					ArrayList<Map<Integer,Action>> actions = new ArrayList<Map<Integer,Action>>();
					for (int i = 0; i < archerPosAndHp.size(); i++){
							Integer[] currentUnit = archerPosAndHp.get(i);
							ArrayList<Map<Integer,Action>> temp = new ArrayList<Map<Integer,Action>>();
							if (actions.isEmpty()) {
									Map<Integer,Action> map;
									for (Integer[] footman : footmanPosAndHp) {
											map = new HashMap<Integer,Action>();
											if (footmanInRange(footman, currentUnit)) {
												map.put(currentUnit[0],Action.createPrimitiveAttack(currentUnit[0],footman[0]));
												actions.add(map);
											}
									}
									for (Direction direction : directions) {
											int nextX = currentUnit[1] + direction.xComponent();
											int nextY = currentUnit[2] + direction.yComponent();
											if (nextX >=0 && nextX < XExtent && nextY >= 0 && nextY < YExtent)
													if (resource[nextX][nextY] != 1 && hasFootman(nextX,nextY) == -1 && hasArcher(nextX,nextY) == -1) {
															map = new HashMap<Integer,Action>();
															map.put(currentUnit[0],Action.createPrimitiveMove(currentUnit[0], direction));
															actions.add(map);
													}
									}
							} else {
									for (Map<Integer,Action> map : actions) {
											for (Integer[] footman : footmanPosAndHp) {
													Map<Integer,Action> tempMap = new HashMap<Integer,Action>();
													tempMap.putAll(map);
													if (footmanInRange(footman, currentUnit)) {
														tempMap.put(currentUnit[0],Action.createPrimitiveAttack(currentUnit[0], footman[0]));
														temp.add(tempMap);
													}
											}
											for (Direction direction : directions) {
													int nextX = currentUnit[1] + direction.xComponent();
													int nextY = currentUnit[2] + direction.yComponent();
													if (nextX >=0 && nextX < XExtent && nextY >= 0 && nextY < YExtent)
														if (resource[nextX][nextY] != 1 && hasFootman(nextX,nextY) == -1 && hasArcher(nextX,nextY) == -1) {
															Map<Integer,Action> tempMap = map;
															tempMap.put(currentUnit[0],Action.createPrimitiveMove(currentUnit[0], direction));
															temp.add(tempMap);
													}
											}
									}
									actions = temp;
							}
					}
					currentActionList = actions;
					//turn = 0;
				}

		List<GameStateChild> res = new ArrayList<GameStateChild>();

		for (int i = 0; i < currentActionList.size(); i++) {
			Map<Integer,Action> map = currentActionList.get(i);
			GameState newState = new GameState(this,map);
			GameStateChild child = new GameStateChild(map,newState);
			res.add(child);
		}
        return res;
    }

    //Check if the input position has an archer.
	private int hasArcher(int x, int y) {
			for (Integer[] archer : archerPosAndHp)
					if (archer[1].intValue() == x && archer[2].intValue() == y)
							return archer[0];
			return -1;
	}

	//Check if the input position has a footman.
	private int hasFootman(int x, int y) {
			for (Integer[] footman : footmanPosAndHp)
					if (footman[1].intValue() == x && footman[2].intValue() == y)
							return footman[0];
			return -1;
	}

	//Check if the input footman is within the attack range of the input archer.
	private boolean footmanInRange(Integer[] footman, Integer[] archer) {
		if (Math.sqrt((footman[1] - archer[1])*(footman[1] - archer[1]) + (footman[2] - archer[2])*(footman[2] - archer[2])) < archerRange)
			return true;
		return false;
	}

	//GETTER METHODS
	public int getTurn() {
		return turn;
	}

	public int getXExtent() {
		return XExtent;
	}

	public int getYExtent() {
		return YExtent;
	}

	public ArrayList<Integer[]> getFootmanInfo() {
		return footmanPosAndHp;
	}

	public ArrayList<Integer[]> getArcherInfo() {
		return archerPosAndHp;
	}

	public int getArcherRange() {
		return archerRange;
	}

	public int getArcherDmg() {
		return archerDmg;
	}

	public int getFootmanDmg() {
		return footmanDmg;
	}

	public int[][] getResourceInfo() {
		return resource;
	}
}
