package com.sanfi.galaxypilot;

import java.util.List;

public interface FirebaseInterface {
    void saveScoreToDatabase(int score);

    interface ScoresCallback{
        void onScoresFetched(List<Integer> scores);
    }

    void fetchScoresFromDatabase(ScoresCallback callback);

}
