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
            StrategyMulti strategyMulti = new StrategyMulti(ds);
            boolean playNow=false;
            if(playNow) {
                strategyMulti.setBase(4).setNbMulti(7).setCoteMin(29).setCoteMax(45).setUseStats(false).processPlaylist();
                BetStrategy betStrategy = new BetStrategy();
                betStrategy.showInter(false).withPlaylist(strategyMulti.getPlaylist());
                betStrategy.showInter(true).startBetAfter(7).stopBetAfter(9).withInitialBet(15.0).bet();
            } else {
                TreeMap<Double,String> results = new TreeMap<Double,String> ();
                // comput les differentes possibilites de paramètres
                for(int coteMin = 25; coteMin<30; coteMin++) {
                    //System.out.println("cote Min is "+coteMin);
                    for(int coteMax = coteMin; coteMax<80; coteMax++) {
                        //System.out.println("cote Max is "+coteMax);
                        strategyMulti.setBase(4).setNbMulti(7).setCoteMin(coteMin).setCoteMax(coteMax).setUseStats(false).processPlaylist();
                        BetStrategy betStrategy = new BetStrategy();
                        betStrategy.showInter(false).withPlaylist(strategyMulti.getPlaylist());
                        for (int i = 0; i < 30; i++) {
                            //System.out.println("start Bet after "+i);
                            for (int k = i; k < 40; k++) {
                                //System.out.println("stop Bet after "+k);
                                for (Double j = 1.0; j < 30.0; j++) {
                                    betStrategy.startBetAfter(i).stopBetAfter(k).withInitialBet(j).bet();
                                    if(betStrategy.nbWin > 5 && betStrategy.gains > 7000 && betStrategy.maxLoose < 500) {
                                        results.put(betStrategy.gains*betStrategy.nbWin/betStrategy.maxLoose, betStrategy.toString());
                                        System.out.println("new winning bet added: "+betStrategy.gains);
                                    }
                                }
                            }
                        }
                    }
                }
                for (Entry<Double, String> entry : results.entrySet()) {
                    System.out.println(entry.getValue());
                    
                }
            }
            
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

}
