package P5Agents;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

public class Move implements StripsAction {

	//public Position to;

	//public int peasantId;

	public Map<Integer[],Position> toMove;

	public Move(Map<Integer[],Position> toMove) {
		this.toMove = toMove;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		HashSet<int[]> checkDuplicate = new HashSet<int[]>();
		for (Map.Entry<Integer[], Position> entry : toMove.entrySet()) {
			int[] pos = new int[] {entry.getValue().x, entry.getValue().y};
			if (checkDuplicate.contains(pos))
				return false;
			else checkDuplicate.add(pos);
		}
		//If there is nothing at the position we want to move to, then that is possible
		for (Map.Entry<Integer[], Position> entry : toMove.entrySet()) {
			Position to = entry.getValue();
			for (Integer[] unit : state.peasant)
				if (unit[1] == to.x && unit[2] == to.y)
					return false;
			for (Integer[] unit : state.townhall)
				if (unit[1] == to.x && unit[2] == to.y)
					return false;
			for (Integer[] unit : state.gold)
				if (unit[1] == to.x && unit[2] == to.y)
					return false;
			for (Integer[] unit : state.tree)
				if (unit[1] == to.x && unit[2] == to.y)
					return false;
			if (!to.inBounds(state.XExtent, state.YExtent))
				return false;
		}
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		double actionCost = 0;
		for (Map.Entry<Integer[], Position> entry : toMove.entrySet()) {
			List<Integer[]> peasant = state.peasant;
			for (Integer[] unit : peasant) {
				if (unit[0].intValue() == entry.getKey()[0].intValue()) {
					Position current = new Position(unit[1],unit[2]);
					actionCost = Math.min(actionCost, entry.getValue().euclideanDistance(current));
				}
			}
		}
		GameState res = new GameState(state, actionCost, false, this);
		for (Map.Entry<Integer[], Position> entry : toMove.entrySet()) {
			List<Integer[]> peasant = res.peasant;
			for (Integer[] unit : peasant) {
				if (unit[0].intValue() == entry.getKey()[0].intValue()) {
					unit[1] = entry.getValue().x;
					unit[2] = entry.getValue().y;
				}
			}
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("Move(" + toMove.size());
		for (Map.Entry<Integer[], Position> entry : toMove.entrySet())
			res.append(",[" + entry.getKey()[0] + "," + entry.getValue().x + "," + entry.getValue().y + "]");
		res.append(")");
		return res.toString();
	}
}
