#include <stdio.h>

int main() {
    int num;
    int arr[10];
    int sum = 0;
    for(int i = 0;i<10;i++){
        scanf("Enter a number",num);
        sum += num;
    }

    printf("The sum is: %d", sum);
    return 0;
}