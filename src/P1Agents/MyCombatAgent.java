package P1Agents;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;

public class MyCombatAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int enemyPlayerNum = 1;
	
	private List<Unit.UnitView> myRangedUnits = new ArrayList<Unit.UnitView>();
	private List<Unit.UnitView> myMeleeUnits = new ArrayList<Unit.UnitView>();
	
	private List<Unit.UnitView> enemyRangedUnits = new ArrayList<Unit.UnitView>();
	private List<Unit.UnitView> enemyMeleeUnits = new ArrayList<Unit.UnitView>();
	
	public MyCombatAgent(int playernum, String[] otherargs) {
		super(playernum);
		
		if(otherargs.length > 0) {
			enemyPlayerNum = new Integer(otherargs[0]);
		}
	}

	@Override
	public Map<Integer, Action> initialStep(StateView arg0, HistoryView arg1) {
		
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		List<Integer> myUnitIDs = arg0.getUnitIds(playernum);
		
		List<Integer> enemyUnitIDs = arg0.getUnitIds(enemyPlayerNum);
		
		analyzeUnits(arg0, arg1, enemyUnitIDs, myUnitIDs);
		
		if(enemyUnitIDs.size() == 0) {
			return actions;
		}
		//System.out.println("These are the enemy units: " + arg0.getAllUnits());
		for(Integer myUnitID : myUnitIDs) {
			
			Unit.UnitView myUnit = arg0.getUnit(myUnitID);
			Unit.UnitView closestRangedEnemy = getClosestEnemy(myUnit, enemyRangedUnits);
			Unit.UnitView closestMeleeEnemy = getClosestEnemy(myUnit, enemyMeleeUnits);
			
			if (enemyMeleeUnits.size() == 0) {
				actions.put(myUnitID, Action.createCompoundAttack(myUnitID, getClosestEnemy(myUnit, enemyRangedUnits).getID()));
			} else {
				//	if this unit is within range of the closest ranged enemy and that ranged enemy cant move, run
				if (inRangeOf (myUnit, closestRangedEnemy) && closestRangedEnemy.getTemplateView().canMove()) {
					actions.put(myUnitID, moveAwayFromEnemy(myUnit, closestRangedEnemy));
    				//	else if closest melee enemy is not within range of the closest ranged enemy or within range of agent, attack 
    				} else if (inRangeOf(closestMeleeEnemy, myUnit)) {
    					actions.put(myUnitID, Action.createCompoundAttack(myUnitID, closestMeleeEnemy.getID()));
        			// try to get within sight range of melee enemy
        			} else if (!inSightOfEnemy(myUnit, closestMeleeEnemy)){
      				actions.put(myUnitID, stepTowardsUnit(myUnit, closestMeleeEnemy));
        			// else move away from closest melee enemy to lure him away from the tower
    				} else {
    					actions.put(myUnitID, moveAwayFromEnemy(myUnit, closestRangedEnemy));
        			}
			}
		}
		
		return actions;
	}
	
	public double getDistanceBetweenUnits (Unit.UnitView unit1, Unit.UnitView unit2) {
		return Math.sqrt(Math.pow(Math.abs(unit1.getXPosition() - unit2.getXPosition()), 2) + Math.pow(Math.abs(unit1.getYPosition() - unit2.getYPosition()), 2));
	}
	
	public Unit.UnitView getClosestEnemy (Unit.UnitView myUnit, List<Unit.UnitView> enemyUnits) {
		Unit.UnitView closestEnemy = enemyUnits.get(0);
		double closestDistance = getDistanceBetweenUnits(myUnit, enemyUnits.get(0));
		for (Unit.UnitView enemyID : enemyUnits) {
			if (closestDistance > getDistanceBetweenUnits(myUnit, enemyID)) {
				closestDistance = getDistanceBetweenUnits(myUnit, enemyID);
				closestEnemy = enemyUnits.get(0);
			}
		}
		
		return closestEnemy;
	}
	
	public boolean inRangeOf (Unit.UnitView unit, Unit.UnitView target) {
		return getDistanceBetweenUnits(unit, target) <= target.getTemplateView().getRange();
	}
	
	public Action stepTowardsUnit(Unit.UnitView unit, Unit.UnitView toUnit) {
		int xDiff = unit.getXPosition() - toUnit.getXPosition();
		int yDiff = unit.getYPosition() - toUnit.getYPosition();
		int newX = unit.getXPosition();
		int newY = unit.getYPosition();
		
		if (xDiff < 0) {
			newX++;
		} else {
			newX--;
		}
		
		if (yDiff < 0) {
			newY++;
		} else {
			newY--;
		}
		return Action.createCompoundMove(unit.getID(), newX, newY);
	}
	
	private void analyzeUnits(StateView arg0, HistoryView arg1, List<Integer>enemyUnitIDs, List<Integer> myUnitIDs) {
		// split units on the board into enemy and player ranged and melee units
				for(int i = 0; i < arg0.getAllUnits().size(); i++) {
					//	If the unit is ranged (Tower, Archer, or Ballista) add to ranged Units list. 
					//	If not assume that the unit is a melee character.
					if(enemyUnitIDs.contains(arg0.getAllUnits().get(i).getID()) ) {
						if(arg0.getAllUnits().get(i).getTemplateView().getRange() > 1) {
							enemyRangedUnits.add(arg0.getAllUnits().get(i)); 
						} else {
							enemyMeleeUnits.add(arg0.getAllUnits().get(i));
						}
					} else if (myUnitIDs.contains(arg0.getAllUnits().get(i).getID())) {
						if(arg0.getAllUnits().get(i).getTemplateView().getRange() > 1) {
							myRangedUnits.add(arg0.getAllUnits().get(i)); 
						} else {
							myMeleeUnits.add(arg0.getAllUnits().get(i));
						}
					}
				}
	}
	
	public Action moveAwayFromEnemy(Unit.UnitView myUnit, Unit.UnitView enemy) {
		int xDiff = myUnit.getXPosition() - enemy.getXPosition();
		int yDiff = myUnit.getYPosition() - enemy.getYPosition();
		int newX = myUnit.getXPosition();
		int newY = myUnit.getYPosition();
		
		if (xDiff < 0) {
			newX--;
		} else {
			newX++;
		}
		
		if (yDiff < 0) {
			newY--;
		} else {
			newY++;
		}
		return Action.createCompoundMove(myUnit.getID(), newX, newY);
	}
	
	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub
	}
	
	public boolean inSightOfEnemy(Unit.UnitView myUnit, Unit.UnitView enemy) {
		return enemy.getTemplateView().getSightRange() > getDistanceBetweenUnits(myUnit, enemy);
	}
	
	@Override
	public Map<Integer, Action> middleStep(StateView arg0, HistoryView arg1) {
        // This stores the action that each unit will perform
        // if there are no changes to the current actions then this
        // map will be empty
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        // This is a list of enemy units
        List<Integer> enemyUnitIDs = arg0.getUnitIds(enemyPlayerNum);
		List<Integer> myUnitIDs = arg0.getUnitIds(playernum);
        if(enemyUnitIDs.size() == 0)
        {
                // Nothing to do because there is no one left to attack
                return actions;
        }

        int currentStep = arg0.getTurnNumber();

        // go through the action history
        for(ActionResult feedback : arg1.getCommandFeedback(playernum, currentStep-1).values())
        {
                // if the previous action is no longer in progress (either due to failure or completion)
                // then add a new action for this unit
                if(feedback.getFeedback() != ActionFeedback.INCOMPLETE)
                {
                	analyzeUnits(arg0, arg1, enemyUnitIDs, myUnitIDs);
         
                	
                    int unitID = feedback.getAction().getUnitId();
            			Unit.UnitView myUnit = arg0.getUnit(unitID);
            			if(myUnit == null) return actions;
            			Unit.UnitView closestRangedEnemy = getClosestEnemy(myUnit, enemyRangedUnits);
            			Unit.UnitView closestMeleeEnemy = getClosestEnemy(myUnit, enemyMeleeUnits);
            			
            			if (enemyMeleeUnits.size() == 0) {
            				actions.put(unitID, Action.createCompoundAttack(unitID, getClosestEnemy(myUnit, enemyRangedUnits).getID()));
            			} else {
            				//	if this unit is within range of the closest ranged enemy and that ranged enemy cant move, run
            				if (inRangeOf (myUnit, closestRangedEnemy) && closestRangedEnemy.getTemplateView().canMove()) {
            					actions.put(unitID, moveAwayFromEnemy(myUnit, closestRangedEnemy));
            				//	else if closest melee enemy is not within range of the closest ranged enemy or within range of agent, attack 
            				} else if (inRangeOf(closestMeleeEnemy, myUnit)) {
            					actions.put(unitID, Action.createCompoundAttack(unitID, closestMeleeEnemy.getID()));
            				// attack enemies that leave cover of ranged units
            				} else if (!inRangeOf(closestMeleeEnemy, closestRangedEnemy)) {
            					actions.put(unitID, Action.createCompoundAttack(unitID, closestMeleeEnemy.getID()));
            				// try to get within sight range of melee enemy
            				} else if (!inSightOfEnemy(myUnit, closestMeleeEnemy)){
            					actions.put(unitID, stepTowardsUnit(myUnit, closestMeleeEnemy));
                			// else move away from closest melee enemy to lure him away from the tower
            				} else {
            					actions.put(unitID, moveAwayFromEnemy(myUnit, closestRangedEnemy));
            				}
            			}
                }
        }

        return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView arg0, HistoryView arg1) {
		// TODO Auto-generated method stub

	}

}
