

#include <stdio.h>
union un {
    int member1;
    char member2;
    float member3;
};
int main() {
    // Union is a user-defined data type in C language that can contain elements
    // of the different data types just like structure. But unlike structures,
    // all the members in the C union are stored in the same memory location.
    // Due to this, only one member can store data at the given instance.

    // defining a union variable
    union un var1;
    // Only one member can contain data at the same time.

    // initializing the union member
    var1.member1 = 15;
    var1.member2 = 'e';
    // var1.member3 = 5.6;
    printf("The value stored in member1 = %d,%d,%d", var1.member1, var1.member2,
           var1.member3);

    // int __func__ = 10; //error: expected identifier or '(' before '__func__'
    printf("In file:%s, function:%s() and line:%d", __FILE__, __func__,
           __LINE__);
    int n;
    scanf("%d", &n);
    if (n % 2) {
        goto odd;
    } else {
        goto even;
    }
even:
    printf("even");
    goto end;
odd:
    printf("odd");
    goto end;
end:
    return 0;
}
/*
goto label;  |    label:
.            |    .
.            |    .
.            |    .
label:       |    goto label;

*/

/*

When the return is used in main(), all the objects are destroyed whether they
are local or static. It is used to return the program control to the calling
function.

When the exit() is used, only the destructor of static objects is
called. It is used to terminate the current process.

*/