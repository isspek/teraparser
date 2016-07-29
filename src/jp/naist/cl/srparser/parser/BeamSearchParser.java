package jp.naist.cl.srparser.parser;

import jp.naist.cl.srparser.model.Sentence;
import jp.naist.cl.srparser.transition.State;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * jp.naist.cl.srparser.parser
 *
 * @author Hiroki Teranishi
 */
public class BeamSearchParser extends GreedyParser implements BeamSearchDecoder {
    private CompletionService<List<BeamItem>> completionService;
    private final boolean useMultiThread;
    private final int beamWidth;

    public BeamSearchParser(Perceptron classifier, ExecutorService executor, int beamWidth) {
        super(classifier);
        useMultiThread = executor instanceof ThreadPoolExecutor && ((ThreadPoolExecutor) executor).getMaximumPoolSize() > 1;
        if (useMultiThread) {
            completionService = new ExecutorCompletionService<>(executor);
        }
        this.beamWidth = beamWidth;
    }

    @Override
    public State parse(Sentence sentence) {
        if (beamWidth == 1) {
            return super.parse(sentence); // same as greedy search
        }
        BeamItem[] beam = {new BeamItem(new State(sentence), 0.0)};

        boolean terminate = false;
        while (!terminate) {
            beam = getNextBeamItems(beam, beamWidth, classifier, completionService);
            terminate = Arrays.stream(beam).allMatch(item -> item.getState().isTerminal());
        }
        return beam[0].getState();
    }

    @Override
    public BeamItem[] getNextBeamItems(BeamItem[] beam, int beamWidth, Perceptron classifier, CompletionService<List<BeamItem>> completionService) {
        // try {
        //     if (useMultiThread) {
        //         return BeamSearchDecoder.super.getNextBeamItems(beam, beamWidth, classifier, completionService);
        //     } else {
        //         return BeamSearchDecoder.super.getNextBeamItems(beam, beamWidth, classifier);
        //     }
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }
        return BeamSearchDecoder.super.getNextBeamItems(beam, beamWidth, classifier);
    }
}
