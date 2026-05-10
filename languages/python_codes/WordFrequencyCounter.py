class Solution:
    def __init__(self, text):
        text = text.lower()
        text = text.replace('?', ' ')
        text = text.replace('!', ' ')
        list = text.split()
        print(list)
        dict = {}
        for word in list:
            print(word, dict.get(word, 0))
            k = dict.get(word, 0)
            dict[word] = k+1

        self.fmtText = text
        self.freqAll = dict
    def feqOf(self,word):
        return self.freqAll.get(word, 0)

text = "satyam satyam fksk ksdck?sdci cdj!sdci!dc"

s = Solution(text)
print(s.freqAll)
print(s.fmtText)
