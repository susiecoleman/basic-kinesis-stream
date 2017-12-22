import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar


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
    KinesisWriter.put(event)(config, stringToByte) match {
      case Left(error) => error match {
        case _: KinesisStreamNotFoundError => assert(true)
        case e => assert(false, s"Expected KinesisStreamNotFoundError got ${e.getClass}")
      }
      case _ => assert(false, "Found right should always be left")
    }
  }

//  it should "return throughput exceeded error" in {
//    val client = mock[AmazonKinesis]
//    when(client.putRecord)
//  }

}
