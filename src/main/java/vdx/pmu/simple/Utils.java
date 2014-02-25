package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.joda.time.DateTime;

/**
 *
 * @author virgile
 */
public class Utils {

    /**
     *
     * @param <K> the keys of the map
     * @param <V> the values that the map will be sorted on
     * @param map the map to sort
     * @return a sorted set
     */
    static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    /**
     * Sort a map by its values
     *
     * @param map the map to sort
     * @return the sorted map
     */
    static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * return a list of date as string YYYY-MM-DD, from date-offset days to date
     * -1 day, comma-separated
     *
     * @param date the date, exclusive to finish the list with
     * @param offset the offset in days to start the list with
     * @return a list of date as string YYYY-MM-DD, from date-offset days to
     * date -1 day, comma-separated
     */
    
    static public ArrayList<String> getDateOfRacesBefore(String date, int offset) {
        return getDateOfRacesBefore(date, offset, 1);
    }
    
    static public ArrayList<String> getDateOfRacesBefore(String date, int offset, int startOffset) {
        DateTime stop = new DateTime(date);
        stop = stop.minusDays(startOffset);
        DateTime start = stop.minusDays(offset);

        return getDateOfRaces(start.toLocalDateTime().toString() + ".." + stop.toLocalDateTime().toString());
    }

    /**
     * permet de générer une suite de date YYYY-MM-DD à partir d'une suite de
     * date avec potentiellement des ranges (deux dates séparées par .. et une
     * suite de date séparées par des ,
     *
     * @param datesArgs la chaine de dates à analyser pour expander les ranges
     * @return une liste de date séparées par des ,
     */
    static public ArrayList<String> getDateOfRaces(String datesArgs) {
        TreeSet<String> listDate = new TreeSet<String>();
        String[] dateItems = datesArgs.split(",");
        for (String dateItem : dateItems) {
            String[] dateRange = dateItem.split("\\.\\.");
            if (dateRange.length == 1) {
                listDate.add(dateRange[0]);
            } else {
                DateTime start = new DateTime(dateRange[0]);
                DateTime stop = new DateTime(dateRange[1]);
                DateTime inter = start;
                while (inter.compareTo(stop) <= 0) {
                    inter = inter.plusDays(1);
                    listDate.add(inter.toLocalDate().toString());
                }
            }
        }
        //System.out.println("select races: " + listDate.toString());
        //System.out.println("select races: " + new ArrayList(listDate));
        return new ArrayList(listDate);
    }

    /**
     * Retrieves the Courses corresponding to a list of dates
     *
     * @param ds the morphia datastore
     * @param listDates the list of dates
     * @return a list of courses
     */
    public List<Course> getCourses(Datastore ds, ArrayList<String> listDates) {
        return ds.find(Course.class).field("date").in(listDates).asList();
    }

}
