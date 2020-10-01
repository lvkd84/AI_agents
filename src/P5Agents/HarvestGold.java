package P5Agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class HarvestGold implements StripsAction {

	public int goldMineId;

	//public int peasantId;

	public List<Integer[]> peasantId;

	/*Constructor
	 *Create a HarvestGold action instance that has the peasant go to the specified gold mine and collect some gold
	 */
	public HarvestGold(List<Integer[]> peasantId, int goldMineId) {
		this.goldMineId = goldMineId;
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
					Position goldMinePos = null;
					int remainingGold = 0;
					for (Integer[] gold : state.gold) {
						if (gold[0] == goldMineId) {
							goldMinePos = new Position(gold[1],gold[2]);
							remainingGold = gold[3];
							break;
						}
					}
					if (!(peasantPos.isAdjacent(goldMinePos) && peasantHolding == 0 && remainingGold >= this.peasantId.size()*100))
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
					unit[3] = -1;
			}
		}
		for (Integer[] gold: res.gold) {
			if (gold[0] == goldMineId) {
				gold[3] -= peasantId.size()*100;
				break;
			}
		}
		return res;
	}

	public Map<Integer,Direction> getDirection(StateView state) {
		Map<Integer,Direction> res = new HashMap<Integer,Direction>();
		for (Integer[] peasantId : peasantId) {
			Position peasantPos = new Position(peasantId[1],peasantId[2]);
			Position minePos = new Position(state.getResourceNode(goldMineId).getXPosition(),state.getResourceNode(goldMineId).getYPosition());
			res.put(peasantId[0], peasantPos.getDirection(minePos));
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("HarvestGold(" + peasantId.size() + ",");
		for (Integer[] peasantId : peasantId)
			res.append(peasantId[0] + ",");
		res.append(goldMineId + ")");
		return res.toString();
	}

}
