package com.alluvia.markets

import io.Source
import java.nio.charset.MalformedInputException
import java.io.File
import collection.mutable.HashMap

trait LSE extends Market {

  // Load ISINs
  val isinMap = new HashMap[String, String]
  // SETS code
  val setsMap = new HashMap[String, String]

  try {
    for (line <- Source.fromInputStream(getClass.getResourceAsStream("/isin" + getMarketName + ".csv"), "windows-1252").getLines) {
      val allTokens = line.split(",", -1)

      if ("" != allTokens(4)) {
        val sets = allTokens(1)
        val ISIN = allTokens(4)
        val security = allTokens(7) + ".L"
        setsMap.put(security, sets)
        isinMap.put(security, ISIN)

      }

    }
  }
  catch {
    case ex: MalformedInputException => ex.printStackTrace()
  }

  override def getBrokerCurrencyMultiplier = 100.0
  override def getCloseTime = "16:30:00.000"
  override def getCurrency = "GBX"
  override def getCurrencyMultiplier = 100.0
  override def getEndAuctionClose = "16:34:30.000"
  override def getIGCurrencySymbol = "£"
  override def getISIN(security: String) = isinMap(security)
  override def getMarketName = "LSE"

  /**
   * info - SETS segment. Exchange info required e.g. RIO.L, BLT.L
   */
  override def getMinTickSize(price: Double, security: String): Double = {

    if (!setsMap.contains(security)) return Double.NaN

    if (setsMap(security) == "SET0") {
      /**
       * Less than 0.9999	Z	One ten-thousandth		0.0001
          1 - 4.9995	Y	One two-thousandth		0.0005
          5 - 9.999	M	One thousandth		0.001
          10 - 49.995	K	One two-hundredth		0.005
          50 - 99.99	J	One hundredth		0.01
          100 - 499.95	V	One twentieth		0.05
          500 - 999.9	X	One tenth		0.1
          1000 - 4999.5	H	Halves		0.5
          5000 - 9999	W	Whole		1
          10000 or more	P	Five		5
       */

      return price match {
        case x if x < 0.9999 => 0.0001
        case x if x >= 1 && x <= 4.9995 => 0.0005
        case x if x >= 10 && x <= 49.995 => 0.005
        case x if x >= 50 && x <= 99.99 => 0.01
        case x if x >= 100 && x <= 999.9 => 0.1
        case x if x >= 1000 && x <= 4999.5 => 0.5
        case x if x >= 5000 && x <= 99999 => 1
        case _ => 5
      }

    }
    if (setsMap(security) == "SET1" || setsMap(security) == "STMM") {
      /**
       * Less than 0.5 	Z	One ten-thousandth		0.0001
        0.5 - 0.9995	Y	One two-thousandth		0.0005
        1 - 4.999	M	One thousandth		0.001
        5 - 9.995	K	One two-hundredth		0.005
        10 - 49.99	J	One hundredth		0.01
        50 - 99.95	V	One twentieth		0.05
        100 - 499.9	X	One tenth		0.1
        500 - 999.5	H	Halves		0.5
        1000 - 4999	W	Whole		1
        5000 - 9995	P	Five		5
        10000 or more	C	Ten		10
       */

        return price match {
          case x if x < 0.5 => 0.0001
          case x if x >= 0.5 && x <= 0.9995 => 0.0005
          case x if x >= 1 && x <= 4.9999 => 0.001
          case x if x >= 5 && x <= 9.995 => 0.005
          case x if x >= 10 && x <= 49.99 => 0.01
          case x if x >= 50 && x <= 99.5 => 0.05
          case x if x >= 100 && x <= 499.9 => 0.1
          case x if x >= 500 && x <= 999.5 => 0.5
          case x if x >= 1000 && x <= 4999 => 1
          case x if x >= 5000 && x <= 9995 => 5
          case x if x >= 100 && x <= 499.9 => 10
        }


    }

    val tier3 = List("SET3", "SSMM", "AMSM", "SSMU", "SFM1", "ASQ1", "ASQ2", "ASQN", "SSQ3", "SSQ4", "SFM2")
    if (tier3.contains(setsMap(security))) {

      /**
       * GBX TM32, 1 (trading parameters

          Less than 10 	J	One hundredth		0.01
          10 - 499.75 	Q	Quarters		0.25
          500 - 999.5	H	Halves		0.5
          1000 or more 	W	Whole		1

       */
        return price match {
          case x if x < 10 => 0.01
          case x if x >= 10 && x <= 499.75 => 0.25
          case x if x >= 500 && x <= 999.95 => 0.5
          case _ => 1
        }
    }

    val aim = List("AIM", "AIMI", "CNVE", "STBS")
    if (aim.contains(setsMap(security))) {

      /**
       * Less than 10	Z	One ten thousandth		0.0001
      10 or more	Q	Quarters		0.25
       */
        return price match {
          case x if x < 10 => 0.0001
          case _ => 0.25
        }
    }

    0.0
  }

