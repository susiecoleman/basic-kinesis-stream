import java.nio.ByteBuffer
import java.util.UUID
import Config.kinesisConfig
import com.amazonaws.services.kinesis.model._
import scala.util.{Failure, Success, Try}

sealed trait KinesisWriterError {
  def message: String
}

case class KinesisStreamNotFoundError(message: String) extends KinesisWriterError
case class ThroughputExceededError(message: String) extends KinesisWriterError
case class PutFailedError(message: String) extends KinesisWriterError

object KinesisWriter {

  def put[T](event: T)(implicit config: KinesisConfig, encoder: T => Array[Byte]): Either[KinesisWriterError, T] = {
    val streamEvent: ByteBuffer = ByteBuffer.wrap(encoder(event))
    val partitionKey = UUID.randomUUID().toString
    val request: PutRecordRequest = new PutRecordRequest()
    request.setPartitionKey(partitionKey)
    request.setStreamName(config.streamName)
    request.setData(streamEvent)
    Try(config.client.putRecord(request)) match {
      case Success(_) => Right(event)
      case Failure(f: ResourceNotFoundException) => Left(KinesisStreamNotFoundError(f.getErrorMessage))
      case Failure(f: ProvisionedThroughputExceededException) =>Left(ThroughputExceededError(f.getErrorMessage))
      case Failure(f) => Left(PutFailedError(f.getLocalizedMessage))
    }
  }
}

object Test {

  implicit val stringToByte: String => Array[Byte] = _.getBytes()

  def putting = {
    KinesisWriter.put("hello") match {
      case Right(s) => println(s)
      case Left(f) => println(f)
    }

  }
}

