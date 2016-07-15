package jp.naist.cl.srparser.parser;

import jp.naist.cl.srparser.io.Logger;
import jp.naist.cl.srparser.model.Feature;
import jp.naist.cl.srparser.model.Sentence;
import jp.naist.cl.srparser.model.Token;
import jp.naist.cl.srparser.transition.Action;
import jp.naist.cl.srparser.transition.Arc;
import jp.naist.cl.srparser.transition.Oracle;
import jp.naist.cl.srparser.transition.State;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * jp.naist.cl.srparser.parser
 *
 * @author Hiroki Teranishi
 */
public class Trainer extends Parser {
    private static final int REPORT_PERIOD = 100;

    private Sentence[] sentences;
    private Oracle oracle;
    private HashMap<Sentence.ID, Arc[]> goldArcMap = new HashMap<>();

    public Trainer(Sentence[] sentences, Oracle oracle) {
        super(new int[Action.SIZE][Feature.SIZE], new Perceptron());
        loadGolds(sentences, oracle);
        setWeights(new int[Action.SIZE][Feature.SIZE]);
    }

    private void loadGolds(Sentence[] sentences, Oracle oracle) {
        int length = sentences.length;
        int i = 0;
        for (Sentence sentence : sentences) {
            if (++i % REPORT_PERIOD == 0) {
                Logger.info("Extracting gold data: %d / %d", i, length);
            }
            goldArcMap.put(sentence.id, extractGoldArcs(sentence));
        }
        this.sentences = sentences;
        this.oracle = oracle;
    }

    public void train() {
        train(null);
    }

    public void train(TrainCallback callback) {
        Map<Sentence.ID, Arc[]> predArcMap = new LinkedHashMap<>();
        int i = 0;
        for (Sentence sentence : sentences) {
            if (++i % REPORT_PERIOD == 0) {
                Logger.info("training: %d of %d ...", i, sentences.length);
            }
            predArcMap.put(sentence.id, trainEach(sentence).arcs);
        }
        if (callback != null) {
            callback.accept(goldArcMap, predArcMap);
        }
    }

    private State trainEach(Sentence sentence) {
        State state = parse(sentence);
        Arc[] goldArcs = goldArcMap.get(sentence.id);
        Arc[] predictArcs = state.arcs;
        for (int i = 0; i < predictArcs.length; i++) {
            if (predictArcs[i] != goldArcs[i]) {
                setWeights(Perceptron.update(weights, oracle.getState(sentence), state));
                break;
            }
        }
        return state;
    }

    public void test(TrainCallback callback) {
        Map<Sentence.ID, Arc[]> predArcMap = new LinkedHashMap<>();
        for (Sentence sentence : sentences) {
            predArcMap.put(sentence.id, parse(sentence).arcs);
        }
        if (callback != null) {
            callback.accept(goldArcMap, predArcMap);
        }
    }

    private Arc[] extractGoldArcs(Sentence sentence) {
        Arc[] goldArcs = new Arc[sentence.tokens.length];
        int i = 0;
        for (Token token : sentence.tokens) {
            if (!token.isRoot()) {
                goldArcs[++i] = new Arc(token.head, token.id);
            }
        }
        return goldArcs;
    }

    public interface TrainCallback extends BiConsumer<Map<Sentence.ID, Arc[]>, Map<Sentence.ID, Arc[]>> {}
}
