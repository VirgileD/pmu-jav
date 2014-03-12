/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import static vdx.pmu.simple.Utils.getDate;
import static vdx.pmu.simple.Utils.getDateOfRacesBefore;

/**
 *
 * @author virgile
 */
public class StrategyMulti {

    Datastore ds;
    int base;
    int nbMulti;
    int coteMin;
    int coteMax;
    boolean useStats;
    boolean coteOnly;
    PlayList playlist;

    public StrategyMulti() {
        base = 4;
        nbMulti = 7;
        coteMin = 28;
        coteMax = 52;
        useStats = false;
        coteOnly = false;
    }

    public StrategyMulti(Datastore ds) {
        this.ds = ds;
        base = 4;
        nbMulti = 7;
        coteMin = 28;
        coteMax = 52;
        useStats = false;
        coteOnly = false;
    }

    public StrategyMulti setDs(Datastore ds) {
        this.ds = ds;
        return this;
    }

    public StrategyMulti setBase(int base) {
        this.base = base;
        return this;
    }

    public StrategyMulti setNbMulti(int nbMulti) {
        this.nbMulti = nbMulti;
        return this;
    }

    public StrategyMulti setCoteMin(int coteMin) {
        this.coteMin = coteMin;
        return this;
    }

    public StrategyMulti setCoteMax(int coteMax) {
        this.coteMax = coteMax;
        return this;
    }

    public StrategyMulti setCoteOnly(boolean coteOnly) {
        this.coteOnly = coteOnly;
        return this;
    }
    
    public StrategyMulti setUseStats(boolean useStats) {
        this.useStats = useStats;
        return this;
    }

    public PlayList getPlaylist() {
        return this.playlist;
    }
    
    public PlayList processPlaylist() {
        PronoStatistics pS = new PronoStatistics();
        PlayList playList = new PlayList(coteMin, coteMax, nbMulti, base);
        Map<String, Stats> stats;
        Map<Double, ChevToPlay> apply;
        for (Course thisCourse : ds.find(Course.class).order("date")) {
            Play play = new Play(thisCourse);
            playList.playList.put(thisCourse.date, play);
            if(!coteOnly) {
                if (useStats) {
                    Query<Course> find = ds.find(Course.class);
                    find.and(find.criteria("date").greaterThan(getDate(thisCourse.date, 60)), find.criteria("date").lessThan(getDate(thisCourse.date, 45)));
                    stats = pS.getStats(find.asList());
                            //.in(getDateOfRacesBefore(thisCourse.date, 30, 15)).asList());
                    //stats = pS.getStats(ds.find(Course.class).field("gains.m7").greaterThan(new  Integer(30)).asList());
                } else {
                    stats = null;
                }
                apply = pS.apply(thisCourse, stats);
            } else {
                TreeMap<Double, ChevToPlay> applyTmp = new TreeMap<Double, ChevToPlay>();
                for (Map.Entry<Integer, Double> entry : thisCourse.refCote.entrySet()) {
                    Integer chev = entry.getKey();
                    Double cote = entry.getValue();
                    ChevToPlay tmpChevToPlay = new ChevToPlay(chev);
                    tmpChevToPlay.cote=cote;
                    tmpChevToPlay.score=cote;
                    applyTmp.put(cote, tmpChevToPlay);
                }
                apply = applyTmp.descendingMap();
                        
            }
            play.myFinish = new ArrayList<Integer>();
            //System.out.println("finish: " + thisCourse.finish.toString());
            //System.out.println("refCote: " + thisCourse.refCote.toString());
            int i = 0;
            for (Map.Entry<Double, ChevToPlay> entry : apply.entrySet()) {
                if (i < base) {
                    //System.out.println("add firsts: " + i + "/4: " + en.getKey());
                    play.myFinish.add(entry.getValue().chev);
                } else {
                    //System.out.println("///"+entry.getValue().toString());
                    Double cote = entry.getValue().cote;
                    //System.out.println("check " + en.getKey() + ": " + en.getValue());
                    if (cote >= coteMin && cote <= coteMax) {
                        //System.out.println("add " + en.getKey() + ": " + en.getValue());
                        play.myFinish.add(entry.getValue().chev);
                    }
                    if (play.myFinish.size() >= nbMulti) {
                        break;
                    }
                }
                i++;
            }
        }
        this.playlist= playList;
        return playList;
    }

}
