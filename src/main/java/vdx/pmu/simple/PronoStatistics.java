package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author virgile
 */
public class PronoStatistics {

    public PronoStatistics() {
    }

    public Map<String, Stats> getStats(List<Course> courses) {
        HashMap<String, Stats> stats = new HashMap<String, Stats>();
        for (Course course : courses) {
            for (Entry<String, List<Integer>> en : course.pronos.entrySet()) {
                String name = en.getKey();
                List<Integer> chevs = en.getValue();
                /*if (name.equals("rmc")) {
                 System.out.println("rmc: " + chevs.toString());
                 System.out.println("fin: " + course.finish);
                
                 }*/
                if (stats.containsKey(name)) {
                    Stats tmpStat = stats.get(name);
                    tmpStat.nbParticip++;
                    tmpStat.results.add(nbIn(course.finish, 4, chevs, 8));
                } else {
                    Stats tmpStat = new Stats(name);
                    tmpStat.nbParticip = 1;
                    tmpStat.results = new ArrayList<Integer>();
                    tmpStat.results.add(nbIn(course.finish, 4, chevs, 8));
                    stats.put(name, tmpStat);
                }
            }
        }
        for (Entry<String, Stats> entry : stats.entrySet()) {
            Stats stats1 = entry.getValue();
            stats1.nbCourses = courses.size();
            Integer tmpInt = 0;
            for (Iterator<Integer> it = stats1.results.iterator(); it.hasNext();) {
                tmpInt += it.next();
            }
            stats1.avg = tmpInt.doubleValue() / stats1.nbParticip;
        }
        for (Iterator<Map.Entry<String, Stats>> it = stats.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Stats> entry = it.next();
            if (entry.getValue().nbParticip < entry.getValue().nbCourses / 3) {
                it.remove();
            }
        }
        //System.out.println(""+stats);
        return stats;
    }

    private Integer nbIn(List<Integer> finish, int finishSublistNb, List<Integer> prono, int pronoSublistNb) {
        Integer nbIn = 0;
        if (finish != null) {
            for (Integer chevOfFinish : finish.subList(0, finishSublistNb < finish.size() ? finishSublistNb : finish.size())) {
                if (prono.subList(0, pronoSublistNb < prono.size() ? pronoSublistNb : prono.size()).contains(chevOfFinish)) {
                    nbIn++;
                }
            }
            return nbIn;
        } else {
            return 0;
        }
    }

    public Map<Double, ChevToPlay> apply(Course course, Map<String, Stats> stats) {
        TreeMap<Integer, ChevToPlay> chevToPlay = new TreeMap<Integer, ChevToPlay>();
        for (Entry<String, List<Integer>> entry : course.pronos.entrySet()) {
            String pronoName = entry.getKey();
            Double avgStatPronoName = 1.0;
            if (stats != null) {
                if (stats.get(pronoName) != null) {
                    avgStatPronoName = stats.get(pronoName).avg;
                }
            }

            List<Integer> prono = entry.getValue();
            for (int i = 0; i < prono.size(); i++) {
                if (course.refCote.containsKey(prono.get(i))) {
                    if (chevToPlay.containsKey(prono.get(i))) {
                        ChevToPlay tmpChevToPlay = chevToPlay.get(prono.get(i));
                        tmpChevToPlay.score += (8 - i) * avgStatPronoName;
                        tmpChevToPlay.cote = course.refCote.get(prono.get(i));
                        chevToPlay.put(prono.get(i), tmpChevToPlay);
                    } else {
                        //System.out.println(course.date+" cev "+i+" of "+pronoName+": "+prono.get(i)));
                        ChevToPlay tmpChevToPlay = new ChevToPlay(prono.get(i));
                        tmpChevToPlay.score = (8 - i) * avgStatPronoName;
                        tmpChevToPlay.cote = course.refCote.get(prono.get(i));
                        chevToPlay.put(prono.get(i), tmpChevToPlay);
                    }
                }
            }
        }
        TreeMap<Double, ChevToPlay> orderedChevToPlay = new TreeMap<Double, ChevToPlay>();
        for (Entry<Integer, ChevToPlay> entry : chevToPlay.entrySet()) {
            ChevToPlay tmpChevToPlay = entry.getValue();
            orderedChevToPlay.put(tmpChevToPlay.score, tmpChevToPlay);
        }
        //System.out.println("chev To Play: " + orderedChevToPlay.descendingMap().toString());
        return orderedChevToPlay.descendingMap();
    }
}
