/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vdx.pmu.simple;

/**
 *
 * @author virgile
 */
public class ChevToPlay {

    Integer chev;
    Double score;
    Double cote;

    public ChevToPlay() {
    }

    public ChevToPlay(Integer chev, Double score, Double cote) {
        this.chev = chev;
        this.score = score;
        this.cote = cote;
    }

    public ChevToPlay(Integer chev) {
        this.chev = chev;
    }

    @Override
    public String toString() {
        return "ChevToPlay{" + "chev=" + chev + ", score=" + score + ", cote=" + cote + '}';
    }

}
