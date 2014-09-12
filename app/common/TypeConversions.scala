package common

import java.util.Date
import com.twitter.util.Time
import scala.language.implicitConversions

trait TypeConversions {
	
  implicit def twitterTimeToJavaDate(time: Time): Date = time.toDate

  implicit def javaDateToTwitterTime(date: Date): Time = Time(date)

}