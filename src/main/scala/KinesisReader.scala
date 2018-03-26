import com.amazonaws.services.kinesis.model._
import Config._

import collection.JavaConverters._
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object KinesisReader {

  def readRecords(sequenceNumber: Option[String] = None)(implicit config: KinesisConfig): Either[KinesisError , Seq[Record]] = {

      val client = config.client
      val streamName = config.streamName

      val shardId = client.describeStream(streamName).getStreamDescription.getShards.asScala.head.getShardId

      val baseIteratorRequest = new GetShardIteratorRequest()
        .withStreamName(streamName)
        .withShardId(shardId)

       val iteratorRequest = sequenceNumber match {
         case Some(number) => baseIteratorRequest.withShardIteratorType("AT_SEQUENCE_NUMBER").withStartingSequenceNumber(number)
         case _ => baseIteratorRequest.withShardIteratorType("TRIM_HORIZON")
       }

      @tailrec
      def recursiveRead(nextIterator: String, records: Seq[Record] = Nil): Either[KinesisError, Seq[Record]] = {
        val request = new GetRecordsRequest().withShardIterator(nextIterator)
        Try(client.getRecords(request)) match {
          case Success(result) if result.getMillisBehindLatest > 0 => recursiveRead(result.getNextShardIterator, records ++ result.getRecords.asScala)
          case Success(_) => Right(records)
          case Failure(f: ResourceNotFoundException) => Left(KinesisStreamNotFoundError(f.getErrorMessage))
          case Failure(f: ProvisionedThroughputExceededException) =>Left(ThroughputExceededError(f.getErrorMessage))
          case Failure(f) => Left(KinesisGenericError(f.getLocalizedMessage))
        }
      }

      val iterator = client.getShardIterator(iteratorRequest).getShardIterator
      recursiveRead(iterator)
  }

}

object ReaderTest {
  def reading() = {
    KinesisReader.readRecords()

  }
}
