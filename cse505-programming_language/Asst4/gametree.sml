datatype 'a gametree = node of 'a * 'a gametree list
fun listmap f []  = []
    |listmap f (h::t)  = f(h) :: (listmap f t)
    
fun treemap f node(p,trl) = node(f(p),listmap (treemap f) trl)

fun prune 0 node(p,trl) = node(p,[])
    |prune x node(p,trl) = node(p,listmap (prune (x-1)) trl);