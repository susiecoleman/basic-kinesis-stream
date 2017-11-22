object Main {
  def main(args: Array[String]): Unit = {
    val event = Event("abc", 123)
    val seqNumber = KinesisWriter.write(event)
    println(seqNumber)
    Thread.sleep(5000)
    println(s"Printing ${KinesisReader.getRecords(seqNumber.get).length}")
  }
}

case class Event(item1: String, item2: Int)