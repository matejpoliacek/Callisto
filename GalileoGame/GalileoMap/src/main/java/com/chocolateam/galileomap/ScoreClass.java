package com.chocolateam.galileomap;

/**
 * Created by Matej Poliacek on 15/03/2018.
 */

public class ScoreClass {

    private int score;
    private long lastTime = System.currentTimeMillis();

    private final int COLLECT_SCORE = 100;
    private final int OBSTALCE_PENALTY_TICK = 10;
    private final int TIME_PENALTY_TICK = 1;
    private final int INIT_MULTIPLIER = 100;


    public ScoreClass(int initial) {
        this.score = initial*INIT_MULTIPLIER;
        this.lastTime = System.currentTimeMillis();
    }


    public void applyObstaclePenalty() {
       applyPenalty(OBSTALCE_PENALTY_TICK);
    }

    public void applyTimePenalty() {
        applyPenalty(TIME_PENALTY_TICK);
    }

    private void applyPenalty(int PENALTY) {
        int secDelta = secsPassed();

        if (secDelta > 0) {
            this.score -= PENALTY * secDelta;
        }
    }

    public void increaseScore() {
        this.score += COLLECT_SCORE;
    }

    private int secsPassed() {
        int secsPassed = (int)((System.currentTimeMillis() - this.lastTime) / 1000.0);
        System.out.print("Time: " + secsPassed);

        if (secsPassed > 0) {
            // if one second has passed, set new last time
            this.lastTime = System.currentTimeMillis();
        }

        return secsPassed;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
