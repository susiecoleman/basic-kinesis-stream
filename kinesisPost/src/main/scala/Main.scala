object Main {
  def main(args: Array[String]): Unit = {
    val event = Event("abc", 123)
    KinesisWriter.write(event)
  }
}

case class Event(item1: String, item2: Int)