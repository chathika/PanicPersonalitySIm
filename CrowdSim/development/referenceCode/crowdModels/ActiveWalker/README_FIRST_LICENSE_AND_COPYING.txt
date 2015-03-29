
LICENSE, COPYING

	 Please, read the non-profit and the 
	 commercial licenses in this directory.


CREDITS

        1. This software is a supplement to the article

             D. Helbing, P. Molnar, I. Farkas, and K. Bolay
                  Self-organizing pedestrian movement
   Environment and Planning B: Planning and Design 28 (3): 361 (2001)

        2. The software is based to a great extent upon ideas published in

             D. Helbing, J. Keltsch, and P. Molnar 
        Modelling the evolution of human trail systems
                  Nature 388, 47-50 (1997)
            


DOCUMENTATION

	0, the source code is in plain text format and
	   the files were compiled and run on various Linux systems 
	1, read the documented parameter list in the file sd.par
	2, sd.c is the main program
	3, sd_lib.c contains the procedures
	4, primary data output format is X11 images
	   suggested command: './sd < sd.in' or './sd 0 sd.par < sd.in'
	5, secondary output format: eps images
           suggested command: './sd 3 sd.par < sd.in'
           warning: eps files will be written into your current directory
           note: the color coding of grass values may be different from
                 that on the X11 display