  override def getOpenTime = "08:00:00.000"

  override def getSecurityExchange = "L"
  override def getTimeZone = java.util.TimeZone.getTimeZone("Europe/London")
  override def getTradeableSecurities(date: java.util.Date): List[String] = List("BLT.L","RIO.L","HSBA.L","VOD.L","BP..L","XTA.L","BARC.L","AAL.L","GSK.L","RB..L","RDSB.L","AZN.L","TSCO.L","BATS.L","LLOY.L","STAN.L","BG..L","RDSA.L","ULVR.L","DGE.L","IMT.L","CNA.L","WPP.L","AV..L","SAB.L","PRU.L","NG..L","ANTO.L","SHP.L","BT.A.L","MKS.L","RBS.L","TLW.L","NXT.L","VED.L","SN..L","SSE.L","RR..L","CPG.L","ARM.L","KAZ.L","BSY.L","CCL.L","MRW.L","REL.L","PSON.L","WOS.L","OML.L","KGF.L","SBRY.L","LGEN.L","BLND.L","EMG.L","RRS.L","BRBY.L","ITV.L","AU..L","CNE.L","GKN.L","IPR.L","HOME.L","ENRC.L","LAND.L","WEIR.L","RSL.L","RSA.L","EXPN.L","IHG.L","PFC.L","SMIN.L","UU..L","WTB.L","WG..L","ABF.L","GFS.L","HMSO.L","IAP.L","AMEC.L","FRES.L","JMAT.L","CPI.L","SL..L","POG.L","PTEC.L","III.L","IMI.L","ISYS.L","REX.L","DRX.L","SVT.L","SRP.L","MCRO.L","SGE.L","MGGT.L","GKP.L","CKSN.L","FGP.L","LMI.L","LOG.L","PER.L","SGRO.L","TT..L","PSN.L","ITRK.L","BNZL.L","PMO.L","TW..L","WMH.L","TCG.L","SDR.L","UBM.L","lse.L","IGG.L","TATE.L","TPK.L","AML.L","BLVN.L","COB.L","LAD.L","INCH.L","INVP.L","CRDA.L","CSCG.L","RKH.L","BLNX.L","FXPO.L","NWG.L","BVIC.L","EZJ.L","MSY.L","ATST.L","PNN.L","MPI.L","FLTR.L","ASHM.L","HFD.L","CLLN.L","ADN.L","HOIL.L","BAB.L","RTO.L","SGC.L","MNDI.L","BDEV.L","NEX.L","CHAR.L","INF.L","PFG.L","DXNS.L","CHTR.L","KESA.L","DEB.L","ESSR.L","CHG.L","DOM.L","SXS.L","CWC.L","SMWH.L","LOND.L","AHT.L","MGCR.L","PFL.L","EO..L","FPT.L","RMV.L","ICP.L","MAB.L","DLN.L","GPOR.L","PFD.L","SGP.L","KAH.L","ROR.L","CIU.L","ENQ.L","SMDS.L","GNK.L","ECM.L","YULC.L","IMG.L","CSR.L","SOLO.L","SIA.L","BRWM.L","TCY.L","AVM.L","HIK.L","SMT.L","TEM.L","CZA.L","CHU.L","SPX.L","PDL.L","GPX.L","HWDN.L","BBA.L","BOY.L","JLT.L","DLAR.L","IPF.L","BWY.L","MLC.L","NPE.L","MTO.L","DES.L","HTG.L","CDN.L","PCI.L","SOU.L","HLMA.L","SHI.L","FRCL.L","TLPR.L","AUL.L","CPW.L","HSV.L","FENR.L","SHB.L","HSTN.L","RSW.L","STOB.L","IQE.L","MTC.L","AVV.L","DNLM.L","ULE.L","AVN.L","SNR.L","BZM.L","RTN.L","OCDO.L","BVS.L","RPC.L","EDIN.L","ELM.L","SMDR.L","RGU.L","MUL.L","JUP.L","RCP.L","ETI.L","JDW.L","MXP.L","VPP.L","LAM.L","MATD.L","GOG.L","APF.L","PUB.L","DAB.L","FEV.L","BRSN.L","JESC.L","NTG.L","BABS.L","LSP.L","HICL.L","SPT.L","MCHL.L","IRV.L","GRI.L","MONY.L","BGC.L","MARS.L","BHMG.L","INPP.L","BAO.L","SVI.L","PIC.L","TALK.L","SBLM.L","MYI.L","VGAS.L","IAE.L","AGLD.L","HGM.L","GRG.L","GMG.L","JKX.L","DVO.L","PTR.L","ITE.L","SPD.L","TNO.L","PZC.L","FCSS.L","BHGG.L","FCAM.L","BWNG.L","CTY.L","DCG.L","SDL.L","KFX.L","WTAN.L","FPM.L","RNK.L","3IN.L","BYG.L","DPLM.L","CLDN.L","LONR.L","STHR.L","JAM.L","CINE.L","PCT.L","JII.L","CCC.L","YELL.L","ELTA.L","DTY.L","MNKS.L","GFRD.L","BOK.L","MEC.L","WLF.L","PGD.L","KENZ.L","AGQ.L","UTG.L","TEP.L","BTEM.L","AMER.L","DSC.L","HBR.L","RPS.L","SCIN.L","XCH.L","LRD.L","VAA.L","BAG.L","JRS.L","SEY.L","IDH.L","SVS.L","JSM.L","SKS.L","FOGL.L","QQ..L","HLCL.L","TNI.L","CTH.L","SBT.L","E2V.L","KCOM.L","DEMG.L","ALN.L","HRI.L","RAT.L","SYR.L","QED.L","BSET.L","JMG.L","KIE.L","PVCS.L","BNKR.L","UKCM.L","ASL.L","TMPL.L","MRCH.L","BRAM.L","BRW.L","TRY.L","HILS.L","HGT.L","FSV.L","SFR.L","CWK.L","MNZS.L","WIN.L","GCM.L","HMV.L","MAYG.L","RDW.L","LOOK.L","PURE.L","MSLH.L","VOG.L","FDSA.L","NYO.L","CAZA.L","WKP.L","WWH.L","BEM.L","BRLA.L","MONI.L","LWDB.L","GKO.L","MUT.L","CLST.L","MCB.L","KLR.L","RRR.L","SXX.L","PHP.L","MJW.L","ASD.L","MKLW.L","RWD.L","SMP.L","PLI.L","SDY.L","RWA.L","SOI.L","TTG.L","RICA.L","AUE.L","DIG.L","SAR.L","HSD.L","SAFE.L","NTOG.L","EMED.L","ACD.L","TSW.L","BRNE.L","TRG.L","FDI.L","HYC.L","PMG.L","VOF.L","ANGM.L","RUS.L","SGI.L","JJB.L","VEC.L","TIG.L","UMC.L","HFEL.L","GPE.L","SNRP.L","CSN.L","UKC.L","AAIF.L","JEO.L","MGNS.L","THRG.L","EUS.L","LWB.L","SQZ.L","NOP.L","SDP.L","IFD.L","SDU.L","AYM.L","CHW.L","SCAM.L","EUK.L","HSL.L","TAN.L","NUM.L","CLIG.L","IRET.L","OXIG.L","SLS.L","JFJ.L","NGP.L","BRCI.L","ERM.L","BRSC.L","HEAD.L","RPT.L","OEX.L","CWR.L","NVTA.L","JHD.L","RNWH.L","JLP.L","JD..L","FLYB.L","AAS.L","DTG.L","SVG.L","CSRT.L","FWEB.L","IVI.L","INTQ.L","ABD.L","FGT.L","FCCN.L","RNO.L","KEFI.L","AFE.L","PRZ.L","CGH.L","VLX.L","IPO.L","BGFD.L","MNR.L","JAI.L","JMO.L","JCH.L","LVD.L","MRS.L","EOG.L","MWA.L","HMB.L","GWP.L","AAZ.L","RGD.L","QFI.L","EVG.L","MNP.L","AR..L","ZEN.L","RMM.L","NRRP.L","OPTS.L","HFG.L","CKN.L","PDG.L","BRGE.L","AGA.L","BEE.L","LONR.L","RGM.L","MNC.L","SCF.L","JMC.L","SPH.L","LCG.L","LWI.L","EST.L","TPJ.L","LGO.L","GRL.L","ASTO.L","GOO.L","AGY.L","NVA.L","ENK.L","JMF.L","ITM.L","AVE.L","BZT.L","KIT.L","LUP.L","HPI.L","CDI.L","TLDH.L","IAT.L","IPT.L","IMM.L","XTR.L","URU.L","DJAN.L","KEA.L","SOLG.L","CRND.L","TSTR.L","ASM.L","TPT.L","RHL.L","ADH.L","SCHE.L","HHI.L","AIE.L","DTZ.L","FSJ.L","UTV.L","ABH.L","RNVO.L","RM..L","SLN.L","SEPU.L","JZCP.L","SRSP.L","OPAY.L","HRCO.L","ZOX.L","SLI.L","CAL.L","OXS.L","RCG.L","FDL.L","JPR.L","VIY.L","PON.L","PHTM.L","HAMP.L","HRG.L","CTO.L","DOO.L","OXB.L","RCDO.L")
  override def getUncrossTime = "16:35:00.000"
}