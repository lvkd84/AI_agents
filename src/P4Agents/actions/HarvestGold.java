package P4Agents.edu.cwru.sepia.agent.planner.actions;

import P4Agents.edu.cwru.sepia.agent.planner.GameState;
import P4Agents.edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class HarvestGold implements StripsAction {

	public int goldMineId;

	public int peasantId;

	/*Constructor
	 *Create a HarvestGold action instance that has the peasant go to the specified gold mine and collect some gold
	 */
	public HarvestGold(int peasantId, int goldMineId) {
		this.goldMineId = goldMineId;
		this.peasantId = peasantId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPos = new Position(state.peasant.get(0)[1],state.peasant.get(0)[2]);
		int peasantHolding = state.peasant.get(0)[3];
		Position goldMinePos = null;
		int remainingGold = 0;
		for (Integer[] gold : state.gold) {
			if (gold[0] == goldMineId) {
				goldMinePos = new Position(gold[1],gold[2]);
				remainingGold = gold[3];
				break;
			}
		}
		return (peasantPos.isAdjacent(goldMinePos) && peasantHolding == 0 && remainingGold > 0);
	}

	@Override
	public GameState apply(GameState state) {
		GameState res = new GameState(state, 1, false, this);
		for (Integer[] gold: res.gold) {
			if (gold[0] == goldMineId) {
				gold[3] -= 100;
				break;
			}
		}
		res.peasant.get(0)[3] = -1;
		return res;
	}

	public Direction getDirection(StateView state) {
		Position peasantPos = new Position(state.getUnit(peasantId).getXPosition(),state.getUnit(peasantId).getYPosition());
		Position minePos = new Position(state.getResourceNode(goldMineId).getXPosition(),state.getResourceNode(goldMineId).getYPosition());
		return peasantPos.getDirection(minePos);
	}

	@Override
	public String toString() {
		return "HarvestGold(" + Integer.toString(peasantId) + "," + Integer.toString(goldMineId) + ")";
	}

}
