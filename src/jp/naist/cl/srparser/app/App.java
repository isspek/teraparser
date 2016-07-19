package jp.naist.cl.srparser.app;

import jp.naist.cl.srparser.io.ConllReader;
import jp.naist.cl.srparser.io.Logger;
import jp.naist.cl.srparser.model.Feature;
import jp.naist.cl.srparser.model.Sentence;
import jp.naist.cl.srparser.transition.Action;
import jp.naist.cl.srparser.transition.Oracle;
import jp.naist.cl.srparser.parser.Parser;
import jp.naist.cl.srparser.parser.Perceptron;
import jp.naist.cl.srparser.parser.Trainer;

/**
 * jp.naist.cl.srparser.app
 *
 * @author Hiroki Teranishi
 */
public final class App {
    private static App instance = null;

    private App() {
        initialize();
    }

    public static void execute(String[] args) {
        App app = getInstance();
        try {
            // app.run();
            app.train();
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            app.finalize();
        }
    }

    private static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    private void initialize() {
        // Config.initialize(new Config.CmdLineArgs(args));
        Config.initialize(new Config.DebugArgs());
    }

    private void run() {
        try {
            Sentence[] sentences = (new ConllReader(Config.getString(Config.Key.TRAINING_FILE))).read();
            float[][] weights = new float[Action.SIZE][Feature.SIZE];
            Parser parser = new Parser(new Perceptron(weights));
            for (Sentence sentence : sentences) {
                Logger.trace(sentence.toString());
                parser.parse(sentence);
                // Logger.trace(new DepTree(sentence));
                // Logger.trace(new DepTree(parsed));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void train() {
        try {
            Logger.info("Reading training samples from %s ...", Config.getString(Config.Key.TRAINING_FILE));
            Sentence[] sentences = new ConllReader(Config.getString(Config.Key.TRAINING_FILE)).read();
            Logger.info("training sample size %d", sentences.length);
            Trainer trainer = new Trainer(sentences, new Oracle(Oracle.Algorithm.STATIC));
            Logger.info("Reading development samples from %s ...", Config.getString(Config.Key.DEVELOPMENT_FILE));
            Sentence[] devSentences = new ConllReader(Config.getString(Config.Key.DEVELOPMENT_FILE)).read();
            Logger.info("development sample size %d", sentences.length);
            Trainer tester = new Trainer(devSentences, new Oracle(Oracle.Algorithm.STATIC));
            int iteration = Config.getInt(Config.Key.ITERATION);
            for (int i = 1; i <= iteration; i++) {
                Logger.info("Iteration: %d", i);
                trainer.train((gold, pred) -> {
                    // Logger.info(gold);
                    // Logger.info(pred);
                    double uas = Evaluator.calcUAS(gold, pred);
                    Logger.info("UAS: %1.6f", uas);
                });
                if (i % 10 == 0) {
                    Logger.info("===== test =====");
                    tester.setWeights(trainer.getWeights());
                    tester.test((gold, pred) -> {
                        // Logger.info(gold);
                        // Logger.info(pred);
                        double uas = Evaluator.calcUAS(gold, pred);
                        Logger.info("UAS: %1.6f", uas);
                    });
                }
                Logger.info("================");
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    protected void finalize() {
        try {
            Logger.terminate();
            instance = null;
        } finally {
            try {
                super.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
