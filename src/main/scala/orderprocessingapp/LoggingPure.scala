package orderprocessingapp

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class LogEntry(timestamp: String, level: String, message: String)

object LoggingPure {
  private def formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def info(message: String): LogEntry =
    LogEntry(LocalDateTime.now().format(formatter), "INFO", message)

  def warn(message: String): LogEntry =
    LogEntry(LocalDateTime.now().format(formatter), "WARN", message)

  def error(message: String): LogEntry =
    LogEntry(LocalDateTime.now().format(formatter), "ERROR", message)
}