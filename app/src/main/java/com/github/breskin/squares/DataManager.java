package com.github.breskin.squares;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.breskin.squares.gameplay.modes.ConstantMode;
import com.github.breskin.squares.gameplay.modes.EndlessMode;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.MoveLimitedMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataManager {

    private static SharedPreferences preferences;
    private static GregorianCalendar calendar;

    private static int endlessMaxScore, constantMaxScore, moveLimitedMaxScore[] = new int[5], timeLimitedMaxScore[] = new int[5];
    private static int todayEndlessMaxScore, todayConstantMaxScore, todayMoveLimitedMaxScore[] = new int[5], todayTimeLimitedMaxScore[] = new int[5];

    public static void init(Context context) {
        preferences = context.getSharedPreferences("scoreboard", Context.MODE_PRIVATE);
        calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        endlessMaxScore = preferences.getInt("high-score-infinite", 0);
        constantMaxScore = preferences.getInt("high-score-until-pointless", 0);

        for (int i = 0; i < 5; i++)
            timeLimitedMaxScore[i] = preferences.getInt("high-score-time-limited-" + i, 0);

        for (int i = 0; i < 5; i++)
            moveLimitedMaxScore[i] = preferences.getInt("high-score-move-limited-" + i, 0);


        if (preferences.getString("last-day-played", "none").equals(calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH))) {
            todayEndlessMaxScore = preferences.getInt("today-high-score-infinite", 0);
            todayConstantMaxScore = preferences.getInt("today-high-score-until-pointless", 0);
            for (int i = 0; i < 5; i++)
                todayTimeLimitedMaxScore[i] = preferences.getInt("today-high-score-time-limited-" + i, 0);

            for (int i = 0; i < 5; i++)
                todayMoveLimitedMaxScore[i] = preferences.getInt("today-high-score-move-limited-" + i, 0);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("last-day-played", calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH));
            editor.putInt("today-high-score-infinite", 0);
            for (int i = 0; i < 5; i++)
                editor.putInt("today-high-score-time-limited-" + i, 0);
            for (int i = 0; i < 5; i++)
                editor.putInt("today-high-score-move-limited-" + i, 0);
            editor.commit();
        }
    }

    public static ScoreType checkScore(GameMode mode, int score) {
        int index = mode.getMultiplier() - 1;
        if (index < 0 || index > 4)
            return ScoreType.Normal;

        ScoreType result = ScoreType.Normal;

        if (mode instanceof EndlessMode) {
            if (score > todayEndlessMaxScore) {
                todayEndlessMaxScore = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("today-high-score-infinite", todayEndlessMaxScore);
                editor.commit();

                result = ScoreType.TodayHigh;
            }

            if (score > endlessMaxScore) {
                endlessMaxScore = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("high-score-infinite", endlessMaxScore);
                editor.commit();

                result = ScoreType.AllTimeHigh;
            }
        } else if (mode instanceof TimeLimitedMode) {
            if (score > todayTimeLimitedMaxScore[index]) {
                todayTimeLimitedMaxScore[index] = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("today-high-score-time-limited-" + index, todayTimeLimitedMaxScore[index]);
                editor.commit();

                result = ScoreType.TodayHigh;
            }

            if (score > timeLimitedMaxScore[index]) {
                timeLimitedMaxScore[index] = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("high-score-time-limited-" + index, timeLimitedMaxScore[index]);
                editor.commit();

                result = ScoreType.AllTimeHigh;
            }
        } else if (mode instanceof MoveLimitedMode) {
            if (score > todayMoveLimitedMaxScore[index]) {
                todayMoveLimitedMaxScore[index] = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("today-high-score-move-limited-" + index, todayMoveLimitedMaxScore[index]);
                editor.commit();

                result = ScoreType.TodayHigh;
            }

            if (score > moveLimitedMaxScore[index]) {
                moveLimitedMaxScore[index] = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("high-score-move-limited-" + index, moveLimitedMaxScore[index]);
                editor.commit();

                result = ScoreType.AllTimeHigh;
            }
        } else if (mode instanceof ConstantMode) {
            if (score > todayConstantMaxScore) {
                todayConstantMaxScore = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("today-high-score-until-pointless", todayConstantMaxScore);
                editor.commit();

                result = ScoreType.TodayHigh;
            }

            if (score > constantMaxScore) {
                constantMaxScore = score;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("high-score-until-pointless", constantMaxScore);
                editor.commit();

                result = ScoreType.AllTimeHigh;
            }
        }

        return result;
    }

    public enum ScoreType { Normal, AllTimeHigh, TodayHigh }
}
