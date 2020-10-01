package P5Agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class Deposit implements StripsAction {

	public int townhallId;

	//public int peasantId;

	public List<Integer[]> peasantId;

	public Deposit(List<Integer[]> peasantId, int townhallId) {
		this.townhallId = townhallId;
		this.peasantId = new ArrayList<Integer[]>();
		for (Integer[] id : peasantId)
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
					Position townhallPos = new Position(state.townhall.get(0)[1],state.townhall.get(0)[2]);
					if (!(townhallPos.isAdjacent(peasantPos) && peasantHolding != 0))
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
				if (unit[0].intValue() == peasantId[0].intValue()) {
					//System.out.println("Carry " + unit[3]);
					if (unit[3].intValue() == -1) {
						res.townhall.get(0)[3] += 100;
						res.currentGold += 100;
					}
					else if (unit[3].intValue() == 1) {
						res.townhall.get(0)[4] += 100;
						res.currentWood += 100;
					} else {
						System.err.println("The peasant should be carrying something!");
					}
					unit[3] = 0;
				}
			}
		}
		return res;
	}

	public Map<Integer,Direction> getDirection(StateView state) {
		Map<Integer,Direction> res = new HashMap<Integer,Direction>();
		for (Integer[] peasantId : peasantId) {
			Position peasantPos = new Position(peasantId[1],peasantId[2]);
			Position townhallPos = new Position(state.getUnit(townhallId).getXPosition(),state.getUnit(townhallId).getYPosition());
			res.put(peasantId[0], peasantPos.getDirection(townhallPos));
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("Deposit(" + peasantId.size() + ",");
		for (Integer[] peasantId : peasantId){
			res.append(peasantId[0] + ",");
		}
		res.append(townhallId + ")");
		return res.toString();
	}

}
