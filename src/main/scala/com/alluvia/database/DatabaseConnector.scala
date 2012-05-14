package com.alluvia.database

import java.sql._

trait DatabaseConnector {

  var statement: Statement = null
  var preparedStatement: PreparedStatement = null

  try {
    Class.forName("org.postgresql.Driver")
  }
  catch {
    case e: ClassNotFoundException => {
      System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!")
      e.printStackTrace()

    }
  }
  var connection: Connection = try {
    DriverManager.getConnection("jdbc:postgresql://192.168.16.23:5432/vbroker", "owner", "t3stt3st")  // <- Use this for real Historical
    //DriverManager.getConnection("jdbc:postgresql://localhost:5432/database", "postgres", "t3stt3st")
    //DriverManager.getConnection("jdbc:postgresql://localhost:5432/new", "postgres", "t3stt3st")  // <- Use this for local
  }
  catch {
    case e: SQLException => {
      System.out.println("Connection Failed! Check output console")
      e.printStackTrace()
      null

    }
  }
  if (connection != null) {
  }
  else {
    System.out.println("Failed to make connection!")
  }

}