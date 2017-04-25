package com.espirit.moddev.serverrunner

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import spec.UnitSpec

/**
  * TODO: fillme
  */
class HttpConnectionTesterSpec extends UnitSpec with GeneratorDrivenPropertyChecks {
  "HttpConnectionTester.isSuccess" should "return true for 2xx and 3xx error codes" in {
    forAll(Gen.choose(200, 399)) { successCode =>
      assert(HttpConnectionTester.isSuccess(successCode))
    }
  }
  it should "return false for any non-success response code" in {
    forAll(Gen.choose(0, 999) suchThat (x => x < 200 || x > 399)) { failureCode =>
      assert(!HttpConnectionTester.isSuccess(failureCode))
    }
  }
}
