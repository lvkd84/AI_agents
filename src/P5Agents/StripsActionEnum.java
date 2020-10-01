package P5Agents;

import java.util.ArrayList;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State.StateView;

public enum StripsActionEnum implements StripsAction {

	GO_TO_NEAREST_GOLD_MINE {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}
			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atGoldMine.size(); i++) {
				if (state.atGoldMine.get(i) == false) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}
			return validPeasantIndexes;
		}

		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);
			newState.atTownHall.set(peasantIndex, false);
			newState.atTree.set(peasantIndex, false);
			newState.atGoldMine.set(peasantIndex, true);
			return newState;
		}

		@Override
		public double costOfAction(GameState state) {
			return state.estimatedDistance;

		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.goToNearestResource(stateView, unitID, ResourceNode.Type.GOLD_MINE);
		}
	},

	GO_TO_NEAREST_TREE {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}

			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atTree.size(); i++) {
				if (state.atTree.get(i) == false) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}

			return validPeasantIndexes;
		}
		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);

				newState.atTownHall.set(peasantIndex, false);
				newState.atGoldMine.set(peasantIndex, false);
				newState.atTree.set(peasantIndex, true);
			return newState;
		}
		@Override
		public double costOfAction(GameState state) {
			return state.estimatedDistance;
		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.goToNearestResource(stateView, unitID, ResourceNode.Type.TREE);
		}
	},

	COLLECT_GOLD {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}

			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atGoldMine.size(); i++) {
				if ((state.atGoldMine.get(i) == true) && (state.emptyHanded.get(i) == true)) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}

			return validPeasantIndexes;
		}
		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);
				newState.emptyHanded.set(peasantIndex, false);
				newState.holdingGold.set(peasantIndex, true);
				newState.goldLeftOnMap -= 100;
			return newState;
		}
		@Override
		public double costOfAction(GameState state) {
			/* Return number of turns it would take to collect gold */
			return 1;
		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.harvestNearestResource(stateView, unitID, ResourceNode.Type.GOLD_MINE);
		}
	},

	COLLECT_WOOD {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}

			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atTree.size(); i++) {
				if ((state.atTree.get(i) == true) && (state.emptyHanded.get(i) == true)) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}

			return new Integer[1];
		}
		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);

			for (int i = 0; i < peasantIndex; i++) {
				newState.emptyHanded.set(peasantIndex, false);
				newState.holdingWood.set(peasantIndex, true);
				newState.woodLeftOnMap -= 100;
			}

			return newState;
		}
		@Override
		public double costOfAction(GameState state) {
			/* Return number of turns it would take to collect wood */
			return 1;
		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.harvestNearestResource(stateView, unitID, ResourceNode.Type.TREE);
		}
	},

	GO_TO_TOWN_HALL {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}

			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atTownHall.size(); i++) {
				if (state.atTownHall.get(i) == false) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}

			return validPeasantIndexes;
		}
		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);

				newState.atGoldMine.set(peasantIndex, false);
				newState.atTree.set(peasantIndex, false);
				newState.atTownHall.set(peasantIndex, true);


			return newState;
		}
		@Override
		public double costOfAction(GameState state) {
			return state.estimatedDistance;
		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.goToTownHall(stateView, unitID);
		}
	},

	DEPOSIT_RESOURCE {
		public Integer[] preconditionsMet(GameState state, int peasantsAtOnce) {
			if (peasantsAtOnce < 1) {
				return new Integer[1];
			}

			int validPeasants = 0;
			Integer[] validPeasantIndexes = new Integer[peasantsAtOnce];
			for (int i = 0; i < state.atTownHall.size(); i++) {
				if (state.atTownHall.get(i) == true && (state.holdingGold.get(i) == true || state.holdingWood.get(i) == true)) {
					validPeasantIndexes[validPeasants] = i;
					validPeasants++;
				}

				if (validPeasants == peasantsAtOnce) {
					return validPeasantIndexes;
				}
			}

			return validPeasantIndexes;
		}
		public GameState apply(GameState state, int peasantIndex) {
			GameState newState = initializeNewGameState(state, peasantIndex, this);
					if(newState.holdingGold.get(peasantIndex) == true){
						newState.holdingGold.set(peasantIndex, false);
						newState.emptyHanded.set(peasantIndex, true);
						newState.currentGold = newState.currentGold + 100;
					}
					else if (newState.holdingWood.get(peasantIndex) == true){
						newState.holdingWood.set(peasantIndex, false);
						newState.emptyHanded.set(peasantIndex, true);
						newState.currentWood = newState.currentWood + 100;
					}
			return newState;
		}
		@Override
		public double costOfAction(GameState state) {
			return 1;
		}
		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.depositResource(stateView, unitID);
		}
	},

	BUILD_PEASANT {
		@Override
		public Integer[] preconditionsMet(GameState state, int foo) {
			Integer[] canBuildPeasant = new Integer[1];

			if (state.currentGold >= 400 && state.spareFood >= 1) {
				canBuildPeasant[0] = 1;
				return canBuildPeasant;
			}
			else {
				return new Integer[1];
			}
		}

		@Override
		public GameState apply(GameState state, int bar) {
			GameState newState = initializeNewGameState(state, 0, this);
			newState.currentGold = newState.currentGold - 400;
			newState.spareFood--;
			newState.numberOfPeasants++;

			return newState;
		}

		@Override
		public double costOfAction(GameState state) {
			return 1;
		}

		@Override
		public Action translateToSepiaAction(PEAgent planEXE, StateView stateView, Integer unitID) {
			return planEXE.buildPeasant(stateView);
		}

	};

	protected GameState initializeNewGameState(GameState state, int peasantIndex, StripsAction action){
		/* Make newState be a deep copy of state */
		GameState newState = new GameState(state);
		newState.actionToGetHere = this;
		newState.previousHash = new Integer(state.hashCode());
		newState.costToGetHere = state.getCost() + this.costOfAction(state);
		newState.lastIndex = peasantIndex;

		return newState;
	}
}
