package com.teriyake.vai.models.matchWinPred;

import java.io.File;

import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.exception.DL4JException;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teriyake.vai.VaiUtil;
import com.teriyake.vai.data.GameValues;

public class MatchWinPred {
    private static Logger log = LoggerFactory.getLogger(MatchWinPred.class);
    private static boolean showInDepth = false;
    private static boolean save = false;
    private static boolean train = true;
    public static void main(String[] args) throws Exception {
        int featPerPlay = 14 + GameValues.AGENT_LIST.length;
        int batchSize = 56;
        int seed = 1;
        double learningRate = 0.006; // 0.0014
        int numInputs = 10 * featPerPlay;
        int numOutputs = 2;
        int numHidden = 6; // 10
        int numEpochs = 1285; // 100
        double threshold = 0.50;

        File trainingData = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/MatchWinPredTrain.csv");
        DataSetIterator trainingIterator = new MatchWinPredIterator(trainingData, batchSize, featPerPlay, false);
        DataSet trainData = trainingIterator.next();
        trainData.shuffle(seed);

        File testingData = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/MatchWinPredTest.csv");
        DataSetIterator testIterator = new MatchWinPredIterator(testingData, batchSize, featPerPlay, false);
        DataSet testData = testIterator.next();
        testData.shuffle(seed);

        // SplitTestAndTrain testAndTrain = trainData.splitTestAndTrain(0.78); // 0.78
        // trainData = testAndTrain.getTrain();
        // DataSet testData = testAndTrain.getTest();

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .activation(Activation.RELU) // SOFTMAX/SIGMOID, lower epochs for softmax?
            .weightInit(WeightInit.XAVIER)
            .updater(new AdaGrad(learningRate)) // Adam, adagrad
            .l2(0.0013)
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden) // 6 hidden
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.XENT) // XENT or MSE?
                .activation(Activation.SIGMOID)
                .nIn(numHidden).nOut(numOutputs)
                .build())
            .build();
        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        // model = MultiLayerNetwork.load(new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/MatchWinPred/MatchWinPred.zip"), false);
        model.init();

        try {
            StatsStorage statsStorage = new InMemoryStatsStorage();
            UIServer uiServer = UIServer.getInstance();
            uiServer.attach(statsStorage);
            model.setListeners(new StatsListener(statsStorage, 1));
        }
        catch(DL4JException e) {
        }

        double[] epochs = new double[numEpochs];
        double[] trainLoss = new double[numEpochs];
        double[] testLoss = new double[numEpochs];
        double[] trainAccur = new double[numEpochs];
        double[] testAccur = new double[numEpochs];

        if(train) {
            log.info("Train model....");
            for(int i = 0; i < numEpochs; i++) {
                model.fit(trainData);
                Evaluation trainEval = new Evaluation(threshold);
                trainEval.eval(trainData.getLabels(), model.output(trainData.getFeatures()));
                Evaluation testEval = new Evaluation(threshold);
                testEval.eval(testData.getLabels(), model.output(testData.getFeatures()));
                epochs[i] = i;
                trainLoss[i] = model.score(trainData);
                testLoss[i] = model.score(testData);
                trainAccur[i] = trainEval.accuracy();
                testAccur[i] = testEval.accuracy();
                if(i % 100 == 0) {
                    log.info("Iteration: " + i + " =Score= Train: " + trainLoss[i] + " - Test: " + testLoss[i] +
                        " =Accuracy= Train: " + trainAccur[i] + " - Test: " + testAccur[i]);
                    // System.out.println("Score - Train: " + model.score(trainData) + " - Test: " + model.score(testData));
                }
            }
        }

        // Map<String, INDArray> paramTable = model.paramTable();
        // INDArray weights = paramTable.get("0_W");
        // for(int feat = 0; feat < featPerPlay; feat++) {
        //     double avgWeight = 0;
        //     for(int numWeights = 0; numWeights < numHidden; numWeights++) {
        //         avgWeight +=  weights.getDouble(feat + numWeights);
        //     }
        //     avgWeight /= numHidden;
        //     System.out.println(feat + ": " + (avgWeight * 1000));
        // }
        

        XYChart lossChart = new XYChartBuilder()
            .title("Loss Score")
            .xAxisTitle("Iterations")
            .yAxisTitle("Loss")
            .width(800)
            .height(500)
            .theme(ChartTheme.GGPlot2)
            .build();
        lossChart.getStyler().setYAxisMin(0.0);
        lossChart.getStyler().setToolTipsEnabled(true);
        lossChart.getStyler().setLegendPosition(LegendPosition.InsideSW);
        lossChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        lossChart.addSeries("Training", epochs, trainLoss).setMarker(SeriesMarkers.NONE);
        lossChart.addSeries("Testing", epochs, testLoss).setMarker(SeriesMarkers.NONE);
        new SwingWrapper<XYChart>(lossChart).displayChart();

        XYChart accurChart = new XYChartBuilder()
            .title("Accuracy")
            .xAxisTitle("Iterations")
            .yAxisTitle("Accuracy")
            .width(800)
            .height(500)
            .theme(ChartTheme.GGPlot2)
            .build();
        accurChart.getStyler().setYAxisMin(0.0);
        accurChart.getStyler().setYAxisMax(1.0);
        accurChart.getStyler().setToolTipsEnabled(true);
        accurChart.getStyler().setLegendPosition(LegendPosition.InsideSW);
        accurChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        accurChart.addSeries("Training", epochs, trainAccur).setMarker(SeriesMarkers.NONE);
        accurChart.addSeries("Testing", epochs, testAccur).setMarker(SeriesMarkers.NONE);
        new SwingWrapper<XYChart>(accurChart).displayChart();

        // evaluate the model on the test set
        Evaluation eval = new Evaluation(threshold);
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
                    System.out.print("Def - ");
                }
                else if(attWin > defWin) {
                    defWin = 0;
                    attWin = 1;
                    System.out.print("Att - ");
                }
                if(defWin == testData.getLabels().getDouble(i, 0) && defWin == 1) {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1) + " MATCH");
                }
                else if(attWin == testData.getLabels().getDouble(i, 1) && attWin == 1) {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1) + " MATCH");
                }
                else {
                    System.out.println(output.getDouble(i, 0) + " - " + output.getDouble(i, 1));
                }
            }
        }
        if(save) {
            log.info("Saving model...");
            File location = new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/MatchWinPred/MatchWinPred.zip");
            ModelSerializer.writeModel(model, location, true);
            model.save(location);
        }
        // UIServer.stopInstance();
    }
}
