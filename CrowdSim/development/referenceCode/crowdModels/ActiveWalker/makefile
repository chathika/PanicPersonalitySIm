INCFLAGS=-Ilib -I.
CFLAGS=-O3 $(INCFLAGS)

SRCS            = sd.c 
OBJS            = $(SRCS:.c=.o)
CC = gcc 
all: sd


sd: $(OBJS)
	gcc $(OBJS) -lm -L/usr/X11R6/lib/ -lX11 -o sd

w: $(OBJS)
	gcc $(OBJS) -Wall -lm -L/usr/X11R6/lib/ -lX11 -o sd

clean:
	rm -f $(OBJS) 
