package main

import (
	"fmt"

	"golang.org/x/exp/constraints"
)
type CustomDataType interface{
    constraints.Ordered|[]rune|[]byte
}
type User[T CustomDataType] struct{
    name string
    id int
    data T
}
type Integer int
type Num interface{
    ~int|float64|int32  // ~ includes all such aliases
}
func Add[T Num](a T,b T)T{
    return a+b
}
// func Add[T constraints.Ordered](a T,b T)T{
//     return a+b
// }
func MapValues[T constraints.Ordered](values []T, MapFunc func(T)T)[]T{
    ret:=[]T{}
    for _,val:=range(values){
        // if err!=nil{
        //     return ret
        // }
        newVal:=MapFunc(val)
        ret=append(ret,newVal)
    }
    return ret
}
func main(){
    fmt.Println(Add(1,2))
    fmt.Println(Add(1.1,2.2))
    fmt.Println(Add(Integer(1),2))
    fmt.Println(MapValues([]float64{1.2,2.2,3.3},func(k float64)float64{
        return k*5
    }))
    u:=User[int]{
        id:0,
        name:"kk",
        data:1,
    }
    fmt.Println(u)
}