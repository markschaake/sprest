package sprest.security

import java.security.{ SecureRandom, MessageDigest }
import spray.json.DefaultJsonProtocol

case class EncryptedPassword(salt: String, encryptedPass: String) {
  def saltBytes = Hex.toBytes(salt)
  def passwordBytes = Hex.toBytes(encryptedPass)
}

object Hex {
  import javax.xml.bind.DatatypeConverter
  def toHex(bytes: Array[Byte]) = DatatypeConverter.printHexBinary(bytes)
  def toBytes(hex: String) = DatatypeConverter.parseHexBinary(hex)
}

object EncryptedPassword extends DefaultJsonProtocol {
  import spray.json._
  implicit val encryptedPasswordFormat = jsonFormat2(EncryptedPassword.apply)
}

sealed abstract class HashingAlgorithm(val name: String) {
  HashingAlgorithm._all += this
}

object HashingAlgorithm {

  private val _all = scala.collection.mutable.ListBuffer[HashingAlgorithm]()

  def all = _all.toList

  case object MD2 extends HashingAlgorithm("MD2")
  case object MD5 extends HashingAlgorithm("MD5")
  case object SHA1 extends HashingAlgorithm("SHA-1")
  case object SHA256 extends HashingAlgorithm("SHA-256")
  case object SHA384 extends HashingAlgorithm("SHA-384")
  case object SHA512 extends HashingAlgorithm("SHA-512")
}

trait PasswordSaltingComponent {

  def passwordSalting: PasswordSalting

  trait PasswordSalting {

    def hashingAlgorithm: HashingAlgorithm

    def hashingIterations: Int = 20000
    def saltSize: Int = 32

    private val random = new SecureRandom

    private def nextSalt = {
      val bytes = new Array[Byte](saltSize)
      random.nextBytes(bytes)
      bytes
    }

    def encrypt(password: String): EncryptedPassword = encryptWithSalt(password, nextSalt)

    def encryptWithSalt(password: String, salt: Array[Byte]): EncryptedPassword = {
      val digest = MessageDigest.getInstance(hashingAlgorithm.name)
      digest.reset()
      digest.update(salt)
      var hashed: Array[Byte] = digest.digest(password.getBytes)

      (1 to hashingIterations) foreach { i =>
        digest.reset()
        hashed = digest.digest(hashed)
      }
      EncryptedPassword(salt = Hex.toHex(salt), encryptedPass = Hex.toHex(hashed))
    }
  }

}

trait SHA256PasswordSaltingComponent extends PasswordSaltingComponent {
  override val passwordSalting = new PasswordSalting {
    override val hashingAlgorithm = HashingAlgorithm.SHA256
  }
}
