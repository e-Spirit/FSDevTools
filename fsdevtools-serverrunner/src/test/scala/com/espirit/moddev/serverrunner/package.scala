package com.espirit.moddev

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

package object serverrunner {

  /** automatically convert java.time.Duration to scala.concurrent.duration.Duration */
  implicit def toFiniteDuration(d: java.time.Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  /** automatically convert scala.concurrent.duration.Duration to java.time.Duration */
  implicit def toJavaDuration(d: scala.concurrent.duration.Duration): java.time.Duration = java.time.Duration.ofNanos(d.toNanos)
}
