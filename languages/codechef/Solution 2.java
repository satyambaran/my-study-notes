
import java.io.*;
import java.util.*;

/*
 Instructions to candidate.
  1) Your task is ultimately to implement a function that takes in an array of non-negative numbers and an integer.
   You want to return the *LENGTH* of the shortest subarray whose sum is at least the integer,
   and -1 if no such sum exists.
  2) Run this code in the REPL to observe its behaviour. The
   execution entry point is main().
  3) Consider adding some additional tests in doTestsPass().
  4) Implement subArrayExceedsSum() correctly.
  5) If time permits, some possible follow-ups.
*/

public class Solution
{
  public static int subArrayExceedsSum(int oldArr[], int target )
  {
    // System.out.println(oldArr[3]);
    int start=0,end=0,len=oldArr.length, minLen = Integer.MAX_VALUE;
    int[] arr = new int[len+1];
    for(int i=1;i<=len;i++){
      arr[i]=oldArr[i-1]+arr[i-1];
    }
    // System.out.println(arr[3]);
    while(start<=len){
      if(end==len+1){
        if(arr[end-1]-arr[start]<=target){
          break;
        }else{
          // System.out.printf("%d %d %d %d\n",minLen, oldArr[end-1], oldArr[start], end-start-1);
          minLen = Math.min(minLen, end-start-1);
          start++;
        }
      }else if(arr[end]-arr[start]>target){
          // System.out.printf("%d %d %d %d\n",minLen,  oldArr[end-1], oldArr[start], end-start);
          minLen = Math.min(minLen, end-start);
          start++;
      }else{
        // System.out.printf("%d %d %d %d\n",minLen,  end==0?0:oldArr[end-1], oldArr[start], end-start);
        // System.out.printf("%d %d %d %d\n", start, end, arr[end], arr[start]);
        end++;
      }
    }
    // System.out.println(minLen);
    return minLen==Integer.MAX_VALUE ?-1:minLen;
  }

  /**
  * int doTestsPass()
  * Returns 1 if all tests pass. Otherwise returns 0.
  */
  public static void doTestsPass()
  {
  boolean result = true; 
  // int[] arr = { 1, 2, 3, 4 };
  int[] arr = { 1,4,45,6,0, 19 };
  /*
      1 3 6 10
      end=4
      start=0


      start=0,end=0, minLen
      while(start<len){
        if(arr[end]-arr[start]>target){
          minLen = min(minLen, end-start+1)
          start++
        }else{
          end++
        }
      }
      return minLen

      1 2 3 7
      1 3 6 13
      end=4
      start = 2
  
  
   */
  result = result && subArrayExceedsSum( arr, 51 ) == 3;
  // result = result && subArrayExceedsSum( arr, 6 ) == 2;
  // result = result && subArrayExceedsSum( arr, 12 ) == -1;

  if( result )
  {
    System.out.println("All tests pass\n");
  }
  else
  {
    System.out.println("There are test failures\n");
  }
  };

  /**
   * Execution entry point.
  */
  public static void main(String[] args)
  {
  doTestsPass();
  }
};
