#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

int main(void){
int r=open("/home/trinity/Desktop/README",O_RDONLY);

return (r);
}



