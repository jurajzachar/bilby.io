import org.scalatest.FunSuite
import org.scalamock.scalatest.MockFactory
import com.blueskiron.bilby.io.model.User

/**
 * @author juri
 */
class ModelSuite extends FunSuite with MockFactory {
 
  test("Entity type is captured for User") {
    
    //can't handle methods with more than 9 parameters (yet).
//    val user: User = User(
//        Some("jack"), 
//        Some("london"), 
//        "jackie12", 
//        "jackie12@email.com", 
//        "$12345", 
//        "", 
//        "", 
//        None,
//        None,
//        None,
//        None,
//        None,
//        None)
  }
 
}