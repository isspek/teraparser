package jp.naist.cl.srparser.app;

import jp.naist.cl.srparser.model.Sentence;
import jp.naist.cl.srparser.transition.Arc;

import java.util.Map;

/**
 * jp.naist.cl.srparser.app
 *
 * @author Hiroki Teranishi
 */
public class Evaluator {

    public static double calcUAS(Map<Sentence.ID, Arc[]> goldArcMap, Map<Sentence.ID, Arc[]> predictArcMap) {
        double total = 0;
        double collect = 0;
        for (Map.Entry<Sentence.ID, Arc[]> entry : goldArcMap.entrySet()) {
            Arc[] goldArcs = entry.getValue();
            Arc[] predictArcs = predictArcMap.get(entry.getKey());
            // i = 0 is null since <ROOT> has no arc.
            for (int i = 1; i < goldArcs.length; i++) {
                if (goldArcs[i].equals(predictArcs[i])) {
                    collect++;
                }
                total++;
            }
        }
        return collect / total;
    }
}
