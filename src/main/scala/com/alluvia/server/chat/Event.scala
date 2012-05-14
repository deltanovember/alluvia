package com.alluvia.server.chat

sealed trait Event
case class Login(user: String) extends Event
case class Logout(user: String) extends Event
case class GetChatLog(from: String) extends Event
case class ChatLog(log: List[String]) extends Event
case class ChatMessage(from: String, message: String) extends Event