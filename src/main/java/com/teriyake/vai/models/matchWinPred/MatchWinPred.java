package com.teriyake.vai.models.matchWinPred;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.exception.DL4JException;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchWinPred {
    private static Logger log = LoggerFactory.getLogger(MatchWinPred.class);
    private static boolean showInDepth = false;
    public static void main(String[] args) throws Exception {
        int batchSize = 56;
        int seed = 111;
        double learningRate = 0.006; // 0.004 explodes with extra hidden layer
        int numInputs = 70;
        int numOutputs = 2;
        int numHidden = 75; // 30
        int numEpochs = 95; // 200 explodes

        File balanced = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVBalanced.csv");
        DataSetIterator trainingIterator = new MatchWinPredIterator(balanced, 106);
        DataSet trainingData = trainingIterator.next();
        trainingData.shuffle(seed);

        File match = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/CSVMatchIndex.csv");
        DataSetIterator testIterator = new MatchWinPredIterator(match, 33);
        DataSet testData = testIterator.next();
        // testData.shuffle(seed);

        SplitTestAndTrain testAndTrain = trainingData.splitTestAndTrain(0.8);
        trainingData = testAndTrain.getTrain();
        testData = testAndTrain.getTest();

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .activation(Activation.RELU) // SOFTMAX/SIGMOID, lower epochs for softmax?
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(learningRate)) // Sgd
            .l2(1e-3)
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
                .build())
            .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
                .build())
            // .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
            //     .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.XENT) // XENT
                .activation(Activation.SIGMOID)
                .nIn(numHidden).nOut(numOutputs).build())
            .build();
        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        try {
            UIServer uiServer = UIServer.getInstance();
            StatsStorage statsStorage = new InMemoryStatsStorage();
            model.setListeners(new StatsListener(statsStorage, 1));
            uiServer.attach(statsStorage);
        }
        catch(DL4JException e) {
        }

        // model.setListeners(new ScoreIterationListener(1));

        log.info("Train model....");
        for(int i = 0; i < numEpochs; i++) {
            model.fit(trainingData);
        }

        // evaluate the model on the test set
        Evaluation eval = new Evaluation(2);
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        log.info(eval.stats());
        System.out.println("Row 0 = Def Win\nRow 1 = Att Win");

        if(showInDepth) {
            for(int i = 0; i < output.rows(); i++) {
                double defWin = output.getDouble(i, 0);
                double attWin = output.getDouble(i, 1);
                if(defWin > attWin) {
                    defWin = 1;
                    attWin = 0;
                }
                else if(attWin > defWin) {
                    defWin = 0;
                    attWin = 1;
                }
                if(defWin == testData.getLabels().getDouble(i, 0) && defWin == 1) {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1) + " Def MATCH");
                }
                else if(attWin == testData.getLabels().getDouble(i, 1) && attWin == 1) {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1) + " Att MATCH");
                }
                else {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1));
                }
            }
        }
        // UIServer.stopInstance();
    }
}
