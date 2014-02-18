package vdx.pmu.simple;

import java.util.ArrayList;

/**
 *
 * @author virgile
 */
class Stats {

    public String name;
    public Integer nbParticip;
    public Integer nbCourses;
    public Double avg;
    public ArrayList<Integer> results;

    public Stats(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Stats{" + "name=" + name + ", nbParticip=" + nbParticip + ", nbCourses=" + nbCourses + ", avg=" + avg + ", results=" + results + '}';
    }

}
