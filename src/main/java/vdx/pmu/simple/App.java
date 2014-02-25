package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

import java.util.Date;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import static vdx.pmu.simple.Utils.getDateOfRacesBefore;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws UnknownHostException, ParseException {
        Mongo mongo = new Mongo("localhost", 27017);
        Morphia morphia = new Morphia();
        morphia.map(Course.class);
        if (true) {
            Datastore ds = morphia.createDatastore(mongo, "pmu");
            //DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            //Course thisCourse1;
            //field("date").equal((java.util.Date)sdf.parse("2013-09-11")).get();
            //System.out.println("course: " + thisCourse.toString());
            //thisCourse1 = ds.find(Course.class).field("date").greaterThan("2013-09-11").get();
            //System.out.println("course: " + thisCourse.toString());
            /*for (int base = 1; base <= 6; base++) {
            System.out.println("*****************"+ shortDateFormat.format(new Date()) +"-- base: "+base);
            for (int nbMulti = 5; nbMulti <= 7; nbMulti++) {
            System.out.println("*****************"+ shortDateFormat.format(new Date()) +"------- multi: "+nbMulti);
            for (int coteMin = 8; coteMin < 80; coteMin++) {
            System.out.println("*****************"+ shortDateFormat.format(new Date()) +"------------- Trying cote min " + coteMin);
            for (int coteMax = coteMin; coteMax < 80; coteMax++) {
            strategyMulti(ds, base, nbMulti, coteMin, coteMax, false);
            }
            }
            }
            }*/

            /*PlayList strategyMulti = strategyMulti(ds, 4, 7, 28, 52, false);
            for (int i = 0; i < 25; i++) {
                for (int j = 1; j < 25; j++) {
                    bet(ds, strategyMulti, i, new Double(j), false);
                }
            }*/
            bet(ds, strategyMulti(ds, 4, 7, 28, 52, false), 17,7.0, true);
        } else {
            Datastore ds = morphia.createDatastore(mongo, "pmuStats");
            int played = 0;
            int dumped = 0;
            int ecart = 0;
            int maxEcart = 0;
            for (Course thisCourse : ds.find(Course.class).order("date")) {
                if(thisCourse.finish!=null&&thisCourse.finish.size()>3&&thisCourse.refCote!=null) {
                    played++;
                    boolean dump = true;
                    for (int i = 0; i < 4; i++) {
                        Integer chev = thisCourse.finish.get(i);
                        Double cote = thisCourse.refCote.get(chev);
                        if (cote > 52) {
                            dump = false;
                        } else {
                            if (cote < 28 && cote > 10) {
                                dump = false;
                            }
                        }
                    }
                    if (dump) {
                        if(maxEcart<ecart) {
                            maxEcart=ecart;
                        }
                        dumped++;
                        ecart=0;
                        System.out.println("course: " + thisCourse.dumpStats());
                    } else {
                        ecart++;
                    }
                }
            }
            System.out.println("dumped "+dumped+" on "+played+" : "+maxEcart);
        }

    }

    public static PlayList strategyMulti(Datastore ds, int base, int nbMulti, int coteMin, int coteMax, boolean useStats) {
        PronoStatistics pS = new PronoStatistics(ds);
        PlayList playList = new PlayList(coteMin, coteMax, nbMulti, base);
        Map<String, Stats> stats;
        for (Course thisCourse : ds.find(Course.class).order("date")) {
            Play play = new Play(thisCourse);
            playList.playList.put(thisCourse.date, play);
            if (useStats) {
                stats = pS.getStats(ds.find(Course.class).field("date").in(getDateOfRacesBefore(thisCourse.date, 60, 15)).asList());
                //stats = pS.getStats(ds.find(Course.class).field("gains.m7").greaterThan(new  Integer(30)).asList());
            } else {
                stats = null;
            }
            Map<Double, ChevToPlay> apply = pS.apply(thisCourse, stats);
            play.myFinish = new ArrayList<Integer>();
            //System.out.println("finish: " + thisCourse.finish.toString());
            //System.out.println("refCote: " + thisCourse.refCote.toString());
            int i = 0;
            for (Entry<Double, ChevToPlay> entry : apply.entrySet()) {
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
        return playList;
    }

    public static void bet(Datastore ds, PlayList playlist, Integer startBetAfter, Double initialBet, boolean showInter) {
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
        for (Entry<String, Play> entry : playlist.playList.entrySet()) {
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
