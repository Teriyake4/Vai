package com.teriyake.vai.models.winPredFromComp;

import java.io.File;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerWinPred {

    private static Logger log = LoggerFactory.getLogger(PlayerWinPred.class);
    public static void main(String[] args) throws Exception {
        
        // Adam: lr = 0.001, hid = 15 | 5.95418e-01
        // Sgd: lr = 0.11, hid = 12 | 5.29028e-01
        // AdaGrad: lr = 0.05, hid = 12 | 5.89893e-01

        int batchSize = 469; // 225 seed: 123
        int seed = 100; // 123
        double learningRate = 0.001;
        int numInputs = 9;
        int numOutputs = 1;
        int numHidden = 15;
        int numEpochs = 2400; // 2400


        DataSetIterator iterator = new PlayerWinPredIterator(batchSize, 0);
        DataSet data = iterator.next();
        data.shuffle(seed);

        SplitTestAndTrain testAndTrain = data.splitTestAndTrain(0.7867); // 0.65
        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();
        
        // DataSetIterator mnistTest = new CompDatasetIterator(batchSize, false, seed);

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(learningRate)) // 0.11 for sgd 1 hidden
            .l2(0.0013) // 0.0013 for adam
            // .l1(0.0011)
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHidden)
                    .activation(Activation.IDENTITY).build())
            .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
                    .activation(Activation.RELU).build())

                    // .layer(new DenseLayer.Builder().nIn(numHidden).nOut(numHidden)
                    // .activation(Activation.RELU).build())

            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                    .activation(Activation.IDENTITY)
                    .nIn(numHidden).nOut(numOutputs).build())
            .build();

        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(200));
        
        log.info("Train model....");
        for(int i = 0; i < numEpochs; i++) {
            model.fit(trainingData);
        }

        log.info("Evaluate model....");
        RegressionEvaluation eval = new RegressionEvaluation();

        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        
        System.out.println("Prediction - Actual");
        for(int i = 0; i < output.rows(); i++) {
            // System.out.println(output.getDouble(i) + " - " + testData.getLabels().getDouble(i));
            // System.out.println(output.getDouble(i)); // Predicted
            // System.out.println(testData.getLabels().getDouble(i)); // Actual
        }
        System.out.println(output.rows());
        System.out.println(eval.stats());
        // System.out.println(trainingData);

        // log.info("Saving model...");
        // File location = new File(System.getProperty("user.home") + "/OneDrive/Documents/Vai/models/PlayerWinPred-Test/PlayerWinPred-Test.zip");
        // ModelSerializer.writeModel(model, location, true);
        // model.save(location);
    }


}