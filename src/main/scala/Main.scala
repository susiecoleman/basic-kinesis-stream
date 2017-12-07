object Main {
  def main(args: Array[String]): Unit = {
    println("hello world")
  }
}

object Test {

  implicit val stringToByte: String => Array[Byte] = _.getBytes()

  def putting = {
    KinesisWriter.put("hello") match {
      case Right(s) => println(s)
      case Left(f) => println("Put failed")
    }

  }
}