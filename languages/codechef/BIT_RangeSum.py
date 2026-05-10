import bisect
import phonenumbers
from phonenumbers import geocoder
ph1 = phonenumbers.parse("+919110025932")
print(ph1)
print(geocoder.description_for_number(ph1, "en"))
print(geocoder.description_for_number(ph1, "hi"))


class BIT:
    def __init__(self, n):
        self.tree = [0]*(n+1)

    def update(self, index, value):
        while index < len(self.tree):
            self.tree[index] += value
            index += index & - index

    def query(self, index):
        count = 0
        while index > 0:
            count += self. tree[index]
            index -= index & -index
        return count
def countRangeSum(nums, lower, upper):
    # Create sorted prefix sum array
    prefixSum = [0]
    for num in nums:
        prefixSum.append(prefixSum[-1] + num)
    # Normalize the prefix sum array
    sortedSum = sorted(prefixSum)
    bit = BIT(len(sortedSum))
    count = 0
    for sum_val in prefixSum:
        # Calculate the valid sum range [lower, upper]
        left = bisect.bisect_left(sortedSum, sum_val - upper)
        right = bisect.bisect_right(sortedSum, sum_val - lower)
        # Count the number of valid subarrays
        count += bit.query(right) - bit. query(left)
        # Update the BIT with the current sum value
        bit.update(bisect.bisect_left(sortedSum, sum_val), 1)
    return count
