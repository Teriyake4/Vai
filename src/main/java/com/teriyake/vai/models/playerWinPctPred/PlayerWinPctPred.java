package com.teriyake.vai.models.playerWinPctPred;

import java.io.File;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
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
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teriyake.vai.VaiUtil;

public class PlayerWinPctPred {

    private static Logger log = LoggerFactory.getLogger(PlayerWinPctPred.class);
    final static boolean save = false;
    final static boolean showInDepth = false;
    public static void main(String[] args) throws Exception {
        // Adam: lr = 0.001, hid = 15 | 5.95418e-01
        // Sgd: lr = 0.11, hid = 12 | 5.29028e-01
        // AdaGrad: lr = 0.05, hid = 12 | 5.89893e-01
        // int batchSize = 1738; // 225 seed: 123
        int seed = 123; // 123 // 333
        double learningRate = 0.01; // 0.001
        int numInputs = 9;
        int numOutputs = 1;
        int numHidden = 8;
        int numEpochs = 3200; // 2400


        File csvPath = new File(VaiUtil.getTestDataPath(), "CSVPlayerIndex.csv");
        DataSetIterator iterator = new PlayerWinPctPredIterator(csvPath, VaiUtil.readCSVFile(csvPath).size());
        DataSet data = iterator.next();
        data.shuffle(seed);

        SplitTestAndTrain testAndTrain = data.splitTestAndTrain(0.9); // 0.65
        DataSet trainData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();
        
        // DataSetIterator mnistTest = new CompDatasetIterator(batchSize, false, seed);

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .cudnnAlgoMode(ConvolutionLayer.AlgoMode.PREFER_FASTEST)
            .seed(seed)
            .activation(Activation.RELU) // IDENTITY
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(learningRate)) // 0.11 for sgd 1 hidden
            .l2(1e-3) // 0.0013 for adam
            // .l1(0.0011)
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
                    .build())
            .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
                    .build())
            // .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
            //         .build())
            // .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
            //         .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                    .activation(Activation.IDENTITY)
                    .nIn(numHidden).nOut(numOutputs).build())
            .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        // try {
        //     UIServer uiServer = UIServer.getInstance();
        //     StatsStorage statsStorage = new InMemoryStatsStorage();
        //     uiServer.attach(statsStorage);
        //     model.setListeners(new StatsListener(statsStorage, 1));
        // }
        // catch(Exception e) { 
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

        RegressionEvaluation eval = new RegressionEvaluation();
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        System.out.println(output.rows());
        
        if(showInDepth) {
            System.out.println("Prediction - Actual");
            for(int i = 0; i < output.rows(); i++) {
                System.out.println(output.getDouble(i) + " - " + testData.getLabels().getDouble(i));
                System.out.println(output.getDouble(i)); // Predicted
                System.out.println(testData.getLabels().getDouble(i)); // Actual
            }
        }
        
        System.out.println("Seed: " + seed);
        System.out.println(eval.stats());
        // System.out.println(trainingData);

        if(save) {
            log.info("Saving model...");
            File location = new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/PlayerWinPred-Test/PlayerWinPred-Test.zip");
            ModelSerializer.writeModel(model, location, true);
            model.save(location);
        }
    
    }


}