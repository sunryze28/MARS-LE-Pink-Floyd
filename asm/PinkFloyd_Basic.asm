nick $t0, $zero, 15
run $t1, $t0
rick $t3, $t1, $t2

diff $t2, $t1, $t1

nick $sp, $sp, -4
sit $t3, 0($sp)
nick $sp, $sp, -4
stay $t4, 0($sp)

wot $t4, 4($sp)

nick $v0, $zero, 10

fly End

diff $v0, $v0, $t4

End:
echoes