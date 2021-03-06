package com.github.breskin.squares;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.breskin.squares.gameplay.GameLogic;
import com.github.breskin.squares.gameplay.modes.ConstantMode;
import com.github.breskin.squares.gameplay.modes.EndlessMode;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.MoveLimitedMode;
import com.github.breskin.squares.gameplay.modes.ResettingTimeMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataManager {

    private static SharedPreferences preferences;
    private static GregorianCalendar calendar;

    private static final String GAMES_PLAYED_COUNT_LABEL = "games-played-count", HIGH_SCORE_ENDLESS_LABEL = "high-score-infinite", HIGH_SCORE_CONSTANT_LABEL = "high-score-until-pointless",
                                HIGH_SCORE_TIME_LIMITED_LABEL = "high-score-time-limited-", HIGH_SCORE_MOVE_LIMITED_LABEL = "high-score-move-limited-", HIGH_SCORE_RESETTING_TIME_LABEL = "high-score-resetting-time",
                                LAST_DAY_PLAYED_LABEL = "last-day-played", TODAY_HIGH_SCORE_ENDLESS_LABEL = "today-high-score-infinite", TODAY_HIGH_SCORE_CONSTANT_LABEL = "today-high-score-until-pointless",
                                TODAY_HIGH_SCORE_TIME_LIMITED_LABEL = "today-high-score-time-limited-", TODAY_HIGH_SCORE_MOVE_LIMITED_LABEL = "today-high-score-move-limited-", TODAY_HIGH_SCORE_RESETTING_TIME_LABEL = "today-high-score-resetting-time";

    private static int gamesPlayed, endlessMaxScore, constantMaxScore, resettingTimeMaxScore, moveLimitedMaxScore[] = new int[5], timeLimitedMaxScore[] = new int[5];
    private static int todayEndlessMaxScore, todayConstantMaxScore, todayResettingTimeMaxScore, todayMoveLimitedMaxScore[] = new int[5], todayTimeLimitedMaxScore[] = new int[5];

    private static boolean firstLaunch = false;

    public static void init(Context context) {
        preferences = context.getSharedPreferences("scoreboard", Context.MODE_PRIVATE);
        calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        gamesPlayed = preferences.getInt(GAMES_PLAYED_COUNT_LABEL, 0);
        endlessMaxScore = preferences.getInt(HIGH_SCORE_ENDLESS_LABEL, 0);
        constantMaxScore = preferences.getInt(HIGH_SCORE_CONSTANT_LABEL, 0);
        resettingTimeMaxScore = preferences.getInt(HIGH_SCORE_RESETTING_TIME_LABEL, 0);

        for (int i = 0; i < 5; i++)
            timeLimitedMaxScore[i] = preferences.getInt(HIGH_SCORE_TIME_LIMITED_LABEL + i, 0);

        for (int i = 0; i < 5; i++)
            moveLimitedMaxScore[i] = preferences.getInt(HIGH_SCORE_MOVE_LIMITED_LABEL + i, 0);


        if (preferences.getString(LAST_DAY_PLAYED_LABEL, "none").equals(calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH))) {
            todayEndlessMaxScore = preferences.getInt(TODAY_HIGH_SCORE_ENDLESS_LABEL, 0);
            todayConstantMaxScore = preferences.getInt(TODAY_HIGH_SCORE_CONSTANT_LABEL, 0);
            todayResettingTimeMaxScore = preferences.getInt(TODAY_HIGH_SCORE_RESETTING_TIME_LABEL, 0);

            for (int i = 0; i < 5; i++)
                todayTimeLimitedMaxScore[i] = preferences.getInt(TODAY_HIGH_SCORE_TIME_LIMITED_LABEL + i, 0);

            for (int i = 0; i < 5; i++)
                todayMoveLimitedMaxScore[i] = preferences.getInt(TODAY_HIGH_SCORE_MOVE_LIMITED_LABEL + i, 0);
        } else {
            firstLaunch = preferences.getString(LAST_DAY_PLAYED_LABEL, "none").equals("none");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LAST_DAY_PLAYED_LABEL, calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH));
            editor.putInt(TODAY_HIGH_SCORE_ENDLESS_LABEL, 0);
            for (int i = 0; i < 5; i++)
                editor.putInt(TODAY_HIGH_SCORE_TIME_LIMITED_LABEL + i, 0);
            for (int i = 0; i < 5; i++)
                editor.putInt(TODAY_HIGH_SCORE_MOVE_LIMITED_LABEL + i, 0);
            editor.apply();
        }
    }

    public static ScoreType onGameFinished(GameLogic logic) {
        int index = logic.getCurrentMode().getMultiplier() - 1;
        if (index < 0 || index > 4)
            return ScoreType.Normal;

        ScoreType result = ScoreType.Normal;

        if (logic.getCurrentMode() instanceof EndlessMode) {
            if (logic.points > todayEndlessMaxScore) {
                todayEndlessMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TODAY_HIGH_SCORE_ENDLESS_LABEL, todayEndlessMaxScore);
                editor.apply();

                result = ScoreType.TodayHigh;
            }

            if (logic.points > endlessMaxScore) {
                endlessMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE_ENDLESS_LABEL, endlessMaxScore);
                editor.apply();

                result = ScoreType.AllTimeHigh;
            }
        } else if (logic.getCurrentMode() instanceof TimeLimitedMode) {
            if (logic.points > todayTimeLimitedMaxScore[index]) {
                todayTimeLimitedMaxScore[index] = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TODAY_HIGH_SCORE_TIME_LIMITED_LABEL + index, todayTimeLimitedMaxScore[index]);
                editor.apply();

                result = ScoreType.TodayHigh;
            }

            if (logic.points > timeLimitedMaxScore[index]) {
                timeLimitedMaxScore[index] = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE_TIME_LIMITED_LABEL + index, timeLimitedMaxScore[index]);
                editor.apply();

                result = ScoreType.AllTimeHigh;
            }
        } else if (logic.getCurrentMode() instanceof MoveLimitedMode) {
            if (logic.points > todayMoveLimitedMaxScore[index]) {
                todayMoveLimitedMaxScore[index] = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TODAY_HIGH_SCORE_MOVE_LIMITED_LABEL + index, todayMoveLimitedMaxScore[index]);
                editor.apply();

                result = ScoreType.TodayHigh;
            }

            if (logic.points > moveLimitedMaxScore[index]) {
                moveLimitedMaxScore[index] = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE_MOVE_LIMITED_LABEL + index, moveLimitedMaxScore[index]);
                editor.apply();

                result = ScoreType.AllTimeHigh;
            }
        } else if (logic.getCurrentMode() instanceof ConstantMode) {
            if (logic.points > todayConstantMaxScore) {
                todayConstantMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TODAY_HIGH_SCORE_CONSTANT_LABEL, todayConstantMaxScore);
                editor.apply();

                result = ScoreType.TodayHigh;
            }

            if (logic.points > constantMaxScore) {
                constantMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE_CONSTANT_LABEL, constantMaxScore);
                editor.apply();

                result = ScoreType.AllTimeHigh;
            }
        } else if (logic.getCurrentMode() instanceof ResettingTimeMode) {
            if (logic.points > todayResettingTimeMaxScore) {
                todayResettingTimeMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TODAY_HIGH_SCORE_RESETTING_TIME_LABEL, todayResettingTimeMaxScore);
                editor.apply();

                result = ScoreType.TodayHigh;
            }

            if (logic.points > resettingTimeMaxScore) {
                resettingTimeMaxScore = logic.points;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE_RESETTING_TIME_LABEL, resettingTimeMaxScore);
                editor.apply();

                result = ScoreType.AllTimeHigh;
            }
        }

        if (logic.moveCount > 10 || logic.gameDuration > 20000) {
            gamesPlayed++;

            preferences.edit().putInt(GAMES_PLAYED_COUNT_LABEL, gamesPlayed).apply();
        }

        return result;
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static int getGamesPlayed() {
        return gamesPlayed;
    }

    public static int getEndlessMaxScore() {
        return endlessMaxScore;
    }

    public static int getTodayEndlessMaxScore() {
        return todayEndlessMaxScore;
    }

    public static int getConstantMaxScore() {
        return constantMaxScore;
    }

    public static int getTodayConstantMaxScore() {
        return todayConstantMaxScore;
    }

    public static int getResettingTimeMaxScore() {
        return resettingTimeMaxScore;
    }

    public static int getTodayResettingTimeMaxScore() {
        return todayResettingTimeMaxScore;
    }

    public static int getTimeLimitedMaxScore(int multiplier) {
        return timeLimitedMaxScore[multiplier];
    }

    public static int[] getTimeLimitedMaxScore() {
        return timeLimitedMaxScore;
    }

    public static int getTodayTimeLimitedMaxScore(int multiplier) {
        return todayTimeLimitedMaxScore[multiplier];
    }

    public static int[] getTodayTimeLimitedMaxScore() {
        return todayTimeLimitedMaxScore;
    }

    public static int getMoveLimitedMaxScore(int multiplier) {
        return moveLimitedMaxScore[multiplier];
    }

    public static int[] getMoveLimitedMaxScore() {
        return moveLimitedMaxScore;
    }

    public static int getTodayMoveLimitedMaxScore(int multiplier) {
        return todayMoveLimitedMaxScore[multiplier];
    }

    public static int[] getTodayMoveLimitedMaxScore() {
        return todayMoveLimitedMaxScore;
    }

    public static boolean isFirstLaunch() {
        return firstLaunch;
    }

    public enum ScoreType { Normal, AllTimeHigh, TodayHigh }
}
