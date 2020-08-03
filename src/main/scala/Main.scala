import scala.concurrent.duration._
import monix.execution.Scheduler
import java.util.concurrent.ForkJoinPool
import scala.concurrent.ExecutionContext

object Main extends App {
  implicit val sc: Scheduler = Scheduler(ExecutionContext.fromExecutor(new ForkJoinPool))

  ModelEvaluator.train().runSyncUnsafe()
}
