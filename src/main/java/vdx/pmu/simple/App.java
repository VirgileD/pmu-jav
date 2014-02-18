package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

import java.net.UnknownHostException;
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
        PronoStatistics pS = new PronoStatistics(ds);
        //Course thisCourse1;
        //field("date").equal((java.util.Date)sdf.parse("2013-09-11")).get();
        //System.out.println("course: " + thisCourse.toString());
        //thisCourse1 = ds.find(Course.class).field("date").greaterThan("2013-09-11").get();
        //System.out.println("course: " + thisCourse.toString());
        for (Course thisCourse : ds.find(Course.class)) {
            if (thisCourse.finish != null && thisCourse.finish.size() >= 4) {
                Map<Double, ChevToPlay> apply = pS.apply(thisCourse, pS.getStats(ds.find(Course.class).field("date").in(getDateOfRacesBefore(thisCourse.date, 30)).asList()));
                ArrayList<Integer> myFinish = new ArrayList<Integer>();
                //System.out.println("finish: " + thisCourse.finish.toString());
                //System.out.println("refCote: " + thisCourse.refCote.toString());
                int i = 0;
                for (Entry<Double, ChevToPlay> entry : apply.entrySet()) {
                    if (i < 4) {
                        //System.out.println("add firsts: " + i + "/4: " + en.getKey());
                        myFinish.add(entry.getValue().chev);
                    } else {
                        //System.out.println("///"+entry.getValue().toString());
                        Double cote = entry.getValue().cote;
                        //System.out.println("check " + en.getKey() + ": " + en.getValue());
                        if (cote > 24 && cote < 59) {
                            //System.out.println("add " + en.getKey() + ": " + en.getValue());
                            myFinish.add(entry.getValue().chev);
                        }
                        if (myFinish.size() >= 7) {
                            break;
                        }
                    }
                    i++;
                }
                thisCourse.finish.remove(null);
                if (myFinish.containsAll(thisCourse.finish.subList(0, 4))) {
                    System.out.println(thisCourse.date + " WIN: " + myFinish.toString() + "/" + thisCourse.finish.toString() + ": " + thisCourse.gains.get("m" + myFinish.size()));
                } else {
                    System.out.println(thisCourse.date + " LOO: " + myFinish.toString() + " / " + thisCourse.finish.subList(0, 4).toString());
                }
            } else {
                System.out.println("no finish (" + thisCourse.finish + ") for course: " + thisCourse.date);
            }
        }

        /*pS.getDateOfRaces("2013-09-12..2013-09-20,2014-03-01");
         Map<String, Stats> stats = pS.getStats(pS.getCourses(pS.getDateOfRaces("2013-09-12..2013-09-13")));
         System.out.println("stats: " + stats.toString());
         System.out.println(pS.apply(thisCourse1, stats).toString());*/
    }
}
