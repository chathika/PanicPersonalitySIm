/* sd

ROUGH DOC: self-driven particles 
  - continuous stochastic model of non-overlapping particles moving on a 
    two-dimensional board with non-periodic boundary conditions

DETAILED DOC: 
  - read sd_lib.c (starting at the top, proceeding towards the bottom):
    = global constants, parameters and variables
    = at the beginning of each definition
    = within the definitions 

RUN MODES:

      0 base
      1 test
      2 normal (for background number-crunching, creating data file)
      3 eps pictures (the empty field at the beginning with no
        particles should be drawn only once into an eps file)
*/

/******************************************************/

#include "sd_lib.c"

int main( int NArg, char * ArgStr[] ) {

  _E("Initializing, please wait... \n"); 
  Init(NArg,ArgStr); 
  _E("...finished.\n");

  switch(RunMode){
  case 0: {

	  do {
                  XPic();
	  	  Save();
		  Upd(); 
	  	  ReInit();

	  } while(    ( CycleNum < MaxCycleNum )
		   && ( RealTime < MaxRealTime )
		   && ( N < Nmax )   
		 ); 

	  Shutdown();
	  break;
  }
  case 1: {

	  do {
	          XPic();
		  Save();
		  Upd(); 
		  Test();
		  ReInit();

	  } while(    ( CycleNum < MaxCycleNum )
		   && ( RealTime < MaxRealTime )
		   && ( N < Nmax )   
		 ); 

	  Shutdown();
	  break;
  }
  case 2: {

	  do {
		  Save();
		  Upd(); 
		  ReInit();

	  } while(    ( CycleNum < MaxCycleNum )
		   && ( RealTime < MaxRealTime )
		   && ( N < Nmax )   
		 ); 

	  Shutdown();
	  break;
  }
  case 3: {

	  do {
	          XPic();

		  // drawing eps file:
		  // - check, if an eps file with no particles has
		  //   already been drawn
		  // - if yes, do not draw an eps file with no
		  //   particles on it
		  if(0==N){
		          if(0==DrawnEmptyEps){
			          MakeEpsPicFile();
				  DrawnEmptyEps=1;
			  }
		  }
		  else{
		          MakeEpsPicFile();
		  }
		  //MakeEpsPicFile(); 


		  Save();
		  Upd(); 
		  ReInit();

	  } while(    ( CycleNum < MaxCycleNum )
		   && ( RealTime < MaxRealTime )
		   && ( N < Nmax )   
		 ); 

	  Shutdown();
	  break;
  }
  default: {
          fprintf(stderr,"sd.c: No such RunMode: %d\n",RunMode);
	  _E("sd.c: Exiting to system.\n");
	  exit(-1);
  }
  }

}
