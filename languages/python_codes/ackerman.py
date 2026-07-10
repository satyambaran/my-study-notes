def ackermann(m, n, memo={}):
    # Check if the result is already in the memo dictionary
    if (m, n) in memo:
        return memo[(m, n)]

    # Base cases
    if m == 0:
        result = n + 1
    elif n == 0:
        result = ackermann(m - 1, 1, memo)
    else:
        result = ackermann(m - 1, ackermann(m, n - 1, memo), memo)

    # Store the result in the memo dictionary
    memo[(m, n)] = result
    return result


# Example usage:
for i in range(10):  # Limit the range due to exponential growth
    for j in range(10):
        print(f"A({i}, {j}) = ")
        print(f"{ackermann(i, j)}")
