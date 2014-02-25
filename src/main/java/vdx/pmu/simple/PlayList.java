/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vdx.pmu.simple;

import java.util.TreeMap;

/**
 *
 * @author virgile
 */
public class PlayList {
    
    TreeMap<String, Play> playList;
    int coteMin;
    int coteMax;
    int nbMulti;
    int base;

    public PlayList(int coteMin, int coteMax, int nbMulti, int base) {
        playList = new TreeMap<String, Play>();
        this.coteMin = coteMin;
        this.coteMax = coteMax;
        this.nbMulti = nbMulti;
        this.base = base;
    }
    

    public PlayList(int coteMin, int coteMax) {
        playList = new TreeMap<String, Play>();
        this.coteMin = coteMin;
        this.coteMax = coteMax;
    }
    
}
