fun Int.test1() {}

fun test2() {
    1.test1()
}

// 1 INVOKESTATIC _1Kt.test1 \(I\)V
// 0 INVOKESTATIC _1Kt.test2 \(\)V
