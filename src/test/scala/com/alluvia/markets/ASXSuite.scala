package com.alluvia.markets

import org.scalatest.WordSpec

class ASXSuite extends WordSpec {
  "An ASX market" should {

     "get min tick" in {
      val asx = new ASX {
      }
      assert(asx.getMinTickSize(45) == 0.01)

    }

    "produce correct ISINs" in {
      val asx = new ASX {
      }
      assert(asx.getISIN("ONT.AX") == "AU000000ONT7")
      assert(asx.getISIN("DMP.AX") == "AU000000DMP0")
      assert(asx.getISIN("GRF.AX") == "AU000000GRF3")
      assert(asx.getISIN("BENJR1.AX") == "AU000BENJR16")
      assert(asx.getISIN("TAS.AX") == "AU000000TAS5")

    }

    "round down correctly" in {
      val asx = new ASX {
      }
      assert(asx.roundDown("125.574", "0.25") == 125.5)
      assert(asx.roundDown("125.574", "0.05") == 125.55)
      assert(asx.roundDown("1.43972", "0.01") == 1.43)

    }

    "round up correctly" in {
      val asx = new ASX {
      }
      assert(asx.roundUp("125.574", "0.25") == 125.75)
      assert(asx.roundUp("125.574", "0.05") == 125.6)
      assert(asx.roundUp("125.574", "0.001") == 125.574)

    }

  }
}