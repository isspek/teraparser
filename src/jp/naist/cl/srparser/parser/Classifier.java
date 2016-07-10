package jp.naist.cl.srparser.parser;

import jp.naist.cl.srparser.model.Feature;

import java.util.List;
import java.util.Map;

/**
 * jp.naist.cl.srparser.parser
 *
 * @author Hiroki Teranishi
 */
public interface Classifier {
    public Parser.Action classify(Feature.Index[] featureIndexes, Map<Parser.Action, Map<Feature.Index, Double>> weights, List<Parser.Action> options);
}
