//import java.util.concurrent.Executors
//
//import com.amazonaws.services.kinesis.model.{GetRecordsRequest, GetShardIteratorRequest, Record}
//import Config._
//import com.amazonaws.auth.AWSCredentialsProvider
//import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
//import com.amazonaws.services.kinesis.metrics.impl.NullMetricsFactory
//import com.google.common.util.concurrent.ThreadFactoryBuilder
//
//import collection.JavaConverters._
//
//trait KinesisStreamReader {
//
//  val streamName: String
//  val stage: String
//  val kinesisCredentialsProvider: AWSCredentialsProvider
//
//
//  lazy val worker = new Worker(
//    eventProcessorFactory,
//    kinesisConfig,
//    new NullMetricsFactory(), // don't send metrics to CloudWatch because it's expensive and not very helpful
//    Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(s"${getClass.getSimpleName}-$workerId-thread-%d").build())
//  )
//
//  /* Start the worker in a new thread. It will run forever */
//  private lazy val workerThread =
//    new Thread(worker, s"${getClass.getSimpleName}-$workerId")
//
//
//  def start(): Unit = workerThread.start()
//}
//
//object KinesisClient {
//
//  def getRecords(startingSequenceNumber: String): Seq[Record] = {
//    try {
//
//      val shardId = client.describeStream(streamName).getStreamDescription.getShards.asScala.head.getShardId
//
//      val iteratorRequest = new GetShardIteratorRequest()
//        .withStreamName(streamName)
//        .withShardId(shardId)
//        .withShardIteratorType("AT_SEQUENCE_NUMBER")
//        .withStartingSequenceNumber(startingSequenceNumber)
//
//      var iterator = client.getShardIterator(iteratorRequest).getShardIterator
//
//      val request = new GetRecordsRequest().withShardIterator(iterator)
//
//      var list = Seq.empty[Record]
//
//      while(list.length < 10) {
//        val result = client.getRecords(request)
//        iterator = result.getNextShardIterator
//        list = list ++ Seq(result.getRecords.asScala: _*)
//        Thread.sleep(1000)
//      }
//      list
//
//    } catch {
//      case e: Throwable =>
//        println(e)
//        Seq.empty[Record]
//    }
//
//  }
//
//}
