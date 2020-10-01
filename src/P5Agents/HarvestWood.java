package P5Agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class HarvestWood implements StripsAction {

	public int treeId;

	//public int peasantId;

	public List<Integer[]> peasantId;

	/*Constructor
	 *Create a HarvestGold action instance that has the peasant go to the specified gold mine and collect some gold
	 */
	public HarvestWood(List<Integer[]> peasantId, int treeId) {
		this.treeId = treeId;
		this.peasantId = new ArrayList<Integer[]>();
		for(Integer[] id : peasantId)
			this.peasantId.add(id);
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		ArrayList<Integer[]> peasant = state.peasant;
		for (Integer[] peasantId : peasantId) {
			for (Integer[] unit : peasant) {
				if (unit[0].intValue() == peasantId[0].intValue()) {
					Position peasantPos = new Position(unit[1],unit[2]);
					int peasantHolding = unit[3];
					Position treePos = null;
					int remainingWood = 0;
					for (Integer[] tree : state.tree) {
						if (tree[0] == treeId) {
							treePos = new Position(tree[1],tree[2]);
							remainingWood = tree[3];
							break;
						}
					}
					if (!(peasantPos.isAdjacent(treePos) && peasantHolding == 0 && remainingWood >= this.peasantId.size()*100))
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		GameState res = new GameState(state, 1, false, this);
		ArrayList<Integer[]> peasant = res.peasant;
		for (Integer[] peasantId : peasantId) {
			for (Integer[] unit : peasant) {
				if (unit[0].intValue() == peasantId[0].intValue())
					unit[3] = 1;
			}
		}
		for (Integer[] tree: res.tree) {
			if (tree[0] == treeId) {
				tree[3] -= peasantId.size()*100;
				break;
			}
		}
		for (Integer[] tree: res.tree) {
			if (tree[0] == treeId) {
				//System.out.println("Remain Wood "+ tree[3]);
			}
		}
		return res;
	}

	public Map<Integer,Direction> getDirection(StateView state) {
		Map<Integer,Direction> res = new HashMap<Integer,Direction>();
		for (Integer[] peasantId : peasantId) {
			Position peasantPos = new Position(peasantId[1],peasantId[2]);
			Position treePos = new Position(state.getResourceNode(treeId).getXPosition(),state.getResourceNode(treeId).getYPosition());
			res.put(peasantId[0], peasantPos.getDirection(treePos));
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("HarvestWood(" + peasantId.size() + ",");
		for (Integer[] peasantId : peasantId)
			res.append(peasantId[0] + ",");
		res.append(treeId + ")");
		return res.toString();
	}

}
