import java.time.Duration;
import java.util.List;
import java.util.Random;

import put.ci.cevo.games.game2048.Action2048;
import put.ci.cevo.games.game2048.Game2048;
import put.ci.cevo.games.game2048.State2048;
import put.ci.cevo.rl.environment.Transition;
import put.ci.cevo.util.Pair;
import put.game2048.*;

public class MyAgent implements Agent {
	Game2048 game = new Game2048();
    public Random random = new Random(123);
    int treeMax = 2; //트리 max차수
        
    public Action chooseAction(Board board, List<Action> possibleActions, Duration timeLimit) {
    	    	    	
       	State2048 state = new State2048(board.get());
       	Action result = null;
       	int treeCount = 0;
    	double[] h = new double[4];
    	double maxH = 0;
    	int maxHindex = 0;
    	int[][] temp = new int[4][4];
    	
    	for(int i=0; i<4; i++) h[i] = 0;
    	
    	
    	State2048 stateUP = new State2048(afterDirection(state, Action2048.UP));
    	State2048 stateRIGHT = new State2048(afterDirection(state, Action2048.RIGHT));
    	State2048 stateDOWN = new State2048(afterDirection(state, Action2048.DOWN));
    	State2048 stateLEFT = new State2048(afterDirection(state, Action2048.LEFT));
    	
       	if (zeroSpaceCount(state) > 5) {
	       	if (possibleActions.contains(Action.UP) || possibleActions.contains(Action.RIGHT)) {     	
	       		//tempBoard = stateUP.getBoard();
	       		if (possibleActions.contains(Action.UP)) {
	       			h[0] = treeSearch(stateUP, treeCount);       			
	       		}
	       		if (possibleActions.contains(Action.RIGHT)) {	       			
	       			h[1] = treeSearch(stateRIGHT, treeCount);	       			
	       		}
	       	}
	       	else {
		       	if (possibleActions.contains(Action.DOWN)) {
		       		h[2] = treeSearch(stateDOWN, treeCount);
		       	}
		       	if (possibleActions.contains(Action.LEFT)) {
		       		h[3] = treeSearch(stateLEFT, treeCount);
		       	}
	       	}
       	}
       	else {
       		if (possibleActions.contains(Action.UP)) {
       			h[0] = treeSearch(stateUP, treeCount);
       		}
       		if (possibleActions.contains(Action.RIGHT)) {
       			h[1] = treeSearch(stateRIGHT, treeCount);
       		}
       		if (possibleActions.contains(Action.DOWN)) {
	       		h[2] = treeSearch(stateDOWN, treeCount);
	       	}
	       	if (possibleActions.contains(Action.LEFT)) {
	       		h[3] = treeSearch(stateLEFT, treeCount);
	       	}
       	}
		
       	for(int i=0; i<4; i++) { //평가값 max 찾기
       		if(maxH < h[i]) {
       			maxH = h[i];
       			maxHindex = i;
       		}
       	}       	
       	switch(maxHindex) { //평가값 가장 큰값 선택
       	case 0:
       		result = Action.UP;       		
       		break;
       	case 1:
       		result = Action.RIGHT;
       		break;
       	case 2:
       		result = Action.DOWN;
       		break;
       	case 3:
       		result = Action.LEFT;
       		break;
       	}
       	

       	
    	return result;
    }
    
    public State2048 afterDirection(State2048 state, Action2048 direction) { //지정된 방향으로 움직인 state 리턴    	
    	State2048 afterState = new State2048(state.getBoard());
    	Transition<State2048, Action2048> transition = game.computeTransition(state, direction);
    	afterState = transition.getAfterState();
    	return afterState;
    }
        
