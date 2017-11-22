import java.nio.ByteBuffer
import java.util.UUID

import Config._
import com.amazonaws.services.kinesis.model.PutRecordRequest
import io.circe.generic.auto._
import io.circe.syntax._

object KinesisWriter {

  def write(event: Event) = {
    val eventJson = event.asJson
    val eventString = eventJson.toString()
    postToKinesis(eventString)
  }

  def postToKinesis(event: String): Option[String] = {
    val streamEvent: ByteBuffer = ByteBuffer.wrap(event.getBytes)

    val partitionKey = UUID.randomUUID().toString
    val request: PutRecordRequest = new PutRecordRequest()
    request.setPartitionKey(partitionKey)
    request.setStreamName(streamName)
    request.setData(streamEvent)
    try {
      val result = client.putRecord(request)
      Some(result.getSequenceNumber)
    } catch {
      case e: Throwable =>
        println(s"Could not post to kinesis stream ${e.getMessage} ${e.getStackTrace}")
        None
    }
  }
}
