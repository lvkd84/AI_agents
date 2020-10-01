package P5Agents;

public class BuildPeasant implements StripsAction {

	public int townhallId;

	public BuildPeasant(int townhallId) {
		this.townhallId = townhallId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return (state.peasant.size() < state.food && state.currentGold >= 400);
	}

	@Override
	public GameState apply(GameState state) {
		GameState res = new GameState(state, 1, true, this);
		res.currentGold -= 400;
		res.townhall.get(0)[4] -= 400;
		int[] newPeasantPos = new int[] {state.townhall.get(0)[1] - 1, state.townhall.get(0)[2]};
		//I don't know how to get the initial position of the peasant that is going to be created
		//so I am assuming that is the initial position of the first peasant.
		res.peasant.add(new Integer[] {res.unitCount, newPeasantPos[0], newPeasantPos[1], 0});
		res.unitCount++;
		return res;
	}

	@Override
	public String toString() {
		return "BuildPeasant(" + Integer.toString(townhallId) + ")";
	}

}
