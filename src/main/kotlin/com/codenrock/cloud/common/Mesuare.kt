package com.codenrock.cloud.common

object Levenstein {
    sealed interface Op<T1, T2> {
        class Nop<T1, T2> : Op<T1, T2>
        data class Add<T1, T2>(val e: T2) : Op<T1, T2>
        data class Delete<T1, T2>(val e: T1) : Op<T1, T2>
        data class Replace<T1, T2>(val e1: T1, val e2: T2) : Op<T1, T2>
    }

    data class Result<T1, T2>(val ops: List<Op<T1, T2>>, val distance: Double)

    fun <T1, T2> instructions(
        s1: List<T1>,
        s2: List<T2>,
        equalsFun: (e1: T1, e2: T2) -> Boolean = { e1, e2 -> e1 == e2 },
        weighFun: (op: Op<T1, T2>) -> Double
    ): Result<T1, T2> {

        val l1 = s1.size + 1
        val l2 = s2.size + 1

        val mOps: Array<Array<Op<T1, T2>>> = Array(l1) { i1 ->
            Array(l2) { i2 ->
                when {
                    i1 == 0 && i2 == 0 -> Op.Nop()
                    i1 == 0 -> Op.Add(s2[i2 - 1])
                    i2 == 0 -> Op.Delete(s1[i1 - 1])
                    else -> Op.Nop()
                }
            }
        }

        val mDst = Array(l1) { i1 ->
            Array(l2) { i2 ->
                weighFun(mOps[i1][i2])
            }
        }

        (1 until l1).forEach { i1 ->
            (1 until l2).forEach { i2 ->
                val opAdd = Op.Add<T1, T2>(s2[i2 - 1])
                val opDelete = Op.Delete<T1, T2>(s1[i1 - 1])
                val opReplace = if (equalsFun(s1[i1 - 1], s2[i2 - 1])) Op.Nop() else Op.Replace(s1[i1 - 1], s2[i2 - 1])

                val wAdd = mDst[i1][i2 - 1] + weighFun(opAdd)
                val wDelete = mDst[i1 - 1][i2] + weighFun(opDelete)
                val wReplace = mDst[i1 - 1][i2 - 1] + weighFun(opReplace)

                if (wAdd < wDelete && wAdd < wReplace) {
                    mDst[i1][i2] = wAdd
                    mOps[i1][i2] = opAdd
                } else if (wDelete < wReplace) {
                    mDst[i1][i2] = wDelete
                    mOps[i1][i2] = opDelete
                } else {
                    mDst[i1][i2] = wReplace
                    mOps[i1][i2] = opReplace
                }
            }
        }

        val ops = mutableListOf<Op<T1, T2>>()
        var i1 = l1 - 1
        var i2 = l2 - 1

        while (i1 >= 0 && i2 >= 0) {
            val op = mOps[i1][i2]
            when (op) {
                is Op.Add -> i2 -= 1
                is Op.Delete -> i1 -= 1
                is Op.Nop -> {
                    i1 -= 1
                    i2 -= 1
                }

                is Op.Replace -> {
                    i1 -= 1
                    i2 -= 1
                }
            }
            ops.add(op)
        }

        return Result(ops.reversed(), mDst[l1 - 1][l2 - 1])
    }
}
