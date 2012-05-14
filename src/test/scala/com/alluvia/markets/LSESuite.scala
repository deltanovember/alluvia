package com.alluvia.markets

import org.scalatest.WordSpec

class LSESuite extends WordSpec {

  "An lse market" should {
    "round values" in  {
      val lse = new LSE {
      }
      assert(lse.getMarketName === "LSE")
      //"min tick size" is (pending)
    }
    "produce correct ISINs" in {
      val lse = new LSE {
      }
      assert(lse.getISIN("RIO.L") == "GB0007188757")
      assert(lse.getISIN("IAEM.L") == "GB00B4M5KX38")
      assert(lse.getISIN("TW..L") == "GB0008782301")
      assert(lse.getISIN("LSE.L") == "GB00B0SWJX34")
      assert(lse.getISIN("FCAM.L") == "GB0004658141")
      assert(lse.getISIN("SVS.L") == "GB00B135BJ46")
      assert(lse.getISIN("MUL.L") == "GB0006094303")
      assert(lse.getISIN("FDSA.L") == "GB0007590234")
      assert(lse.getISIN("CC..L") == "GB0002036720")
    }

    "produce tick sizes " in {
      val lse = new LSE {
      }
      assert(lse.getMinTickSize(44, "FWEB.L") == 0.25)
      assert(lse.getMinTickSize(48, "CTO.L") == 0.25)
      assert(lse.getMinTickSize(165, "HSD.L") == 0.25)
      assert(lse.getMinTickSize(1971, "BLT.L") == 0.5)
      assert(lse.getMinTickSize(1115, "BAG.L") == 1)
      assert(lse.getMinTickSize(846, "LSE.L") == 0.5)
      assert(lse.getMinTickSize(65, "FCAM.L") == 0.05)
      assert(lse.getMinTickSize(290, "SVS.L") == 0.1)
      assert(lse.getMinTickSize(1460, "MUL.L") == 1)
      assert(lse.getMinTickSize(1560, "FDSA.L") == 1)
      assert(lse.getMinTickSize(10.75, "CC..L") == 0.25)
      
    }
  }
}