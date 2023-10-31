//package com.authService.auth
//
//import com.authService.UnitSpec
//import com.authService.mocks.JwtMock
//import pdi.jwt.JwtAlgorithm
//import pdi.jwt.algorithms.JwtHmacAlgorithm
//
//import scala.util.{Failure, Success}
//
//class AuthServiceSpec extends UnitSpec {
//  describe("#createToken") {
//    it("should return a token") {
//      val svs = new AuthService()
//      assert(svs.createToken() == "fake-jwt-token")
//    }
//  }
//
//  describe("#validateToken") {
//    describe("when the token is valid") {
//      it("should succeed") {
//        val svs = new AuthService()
//        assert(
//          svs.validateToken("mock-token") == Success(
//            "mock-header",
//            "mock-claim",
//            "mock-signature"
//          )
//        )
//      }
//    }
//
//    describe("when the token is not valid") {
//      it("should fail") {
//        val svs = new AuthService()
//        assert(svs.validateToken("mock-token").isFailure)
//      }
//    }
//  }
//}
