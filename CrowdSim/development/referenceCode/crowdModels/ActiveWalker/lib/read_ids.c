#include <stdio.h>
#include <string.h>
#define READ_IDS_EXIT {fprintf(stderr,"read_ids.c: Exiting to system.\n");fflush(stderr);exit(-1);}



void Read_IDS ( char *sw, char *ifn, 
		int *iva[], char *ina[], int inu, 
		double *dva[], char *dna[], int dnu,
		char *sva[], char *sna[], int snu, 
		char *lfn, int log_num ){

  /* sw: switch = "start" or "re" 
     ifn: input file name 
     integer/double/string parameters' value/name/number array 
     -- each array starting with the 0. element 
     lfn: log file name
     lnum: id (number) of log message */

  

  FILE *ifp,*lfp;
  int i,j,k,*ich,*dch,*sch,ach; 
  char tmps[100], tmpsva[100];  
  double d;
  /* ich/dch/sch/ach = integer/double/string/any changed */


  /* malloc */
  ich = ivector(0,inu-1);
  dch = ivector(0,dnu-1);
  sch = ivector(0,snu-1);




  /* open file & init */
  if( !(ifp = fopen(ifn, "r")) ) {
          fprintf(stderr,"read_ids.c: Couldn't open \"%s\" for reading\n",ifn);
	  READ_IDS_EXIT;
  }

  if(strcmp(sw,"start")==0) {
	  for(i=0; i<inu; i++) { ich[i] = 1; }
	  for(i=0; i<dnu; i++) { dch[i] = 1; }
	  for(i=0; i<snu; i++) { sch[i] = 1; }
	  ach = 1;
  }
  else /* i.e. if(strcmp(sw,"re")==0) */ { 
	  for(i=0; i<inu; i++) { ich[i] = 0; }
	  for(i=0; i<dnu; i++) { dch[i] = 0; }
	  for(i=0; i<snu; i++) { sch[i] = 0; }
	  /*	  for(i=0; i<snu; i++) { sch[i] = 1; } */
	  ach = 0;
  }

  /* reading integer parameters */
  for(i=0; i<inu; i++) {
          while(getc(ifp)!=0x23) {};   /* 0x23 = '#' */
          fscanf(ifp, "%d %s %d", &k, tmps, &j);
	  if(    (strcmp(sw,"start")==0) 
              || ( (strcmp(sw,"re")==0) && (k==1) ) 
	    ) {
	          if(strcmp(tmps,ina[i])!=0) {
		    fprintf(stderr,"read_ids.c: Name of %d. integer:\n",i+1);
		    fprintf(stderr,"read_ids.c: wrong: %s , correct: %s .\n", tmps, ina[i]);
		    READ_IDS_EXIT;
		  }
		  else { 
		          if(strcmp(sw,"start")==0) {
			          *iva[i] = j;
			  }
			  if((strcmp(sw,"re")==0) && (j != *iva[i])) { 
			          *iva[i] = j;
				  ich[i] = 1;
				  ach = 1;
			  }
		  }
	  }
  }

  /* reading double parameters */
  for(i=0; i<dnu; i++) {
          while(getc(ifp)!=0x23) {};   /* 0x23 = '#' */
          fscanf(ifp, "%d %s %lf", &k, tmps, &d);
	  if(    (strcmp(sw,"start")==0) 
	      || ( (strcmp(sw,"re")==0) && (k==1) ) 
	    ) {
	          if(strcmp(tmps,dna[i])!=0) {
		    fprintf(stderr,"read_ids.c: Name of %d. double:\n",i+1);
		    fprintf(stderr,"read_ids.c: wrong: %s , correct: %s .\n", tmps, dna[i]);
		    READ_IDS_EXIT;
		  }
		  else {
		          if(strcmp(sw,"start")==0) {
			          *dva[i] = d;
			  }
			  if((strcmp(sw,"re")==0) && (d != *dva[i])) {
			          *dva[i] = d;
				  dch[i] = 1;
				  ach = 1;
		    }
		  }
	  }
  }

  /* reading string parameters */
  for(i=0; i<snu; i++) {
          while(getc(ifp)!=0x23) {};   /* 0x23 = '#' */
          fscanf(ifp, "%d %s %s", &k, tmps, tmpsva);
	  if(    (strcmp(sw,"start")==0) 
              || ( (strcmp(sw,"re")==0) && (k==1) ) 
	     ) {
	          if(strcmp(tmps,sna[i])!=0) {
		    fprintf(stderr,"read_ids.c: Name of %d. string:\n",i+1);
		    fprintf(stderr,"read_ids.c: wrong: %s , correct: %s .\n", tmps, sna[i]);
		    READ_IDS_EXIT;
		  }
		  else { 
		          if(strcmp(sw,"start")==0){
		                  strcpy(sva[i],tmpsva);
			  }
			  else if(  (strcmp(sw,"re")==0)
                                  &&(strcmp(sva[i],tmpsva)!=0)
				 ){
			            strcpy(sva[i],tmpsva);
				    sch[i] = 1;
				    ach = 1;
			  }
		  }
	  }
	  
  }
  



  /* closing input file, opening log file */
  fclose(ifp);
  


  /* changes to log file */
  // temporarily off
  /*
  if(ach==1) {
          if(strcmp(sw,"start")==0) {
	          if( !(lfp = fopen(lfn, "w")) ) {
		    fprintf(stderr,"read_ids.c: Couldn't open \"%s\" for: truncating to 0 + writing.\n",lfn);
		    READ_IDS_EXIT;
		  }
  
		  fprintf( lfp, "Parameter file used: %s .\n*\n", ifn );
	  }
	  else { // ie. if(strcmp(sw,"re")==0) 
	          if( !(lfp = fopen(lfn, "a")) ) {
		    fprintf(stderr,"read_ids.c: Couldn't open \"%s\" for: appending.\n",lfn);
		    READ_IDS_EXIT;
		  }
		  fprintf( lfp, "*\nParameters changed at %i .\n*\n",log_num);
	  }

	  for(i=0; i<inu; i++) { 
	          if(ich[i]==1) { 
		          fprintf(lfp, "%s %d\n", ina[i], *iva[i]); 
		  }
	  }	  
	  for(i=0; i<dnu; i++) { 
	          if(dch[i]==1) { 
		          fprintf(lfp, "%s %g\n", dna[i], *dva[i]); 
		  }
	  }	  
	  for(i=0; i<snu; i++) { 
	          if(sch[i]==1) { 
		          fprintf(lfp, "%s %s\n", sna[i], sva[i]);
		  }	  
	  }


	  
	  fprintf(lfp,"*\n");
	  fflush(lfp);
	  fclose(lfp);
  }
  */



  /* free allocated memory */
  free_ivector(ich,0,inu-1);
  free_ivector(dch,0,dnu-1);
  free_ivector(sch,0,snu-1);



}

