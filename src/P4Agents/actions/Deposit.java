package P4Agents.edu.cwru.sepia.agent.planner.actions;

import P4Agents.edu.cwru.sepia.agent.planner.GameState;
import P4Agents.edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class Deposit implements StripsAction {

	public int townhallId;

	public int peasantId;

	public Deposit(int peasantId, int townhallId) {
		this.townhallId = townhallId;
		this.peasantId = peasantId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPos = new Position(state.peasant.get(0)[1],state.peasant.get(0)[2]);
		int peasantHolding = state.peasant.get(0)[3];
		Position townhallPos = new Position(state.townhall.get(0)[1],state.townhall.get(0)[2]);
		return townhallPos.isAdjacent(peasantPos) && peasantHolding != 0;
	}

	@Override
	public GameState apply(GameState state) {
		GameState res = new GameState(state, 1, false, this);
		if (res.peasant.get(0)[3] == -1) {
			res.townhall.get(0)[3] += 100;
			res.currentGold += 100;
		}
		else if (res.peasant.get(0)[3] == 1) {
			res.townhall.get(0)[4] += 100;
			res.currentWood += 100;
		} else {
			System.err.println("The peasant should be carrying something!");
		}
		res.peasant.get(0)[3] = 0;
		return res;
	}

	public Direction getDirection(StateView state) {
		Position peasantPos = new Position(state.getUnit(peasantId).getXPosition(),state.getUnit(peasantId).getYPosition());
		Position townhallPos = new Position(state.getUnit(townhallId).getXPosition(),state.getUnit(townhallId).getYPosition());
		return peasantPos.getDirection(townhallPos);
	}

	@Override
	public String toString() {
		return "Deposit(" + Integer.toString(peasantId) + "," + Integer.toString(townhallId) + ")";
	}

}
