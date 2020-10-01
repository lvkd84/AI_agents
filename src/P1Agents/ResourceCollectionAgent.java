package P1Agents;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import edu.cwru.sepia.action.*;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class ResourceCollectionAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int playernum = 0;

	public ResourceCollectionAgent(int playernum) {
		super(playernum);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView arg0, HistoryView arg1) {
		return null;
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView arg0, HistoryView arg1) {

		//Define necessary lists to store actions and different units
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		List<Integer> unitIDs = arg0.getUnitIds(playernum);

		List<Integer> peasantIDs = new ArrayList<Integer>();
		List<Integer> townhallIDs = new ArrayList<Integer>();
		List<Integer> farmIDs = new ArrayList<Integer>();

		List<Integer> goldMines = arg0.getResourceNodeIds(Type.GOLD_MINE);
		List<Integer> trees = arg0.getResourceNodeIds(Type.TREE);

		int currentGold = arg0.getResourceAmount(playernum, ResourceType.GOLD);
        int currentWood = arg0.getResourceAmount(playernum, ResourceType.WOOD);


		//Build the lists of different units
		for (Integer unitID : unitIDs) {
			UnitView unit = arg0.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			if (unitTypeName.equals("Peasant"))
				peasantIDs.add(unitID);
			else if (unitTypeName.equals("TownHall"))
				townhallIDs.add(unitID);
			else if (unitTypeName.equals("Farm"))
				farmIDs.add(unitID);
			else
				System.out.print("No such unit type");
		}


		//Initialize the action list
		Action action = null;


		//Check the number of farms built
		int farmCount = farmIDs.size();


		//Main loop: go collect 800 gold, then 800 wood, then build a farm
		if (farmCount < 3) {
			if (currentGold < 500) {
				for (Integer peasantID : peasantIDs) {
					if (arg0.getUnit(peasantID).getCargoAmount() > 0)
						action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIDs.get(0));
					else
						action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
					actions.put(peasantID, action);
				}
			} else if (currentWood < 250) {
				for (Integer peasantID : peasantIDs) {
					if (arg0.getUnit(peasantID).getCargoAmount() > 0)
						action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIDs.get(0));
					else
						action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
					actions.put(peasantID, action);
				}
			} else {
				for (Integer peasantID : peasantIDs) {		
					action = Action.createCompoundBuild(peasantID, arg0.getTemplate(playernum, "Farm").getID(), 15, 5*(farmCount+1));
					actions.put(peasantID, action);
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
		System.out.println("Finish!");
	}

}
