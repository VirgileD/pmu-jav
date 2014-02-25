package vdx.pmu.simple;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bson.types.ObjectId;

/**
 *
 * @author virgile
 */
@Entity("courses")
public class Course {

    @Id
    private ObjectId id;
    String date;
    List<Integer> finish;
    Map<String, Double> gains;
    TreeMap<Integer, Double> refCote;
    String name;
    Integer nbPartants;
    Map<String, List<Integer>> pronos;
    String location;

    @Override
    public String toString() {
        return "Course " + name + ": le " + date + " à " + location;
    }

    String dumpStats() {
        String stats = "";
        for (int i = 0; i < 4; i++) {
            Integer chev = finish.get(i);
            Double cote = refCote.get(chev);
            stats += chev +"("+cote+") ";
        }
        return "Course du " + date + ": "+stats;
    }

}
