package io.github.ragazoor.migration

import io.github.ragazoor.Task

import scala.concurrent.{ Future => StdFuture }
import scala.language.implicitConversions

trait TaskToStdFuture {
  implicit def ioToStdFutureF0[E <: Throwable, B](f0: () => Task[E, B]): () => StdFuture[B] =
    () => f0().toFuture

  implicit def ioToStdFutureF1[E <: Throwable, X1, B](f1: X1 => Task[E, B]): X1 => StdFuture[B] =
    x1 => f1(x1).toFuture

  implicit def ioToStdFutureF2[E <: Throwable, X1, X2, B](f2: (X1, X2) => Task[E, B]): (X1, X2) => StdFuture[B] =
    (x1, x2) => f2(x1, x2).toFuture

  implicit def ioToStdFutureF3[E <: Throwable, X1, X2, X3, B](
    f3: (X1, X2, X3) => Task[E, B]
  ): (X1, X2, X3) => StdFuture[B] =
    (x1, x2, x3) => f3(x1, x2, x3).toFuture

  implicit def ioToStdFutureF4[E <: Throwable, X1, X2, X3, X4, B](
    f4: (X1, X2, X3, X4) => Task[E, B]
  ): (X1, X2, X3, X4) => StdFuture[B] =
    (x1, x2, x3, x4) => f4(x1, x2, x3, x4).toFuture

  implicit def ioToStdFutureF5[E <: Throwable, X1, X2, X3, X4, X5, B](
    f5: (X1, X2, X3, X4, X5) => Task[E, B]
  ): (X1, X2, X3, X4, X5) => StdFuture[B] =
    (x1, x2, x3, x4, x5) => f5(x1, x2, x3, x4, x5).toFuture

  implicit def ioToStdFutureF6[E <: Throwable, X1, X2, X3, X4, X5, X6, B](
    f6: (X1, X2, X3, X4, X5, X6) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6) => f6(x1, x2, x3, x4, x5, x6).toFuture

  implicit def ioToStdFutureF7[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, B](
    f7: (X1, X2, X3, X4, X5, X6, X7) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7) => f7(x1, x2, x3, x4, x5, x6, x7).toFuture

  implicit def ioToStdFutureF8[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, B](
    f8: (X1, X2, X3, X4, X5, X6, X7, X8) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8) => f8(x1, x2, x3, x4, x5, x6, x7, x8).toFuture

  implicit def ioToStdFutureF9[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, B](
    f9: (X1, X2, X3, X4, X5, X6, X7, X8, X9) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9) => f9(x1, x2, x3, x4, x5, x6, x7, x8, x9).toFuture

  implicit def ioToStdFutureF10[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, B](
    f10: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10) => f10(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10).toFuture

  implicit def ioToStdFutureF11[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, B](
    f11: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11) => f11(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11).toFuture

  implicit def ioToStdFutureF12[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, B](
    f12: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12) =>
      f12(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12).toFuture

  implicit def ioToStdFutureF13[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, B](
    f13: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13) =>
      f13(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13).toFuture

  implicit def ioToStdFutureF14[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, B](
    f14: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14) =>
      f14(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14).toFuture

  implicit def ioToStdFutureF15[E <: Throwable, X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, B](
    f15: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15) =>
      f15(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15).toFuture

  implicit def ioToStdFutureF16[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    B
  ](
    f16: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16) =>
      f16(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16).toFuture

  implicit def ioToStdFutureF17[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    B
  ](
    f17: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17) =>
      f17(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17).toFuture

  implicit def ioToStdFutureF18[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    X18,
    B
  ](
    f18: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18) =>
      f18(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18).toFuture

  implicit def ioToStdFutureF19[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    X18,
    X19,
    B
  ](
    f19: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19) =>
      f19(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19).toFuture

  implicit def ioToStdFutureF20[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    X18,
    X19,
    X20,
    B
  ](
    f20: (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19, X20) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19, X20) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20) =>
      f20(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20).toFuture

  implicit def ioToStdFutureF21[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    X18,
    X19,
    X20,
    X21,
    B
  ](
    f21: (
      X1,
      X2,
      X3,
      X4,
      X5,
      X6,
      X7,
      X8,
      X9,
      X10,
      X11,
      X12,
      X13,
      X14,
      X15,
      X16,
      X17,
      X18,
      X19,
      X20,
      X21
    ) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19, X20, X21) => StdFuture[B] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21) =>
      f21(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21).toFuture

  implicit def ioToStdFutureF22[
    E <: Throwable,
    X1,
    X2,
    X3,
    X4,
    X5,
    X6,
    X7,
    X8,
    X9,
    X10,
    X11,
    X12,
    X13,
    X14,
    X15,
    X16,
    X17,
    X18,
    X19,
    X20,
    X21,
    X22,
    B
  ](
    f22: (
      X1,
      X2,
      X3,
      X4,
      X5,
      X6,
      X7,
      X8,
      X9,
      X10,
      X11,
      X12,
      X13,
      X14,
      X15,
      X16,
      X17,
      X18,
      X19,
      X20,
      X21,
      X22
    ) => Task[E, B]
  ): (X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15, X16, X17, X18, X19, X20, X21, X22) => StdFuture[
    B
  ] =
    (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22) =>
      f22(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22).toFuture

}
