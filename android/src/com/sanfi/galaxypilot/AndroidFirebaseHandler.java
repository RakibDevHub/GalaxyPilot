package com.sanfi.galaxypilot;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AndroidFirebaseHandler implements FirebaseInterface{
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    public AndroidFirebaseHandler(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("score");
    }

//    @Override
//    public void fetchScoreFromDatabase(ScoreCallback callback) {
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                int highestScore = 0;
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    int score = snapshot.getValue(Integer.class);
//                    if (score > highestScore) {
//                        highestScore = score;
//                    }
//                }
//                callback.onScoreFetched(highestScore);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle error
//            }
//        });
//    }

    @Override
    public void saveScoreToDatabase(int score) {
        databaseReference.push().setValue(score);
    }

    @Override
    public void fetchScoresFromDatabase(ScoresCallback callback) {
        databaseReference.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Integer> scores = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int score = snapshot.getValue(Integer.class);
                    scores.add(score);
                }
                callback.onScoresFetched(scores);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

}
