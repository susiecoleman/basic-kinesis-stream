import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.model.{ProvisionedThroughputExceededException, PutRecordRequest}
import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers._

import scala.reflect.ClassTag


class KinesisWriterTest extends org.scalatest.FlatSpec with Matchers with MockitoSugar {

  val event = "test event"
  val stringToByte: String => Array[Byte] = _.getBytes()

  val region: Region = Option(Regions.getCurrentRegion).getOrElse(Region.getRegion(Regions.EU_WEST_1))

  val awsCredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("developerPlayground"),
    new InstanceProfileCredentialsProvider(false)
  )
  val client: AmazonKinesis = AmazonKinesisClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region.getName).build()

  "A kinesis put" should "return a Right of the input type" in {
    KinesisWriter.put(event)(Config.kinesisConfig, stringToByte) should be(Right(event))
  }

  it should "return a stream not found error" in {
    val config = KinesisConfig("InvalidStream", client)
    testPutFailure[KinesisStreamNotFoundError](config)
  }

  it should "return throughput exceeded error" in {
    val client = mock[AmazonKinesis]
    when(client.putRecord(any())).thenThrow(new ProvisionedThroughputExceededException("KinesisGenericError throughput exceeded"))

    val config = KinesisConfig("ThroughputExceeded", client)

    testPutFailure[ThroughputExceededError](config)
  }

  private def testPutFailure[T <: KinesisError](config: KinesisConfig)(implicit tag: ClassTag[T]) = {
    KinesisWriter.put(event)(config, stringToByte) match {
      case Left(error) => error match {
        case _: T => assert(true)
        case e => assert(false, s"Expected ${tag.runtimeClass} got ${e.getClass}")
      }
      case _ => assert(false, "Found right should always be left")
    }
  }

}
