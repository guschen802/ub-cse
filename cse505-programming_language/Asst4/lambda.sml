datatype term = V of string | L of string * term | A of term * term;
fun show(V(x)) = x
    |show(L(x,y)) = "L"^x^"."^show(y)
    |show(A(x,y)) = "("^show(x)^" "^show(y)^")"
fun matchV(x,y,[]) = x=y
    |matchV(x,y,(h1,h2)::t) = if h1 =x andalso h2 = y
                              then true
                              else if h1<>x andalso h2<>y
                                   then matchV(x,y,t)
                                   else false
fun alpha2(V(x),V(y),l) =matchV(x,y,l)
    |alpha2(L(x,a),L(y,b),l) = let val l=(x,y)::l
                               in alpha2(a,b,l)
                               end
    |alpha2(A(x,y),A(a,b),l) = if alpha2(x,a,l)
                               then alpha2(y,b,l)
                               else false
    |alpha2(_)=false
fun alpha(x,y) = alpha2(x,y,[]);
    
