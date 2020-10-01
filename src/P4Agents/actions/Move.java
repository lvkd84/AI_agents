package P4Agents.edu.cwru.sepia.agent.planner.actions;

import P4Agents.edu.cwru.sepia.agent.planner.GameState;
import P4Agents.edu.cwru.sepia.agent.planner.Position;

public class Move implements StripsAction {

	public Position to;

	public int peasantId;

	public Move(int peasantId, int x, int y) {
		this.to = new Position(x,y);
		this.peasantId = peasantId;
	}

	//If there is nothing at the position we want to move to, then that is possible
	@Override
	public boolean preconditionsMet(GameState state) {
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
		return to.inBounds(state.XExtent, state.YExtent);
	}

	@Override
	public GameState apply(GameState state) {
		Position current = new Position(state.peasant.get(0)[1],state.peasant.get(0)[2]);
		double actionCost = to.euclideanDistance(current);
		GameState res = new GameState(state, actionCost, false, this);
		res.peasant.get(0)[1] = to.x;
		res.peasant.get(0)[2] = to.y;
		return res;
	}

	@Override
	public String toString() {
		return "Move(" + Integer.toString(peasantId) + "," + Integer.toString(to.x) + "," + Integer.toString(to.y) + ")";
	}
}
