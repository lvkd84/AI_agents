package P4Agents.edu.cwru.sepia.agent.planner.actions;

import P4Agents.edu.cwru.sepia.agent.planner.GameState;
import P4Agents.edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class HarvestWood implements StripsAction {

	public int treeId;

	public int peasantId;

	/*Constructor
	 *Create a HarvestWood action instance that has the peasant go to the specified tree and collect some wood
	 */
	public HarvestWood(int peasantId, int treeId) {
		this.treeId = treeId;
		this.peasantId = peasantId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPos = new Position(state.peasant.get(0)[1],state.peasant.get(0)[2]);
		int peasantHolding = state.peasant.get(0)[3];
		Position treePos = null;
		int remainingWood = 0;
		for (Integer[] tree : state.tree) {
			if (tree[0] == treeId) {
				treePos = new Position(tree[1],tree[2]);
				remainingWood = tree[3];
				break;
			}
		}
		return (peasantPos.isAdjacent(treePos) && peasantHolding == 0 && remainingWood > 0);
	}

	@Override
	public GameState apply(GameState state) {
		GameState res = new GameState(state, 1, false, this);
		for (Integer[] tree : res.tree) {
			if (tree[0] == treeId) {
				tree[3] -= 100;
				break;
			}
		}
		res.peasant.get(0)[3] = 1;
		return res;
	}

	public Direction getDirection(StateView state) {
		Position peasantPos = new Position(state.getUnit(peasantId).getXPosition(),state.getUnit(peasantId).getYPosition());
		Position treePos = new Position(state.getResourceNode(treeId).getXPosition(),state.getResourceNode(treeId).getYPosition());
		return peasantPos.getDirection(treePos);
	}

	@Override
	public String toString() {
		return "HarvestWood(" + Integer.toString(peasantId) + "," + Integer.toString(treeId) + ")";
	}

}
