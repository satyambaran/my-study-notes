// package main

// import (
// 	"fmt"
// 	"math"

// 	"example.com/scope"
// )

//	func Min(nums ...int) int {
//		mn := nums[0]
//		for i := 1; i < len(nums); i++ {
//			if mn > nums[i] {
//				mn = nums[i]
//			}
//		}
//		return mn
//	}
//
//	func Max(nums ...int) int {
//		mx := nums[0]
//		for i := 1; i < len(nums); i++ {
//			if mx < nums[i] {
//				mx = nums[i]
//			}
//		}
//		return mx
//	}
//
//	func MinMax(nums ...int) (int, int) {
//		mn, mx := nums[0], nums[0]
//		for i := 1; i < len(nums); i++ {
//			if mn > nums[i] {
//				mn = nums[i]
//			} else if mx < nums[i] {
//				mx = nums[i]
//			}
//		}
//		return mn, mx
//	}
//
//	func maximumBeauty(nums []int, k int) int {
//		mn, mx := MinMax(nums...)
//		l := mx - mn + (2 * k) + 2
//		var freq []int
//		freq = make([]int, l)
//		for i := 0; i < len(nums); i++ {
//			freq[nums[i]-mn]++
//			freq[nums[i]-mn+k+k+1]--
//		}
//		return freq[0]
//	}
//
//	func main() {
//		scope.Test1()
//		var t int
//		fmt.Scan(&t)
//		for i := 0; i < t; i++ {
//			var n, k, a, ans int
//			var last, max, secondMax []int
//			fmt.Scan(&n, &k)
//			ans = math.MaxInt32
//			last = make([]int, k+1)
//			max = make([]int, k+1)
//			secondMax = make([]int, k+1)
//			for j := 1; j <= n; j++ {
//				fmt.Scan(&a)
//				if j-last[a] > max[a] {
//					secondMax[a] = max[a]
//					max[a] = j - last[a]
//				} else if j-last[a] > secondMax[a] {
//					secondMax[a] = j - last[a]
//				}
//				last[a] = j
//			}
//			n++
//			for a := 1; a <= k; a++ {
//				if n-last[a] >= max[a] {
//					secondMax[a] = max[a]
//					max[a] = n - last[a]
//				} else if n-last[a] > secondMax[a] {
//					secondMax[a] = n - last[a]
//				}
//			}
//			for a := 1; a <= k; a++ {
//				ansForA := maxm((1+max[a])/2, secondMax[a])
//				ans = minm(ans, ansForA)
//			}
//			fmt.Println(ans - 1)
//		}
//	}
//
//	func minm(ar ...int) int {
//		for i := 0; i < len(ar); i++ {
//			if ar[0] > ar[i] {
//				ar[0] = ar[i]
//			}
//		}
//		return ar[0]
//	}
//
//	func maxm(ar ...int) int {
//		for i := 0; i < len(ar); i++ {
//			if ar[0] < ar[i] {
//				ar[0] = ar[i]
//			}
//		}
//		return ar[0]
//	}
package main

import (
	"fmt"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	logger.Info("This is an info log")
	x := 10
	y := &x
	fmt.Println(y)
	*y = *y + 10
	fmt.Println(y)

	var a, b, c byte
	fmt.Scan(&a)
	fmt.Scan(&b)
	c = a + b
	go func() {
	}()
	fmt.Println(a, b, c)

	arr := [5]int{1, 2, 3, 4, 5}
	slice1 := arr[1:4]          // Slice of arr
	slice2 := append(slice1, 6) // May create a new array if capacity is exceeded

	slice1[0] = 10                 // Affects arr as well, since it’s the same underlying array
	fmt.Println("Array:", arr)     // Output: [1, 10, 3, 4, 5]
	fmt.Println("Slice1:", slice1) // Output: [10, 3, 4]
	fmt.Println("Slice2:", slice2) // Output: [10, 3, 4, 6]

}

/*
	Modifying a slice only affects the underlying array (and other slices) until a reallocation occurs. If a slice outgrows its capacity, Go allocates a new array and detaches it from the original array.
	Thus, arrays and slices are tightly coupled, with slices acting as flexible views over arrays, while also providing a way to work with arrays without exposing their fixed size limitation.
*/
