/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import java.util.Map;

/**
 *
 * @author virgile
 */
public class BetStrategy {

    private PlayList playlist;
    private Integer startBetAfter;
    private Integer stopBetAfter;
    private Double initialBet;
    private boolean showInter;

    public Double gains = 0.0;
    public Double bet = 0.0;
    public Double maxLoose = 0.0;
    public Double loose = 0.0;
    public Integer maxEcart = 0;
    public Integer ecart = 0;
    public Integer nbEcart = 0;
    public Double avgEcart = 0.0;
    public Integer nbWin = 0;
    public Integer nbCourses = 0;

    public BetStrategy() {
    }

    public BetStrategy withPlaylist(PlayList playlist) {
        this.playlist = playlist;
        return this;
    }

    public BetStrategy startBetAfter(Integer startBetAfter) {
        this.startBetAfter = startBetAfter;
        return this;
    }

    public BetStrategy stopBetAfter(Integer stopBetAfter) {
        this.stopBetAfter = stopBetAfter;
        return this;
    }

    public BetStrategy withInitialBet(Double initialBet) {
        this.initialBet = initialBet;
        return this;
    }

    public BetStrategy showInter(boolean showInter) {
        this.showInter = showInter;
        return this;
    }

    public void bet() {
        initBet();
        for (Map.Entry<String, Play> entry : this.playlist.playList.entrySet()) {
            String string = entry.getKey();
            Play thisPlay = entry.getValue();
            Course thisCourse = thisPlay.course;
            if (thisCourse.finish != null && thisCourse.finish.size() >= 4 && thisPlay.myFinish.size() > 4) {
                nbCourses++;
                if (ecart == startBetAfter) {
                    if (showInter) {
                        System.out.println("------ start betting--");
                    }
                    bet = initialBet;
                } else {
                    if (ecart > startBetAfter) {
                        bet++;
                    }
                    if (ecart == stopBetAfter) {
                        bet = 0.0;
                        ecart = 0;
                        if (showInter) {
                            System.out.println("------ stop betting--");
                        }
                    }
                }
                gains -= bet * 3;
                thisCourse.finish.remove(null);
                if (thisCourse.gains.containsKey("m" + thisPlay.myFinish.size()) && thisPlay.myFinish.containsAll(thisCourse.finish.subList(0, 4))) {
                    //System.out.println("- "+thisCourse.date+" "+thisCourse.gains+" "+myFinish.size());
                    gains += thisCourse.gains.get("m" + thisPlay.myFinish.size()) * bet;
                    if (bet != 0.0 && !thisPlay.myFinish.subList(0, 4).containsAll(thisCourse.finish.subList(0, 4))) {
                        bet = 0.0;
                        if (maxLoose <= loose) {
                            if (showInter) {
                                System.out.println("max loose reached: " + maxLoose.toString());
                            }
                            maxLoose = loose;
                        }
                        nbEcart++;
                        avgEcart += ecart;
                        ecart = 0;
                        loose = 0.0;
                        nbWin++;
                        if (showInter) {
                            System.out.println(thisCourse.date + " WIN: " + thisPlay.myFinish.toString() + " / " + thisCourse.finish.toString() + ": m" + thisPlay.myFinish.size() + ": " + thisCourse.gains.get("m" + thisPlay.myFinish.size()) + " -> " + gains.toString());
                            System.out.println("------ stop betting--");
                        }
                    } else {
                        if (showInter) {
                            System.out.println(thisCourse.date + " WIN:-" + thisPlay.myFinish.toString() + " / " + thisCourse.finish.toString() + ": m" + thisPlay.myFinish.size() + ": " + thisCourse.gains.get("m" + thisPlay.myFinish.size()) + " -> " + gains.toString());
                        }
                    }
                } else {
                    loose += bet * 3;
                    ecart++;
                    if (maxLoose < loose) {
                        maxLoose = loose;
                        maxEcart = ecart;
                    }
                    if (showInter) {
                        System.out.println(thisCourse.date + " LOO: " + thisPlay.myFinish.toString() + " / " + thisCourse.finish.subList(0, 4).toString() + " -> " + gains.toString());
                    }
                }
            } else {
                if (showInter) {
                    System.out.println(thisCourse.date + " NOF: " + thisPlay.myFinish.toString() + " (finish is " + thisCourse.finish + ")");
                }
            }
        }
        avgEcart /= nbEcart;
        if (showInter) {
            System.out.println(this);
        }
        /*pS.getDateOfRaces("2013-09-12..2013-09-20,2014-03-01");
         Map<String, Stats> stats = pS.getStats(pS.getCourses(pS.getDateOfRaces("2013-09-12..2013-09-13")));
         System.out.println("stats: " + stats.toString());
         System.out.println(pS.apply(thisCourse1, stats).toString());*/
    }

    @Override
    public String toString() {
        return String.format("[ %2d-%2d-%2.1f / %2d/%2d] gains: %9.1f / maxLoose: %6.1f / nbWin: %2d / nbCourses: %4d / maxEcart: %3d / avgEcart: %3.1f",
            startBetAfter, stopBetAfter, initialBet, playlist.coteMin, playlist.coteMax, gains,
            maxLoose, nbWin, nbCourses, maxEcart, avgEcart);
    }

    private void initBet() {
        gains = 0.0;
        bet = 0.0;
        maxLoose = 0.0;
        loose = 0.0;
        maxEcart = 0;
        ecart = 0;
        nbEcart = 0;
        avgEcart = 0.0;
        nbWin = 0;
        nbCourses = 0;
    }

}
