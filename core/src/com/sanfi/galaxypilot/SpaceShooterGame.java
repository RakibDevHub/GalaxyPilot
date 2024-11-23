package com.sanfi.galaxypilot;

import com.badlogic.gdx.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShooterGame extends Game {
	GameScreen gameScreen;
	FirebaseInterface _FB;
	public static Random random = new Random();
	public SpaceShooterGame(FirebaseInterface FB){
		_FB = FB; // Initialize _FB here
	}

	@Override
	public void create() {
		gameScreen = new GameScreen(new FirebaseInterface() {

			@Override
			public void saveScoreToDatabase(int score) {
				_FB.saveScoreToDatabase(score);
			}

			@Override
			public void fetchScoresFromDatabase(ScoresCallback callback) {
				_FB.fetchScoresFromDatabase(new FirebaseInterface.ScoresCallback() {
					@Override
					public void onScoresFetched(List<Integer> scores) {
						List<Integer> copiedScores = new ArrayList<>(scores); // Create a copy of the fetched scores
						callback.onScoresFetched(copiedScores); // Pass the copied scores back to the caller
					}
				});
			}

		});
		setScreen(gameScreen);
	}

	@Override
	public void resize(int width, int height) {
		gameScreen.resize(width, height);
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		gameScreen.dispose();
	}
}
