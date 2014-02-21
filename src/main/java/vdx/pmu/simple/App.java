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
        Datastore ds = morphia.createDatastore(mongo, "pmu");
        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
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
        //strategyMulti(ds, 5, 7, 26, 52, true);
        strategyMulti(ds, 4, 7, 28, 52, true, true);

    }

    public static void strategyMulti(Datastore ds, int base, int nbMulti, int coteMin, int coteMax, boolean showInter, boolean useStats) {
        PronoStatistics pS = new PronoStatistics(ds);
        
        Double gains = 0.0;
        Double bet = 1.0;
        Double maxLoose = 0.0;
        Double loose = 0.0;
        Integer maxEcart = 0;
        Integer ecart = 0;
        Integer nbEcart = 0;
        Double avgEcart = 0.0;
        Integer nbWin = 0;
        Integer nbCourses = 0;
        Map<String, Stats> stats;
        for (Course thisCourse : ds.find(Course.class).order("date")) {
            if(useStats) {
                stats = pS.getStats(ds.find(Course.class).field("date").in(getDateOfRacesBefore(thisCourse.date, 60)).asList());
            } else {
                stats = null;
            }
            Map<Double, ChevToPlay> apply = pS.apply(thisCourse, stats);
            ArrayList<Integer> myFinish = new ArrayList<Integer>();
            //System.out.println("finish: " + thisCourse.finish.toString());
            //System.out.println("refCote: " + thisCourse.refCote.toString());
            int i = 0;
            for (Entry<Double, ChevToPlay> entry : apply.entrySet()) {
                if (i < base) {
                    //System.out.println("add firsts: " + i + "/4: " + en.getKey());
                    myFinish.add(entry.getValue().chev);
                } else {
                    //System.out.println("///"+entry.getValue().toString());
                    Double cote = entry.getValue().cote;
                    //System.out.println("check " + en.getKey() + ": " + en.getValue());
                    if (cote >= coteMin && cote <= coteMax) {
                        //System.out.println("add " + en.getKey() + ": " + en.getValue());
                        myFinish.add(entry.getValue().chev);
                    }
                    if (myFinish.size() >= nbMulti) {
                        break;
                    }
                }
                i++;
            }
            if (thisCourse.finish != null && thisCourse.finish.size() >= 4) {
                nbCourses++;
                thisCourse.finish.remove(null);
                if (thisCourse.gains.containsKey("m" + myFinish.size()) && myFinish.containsAll(thisCourse.finish.subList(0, 4))) {
                    //System.out.println("- "+thisCourse.date+" "+thisCourse.gains+" "+myFinish.size());
                    gains += thisCourse.gains.get("m" + myFinish.size()) * bet;
                    gains -= bet * 3;
                    if (!myFinish.subList(0, 4).containsAll(thisCourse.finish.subList(0, 4))) {
                        bet = 1.0;
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
                            System.out.println(thisCourse.date + " WIN: " + myFinish.toString() + " / " + thisCourse.finish.toString() + ": m" + myFinish.size() + ": " + thisCourse.gains.get("m" + myFinish.size()) + " -> " + gains.toString());
                        }
                    } else {
                        if (showInter) {
                            System.out.println(thisCourse.date + " WIN:-" + myFinish.toString() + " / " + thisCourse.finish.toString() + ": m" + myFinish.size() + ": " + thisCourse.gains.get("m" + myFinish.size()) + " -> " + gains.toString());
                        }
                    }
                } else {
                    gains -= bet * 3;
                    loose += bet * 3;
                    bet++;
                    ecart++;
                    if (maxLoose < loose) {
                        maxLoose = loose;
                        maxEcart = ecart;
                    }
                    if (showInter) {
                        System.out.println(thisCourse.date + " LOO: " + myFinish.toString() + " / " + thisCourse.finish.subList(0, 4).toString());
                    }
                }
            } else {
                if (showInter) {
                    System.out.println(thisCourse.date + " NOF: " + myFinish.toString() + " (finish is " + thisCourse.finish + ")");
                }
            }
        }
        avgEcart /= nbEcart;
        if (gains > 3000 && maxLoose < 1500 && nbWin>10) {
            System.out.println("[" + coteMin + "/" + coteMax + "] gains: " + gains.toString() + " / maxLoose: " + maxLoose.toString() + " / nbWin: " + nbWin + " / nbCourses: " + nbCourses + " maxEcart: " + maxEcart + " / avgEcart: " + avgEcart);
        }
        /*pS.getDateOfRaces("2013-09-12..2013-09-20,2014-03-01");
         Map<String, Stats> stats = pS.getStats(pS.getCourses(pS.getDateOfRaces("2013-09-12..2013-09-13")));
         System.out.println("stats: " + stats.toString());
         System.out.println(pS.apply(thisCourse1, stats).toString());*/
    }
}
