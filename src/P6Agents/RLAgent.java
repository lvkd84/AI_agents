package P6Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.DamageLog;
import edu.cwru.sepia.environment.model.history.DeathLog;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.*;
import java.util.*;

public class RLAgent extends Agent {

    /**
     * Set in the constructor. Defines how many learning episodes your agent should run for.
     * When starting an episode. If the count is greater than this value print a message
     * and call sys.exit(0)
     */
    public final int numEpisodes;

    /**
     * List of your footmen and your enemies footmen
     */
    private List<Integer> myFootmen;
    private List<Integer> enemyFootmen;

    /**
     * List of your footmen and the last turn an event happens
     */
    // Instantiate the map
    private Map<Integer,Integer> myFootmenAndEvent = new HashMap<Integer, Integer>();

    /**
     * Convenience variable specifying enemy agent number. Use this whenever referring
     * to the enemy agent. We will make sure it is set to the proper number when testing your code.
     */
    public static final int ENEMY_PLAYERNUM = 1;

    /**
     * Set this to whatever size your feature vector is.
     */
    public static final int NUM_FEATURES = 4;

    /** Use this random number generator for your epsilon exploration. When you submit we will
     * change this seed so make sure that your agent works for more than the default seed.
     */
    public final Random random = new Random(12345);

    /**
     * Your Q-function weights.
     */
    public Double[] weights = new Double[NUM_FEATURES + 1];

    /**
     * These variables are set for you according to the assignment definition. You can change them,
     * but it is not recommended. If you do change them please let us know and explain your reasoning for
     * changing them.
     */
    public final double gamma = 0.9;
    public final double learningRate = .0001;
    public final double epsilon = .02;


    /**
     * These variables keeps track of the episode that the agent is currently at.
     */
    public int episodeCount = 0;
    public boolean learningEp = true;
    public boolean testingEp = false;
    public boolean saveData = false;
    List<Double> reward = new ArrayList<Double>();
    public double cumulativeReward = 0;

