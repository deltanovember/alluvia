package com.alluvia.tools.kaggle

/**
 * Evaluate solutions by RMSD
 */
import com.alluvia.algo.Toolkit
import collection.mutable.ListBuffer
import io.Source
import scala.math.sqrt

object Evaluator extends App with Toolkit {
  val solutionFile = "data\\solution.csv"
  val predictionFile = "data\\example_entry_linear.csv"
  val solutions = new ListBuffer[List[Double]]
  val predictions = new ListBuffer[List[Double]]
  for (line <- Source.fromFile(solutionFile).getLines.filter(!_.contains("bid"))) solutions.append(line.split(",").toList.tail.map(_.toDouble))
  for (line <- Source.fromFile(predictionFile).getLines.filter(!_.contains("bid"))) predictions.append(line.split(",").toList.tail.map(_.toDouble))

  println(evaluate(solutions, predictions))
  
  def evaluate(solution: Seq[Seq[Double]], prediction: Seq[Seq[Double]]) = {
    val number = solution.length * solution(0).length
    val sumSquares = solution.zip(prediction).view.map(x => evaluateRow(x._1, x._2)).sum
    sqrt(sumSquares / number)
  }

  // Compute sum of squares
  def evaluateRow(solution: Seq[Double], prediction: Seq[Double]) = solution.zip(prediction).view.map(t => t._1 - t._2).map(x => x*x).sum

}