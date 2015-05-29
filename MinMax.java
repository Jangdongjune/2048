import java.time.Duration;
import java.util.List;
import java.util.Random;

import put.ci.cevo.games.game2048.Action2048;
import put.ci.cevo.games.game2048.Game2048;
import put.ci.cevo.games.game2048.State2048;
import put.ci.cevo.rl.environment.Transition;
import put.ci.cevo.util.Pair;
import put.game2048.*;

public class MinMax implements Agent {
	Game2048 game = new Game2048();
    public Random random = new Random(123);
            
    public Action chooseAction(Board board, List<Action> possibleActions, Duration timeLimit) {
    	int mmMax = 1; //min-max Max
    	int mmCount = 0; //min-max Count
    	int flag = 0; //0(MIN), 1(MAX)
    	double[] h = new double[4];
    	double maxH = 0;
    	int maxHindex = 0;
    	
       	State2048 state = new State2048(board.get());
       	State2048[] stateAfter = new State2048[4]; //0(UP),1(RIGHT),2(DOWN),3(LEFT)
       	Action2048[] act2048 = Action2048.values();
       	Action[] act = Action.values();       	
       	for(int i=0; i<4; i++) h[i] = 0;
       	      	
        if(zeroSpaceCount(state) > 6) {
        	if(possibleActions.contains(Action.UP) || possibleActions.contains(Action.RIGHT)) {
        		for(int i=0; i<2; i++) { //up, right
           			if (possibleActions.contains(act[i])) {
           				stateAfter[i] = afterDirection(state, act2048[i]);
           				h[i] = minmax(stateAfter[i], mmCount, mmMax, flag);
           				if(h[i] == 0) h[i]++;
           			}
           		}
        	}
        	else {
        		for(int i=2; i<4; i++) { //down, left
           			if (possibleActions.contains(act[i])) {
           				stateAfter[i] = afterDirection(state, act2048[i]);
           				h[i] = minmax(stateAfter[i], mmCount, mmMax, flag);
           				if(h[i] == 0) h[i]++;
           			}
           		}
        	}        	
        }
        else {
	   		for(int i=0; i<4; i++) { //전체
	   			if (possibleActions.contains(act[i])) {
	   				stateAfter[i] = afterDirection(state, act2048[i]);
	   				h[i] = minmax(stateAfter[i], mmCount, mmMax, flag);
	   				if(h[i] == 0) h[i]++;
	   			}
	   		}
        }
        
   		for(int i=0; i<4; i++) { //max값 찾기
	   		if(maxH < h[i]) {
					maxH = h[i];
					maxHindex = i;
			}
	   		else if(maxH == h[i] && maxHindex != i) {
				if(random.nextInt(2) == 0) {
					maxH = h[i];
					maxHindex = i;
				}
			}
   		}
		
    	return act[maxHindex];
    }    
        
    private double minmax(State2048 state, int mmCount, int mmMax, int flag) { //min-max algorithm
    	double result = 0;
    	double min = 0, max = 0;
    	List<Pair<Double,State2048>> nextPossibleList = state.getPossibleNextStates();
    	
    	if(nextPossibleList.size() > 0) {
    		Pair<Double,State2048> nextPossible = nextPossibleList.get(0);
    		if(mmCount >= mmMax) {
        		result = calcHeuristic(state);
        	}
        	else {
        		if (flag == 0) mmCount++;
    	    	for(int i=0; i<nextPossibleList.size(); i++) {
    	    		nextPossible = nextPossibleList.get(i);    	    		
    	    		if(flag == 0) { //min
    	    			result = minmax(nextPossible.second(), mmCount, mmMax, 1);
    	    			if(i == 0) min = result;
    	    			if(min > result) {
    	    				min = result;
    	    			}
    	    		}
    	    		else if(flag == 1) { //max
    	    			result = minmax(nextPossible.second(), mmCount, mmMax, 0);
    	    			if(i == 0) max = result;
    	    			if(max < result) {
    	    				max = result;
    	    			}
    	    		}
    	    	}
    	    	if (flag == 0) result = min;
    	    	else if (flag == 1) result = max;
    		}
    	}
    	else {
    		result = calcHeuristic(state);
    	}    	
	    return result;
    }
    
    private double calcHeuristic(State2048 state) { //평가값 계산
    	double result = 0;
    	double zeroCount = zeroSpaceCount(state); //빈공간 개수
    	double possibleMovesCount = state.getPossibleMoves().size(); //움직일수 있는 방향 개수    	
    	double zeroAdCount = zeroAdjacentCount(state); // 빈공간에 인접한 변의 개수
    	double alignCount = alignmentCount(state);
    	double maxTile = state.getMaxTile();
    	
    	result = 128 + (zeroCount*128) + ((1/zeroAdCount)*4096) + (Math.log(24-zeroAdCount)*4) + (possibleMovesCount*256) + (alignCount*2) + maxTile;
    	    	
		return result;
    }

    private State2048 afterDirection(State2048 state, Action2048 direction) { //지정된 방향으로 움직인 state 리턴    	
    	State2048 afterState = new State2048(state.getBoard());
    	Transition<State2048, Action2048> transition = game.computeTransition(state, direction);
    	afterState = transition.getAfterState();
    	    	
    	return afterState;
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