    public RLAgent(int playernum, String[] args) {
        super(playernum);

        if (args.length >= 1) {
            numEpisodes = Integer.parseInt(args[0]);
            System.out.println("Running " + numEpisodes + " episodes.");
        } else {
            numEpisodes = 10;
            System.out.println("Warning! Number of episodes not specified. Defaulting to 10 episodes.");
        }

        boolean loadWeights = false;
        if (args.length >= 2) {
            loadWeights = Boolean.parseBoolean(args[1]);
        } else {
            System.out.println("Warning! Load weights argument not specified. Defaulting to not loading.");
        }

        if (loadWeights) {
            weights = loadWeights();
        } else {
            // initialize weights to random values between -1 and 1
            weights = new Double[NUM_FEATURES + 1];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = random.nextDouble() * 2 - 1;
            }
        }
    }

    /**
     * We've implemented some setup code for your convenience. Change what you need to.
     */
    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        // You will need to add code to check if you are in a testing or learning episode
    		episodeCount++;
    		if (episodeCount % 15 >= 10) {   //Learning episode
    			learningEp = true;
    			testingEp = false;
    		} else {                         //Testing episode
    			learningEp = false;
    			testingEp = true;
    			if (episodeCount % 15 == 0) {
    				System.out.println("Current Episode: " + episodeCount);
    				saveData = true;	
    			} else {
    				saveData = false;	
    			}
    		}


        // update the living units
    		updateUnitRoster(stateView);

        return middleStep(stateView, historyView);
    }

    /**
     * You will need to calculate the reward at each step and update your totals. You will also need to
     * check if an event has occurred. If it has then you will need to update your weights and select a new action.
     *
     * If you are using the footmen vectors you will also need to remove killed units. To do so use the historyView
     * to get a DeathLog. Each DeathLog tells you which player's unit died and the unit ID of the dead unit. To get
     * the deaths from the last turn do something similar to the following snippet. Please be aware that on the first
     * turn you should not call this as you will get nothing back.
     *
     * for(DeathLog deathLog : historyView.getDeathLogs(stateView.getTurnNumber() -1)) {
     *     System.out.println("Player: " + deathLog.getController() + " unit: " + deathLog.getDeadUnitID());
     * }
     *
     * You should also check for completed actions using the history view. Obviously you never want a footman just
     * sitting around doing nothing (the enemy certainly isn't going to stop attacking). So at the minimum you will
     * have an event whenever one your footmen's targets is killed or an action fails. Actions may fail if the target
     * is surrounded or the unit cannot find a path to the unit. To get the action results from the previous turn
     * you can do something similar to the following. Please be aware that on the first turn you should not call this
     *
     * Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     * for(ActionResult result : actionResults.values()) {
     *     System.out.println(result.toString());
     * }
     *
     * @return New actions to execute or nothing if an event has not occurred.
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    		updateUnitRoster(stateView);
    		HashMap<Integer, Action> res = new HashMap<Integer,Action>();
    		List<DeathLog> deathLog = historyView.getDeathLogs(stateView.getTurnNumber() - 1);
    		List<DamageLog> damageLog = historyView.getDamageLogs(stateView.getTurnNumber() - 1);
    		Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
    		//Loop through all the footmen and allocate new actions
    		for (int footman : myFootmen) {
    			boolean attackedOrAttack = false; boolean dead = false;
    			for (DamageLog damage : damageLog) {
    				if (damage.getDefenderID() == footman || damage.getAttackerID() == footman)
    					attackedOrAttack = true;
    			}
    			for (DeathLog death : deathLog) {
    				if (death.getDeadUnitID() == footman)
    					dead = true;
    			}
    			if (attackedOrAttack && !dead) { //Something happened -> EVENT point, update weight + reallocate new action
    				double reward = calculateReward(stateView, historyView, footman);
    				int enemyId = -1;
    				//Get which enemy this footman is/was attacking
    				for (ActionResult result : actionResults.values()) {
        				if (result.getAction().getUnitId() == footman)
        					// update this map
        					myFootmenAndEvent.put(footman, stateView.getTurnNumber() - 1);
        					enemyId = ((TargetedAction) result.getAction()).getTargetId();
        			}
    				//Generate the next action
    				int nextEnemy = selectAction(stateView, historyView, footman);
    				res.put(footman, Action.createCompoundAttack(footman, nextEnemy));
    				//Update the weight if in a learning episode
    				if (learningEp) {
    					//Calculate f(s,a)
        				double[] oldFeatures = calculateFeatureVector(stateView, historyView, footman, enemyId);
        				//Getting w(i)
        				double[] oldWeights = new double[weights.length];
        				for (int i = 0; i < weights.length; i++) {
        					oldWeights[i] = weights[i].doubleValue();
        				}
        				//w(i+1) <- w(i) + alpha*(R(s,a) + gamma*max(Q(s',a')) - Q(s,a))*f(s,a)
        				updateWeights(oldWeights, oldFeatures, reward, stateView, historyView, footman);
        			//Else update the cumulative reward.
    				} else {
    					cumulativeReward += reward;
    				}
    			}
          //Initial allocation of actions
    			if (stateView.getTurnNumber() == 1) {
    				int nextEnemy = selectAction(stateView, historyView, footman);
    				res.put(footman, Action.createCompoundAttack(footman, nextEnemy));
    			}
    		}
        return res;
    }

    /**
     * Here you will calculate the cumulative average rewards for your testing episodes. If you have just
     * finished a set of test episodes you will call out testEpisode.
     *
     * It is also a good idea to save your weights with the saveWeights function.
     */
    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

        // MAKE SURE YOU CALL printTestData after you finish a test episode.
    		if (saveData) {
    			reward.add(cumulativeReward / 5);
    			cumulativeReward = 0;
    		}

    		if (episodeCount == numEpisodes)
    			printTestData(reward);
