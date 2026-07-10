#include <stdio.h>
#include <stdlib.h>


int main() {
    // main1();
    // main2();
    main3();
}
void main1() {
    int r = 3, c = 4, i, j, count;
    int *ptr = malloc((r * c) * sizeof(int));

    int *ar[r];
    for (i = 0; i < r; i++) ar[i] = (int *)malloc(c * sizeof(int));

    int(*arr)[r][c] = malloc(sizeof *arr);
    // int *ar[r][c]=malloc(sizeof *ar); //!wrong

    int **arr2 = (int **)malloc(r * sizeof(int *));
    for (i = 0; i < r; i++) arr2[i] = (int *)malloc(c * sizeof(int));

    count = 0;
    for (i = 0; i < r; i++)
        for (j = 0; j < c; j++) (*arr)[i][j] = ++count;

    for (i = 0; i < r; i++)
        for (j = 0; j < c; j++) printf("%d ", (*arr)[i][j]);

    free(arr);

    return 0;
}
void main2() {
    int r = 3, c = 4, len = 0;
    int *ptr, **arr;
    int count = 0, i, j;

    len = sizeof(int *) * r + sizeof(int) * c * r;
    arr = (int **)malloc(len);

    // ptr is now pointing to the first element in of 2D array
    ptr = (int *)(arr + r);

    // for loop to point rows pointer to appropriate location in 2D array
    for (i = 0; i < r; i++) arr[i] = (ptr + c * i);

    for (i = 0; i < r; i++)
        for (j = 0; j < c; j++) {
            arr[i][j] = ++count;  // OR
            *(*(arr + i) + j) = ++count;
        }

    for (i = 0; i < r; i++)
        for (j = 0; j < c; j++) printf("%d ", arr[i][j]);

    return 0;
}
void main3() {
    int row = 3, col = 4, i, j, count;
    // scanf("%d %d",&row,&col);

    int(*arr)[row][col] = malloc(sizeof *arr);  // pointer to a 2d array

    count = 0;
    for (i = 0; i < row; i++)
        for (j = 0; j < col; j++) {
            (*arr)[i][j] = ++count;
            *(*((*arr) + i) + j) = ++count;
        }

    for (i = 0; i < row; i++)
        for (j = 0; j < col; j++) printf("%d ", (*arr)[i][j]);

    free(arr);

    return 0;
}
