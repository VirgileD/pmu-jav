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
        Double gains = 0.0;
        Double bet = 0.0;
        Double maxLoose = 0.0;
        Double loose = 0.0;
        Integer maxEcart = 0;
        Integer ecart = 0;
        Integer nbEcart = 0;
        Double avgEcart = 0.0;
        Integer nbWin = 0;
        Integer nbCourses = 0;
        for (Map.Entry<String, Play> entry : this.playlist.playList.entrySet()) {
            String string = entry.getKey();
            Play thisPlay = entry.getValue();
            Course thisCourse = thisPlay.course;
            if (thisCourse.finish != null && thisCourse.finish.size() >= 4) {
                nbCourses++;
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
                        }
                    } else {
                        if (showInter) {
                            System.out.println(thisCourse.date + " WIN:-" + thisPlay.myFinish.toString() + " / " + thisCourse.finish.toString() + ": m" + thisPlay.myFinish.size() + ": " + thisCourse.gains.get("m" + thisPlay.myFinish.size()) + " -> " + gains.toString());
                        }
                    }
                } else {
                    loose += bet * 3;
                    if (ecart == startBetAfter) {
                        if (showInter) {
                            System.out.println("------ start betting--");
                        }
                        bet = initialBet;
                    } else {
                        if (ecart > startBetAfter) {
                            bet++;
                        }
                    }
                    ecart++;
                    if (maxLoose < loose) {
                        maxLoose = loose;
                        maxEcart = ecart;
                    }
                    if (showInter) {
                        System.out.println(thisCourse.date + " LOO: " + thisPlay.myFinish.toString() + " / " + thisCourse.finish.subList(0, 4).toString());
                    }
                }
            } else {
                if (showInter) {
                    System.out.println(thisCourse.date + " NOF: " + thisPlay.myFinish.toString() + " (finish is " + thisCourse.finish + ")");
                }
            }
        }
        avgEcart /= nbEcart;
        //if (gains > 3000 && maxLoose < 1500 && nbWin > 10) {
        if(maxLoose<400)
        System.out.println("[ "+startBetAfter+"/"+ initialBet +" / "+ playlist.coteMin + "/" + playlist.coteMax + "] gains: " + gains.toString() + " / maxLoose: " + maxLoose.toString() + " / nbWin: " + nbWin + " / nbCourses: " + nbCourses + " maxEcart: " + maxEcart + " / avgEcart: " + avgEcart);
        //}
        /*pS.getDateOfRaces("2013-09-12..2013-09-20,2014-03-01");
         Map<String, Stats> stats = pS.getStats(pS.getCourses(pS.getDateOfRaces("2013-09-12..2013-09-13")));
         System.out.println("stats: " + stats.toString());
         System.out.println(pS.apply(thisCourse1, stats).toString());*/
    }
}