//    		System.out.println("Reward: " + reward);
//    		System.out.println("Cumulative Reward: " + cumulativeReward);
        // Save your weights
        saveWeights(weights);

    }

    /**
     * Calculate the updated weights for this agent.
     * @param oldWeights Weights prior to update
     * @param oldFeatures Features from (s,a)
     * @param totalReward Cumulative discounted reward for this footman.
     * @param stateView Current state of the game.
     * @param historyView History of the game up until this point
     * @param footmanId The footman we are updating the weights for
     * @return The updated weight vector.
     */
    public double[] updateWeights(double[] oldWeights, double[] oldFeatures, double totalReward, State.StateView stateView, History.HistoryView historyView, int footmanId) {
    		double[] newWeights = new double[oldWeights.length];

    		//Calculate the old Q
    		double oldQ = oldWeights[0];
    		for (int i = 0; i < NUM_FEATURES; i++) {
    			oldQ += oldFeatures[i]*oldWeights[i+1];
    		}

    		int nextEnemy = selectAction(stateView, historyView, footmanId);

    		for (int i = 0; i < NUM_FEATURES; i++) {
    			newWeights[i+1] = oldWeights[i+1] + learningRate*(totalReward + gamma*calcQValue(stateView, historyView, footmanId, nextEnemy) - oldQ)*oldFeatures[i];
    		}
        return null;
    }

    /**
     * Given a footman and the current state and history of the game select the enemy that this unit should
     * attack. This is where you would do the epsilon-greedy action selection.
     *
     * @param stateView Current state of the game
     * @param historyView The entire history of this episode
     * @param attackerId The footman that will be attacking
     * @return The enemy footman ID this unit should attack
     */
    public int selectAction(State.StateView stateView, History.HistoryView historyView, int attackerId) {
    		double bestQ = -1000;
    		int bestEnemy = -1;
    		for (int enemy : enemyFootmen) {
    			double qValue = calcQValue(stateView, historyView, attackerId, enemy);
    			if (qValue > bestQ) {
    				bestQ = qValue;
    				bestEnemy = enemy;
    			}
    		}
    		if (random.nextDouble() < epsilon) {     //Epsilon-greedy selection
    			bestEnemy = enemyFootmen.get(random.nextInt(enemyFootmen.size())); //CHECK LATER
    		}
    		return bestEnemy;
    }

    /**
    	double number = random.nextDouble();
    	if (number >= 0 && number < 1 - epsilon) {
    		int defenderIDToReturn = -1;
    		Double qValueForAttackingDefenderIDToReturn = Double.NEGATIVE_INFINITY;
    		for (int i = 0; i < enemyFootmen.size(); i++) {
    			double qValueForAttackingEnemyI = calcQValue(stateView, historyView, attackerID, enemyFootmen.get(i));
    			if (qValueForAttackingEnemyI > qValueForAttackingDefenderIDToReturn) {
    				defenderIDToReturn = enemyFootmen.get(i);
    			}
    		}
    		currentTarget.put(attackerID, defenderIDToReturn);
    		return defenderIDToReturn;
    	}
    	else {
    		return enemyFootmen.get(random.nextInt(enemyFootmen.size()));
    	}
     */

    /**
     * Given the current state and the footman in question calculate the reward received on the last turn.
     * This is where you will check for things like Did this footman take or give damage? Did this footman die
     * or kill its enemy. Did this footman start an action on the last turn? See the assignment description
     * for the full list of rewards.
     *
     * Remember that you will need to discount this reward based on the timestep it is received on. See
     * the assignment description for more details.
     *
     * As part of the reward you will need to calculate if any of the units have taken damage. You can use
     * the history view to get a list of damages dealt in the previous turn. Use something like the following.
     *
     * for(DamageLog damageLogs : historyView.getDamageLogs(lastTurnNumber)) {
     *     System.out.println("Defending player: " + damageLog.getDefenderController() + " defending unit: " + \
     *     damageLog.getDefenderID() + " attacking player: " + damageLog.getAttackerController() + \
     *     "attacking unit: " + damageLog.getAttackerID());
     * }
     *
     * You will do something similar for the deaths. See the middle step documentation for a snippet
     * showing how to use the deathLogs.
     *
     * To see if a command was issued you can check the commands issued log.
     *
     * Map<Integer, Action> commandsIssued = historyView.getCommandsIssued(playernum, lastTurnNumber);
     * for (Map.Entry<Integer, Action> commandEntry : commandsIssued.entrySet()) {
     *     System.out.println("Unit " + commandEntry.getKey() + " was command to " + commandEntry.getValue().toString);
     * }
     *
     * @param stateView The current state of the game.
     * @param historyView History of the episode up until this turn.
     * @param footmanId The footman ID you are looking for the reward from.
     * @return The current reward
     */
    public double calculateReward(State.StateView stateView, History.HistoryView historyView, int footmanId) {
    		updateUnitRoster(stateView);
    		double reward = 0;
    		double currentGamma = 1;
    		List<DeathLog> deathLog = historyView.getDeathLogs(stateView.getTurnNumber() - 1);
    		List<DamageLog> damageLog = historyView.getDamageLogs(stateView.getTurnNumber() - 1);
    		Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
   		myFootmenAndEvent.put(footmanId, stateView.getTurnNumber() - 1);

    		/*Check every steps from the last event
    		 *Each primitive action costs -0.1,
    		 *killing enemy grants 100, dying costs -100,
    		 *hitting an enemy grants d, taking damage grants -d
    		 */
   		int walkingSteps = stateView.getTurnNumber() - 1;
   		if (myFootmenAndEvent.get(footmanId) != null) {
   			walkingSteps -= myFootmenAndEvent.get(footmanId);
   		}

    		for (int i = 0; i <= walkingSteps; i++) {
    			reward += -0.1*currentGamma;
    			currentGamma *= gamma;
    		}

    		double enemyId = -1;
    		for (ActionResult result : actionResults.values()) {
			if (result.getAction().getUnitId() == footmanId)
				enemyId = ((TargetedAction) result.getAction()).getTargetId();
		}

    		for (DeathLog death : deathLog) {
			if (death.getDeadUnitID() == footmanId)     //If the footman was killed
				reward += -100*currentGamma;
			else if (death.getDeadUnitID() == enemyId)  //If the footman killed the enemy it was attacking
				reward += 100*currentGamma;
		}

    		for (DamageLog damage : damageLog) {
			if (damage.getDefenderID() == footmanId)           //If the footman took damage
				reward += -damage.getDamage()*currentGamma;
			else if (damage.getAttackerID() == footmanId)      //If the footman did damage
				reward += damage.getDamage()*currentGamma;
		}

        return reward;
    }

    /**
     * Calculate the Q-Value for a given state action pair. The state in this scenario is the current
     * state view and the history of this episode. The action is the attacker and the enemy pair for the
     * SEPIA attack action.
     *
     * This returns the Q-value according to your feature approximation. This is where you will calculate
     * your features and multiply them by your current weights to get the approximate Q-value.
     *
     * @param stateView Current SEPIA state
     * @param historyView Episode history up to this point in the game
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman that your footman would be attacking
     * @return The approximate Q-value
     */
    public double calcQValue(State.StateView stateView, History.HistoryView historyView, int attackerId, int defenderId) {
    		double[] features = calculateFeatureVector(stateView, historyView, attackerId, defenderId);
    		double res = features[NUM_FEATURES];
    		for (int i = 0; i < NUM_FEATURES; i++) {
    			res += features[i]*weights[i+1];
    		}
        return res;
    }

    /**
     * Given a state and action calculate your features here. Please include a comment explaining what features
     * you chose and why you chose them.
     *
     * All of your feature functions should evaluate to a double. Collect all of these into an array. You will
     * take a dot product of this array with the weights array to get a Q-value for a given state action.
     *
     * It is a good idea to make the first value in your array a constant. This just helps remove any offset
     * from 0 in the Q-function. The other features are up to you. Many are suggested in the assignment
     * description.
     *
     * @param stateView Current state of the SEPIA game
     * @param historyView History of the game up until this turn
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman. The one you are considering attacking.
     * @return The array of feature function outputs.
     */
    public double[] calculateFeatureVector(State.StateView stateView, History.HistoryView historyView, int attackerId, int defenderId) {
    		// update list of living units
    		updateUnitRoster(stateView);
    		//NUMBER OF FEATURES = 5
    		double[] features = new double[NUM_FEATURES+1];
    		Unit.UnitView footman = stateView.getUnit(attackerId);
    		Unit.UnitView enemy = stateView.getUnit(defenderId);
    		features[0] = 1;
    		//FEATURE 1: HP Ratio of This Footman over The Enemy. The higher the better.
    		if (enemy != null && footman != null) {
    			features[1] = footman.getHP() / enemy.getHP();
    			// if the footman is still alive and the enemy is dead
    		} else if (footman != null){
    			features[1] = footman.getHP();
    			// if footman is dead
    		} else {
    			features[1] = 0;
    		}
    		//FEATURE 2: Average distance of other enemies to this enemy. We do not want to attack someone with friends around.
    		double average = 0;
    		
    		for (int enemyId : enemyFootmen) {
    			Unit.UnitView otherEnemy = stateView.getUnit(enemyId);
    			// need to make sure we are not using null variables
    			if (otherEnemy != null) {
        			int otherEnemyX = otherEnemy.getXPosition();
        			int otherEnemyY = otherEnemy.getYPosition();
        			if (enemy != null) {
        				int enemyX = enemy.getXPosition();
                		int enemyY = enemy.getYPosition();
            			average += Math.sqrt((enemyX - otherEnemyX)*(enemyX - otherEnemyX) + (enemyY - otherEnemyY)*(enemyY - otherEnemyY));
        			} else {
            			average += Math.sqrt((otherEnemyX)*(otherEnemyX) + (otherEnemyY)*(otherEnemyY));
        			}
    			}
    		}
    		
    		features[2] = average/enemyFootmen.size();
    		//FEATURE 3: Number of Other Footmen Also Attacking The Enemy.
    		int count = 0;
    		Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
    		for (ActionResult result : actionResults.values()) {
    			if (((TargetedAction) result.getAction()).getTargetId() == defenderId)
    				count++;
    		}
    		features[3] = count;
    		//FEATURE 4: Is The Enemy Also Attacking The Footman?
    		//Return 1 if YES, 0 if NO
    		double alsoAttack = 0;
    		for (ActionResult result : actionResults.values()) {
    			if (((TargetedAction) result.getAction()).getUnitId() == defenderId && ((TargetedAction) result.getAction()).getTargetId() == attackerId) {
    				alsoAttack = 1;
    				break;
    			}
    		}
    		features[4] = alsoAttack;
    		//FEATURE 5:

        return features;
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * Prints the learning rate data described in the assignment. Do not modify this method.
     *
     * @param averageRewards List of cumulative average rewards from test episodes.
     */
    public void printTestData (List<Double> averageRewards) {
        System.out.println("");
        System.out.println("Games Played      Average Cumulative Reward");
        System.out.println("-------------     -------------------------");
        for (int i = 0; i < averageRewards.size(); i++) {
            String gamesPlayed = Integer.toString(10*i);
            String averageReward = String.format("%.2f", averageRewards.get(i));

            int numSpaces = "-------------     ".length() - gamesPlayed.length();
            StringBuffer spaceBuffer = new StringBuffer(numSpaces);
            for (int j = 0; j < numSpaces; j++) {
                spaceBuffer.append(" ");
            }
            System.out.println("" + gamesPlayed + spaceBuffer.toString() + averageReward);
        }
        System.out.println("");
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will take your set of weights and save them to a file. Overwriting whatever file is
     * currently there. You will use this when training your agents. You will include the output of this function
     * from your trained agent with your submission.
     *
     * Look in the agent_weights folder for the output.
     *
     * @param weights Array of weights
     */
    public void saveWeights(Double[] weights) {
        File path = new File("agent_weights/weights.txt");
        // create the directories if they do not already exist
        path.getAbsoluteFile().getParentFile().mkdirs();

        try {
            // open a new file writer. Set append to false
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, false));

            for (double weight : weights) {
                writer.write(String.format("%f\n", weight));
            }
            writer.flush();
            writer.close();
        } catch(IOException ex) {
            System.err.println("Failed to write weights to file. Reason: " + ex.getMessage());
        }
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will load the weights stored at agent_weights/weights.txt. The contents of this file
     * can be created using the saveWeights function. You will use this function if the load weights argument
     * of the agent is set to 1.
     *
     * @return The array of weights
     */
    public Double[] loadWeights() {
        File path = new File("agent_weights/weights.txt");
        if (!path.exists()) {
            System.err.println("Failed to load weights. File does not exist");
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            List<Double> weights = new LinkedList<>();
            while((line = reader.readLine()) != null) {
                weights.add(Double.parseDouble(line));
            }
            reader.close();

            return weights.toArray(new Double[weights.size()]);
        } catch(IOException ex) {
            System.err.println("Failed to load weights from file. Reason: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
    
    public void updateUnitRoster(State.StateView stateView) {
        // Update all of the friendly units
        myFootmen = new LinkedList<>();
        for (Integer unitId : stateView.getUnitIds(playernum)) {
        		Unit.UnitView unit = stateView.getUnit(unitId);
        		String unitName = unit.getTemplateView().getName().toLowerCase();
        		if (unitName.equals("footman")) {
        			myFootmen.add(unitId);
        		} else {
        			System.err.println("Unknown unit type: " + unitName);
        		}
        }

        // Update all of the enemy units
        enemyFootmen = new LinkedList<>();
        for (Integer unitId : stateView.getUnitIds(ENEMY_PLAYERNUM)) {
        		Unit.UnitView unit = stateView.getUnit(unitId);
        		String unitName = unit.getTemplateView().getName().toLowerCase();
        		if (unitName.equals("footman")) {
        			enemyFootmen.add(unitId);
        		} else {
        			System.err.println("Unknown unit type: " + unitName);
        		}
        }
    }
}
