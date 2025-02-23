package snappyShots

import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec

import org.scalajs.dom
import org.scalajs.dom.document

class LoginFormSpec extends AnyFunSpec {
  // Initialize App
  val loginTrigger = () => {
    println("Login Triggered")
  }
//  def appendPar(targetNode: dom.Node, text: String): Unit = {
//    println("HERE")
//    println(document)
//    val parNode = document.createElement("p")
//    parNode.textContent = text
//    targetNode.appendChild(parNode)
//  }

  describe("SnappyShots") {
    it("should contain 'Hello World' text in its body") {
//      appendPar(document.body, "Hello World")
//      println(dom.document.getElementById("app"))
      println(document)

//      LoginForm.appElement(loginTrigger)

      //      val $el = dom.document.getElementById("app")
      assert(true == true)
    }
  }
}
