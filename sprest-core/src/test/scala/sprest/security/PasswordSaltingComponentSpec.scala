package sprest.security

import org.specs2.mutable._
import org.specs2.specification.Scope

class PasswordSaltingComponentSpec extends Specification {

  "Password salting" should {
    "work for basic password" in new passSaltingComponent {
      testHashing("secret")
    }

    "work for empty password" in new passSaltingComponent {
      testHashing("")
    }

    "work for complex password" in new passSaltingComponent {
      testHashing("""12kjsdfLKDF2+=~sfd#$%^&&*!sdfklLKJD'"kj""")
    }
  }

  trait passSaltingComponent extends Scope {

    def testHashing(password: String) = {
      HashingAlgorithm.all map { alg =>
        val passwordSalting = new PasswordSaltingComponent {
          override def passwordSalting = new PasswordSalting {
            override val hashingAlgorithm = alg
          }
        }
        val encryptedPass = passwordSalting.passwordSalting.encrypt(password)
        encryptedPass.encryptedPass must_== passwordSalting.passwordSalting.encryptWithSalt(password, encryptedPass.saltBytes).encryptedPass
      }
    }
  }

}
