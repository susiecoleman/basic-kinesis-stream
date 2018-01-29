import com.amazonaws.services.kinesis.model._
import Config._

import collection.JavaConverters._
import scala.util.{Failure, Success, Try}


implicit class RichTry[T](t: Try[T]) {
  def convertTryToEither:Either[KinesisError, T] =
    t match {
      case Success(event) => Right(event)
      case Failure(f: ResourceNotFoundException) => Left(KinesisStreamNotFoundError(f.getErrorMessage))
      case Failure(f: ProvisionedThroughputExceededException) =>Left(ThroughputExceededError(f.getErrorMessage))
      case Failure(f) => Left(KinesisGenericError(f.getLocalizedMessage))
    }
}

object KinesisReader {

  def readRecord(sequenceNumber: String)(implicit config: KinesisConfig): Either[KinesisError , Option[Record]] = {
    Try {

      val client = config.client
      val streamName = config.streamName

      val shardId = client.describeStream(streamName).getStreamDescription.getShards.asScala.head.getShardId

      val iteratorRequest = new GetShardIteratorRequest()
        .withStreamName(streamName)
        .withShardId(shardId)
        .withShardIteratorType("AT_SEQUENCE_NUMBER")
        .withStartingSequenceNumber(sequenceNumber)

      val iterator = client.getShardIterator(iteratorRequest).getShardIterator

      val request = new GetRecordsRequest().withShardIterator(iterator)
      client.getRecords(request).getRecords.asScala.headOption

    } match {
      case Success(event) => Right(event)
      case Failure(f: ResourceNotFoundException) => Left(KinesisStreamNotFoundError(f.getErrorMessage))
      case Failure(f: ProvisionedThroughputExceededException) =>Left(ThroughputExceededError(f.getErrorMessage))
      case Failure(f) => Left(KinesisGenericError(f.getLocalizedMessage))
    }
  }



}