    public double treeSearch(State2048 state, int treeCount) { //트리
    	double result = 0;
    	double resultUP = 0, resultRIGHT = 0, resultDOWN = 0, resultLEFT = 0;
    	List<Action2048> possibleMoves = state.getPossibleMoves();
    	
    	if(treeCount >= treeMax || possibleMoves.isEmpty()) {
    		result = calcHeuristic(state);    		
    	}
    	else {
    		treeCount++;
	    	if(possibleMoves.contains(Action2048.UP)) {
	    		State2048 stateUP = new State2048(afterDirection(state, Action2048.UP));
	    		resultUP = treeSearch(stateUP, treeCount);
	    	}
	    	if(possibleMoves.contains(Action2048.RIGHT)) {
	    		State2048 stateRIGHT = new State2048(afterDirection(state, Action2048.RIGHT));
	    		resultRIGHT = treeSearch(stateRIGHT, treeCount);
	    	}
	    	if(possibleMoves.contains(Action2048.DOWN)) {
	    		State2048 stateDOWN = new State2048(afterDirection(state, Action2048.DOWN));
	    		resultDOWN = treeSearch(stateDOWN, treeCount);
	    	}
	    	if(possibleMoves.contains(Action2048.LEFT)) {
	    		State2048 stateLEFT = new State2048(afterDirection(state, Action2048.LEFT));
	    		resultLEFT = treeSearch(stateLEFT, treeCount);
	    	}
	    	result = Math.max(Math.max(resultUP,resultRIGHT), Math.max(resultDOWN, resultLEFT));
    	}	    
	    return result;
    }
    
    public double calcHeuristic(State2048 state) { //평가값 계산
    	double result = 0;
    	double zeroCount = zeroSpaceCount(state); //빈공간 개수
    	double possibleMovesCount = state.getPossibleMoves().size(); //움직일수 있는 방향 개수    	
    	double zeroAdCount = zeroAdjacentCount(state); // 빈공간에 인접한 변의 개수
    	double alignCount = alignmentCount(state);
    	double maxTile = state.getMaxTile();
    	
    	result = 128 + (zeroCount*128) + ((1/zeroAdCount)*4096) + (Math.log(24-zeroAdCount)*4) + (possibleMovesCount*256) + (alignCount*2) + maxTile;
    	    	
		return result;
    }

	private double zeroSpaceCount(State2048 state) {
    	double result = 0;
    	int[][] tempBoard = new int[4][4];
    	tempBoard = state.getBoard();
    	for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
	    		if (tempBoard[i][j] == 0) result++;
			}
		}    	
    	return result;
    }
    private int maxTileLocation(State2048 state) {
    	int result = 0;
    	int maxValue = 0;
    	int[][] tempBoard = new int[4][4];
    	tempBoard = state.getBoard();
    	maxValue = state.getMaxTile();
    	for(int i=0; i<4; i++) {
    		for(int j=0; j<4; j++) {
    			if(tempBoard[i][j] == maxValue) {
    				result = (i*10) + j;
    				break;
    			}
    		}
    	}    	
    	return result;
    }
    private double zeroAdjacentCount(State2048 state) {
    	double result = 1;
    	int[][] boardValue = new int[4][4];
    	boardValue = state.getBoard();    	
    	
    	for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
	    		if (boardValue[i][j] != 0) {
	    			if(i != 0) {
	    				if(boardValue[i-1][j] == 0) result++;
	    			}
	    			if(i != 3) {
	    				if(boardValue[i+1][j] == 0) result++;
	    			}
	    			if(j != 0) {
	    				if(boardValue[i][j-1] == 0) result++;
	    			}
	    			if(j != 3) {
	    				if(boardValue[i][j+1] == 0) result++;
	    			}
	    		}
			}
		}    	
    	return result;
    }
    private double alignmentCount(State2048 state) {
    	double result = 1;
    	int[][] tempBoard = new int[4][4];
    	tempBoard = state.getBoard();
    	for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
    			if(i != 0) {
    				if(tempBoard[i][j] == tempBoard[i-1][j] || tempBoard[i][j] == tempBoard[i-1][j]*2) result++;
    			}
    			if(i != 3) {
    				if(tempBoard[i][j] == tempBoard[i+1][j] || tempBoard[i][j] == tempBoard[i+1][j]*2) result++;
    			}
    			if(j != 0) {
    				if(tempBoard[i][j] == tempBoard[i][j-1] || tempBoard[i][j] == tempBoard[i][j-1]*2) result++;
    			}
    			if(j != 3) {
    				if(tempBoard[i][j] == tempBoard[i][j+1] || tempBoard[i][j] == tempBoard[i][j+1]*2) result++;
    			}
			}
		}
    	return result;
    }
}