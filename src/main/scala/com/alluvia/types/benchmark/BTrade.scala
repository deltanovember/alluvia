package com.alluvia.types.benchmark

import com.alluvial.mds.contract.Trade


class BTrade(securityID: Int, sellerId: Short, sellerOrderId: Long, buyerId: Short, buyerOrderId: Long,
               tradeNo: Int, tradeValue: Double, tradeVolume: Double, tradePrice: Double,
               tradeTime: Long, actionFlag: Int, conditionCodes: String,
               UpdateTime: Long, UpdateTimeNS: Long) extends Trade(securityID,
                sellerId, sellerOrderId, buyerId, buyerOrderId, tradeNo, tradeValue, tradeVolume, tradePrice,
                tradeTime, actionFlag, conditionCodes, UpdateTime, UpdateTimeNS)