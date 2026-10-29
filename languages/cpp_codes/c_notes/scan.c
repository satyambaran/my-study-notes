#include <stdio.h>
int main(void)
{
    char str[128];
    printf("Enter a string: ");
    // scanf("%[A-Z]s", str);
    // // AsD :-> will only print A
    // printf("You entered1: %s\n", str);
    // scanf("%[^\n]s", str);
    // printf("You entered2: %s\n", str);
    // scanf("%[^m]s", str); // will wait for an 'm' forever
    // printf("You entered3: %s\n", str);
    gets(str); // it's unbounded can be dangerous sometimes
    fgets(str,456,stdin);
    printf("You entered4: %s\n", str);
    fgets(str,456,stdin);
    printf("You entered5: %s\n", str);

    // main difference between fgets() function and gets() function is that fgets() function allows the user to specify the maximum number of characters 

    return 0;
}
// #include<stdio.h>
// int main(){

// }
// void main2(){
//     int var = 10;
//     int * ptr = &var;
//     printf("Value at ptr = %p \n", ptr);
//     printf("Value at var = %d \n", var);
//     printf("Value at *ptr = %d \n", *ptr);
// }