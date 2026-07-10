import math


def f(x):
    if x < 1:
        return 0
    sum = 0
    for i in range(1, x+1):
        if (i % 2 == 0 and x % i == 0):
            sum += 1
    return sum


t = int(input())
while (t > 0):
    n = int(input())
    print(int(f(math. factorial(n)) % (math.pow(10, 9)+7)))
    t -= 1
