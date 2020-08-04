import java.util.concurrent.TimeUnit

import org.datavec.api.io.filters.RandomPathFilter
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration
import org.deeplearning4j.earlystopping.saver.{InMemoryModelSaver, LocalFileModelSaver}
import org.deeplearning4j.earlystopping.scorecalc.ClassificationScoreCalculator
import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.evaluation.classification.Evaluation._

import scala.util.Try
import monix.eval.Task

object ModelEvaluator {

  def train(): Task[Unit] = Task {

    println("**************** starting ********************")

    // Load images
    println("Loading images....")

    val randomPathFilter = new RandomPathFilter(Settings.rng, Settings.allowedExtensions, 0L)
    val inputSplits = Settings.fileSplit.sample(randomPathFilter, Settings.trainSetPercentage, 100.0 - Settings.trainSetPercentage)

    val trainData = inputSplits(0)
    val testData = inputSplits(1)
    println(s"all files: ${Settings.fileSplit.length}, train set: ${trainData.length}, test set: ${testData.length}")

    // Prepare iterators for train and test
    println("Preparing train and test iterators....")
    val trainIterator = Settings.getInitializedIterator(trainData)
    val testIterator = Settings.getInitializedIterator(testData)

    // Construct the neural network
    println("Build trainer...")
    val outputFolder = "./output"
    val saver =
      if (Settings.saveModel) new LocalFileModelSaver(outputFolder)
      else new InMemoryModelSaver[MultiLayerNetwork]()

    val esConf = new EarlyStoppingConfiguration.Builder[MultiLayerNetwork]()
      .epochTerminationConditions(new MaxEpochsTerminationCondition(30))
      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(6, TimeUnit.MINUTES))
      .scoreCalculator(new ClassificationScoreCalculator(Metric.ACCURACY, testIterator))
      .evaluateEveryNEpochs(1)
      .modelSaver(saver)
      .build

    val trainer = new EarlyStoppingTrainer(esConf, Settings.getNetworkConfiguration, trainIterator)

    // Conduct early stopping training:
    println("Train network...")
    val result = trainer.fit

    // Print out the results:
    println("Done training!")
    println("Termination reason: " + result.getTerminationReason)
    println("Termination details: " + result.getTerminationDetails)
    println("Total epochs: " + result.getTotalEpochs)
    println("Best epoch number: " + result.getBestModelEpoch)
    println("Score at best epoch: " + result.getBestModelScore)

    // Get the best model:
    val model = result.getBestModel
    val eval: Evaluation = model.evaluate(testIterator)
    println(eval.stats())

    println("try to classify images:\n")

    trainIterator.reset()
    Settings.predictLabels(model, trainIterator)

    println("**************** done ********************")

  }
}
