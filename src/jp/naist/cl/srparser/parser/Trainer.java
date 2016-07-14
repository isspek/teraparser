package jp.naist.cl.srparser.parser;

import jp.naist.cl.srparser.io.Logger;
import jp.naist.cl.srparser.model.Feature;
import jp.naist.cl.srparser.model.Sentence;
import jp.naist.cl.srparser.model.Token;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * jp.naist.cl.srparser.parser
 *
 * @author Hiroki Teranishi
 */
public class Trainer extends Parser {
    private Sentence[] sentences;
    private Map<Sentence.ID, Set<Arc>> goldArcSets = new LinkedHashMap<>();

    public Trainer(Sentence[] sentences) {
        super(new int[Action.SIZE][Feature.SIZE], new Perceptron());
        this.sentences = sentences;
        for (Sentence sentence : sentences) {
            goldArcSets.put(sentence.id, parseGold(sentence));
        }
        setWeights(new int[Action.SIZE][Feature.SIZE]);
    }

    public void train() {
        train(null);
    }

    public void train(TrainCallback callback) {
        Map<Sentence.ID, Set<Arc>> predArcSets = new LinkedHashMap<>();
        int length = sentences.length;
        int i = 0;
        for (Sentence sentence : sentences) {
            Logger.info("Training: %d / %d", ++i, length);
            predArcSets.put(sentence.id, parse(sentence));
            setWeights(Perceptron.update(weights, state));
        }
        if (callback != null) {
            callback.accept(goldArcSets, predArcSets);
        }
    }

    public void test(TrainCallback callback) {
        Map<Sentence.ID, Set<Arc>> predArcSets = new LinkedHashMap<>();
        for (Sentence sentence : sentences) {
            predArcSets.put(sentence.id, parse(sentence));
        }
        if (callback != null) {
            callback.accept(goldArcSets, predArcSets);
        }
    }

    private Set<Arc> parseGold(Sentence sentence) {
        Set<Arc> goldArcSet = new LinkedHashSet<>();
        for (Token token : sentence.tokens) {
            if (!token.isRoot()) {
                goldArcSet.add(new Arc(token.head, token.id));
            }
        }
        return goldArcSet;
    }

    public interface TrainCallback extends BiConsumer<Map<Sentence.ID, Set<Arc>>, Map<Sentence.ID, Set<Arc>>> {}
}
