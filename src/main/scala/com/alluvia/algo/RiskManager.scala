package com.alluvia.algo

/**

trait RiskManager extends Algo {

  // Variables
  // ------------------------------------------------------------

  //Open order size
  val openOrderSize = new HashMap[String, String, String, Double]

  // Position size
  val positionSize = new HashMap[String, String, String, Double]

  
  // Functions
  // ------------------------------------------------------------

  // Check open order count limits
  def checkOpenOrderCountLimits(String : Market, String : Algo, String : Security) {

    // IF total number of open orders will exceed limit THEN break
    if (openOrderSize.size > limitOpenOrderNumTotalMax) return
  
    // IF number of open orders for current algo will exceed limit THEN break
    if (openOrderSize(Market, Algo).size > limitOpenOrderNumPerAlgoMax(Market, Algo)) return

    // IF number of open orders for current algo and security will exceed limit THEN break
    if (openOrderSize(Market, Algo, Security).size > limitOpenOrderNumPerAlgoSecurityMax(Market, Algo)) return

  }

  // Check open order size limits
  def checkOpenOrderSizeLimits(String : Market, String : Algo, String : Security) {
    // IF total open order size will exceed limit THEN break
    if (openOrderSize.sum > limitOpenOrderSizeMax) return

    // IF open order size for current algo will exceed limit THEN break
    if (openOrderSize(Market, Algo).sum > limitPositionSizePerAlgoMax(Market, Algo)) return

    // IF open order size for current algo and security will exceed limit THEN break
    if (openOrderSize(Market, Algo, Security).sum > limitPositionSizePerAlgoSecurityMax(Market, Algo)) return

  }

  // Check position size limits
  def checkPositionSizeLimits(String : Market, String : Algo, String : Security) {

    // IF total position size will exeed limit THEN break
    if (positionSize.sum > limitPositionSizeMax) return

    // IF position size for current algo will exeed limit THEN break
    if (positionSize(Market, Algo).sum > limitPositionSizePerAlgoMax(Market, Algo)) return

    // IF position size for current algo and security will exeed limit THEN break
    if (positionSize(Market, Algo, Security).sum > limitPositionSizePerAlgoSecurityMax(Market, Algo)) return

  }

  // Update order size
  def updateOpenOrderSize(String : Market, String : Algo, String : Security, Double : Value) {

    // Update position size for current algo and security
    positionSize(Market, Algo, Security) += Value

  }

  // Update position size
  def updatePositionSize(String : Market, String : Algo, String : Security, Double : Value) {

    // Update position size for current algo and security
    positionSize(Market, Algo, Security) += Value

  }


    /**
   * default stub
   */
  def cancelOrder(order: Order): Unit = {
  }

  def dailyValue: Double = {
    val result = dailyValues.get(security)
    result match {
      case None => Double.NaN
      case Some(x) => x
    }
  }

}*/