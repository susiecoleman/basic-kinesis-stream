import java.lang

import com.amazonaws.services.kinesis.model.{GetRecordsRequest, GetShardIteratorRequest, Record}
import Config._

import collection.JavaConverters._

object KinesisReader {

  def getRecords(startingSequenceNumber: String): Seq[Record] = {
    try {

      val shardId = client.describeStream(streamName).getStreamDescription.getShards.asScala.head.getShardId

      val iteratorRequest = new GetShardIteratorRequest()
        .withStreamName(streamName)
        .withShardId(shardId)
        .withShardIteratorType("AT_SEQUENCE_NUMBER")
        .withStartingSequenceNumber(startingSequenceNumber)

      var iterator = client.getShardIterator(iteratorRequest).getShardIterator

      val request = new GetRecordsRequest().withShardIterator(iterator)

      var list = Seq.empty[Record]

      while(list.length < 10) {
        val result = client.getRecords(request)
        iterator = result.getNextShardIterator
        list = list ++ Seq(result.getRecords.asScala: _*)
        Thread.sleep(1000)
      }
      list

    } catch {
      case e: Throwable =>
        println(e)
        Seq.empty[Record]
    }

  }

}
