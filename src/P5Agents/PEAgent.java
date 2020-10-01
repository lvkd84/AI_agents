package P5Agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
    		System.out.println("START EXECUTING START EXECUTING");
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
     * To check an action's progress you can use the historyview object. Here is a short example.
     * if (stateView.getTurnNumber() != 0) {
     *   Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     *   for (ActionResult result : actionResults.values()) {
     *     <stuff>
     *   }
     * }
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {                      //REWRITE THIS
        System.out.println("INSIDE MIDDLESTEP INSIDE MIDDLESTEP");

        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("peasant"))
                System.out.println("PEASANT ID: " + unitId);
            
        }

    		Map<Integer, Action> res = new HashMap<Integer,Action>();
    		if (stateView.getTurnNumber() != 0) {
    			Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
    		    for (ActionResult result : actionResults.values()) {
    		    		if (result.getFeedback().equals(ActionFeedback.INCOMPLETE))
    		    			return res;
    		    		else if (result.getFeedback().equals(ActionFeedback.INCOMPLETEMAYBESTUCK)) {
    		    			System.err.println("Got stuck somewhere.");
    		    			return res;
    		    		}
    		    }
    		}
    		int count = 1;
    		
    		for (int i = 0; i < count; i++) {

    			System.out.println("EXECUTING EXE EXE EXE EXE EXE");

    			StripsAction action = plan.pop();
    			if (action instanceof Move) {
    				Move nextAction = (Move) action;
    				res.putAll(createSepiaAction(nextAction,stateView));
    			} else if (action instanceof Deposit) {
    				Deposit nextAction = (Deposit) action;
    				res.putAll(createSepiaAction(nextAction,stateView));
    			} else if (action instanceof HarvestGold) {
    				HarvestGold nextAction = (HarvestGold) action;
    				res.putAll(createSepiaAction(nextAction,stateView));
    			} else if (action instanceof HarvestWood) {
    				HarvestWood nextAction = (HarvestWood) action;
    				res.putAll(createSepiaAction(nextAction,stateView));
    			} else if (action instanceof BuildPeasant) {
    				BuildPeasant nextAction = (BuildPeasant) action;
    				res.putAll(createSepiaAction(nextAction,stateView));
    			} else
    				System.err.println("Invalid action type");
    			}
    		return res;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Map<Integer,Action> createSepiaAction(StripsAction action, State.StateView state) {                                  //REWRITE THIS
    		HashMap<Integer,Action> res = new HashMap<Integer,Action>();
    		if (action instanceof Move) {
    			System.out.println(((Move)action).toString());
    			for (Map.Entry<Integer[], Position> entry : ((Move) action).toMove.entrySet()) {
    				Integer realId;
    				for(int unitId : state.getUnitIds(playernum)) {
    		            Unit.UnitView unit = state.getUnit(unitId);
    		            String unitType = unit.getTemplateView().getName().toLowerCase();
    		            if(unitType.equals("peasant")) {
    		                if (entry.getKey()[1].intValue() == unit.getXPosition() && entry.getKey()[2].intValue() == unit.getYPosition()) {
    		                		realId = unitId;
    		        				res.put(realId, Action.createCompoundMove(realId, entry.getValue().x, entry.getValue().y));
    		                		break;
    		                }
    		            }
    		        }
    			}
    		} else if (action instanceof Deposit) {
    			System.out.println(((Deposit)action).toString());
    			for (Integer[] peasant : ((Deposit) action).peasantId) {
    				Integer realId;
    				for(int unitId : state.getUnitIds(playernum)) {
    		            Unit.UnitView unit = state.getUnit(unitId);
    		            String unitType = unit.getTemplateView().getName().toLowerCase();
    		            if(unitType.equals("peasant")) {
    		                if (peasant[1].intValue() == unit.getXPosition() && peasant[2].intValue() == unit.getYPosition()) {
    		                		realId = unitId;
    		        				res.put(realId, Action.createPrimitiveDeposit(realId, ((Deposit) action).getDirection(state).get(peasant[0])));
    		                		break;
    		                }
    		            }
    		        }
    			}
    		} else if (action instanceof HarvestGold) {
    			System.out.println(((HarvestGold)action).toString());
    			for (Integer[] peasant : ((HarvestGold) action).peasantId) {
    				Integer realId;
    				for(int unitId : state.getUnitIds(playernum)) {
    		            Unit.UnitView unit = state.getUnit(unitId);
    		            String unitType = unit.getTemplateView().getName().toLowerCase();
    		            if(unitType.equals("peasant")) {
    		                if (peasant[1].intValue() == unit.getXPosition() && peasant[2].intValue() == unit.getYPosition()) {
    		                		realId = unitId;
    		                		res.put(realId, Action.createPrimitiveGather(realId, ((HarvestGold) action).getDirection(state).get(peasant[0])));
    		                		break;
    		                }
    		            }
    		        }
    			}
    		} else if (action instanceof HarvestWood) {
    			System.out.println(((HarvestWood)action).toString());
    			for (Integer[] peasant : ((HarvestWood) action).peasantId) {
    				Integer realId;
    				for(int unitId : state.getUnitIds(playernum)) {
    		            Unit.UnitView unit = state.getUnit(unitId);
    		            String unitType = unit.getTemplateView().getName().toLowerCase();
    		            if(unitType.equals("peasant")) {
    		                if (peasant[1].intValue() == unit.getXPosition() && peasant[2].intValue() == unit.getYPosition()) {
    		                		realId = unitId;
    		        				res.put(realId, Action.createPrimitiveGather(realId, ((HarvestWood) action).getDirection(state).get(peasant[0])));
    		                		break;
    		                }
    		            }
    		        }
    			}
    		} else if (action instanceof BuildPeasant) {
    			System.out.println(((BuildPeasant)action).toString());
    			Integer townhallId = ((BuildPeasant) action).townhallId;
    			res.put(townhallId, Action.createPrimitiveBuild(townhallId, 26));
    		} else
    			System.err.println("Not an Appropriate action type");
    		return res;
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
}
