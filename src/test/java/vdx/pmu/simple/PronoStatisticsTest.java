/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vdx.pmu.simple;

import com.google.code.morphia.Datastore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.mongodb.Mongo;
import java.net.UnknownHostException;

import junit.framework.TestCase;

/**
 *
 * @author virgile
 */
public class PronoStatisticsTest extends TestCase {
    
    public PronoStatisticsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private List<Course> getFakeCourses() throws UnknownHostException {
        Mongo mongo = new Mongo("localhost", 27017);
        Morphia morphia = new Morphia();
        morphia.map(Course.class);
        Datastore ds = morphia.createDatastore(mongo, "pmutest");
        List<Course> courses = new ArrayList<Course>() ;
        for(Course thisCourse : ds.find(Course.class).order("date")) {
            courses.add(thisCourse);
        }
        return courses;
    }
    
        
    /**
     * Test of getStats method, of class PronoStatistics.
     * @throws java.net.UnknownHostException
     */
    public void testGetStats() throws UnknownHostException {
        
        PronoStatistics instance = new PronoStatistics();
        Map<String, Stats> expResult = null;
        Map<String, Stats> result = instance.getStats(getFakeCourses());
        System.out.println(result);
        assertEquals(result.get("quinte_net").avg, 1.0);
        assertEquals(result.get("paris turf").avg, 3.0);
        assertEquals((int) result.get("paris turf").nbParticip, (int) 3);
        assertEquals((int) result.get("paris turf").nbCourses, (int) 3);
        assertEquals((int) result.get("paris turf").results.get(0), (int) 3);
        assertEquals((int) result.get("paris turf").results.get(1), (int) 4);
        assertEquals((int) result.get("paris turf").results.get(2), (int) 2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of apply method, of class PronoStatistics.
     */
    public void testApply() {
        
    }
    
}
