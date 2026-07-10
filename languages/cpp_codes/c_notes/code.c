#include <stdio.h>
void print(int i){
    if(i>1){
        print(i/2);
        print(i/2);
    }
    cout<<"#";
}
int main() {
    int m[] = {7, 4, 3, 10, 20};
    int i, sum = 0, *n = m + 4;
    for (i = 0; i < 5; i++) {
        sum = sum + (*n - i) * *(n - i);
        printf("%d %d %d %d %d\n", sum, (*n - i), *(n - i), *n, i);
    }
    printf("%d\n", sum);
    return 0;
}