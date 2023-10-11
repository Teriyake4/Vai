package com.teriyake.vai.models.matchWinPred;

import java.io.File;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teriyake.vai.VaiUtil;

public class MatchWinPredByRounds {
    private static Logger log = LoggerFactory.getLogger(MatchWinPredByRounds.class);
    private static boolean showInDepth = true;
    private static boolean save = false;
    private static boolean train = true;
    public static void main(String[] args) throws Exception {
        int featPerPlay = 14 + 22;
        int batchSize = 56;
        int seed = 123;
        double learningRate = 0.01; // 0.001 @ 49
        int numInputs = 10 * featPerPlay;
        int numOutputs = 2;
        int numHidden = 13; // 10
        int numEpochs = 1000; // 100

        File trainingData = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/MatchWinPredTest.csv");
        DataSetIterator trainingIterator = new MatchWinPredIterator(trainingData, VaiUtil.readCSVFile(trainingData).size(), featPerPlay, true);
        DataSet trainData = trainingIterator.next();
        trainData.shuffle(seed);

        // File testingData = new File(System.getProperty("user.dir") + "/src/main/java/com/teriyake/vai/data/MatchWinPredOther.csv");
        // DataSetIterator testIterator = new MatchWinPredIterator(testingData, VaiUtil.readCSVFile(testingData).size(), featPerPlay, true);
        // DataSet testData = testIterator.next();
        // testData.shuffle(seed);

        SplitTestAndTrain testAndTrain = trainData.splitTestAndTrain(0.78);
        trainData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .activation(Activation.RELU) // SOFTMAX/SIGMOID, lower epochs for softmax?
            .weightInit(WeightInit.XAVIER)
            .updater(new Sgd(learningRate)) // Adam, adagrad
            .l2(0.0013)
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
                .activation(Activation.RELU)
                .build())
            // .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden) // 6 hidden
            //     .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE) // XENT or MSE?
                .activation(Activation.RELU)
                .nIn(numHidden).nOut(numOutputs)
                .build())
            .build();
        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        // model = MultiLayerNetwork.load(new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/MatchWinPred/MatchWinPred.zip"), false);
        model.init();

        // StatsStorage statsStorage = new InMemoryStatsStorage();
        // try {
        //     UIServer uiServer = UIServer.getInstance();
        //     uiServer.attach(statsStorage);
        //     model.setListeners(new StatsListener(statsStorage, 1));
        // }
        // catch(DL4JException e) {
        // }

        double[] epochs = new double[numEpochs];
        double[] trainLoss = new double[numEpochs];
        double[] testLoss = new double[numEpochs];
        double[] trainRSquare = new double[numEpochs];
        double[] testRSquare = new double[numEpochs];

        log.info("Train model....");
        for(int i = 0; i < numEpochs; i++) {
            model.fit(trainData);
            RegressionEvaluation trainEval = new RegressionEvaluation();
            trainEval.eval(trainData.getLabels(), model.output(trainData.getFeatures()));
            RegressionEvaluation testEval = new RegressionEvaluation();
            testEval.eval(testData.getLabels(), model.output(testData.getFeatures()));
            epochs[i] = i;
            trainLoss[i] = model.score(trainData);
            testLoss[i] = model.score(testData);
            trainRSquare[i] = trainEval.averageRSquared();
            testRSquare[i] = testEval.averageRSquared();
            if(i % 100 == 0) {
                log.info("Iteration: " + i + " =Score= Train: " + trainLoss[i] + " - Test: " + testLoss[i] +
                    " =rSquare= Train: " + trainRSquare[i] + " - Test: " + testRSquare[i]);
                // System.out.println("Score - Train: " + model.score(trainData) + " - Test: " + model.score(testData));
            }
        }

        log.info("Evaluate model....");

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
            .title("r-Squared")
            .xAxisTitle("Iterations")
            .yAxisTitle("r-Squared")
            .width(800)
            .height(500)
            .theme(ChartTheme.GGPlot2)
            .build();
        accurChart.getStyler().setYAxisMin(-1.0);
        accurChart.getStyler().setYAxisMax(1.0);
        accurChart.getStyler().setToolTipsEnabled(true);
        accurChart.getStyler().setLegendPosition(LegendPosition.InsideSW);
        accurChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        accurChart.addSeries("Training", epochs, trainRSquare).setMarker(SeriesMarkers.NONE);
        accurChart.addSeries("Testing", epochs, testRSquare).setMarker(SeriesMarkers.NONE);
        new SwingWrapper<XYChart>(accurChart).displayChart();

        // evaluate the model on the test set
        RegressionEvaluation eval = new RegressionEvaluation();
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        System.out.println(eval.stats());
        
        if(showInDepth) {
            System.out.println("Prediction - Actual");
            for(int i = 0; i < output.rows(); i++) {
                System.out.println(output.getDouble(i, 0) + ":" + output.getDouble(i, 1) + " - " + testData.getLabels().getDouble(i, 0) + ":" + testData.getLabels().getDouble(i, 1));
                // System.out.println(output.getDouble(i)); // Predicted
                // System.out.println(testData.getLabels().getDouble(i)); // Actual
            }
        }
        if(save) {
            log.info("Saving model...");
            File location = new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/MatchWinPred/MatchWinPredByRounds.zip");
            ModelSerializer.writeModel(model, location, true);
            model.save(location);
        }
        // UIServer.stopInstance();
    }
}
