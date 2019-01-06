package com.github.breskin.squares.achievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.breskin.squares.MainActivity;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;
import com.github.breskin.squares.game.Overlord;
import com.github.breskin.squares.game.Square;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Kuba on 30.11.2018.
 */

public class Achievements {

    private static final String GAMES_PLAYED = "games-played";
    private static final String HEART_ACHIEVEMENT = "heart-achievement", OVER_5000_ACHIEVEMENT = "over-5000-achievement", OVER_9000_ACHIEVEMENT = "over-9000-achievement", CHECKBOARD_ACHIEVEMENT = "checkboard-achievement";

    public int GamesPlayed = 0;

    private MainActivity mMainActivity;
    private SharedPreferences mPreferences;
    private Queue<String> messageQueue;

    private String achievementUnlocked, playNGames, noMoves, noPoints, heartAchievement, over5000, over9000, checkboard;
    private boolean hasHeartAchievement = false, hasOver5000Achievement = false, hasOver9000Achievement = false, hasCheckboardAchievement = false;

    private int messageDelay = 0;
    private long lastTime = 0;

    public Achievements() {
        messageQueue = new ArrayDeque<>();
        lastTime = System.currentTimeMillis();
    }

    public void update() {
        if (messageDelay <= 0 && !messageQueue.isEmpty()) {
            messageDelay = 2300;
            show();
        } else if (messageDelay > 0) {
            messageDelay -= System.currentTimeMillis() - lastTime;
        }

        lastTime = System.currentTimeMillis();
    }

    public void show() {
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(mMainActivity, achievementUnlocked + " \n" + messageQueue.poll() + " ", Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if(v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    public void onGameFinished() {
        if (Overlord.Moves < 10 && Overlord.Time < 20000)
            return;

        int newValue = mPreferences.getInt(GAMES_PLAYED, 0) + 1;
        SharedPreferences.Editor editor =  mPreferences.edit();
        editor.putInt(GAMES_PLAYED, newValue);

        GamesPlayed = newValue;

        if (newValue != 0 && (newValue == 5 || newValue == 10 || newValue == 25 || newValue == 50 || newValue % 100 == 0))
            messageQueue.offer(String.format(playNGames, newValue));

        if (Overlord.CurrentMode == Overlord.GameMode.TimeLimited && Overlord.Time >= 60000 && Overlord.Moves == 0) {
            messageQueue.offer(noMoves);
        }

        if (Overlord.CurrentMode == Overlord.GameMode.MoveLimited && Overlord.Moves > 49 && Overlord.Points == 0) {
            messageQueue.offer(noPoints);
        }

        if (Overlord.Points > 9000 && !hasOver9000Achievement) {
            messageQueue.offer(over9000);

            hasOver9000Achievement = true;
            editor.putBoolean(OVER_9000_ACHIEVEMENT, true);
        } else if (Overlord.Points >= 5000 && !hasOver5000Achievement) {
            messageQueue.offer(over5000);

            hasOver5000Achievement = true;
            editor.putBoolean(OVER_5000_ACHIEVEMENT, true);
        }

        editor.apply();
    }

    public void checkBoardState(Square[][] squares) {
        if (squares[0][0] == null)
            return;

        try {
            Square.SquareColor heartColor = Square.SquareColor.Red, c0 = squares[0][0].getCurrentColor(), c1 = squares[1][0].getCurrentColor();
            if (!hasHeartAchievement && squares[2][4].getCurrentColor() == heartColor && squares[1][3].getCurrentColor() == heartColor && squares[3][3].getCurrentColor() == heartColor &&
                    squares[0][2].getCurrentColor() == heartColor && squares[4][2].getCurrentColor() == heartColor && squares[0][1].getCurrentColor() == heartColor &&
                    squares[2][1].getCurrentColor() == heartColor && squares[4][1].getCurrentColor() == heartColor && squares[1][0].getCurrentColor() == heartColor && squares[3][0].getCurrentColor() == heartColor) {
                messageQueue.offer(heartAchievement);

                hasHeartAchievement = true;
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(HEART_ACHIEVEMENT, true);
                editor.apply();
            }

            if (!hasCheckboardAchievement && squares[2][0].getCurrentColor() == c0 && squares[4][0].getCurrentColor() == c0 && squares[1][1].getCurrentColor() == c0 &&
                    squares[3][1].getCurrentColor() == c0 && squares[0][2].getCurrentColor() == c0 && squares[2][2].getCurrentColor() == c0 && squares[4][2].getCurrentColor() == c0 &&
                    squares[1][3].getCurrentColor() == c0 && squares[3][3].getCurrentColor() == c0 && squares[0][4].getCurrentColor() == c0 && squares[2][4].getCurrentColor() == c0 &&
                    squares[4][4].getCurrentColor() == c0 && squares[3][0].getCurrentColor() == c1 && squares[0][1].getCurrentColor() == c1 && squares[2][1].getCurrentColor() == c1 &&
                    squares[4][1].getCurrentColor() == c1 && squares[1][2].getCurrentColor() == c1 && squares[3][2].getCurrentColor() == c1 && squares[0][3].getCurrentColor() == c1 &&
                    squares[2][3].getCurrentColor() == c1 && squares[4][3].getCurrentColor() == c1 && squares[1][4].getCurrentColor() == c1 && squares[3][4].getCurrentColor() == c1) {
                messageQueue.offer(checkboard);

                hasCheckboardAchievement = true;
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(CHECKBOARD_ACHIEVEMENT, true);
                editor.apply();
            }
        } catch (Exception e) {}
    }

    public void load(Context context) {
        mMainActivity = (MainActivity)context;

        mPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE);

        GamesPlayed = mPreferences.getInt(GAMES_PLAYED, 0);

        hasHeartAchievement = mPreferences.getBoolean(HEART_ACHIEVEMENT, false);
        hasOver5000Achievement = mPreferences.getBoolean(OVER_5000_ACHIEVEMENT, false);
        hasOver9000Achievement = mPreferences.getBoolean(OVER_9000_ACHIEVEMENT, false);
        hasCheckboardAchievement = mPreferences.getBoolean(CHECKBOARD_ACHIEVEMENT, false);

        achievementUnlocked = context.getString(R.string.achievement_unlocked);
        playNGames = context.getString(R.string.achievement_games_played);
        noMoves = context.getString(R.string.achievement_no_moves);
        noPoints = context.getString(R.string.achievement_no_points);
        heartAchievement = context.getString(R.string.achievement_heart);
        over5000 = context.getString(R.string.achievement_over_5000);
        over9000 = context.getString(R.string.achievement_over_9000);
        checkboard = context.getString(R.string.achievement_checkboard);
    }
}
