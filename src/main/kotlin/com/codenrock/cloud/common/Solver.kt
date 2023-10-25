package com.codenrock.cloud.common

import java.time.Instant
import kotlin.math.*

object LinearSystem {

    /**
    a11 * x1 + a12 * x2 = b1
    a21 * x1 + a22 * x2 = b2
     */
    fun solve(a11: Double, a12: Double, a21: Double, a22: Double, b1: Double, b2: Double): Pair<Double, Double>? {
        val d = a11 * a22 - a12 * a21
        if (d == .0) {
            return null
        }

        val x1 = (b1 * a22 - b2 * a12) / d
        val x2 = (-b1 * a21 + b2 * a11) / d

        return Pair(x1, x2)
    }


    /**
    a11 * x1 + a12 * x2 ... a1n * xn = b1
    a21 * x1 + a22 * x2 ... a2n * xn = b2
    ...
    an1 * x1 + an2 * x2 ... ann * xn = bn
     */
    fun solve(mA: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val a = mA.mapIndexed { i, ai -> ai + b[i] }.toTypedArray()
        val n = a.size
        for (i in 0 until n) {
            val pivot = (i until n).maxBy { abs(a[it][i]) }
            a[i] = a[pivot].also { a[pivot] = a[i] }
            for (j in i + 1..n) {
                a[i][j] /= a[i][i]
            }

            for (j in 0 until n) {
                if (j != i && a[j][i] != 0.0) {
                    for (k in i + 1..n) {
                        a[j][k] -= a[i][k] * a[j][i]
                    }
                }
            }

        }
        return a.map { it.last() }.toDoubleArray()
    }
}

@Suppress("MemberVisibilityCanBePrivate")
object Approximation {

    fun leastSquares(n: Int, f: (k: Int, x: Double) -> Double, x: DoubleArray, y: DoubleArray): DoubleArray {
        if (x.size != y.size) {
            throw IllegalArgumentException("X size and Y size should be equals, but x.size = ${x.size}, y.size = ${y.size}")
        }

        val mA = Array(n) { j ->
            DoubleArray(n) { k ->
                x.fold(.0) { s, xi -> s + f(j, xi) * f(k, xi) }
            }
        }

        val b = DoubleArray(n) { j ->
            x.indices.fold(.0) { s, i -> s + y[i] * f(j, x[i]) }
        }

        return LinearSystem.solve(mA, b)
    }

    fun leastSquaresFun(k: Int, x: DoubleArray, y: DoubleArray) = leastSquaresFun(k, { i, xi -> xi.pow(i) }, x, y)

    fun leastSquaresFun(
        k: Int,
        f: (k: Int, x: Double) -> Double,
        x: DoubleArray,
        y: DoubleArray
    ): (x: Double) -> Double {

        val avgX = x.average()
        val avgY = y.average()
        val rmsX = sqrt(x.map { it - avgX }.fold(.0) { s, z -> s + z * z })
        val rmsY = sqrt(y.map { it - avgY }.fold(.0) { s, z -> s + z * z })

        val a =
            leastSquares(
                k,
                f,
                x.map { (it - avgX) / rmsX }.toDoubleArray(),
                y.map { (it - avgY) / rmsY }.toDoubleArray()
            )
        return { xs ->
            val xss = (xs - avgX) / rmsX
            avgY + rmsY * a.mapIndexed { i, a -> a to i }.fold(.0) { s, (a, i) -> s + a * xss.pow(i) }
        }
    }

    private fun error(k: Int, f: (x: Double) -> Double, xs: DoubleArray, ys: DoubleArray): Double {
        val ssm = xs.indices.fold(.0) { s, i -> s + (ys[i] - f(xs[i])).pow(2) }
        return (ssm / (xs.size - k)).pow(.5)
    }

    fun leastSquaresFunOpt(x: DoubleArray, y: DoubleArray, kmax: Int = 100) =
        (0 until min(x.size, kmax))
            .map { it to leastSquaresFun(it, x, y) }
            .minBy { (k, f) -> error(k, f, x, y) }.second

    fun leastSquaresFunOpt1(x: DoubleArray, y: DoubleArray): (x: Double) -> Double {
        var f = leastSquaresFun(1, x, y)
        var e = error(1, f, x, y)
        for (i in (2 until x.size)) {
            val f1 = leastSquaresFun(i, x, y)
            val e1 = error(i, f1, x, y)
            if(e <= e1) {
               break
            }
            f = f1
            e = e1
        }

        return f
    }

    fun leastSquaresFunOpt(f: (k: Int, x: Double) -> Double, x: DoubleArray, y: DoubleArray, kmax: Int = 20) =
        (0 until min(kmax, x.size))
            .map { it to leastSquaresFun(it, f, x, y) }
            .minBy { (k, f) ->
                val ssm = x.indices.fold(.0) { s, i -> s + (y[i] - f(x[i])).pow(2) }
                ssm.pow(.5) / (x.size - k)
            }.second

}

object Fourier {
    // оставлю этот код, вдруг пригодится
    fun fft(y: DoubleArray): DoubleArray {
        val tmvl = DoubleArray(2 * y.size) {
            if (it % 2 == 0) {
                .0
            } else {
                y[(it - 1) / 2]
            }
        }

        var i = 1
        var j = 1
        var tmpr: Double
        var m: Int

        while (i < 2 * y.size) {
            if (j > i) {
                tmpr = tmvl[i]
                tmvl[i] = tmvl[j]
                tmvl[j] = tmpr
                tmpr = tmvl[i + 1]
                tmvl[i + 1] = tmvl[j + 1]
                tmvl[j + 1] = tmpr
            }
            i += 2
            m = y.size
            while (m in (2 until j)) {
                j -= m
                m /= 2
            }
            j += m
        }


        var mmax = 2

        while (mmax < 2 * y.size) {
            val theta = -2 * PI / mmax
            val wpi = sin(theta)
            val wtmp = sin(theta / 2)
            val wpr = 2 * wtmp.pow(2)
            val istp = 2 * mmax
            var wr = 1.0
            var wi = 0.0
            m = 1
            while (m < mmax) {
                i = m
                m += 2
                tmpr = wr
                var tmpi = wi
                wr = wr - tmpr * wpr - tmpi * wpi
                wi = wi + tmpr * wpi - tmpi * wpr
                while (i < 2 * y.size) {
                    j = i + mmax
                    tmpr = wr * tmvl[j] - wi * tmvl[j - 1]
                    tmpi = wi * tmvl[j] + wr * tmvl[j - 1]

                    tmvl[j] = tmvl[i] - tmpr
                    tmvl[j - 1] = tmvl[i - 1] - tmpi
                    tmvl[i] = tmvl[i] + tmpr
                    tmvl[i - 1] = tmvl[i - 1] + tmpi
                    i += istp
                }
            }
            mmax = istp
        }

        return DoubleArray(y.size) {
            2 * sqrt(tmvl[2 * it].pow(2) + tmvl[2 * it + 1].pow(2)) / y.size
        }
    }
}
