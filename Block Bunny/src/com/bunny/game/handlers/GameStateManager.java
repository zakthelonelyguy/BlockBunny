package com.bunny.game.handlers;

import java.util.Stack;

import com.bunny.game.main.Game;
import com.bunny.game.states.GameState;
import com.bunny.game.states.Play;

public class GameStateManager {
	private Game game;
	
	private Stack<GameState> gameStates;
	
	public static final int PLAY = 0;
	
	public GameStateManager (Game game) {
		this.game = game;
		gameStates = new Stack<GameState>();
		pushState(PLAY);
	}
	
	public Game game() { return game; }
	
	public void update(float dt) {
		gameStates.peek().update(dt);
	}
	
	public void render() {
		gameStates.peek().render();
	}
	
	private GameState getState(int state) {
		if (state == PLAY) return new Play(this);
		return null;
	}
	
	public void setState(int state) {
		popState();
		pushState(state);
	}
	
	public void pushState(int state) {
		gameStates.push(getState(state));
	}
	
	public void popState() {
		GameState g = gameStates.pop();
		g.dispose();
	}
}
