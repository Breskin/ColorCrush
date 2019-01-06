package com.github.breskin.squares.game;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.github.breskin.squares.Logo;
import com.github.breskin.squares.game.context.ContextualInfo;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;
import com.github.breskin.squares.Scoreboard;

/**
 * Created by Kuba on 15.11.2018.
 */

public class Overlord {

    public static GameMode CurrentMode = GameMode.Infinite;

    public static final int MAX_MOVES = 50, DURATION = 60000;
    public static int Moves = 0, Time = 0, ModeMultiplier = 1;
    public static float Points = 0;
    private float pointsIncrease = 0;

    private boolean gameStarted = false, gameFinished = false, gameConditionMet = false;
    private Scoreboard.ScoreType ScoreType = Scoreboard.ScoreType.Default;

    private MainView mMainView;
    private Scoreboard mScoreboard;

    Board Board;
    ContextualInfo GameInfo;
    ScoreboardButton scoreboardButton;

    public Overlord(MainView mv) {
        mMainView = mv;

        Board = new Board(this);
        GameInfo = new ContextualInfo(this);
        scoreboardButton = new ScoreboardButton(this);
    }

    public void update() {
        updatePoints();
        checkGameState();

        GameInfo.update();
        Board.update();
        scoreboardButton.update();
    }

    public void render(Canvas canvas) {
        GameInfo.render(canvas);
        Board.render(canvas);
        scoreboardButton.render(canvas);
    }

    public void createNewGame() {
        MainView.CurrentView = MainView.ViewType.Game;
        gameStarted = gameFinished = gameConditionMet = false;
        ScoreType = Scoreboard.ScoreType.Default;

        Moves = 0;
        Time = 0;
        Points = 0;
        pointsIncrease = 0;

        Board.startGame();
        GameInfo.playInitialAnimation();
        scoreboardButton.show();
    }

    public void addPoints(float p ) {
        pointsIncrease += p;
    }

    void updatePoints() {
        if (pointsIncrease > 0) {
            Points += pointsIncrease * 0.15f;
            pointsIncrease *= 0.85f;
        }
    }

    void checkGameState() {
        if (!gameStarted)
            return;

        switch (Overlord.CurrentMode) {
            case TimeLimited:
                if (Overlord.Time > Overlord.DURATION * Overlord.ModeMultiplier) {
                    gameConditionMet = true;
                }
                break;

            case MoveLimited:
                if (Overlord.Moves >= Overlord.MAX_MOVES * Overlord.ModeMultiplier) {
                    gameConditionMet = true;
                }
                break;
        }
    }

    public void onGameStarted() {
        gameStarted = true;
        GameInfo.onGameStarted();
    }

    public void onGameFinished() {
        ScoreType = mScoreboard.checkScore(Math.round(Points + pointsIncrease));
        gameFinished = true;

        GameInfo.onGameFinished();
    }

    public void onUIHidden() {
        mMainView.nextView();
    }

    public void hideGameUI() {
        Board.finishGame();
        GameInfo.hide();
        scoreboardButton.hide();
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (y > Board.getTopMargin() && y < Board.getTopMargin() + Board.getHeight())
            Board.onTouchEvent(event);
        else
            Board.onExternalTouch();

        if ((y > 0 && y < Board.getTopMargin()) || gameFinished)
            GameInfo.onTouchEvent(event);

        if (y > MainView.ViewHeight - Logo.Size.y)
            scoreboardButton.onTouchEvent(event);
    }

    public boolean onBackPressed() {
        if (gameStarted && !gameFinished) {
            gameConditionMet = true;
            return true;
        } else if (gameStarted && gameFinished) {
            GameInfo.back();
            return true;
        }

        return false;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public boolean isGameConditionMet() {
        return gameConditionMet;
    }

    public void setGameConditionMet(boolean gameConditionMet) {
        this.gameConditionMet = gameConditionMet;
    }

    public Scoreboard.ScoreType getScoreType() {
        return ScoreType;
    }

    public void setScoreboard(Scoreboard sc) {
        mScoreboard = sc;
    }

    public void load(Context context) {
        GameMode.load(context);
        GameInfo.load(context);
        scoreboardButton.load(context);
        Board.load(context);
    }

    public boolean canEndGame() {
        return !GameInfo.isAnimating();
    }

    public enum GameMode {
        Infinite, TimeLimited, MoveLimited, UntilPointless;

        static String infiniteName, timeLimitedName, moveLimitedName, untilPointlessName, infiniteDesc, timeLimitedDesc, timeLimitedDescOne, moveLimitedDesc, untilPointlessDesc;

        public static void load(Context context) {
            infiniteName = context.getString(R.string.gamemode_infinite);
            timeLimitedName = context.getString(R.string.gamemode_timelimited);
            moveLimitedName = context.getString(R.string.gamemode_movelimited);
            untilPointlessName = context.getString(R.string.gamemode_untilpointless);

            infiniteDesc = context.getString(R.string.gamemode_infinite_description);
            timeLimitedDesc = context.getString(R.string.gamemode_timelimited_description);
            timeLimitedDescOne = context.getString(R.string.gamemode_timelimited_one_description);
            moveLimitedDesc = context.getString(R.string.gamemode_movelimited_description);
            untilPointlessDesc = context.getString(R.string.gamemode_untilpointless_description);
        }

        public GameMode next() {
            switch (this) {
                case Infinite:
                    return GameMode.UntilPointless;
                case TimeLimited:
                    return GameMode.Infinite;
                case MoveLimited:
                    return GameMode.TimeLimited;
                case UntilPointless:
                    return GameMode.MoveLimited;

                default:
                    return GameMode.Infinite;
            }
        }

        public GameMode previous() {
            switch (this) {
                case Infinite:
                    return GameMode.TimeLimited;
                case TimeLimited:
                    return GameMode.MoveLimited;
                case MoveLimited:
                    return GameMode.UntilPointless;
                case UntilPointless:
                    return GameMode.Infinite;

                default:
                    return GameMode.Infinite;
            }
        }

        public String getName() {
            switch (this) {
                case Infinite:
                    return infiniteName;
                case TimeLimited:
                    return timeLimitedName;
                case MoveLimited:
                    return moveLimitedName;
                case UntilPointless:
                    return untilPointlessName;

                default:
                    return infiniteName;
            }
        }

        public String getDescription() {
            switch (this) {
                case Infinite:
                    return infiniteDesc;
                case TimeLimited:
                    if (Overlord.ModeMultiplier == 1)
                        return String.format(timeLimitedDescOne,Overlord.ModeMultiplier);
                    else
                        return String.format(timeLimitedDesc, Overlord.ModeMultiplier);
                case MoveLimited:
                    return String.format(moveLimitedDesc, Overlord.MAX_MOVES * Overlord.ModeMultiplier);

                case UntilPointless:
                    return untilPointlessDesc;

                default:
                    return infiniteDesc;
            }
        }
    }

}
