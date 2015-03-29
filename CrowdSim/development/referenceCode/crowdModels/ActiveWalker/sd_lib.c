/* sd_lib.c */

/***********************  global constants  ********************/  

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <sys/stat.h>
#include <unistd.h>

#include "nrutil.c"
#include "base.c"
#include "Xlibext.c" 
#include "read_ids.c"
#include "Epslib.c"

#define SD_LIB_EXIT {_E("sd_lib.c: Exiting to system.\n");exit(-1);}
#define SD_STRLEN 200

/* default RunMode and input file name */
int RunMode_DEFAULT = 0;
char *IFN_DEFAULT = "sd.par";

#ifndef EPSILON 
#define EPSILON 1.0e-5
#endif

/* maximum number of tries for inserting a particle 
   (see InsertParticles) */ 
#define INSERT_NMAXTRY 100

/*********  global parameters  ******  description in sd.par **********/


/* I. parameters from parameter file */

static int EMType, DrawMode, DrawCNFreq, SaveCNFreq, Margin, InFW,
  InFH, XLineHeight, GrFH, Sleep, 
  EpsDataFile,EpsXSize, EpsYSize, EpsMargin, EpsSleep,
  RandomSeed, MaxCycleNum, Nmax;
static double FieldXSize, FieldYSize, GG, DMean, DPlusMinus, Tau, V0, Vmax,
  B_eta, T_G, G_min, G_max, SiF, I, C_V, Sigma, WR, GrCut, R, A1, A2, C_Back,
  C_Front, GaussMean, GaussTheta, GaussCutMult, SaveRTFreq,
  DrawRTFreq, ObjMag, PicMag, MaxRealTime, DeltaT, H, H2, C_Euler, C_NS;  
static char BgCol[SD_STRLEN], InfoCol[SD_STRLEN],
  XFontName[SD_STRLEN], OFN[SD_STRLEN], LOFN[SD_STRLEN],
  MFN[SD_STRLEN]; 


int *IPar[] = {&EMType, &DrawMode, &DrawCNFreq, &SaveCNFreq, &Margin,
	       &InFW, &InFH, &XLineHeight, &GrFH, &Sleep, 
	       &EpsDataFile,&EpsXSize,&EpsYSize,&EpsMargin,&EpsSleep,
	       &RandomSeed, &MaxCycleNum, &Nmax}; 
char *IParName[] = {"EMType", "DrawMode", "DrawCNFreq", "SaveCNFreq",
		    "Margin", "InFW", "InFH", "XLineHeight", "GrFH",
		    "Sleep", 
		    "EpsDataFile","EpsXSize","EpsYSize","EpsMargin","EpsSleep", 
		    "RandomSeed", "MaxCycleNum", "Nmax"}; 
double *DPar[] = {&FieldXSize, &FieldYSize, &GG, &DMean, &DPlusMinus,
		  &Tau, &V0, &Vmax, &B_eta, &T_G, &G_min, &G_max,
		  &SiF, &I, &C_V, &Sigma,
		  &WR, &GrCut, &R, &A1, &A2, &C_Back, &C_Front, &GaussMean,
		  &GaussTheta, &GaussCutMult, &SaveRTFreq,
		  &DrawRTFreq, &ObjMag, &PicMag, &MaxRealTime,
		  &DeltaT, &H, &H2, &C_Euler, &C_NS};   
char *DParName[] = {"FieldXSize", "FieldYSize", "GG", "DMean",
		    "DPlusMinus", "Tau", "V0", "Vmax", "B_eta", "T_G",
		    "G_min", "G_max", "SiF", "I", "C_V", "Sigma",
		    "WR", "GrCut", "R", "A1",
		    "A2", "C_Back", "C_Front", "GaussMean",
		    "GaussTheta", "GaussCutMult", "SaveRTFreq",
		    "DrawRTFreq", "ObjMag", "PicMag", "MaxRealTime",
		    "DeltaT", "H", "H2", "C_Euler", "C_NS"};   
char *SPar[] = {BgCol, InfoCol, XFontName, OFN, LOFN, MFN};
char *SParName[] = {"BgCol", "InfoCol", "XFontName", "OFN", "LOFN", "MFN"};

int IParNum = sizeof(IPar)/sizeof(int*),
    DParNum = sizeof(DPar)/sizeof(double*),
    SParNum = sizeof(SPar)/sizeof(char*);



/* II.1. constants for parameters from stdin */

#define NPmax 20 /* maximum number of points */
double PRmax = 20.0; /* maximum radius of circles around points */
#define NFmax 20 /* maximum number of fluxes */
#define NPFmax 20 /* maximum number of points one flux is allowed to
		     include */
#define NGrColmax 257 /* maximum number of grass colors */
 

/* II.2. parameters from stdin */

int NP, NF, *NPF, **F, NGrCol;
double *PX, *PY, *PR, *F_Int, *F_Int_delta;
char PCol[NPmax][SD_STRLEN], GrColName[NGrColmax][SD_STRLEN];

/************************  global variables  ***************************/

int RunMode, CycleNum, N, BX, BY, B, *BIndBd, *BInd, GaussFlag,
  WinWidth, WinHeight, GrFW, GrFUp, BgColCode, InfoColCode,
  *GrColCode, *PColCode,
  iRV, iRGM, iRFS, iREGM, GBX, GBY, GB, *F_id, *F_nt, DrawnEmptyEps=0;
double XS, YS, EdO, RealTime, RealTime_prev, *D, *Theta, *X,
  *Xprev, *Y, *Yprev, *VX, *VY, *VXNew, *VYNew, /* *Graph, */ GaussSet1, GaussSet2,
  *PX, *PY, *PR, **VMask, *G, *Dest_Xoffset, *Dest_Yoffset,
  *LastInsert;

char IFN[SD_STRLEN];
FILE *OFP,*LOFP,*MFP; 
struct stat IFStatBuf;
long int IFModTime;

XColor 
  GrCol_sdef, NoGrCol_sdef, PCol_sdef[NPmax];


/* XS,YS:     the board is: [0,XS) x [0,YS) 
   EdO        (=Edge Offset)
              width of the strip at the left/right/upper/lower edge of the field 
   N          number of particles on board 
   RunMode, CycleNum
              used by "main" in sd.c

   IFStatBuf  input file status buffer
   IFModTime  modification time of input file
   WinWidth   window width
   WinHeight  window height
   GrFUp      upper edge of graph field
   GrFW       width of graph field
   *ColCod    color codes
   *Mask      mask for evaluation of functions
   iR*        integer radius for evaluation of functions (by masks)
              V: potential
	      GM: grass mean
	      FS: foot size
   G[i]       is the grass value in the ith grass cell	     
   F_id[i]    (flux identification number) is the index of the flux
              particle i belongs to 
   F_nt[i]    (flux number of target) the index of the current target
              point of particle i is given by F[F_id[i]][F_nt[i]] 
	      ie. the (F_nt[i])th point along the path defined by the
	      (F_id[i])th flux
   Dest_(X/Y)Offset
              particles want to move towards a point near to
	      ( PX[ F[F_id[i]][F_nt[i]] ], PY[ F[F_id[i]][F_nt[i]] ]
	      ),
	      this is the difference vector
   LastInsert[i]
              the last particle inserted for flux i was inserted at LastInsert[i]

   *sdef:     screen definitions of colors,
              GrCol: grass color
	      NoGrCol: no grass 
	      PCol: point (target point) color

   DrawnEmptyEps
              whether the empty image with no particles has already
              been drawn into an eps file, default value is 0



BOOK-KEEPING OF PARTICLES:
              the board will be divided into BX*BY blocks (all have the 
	      same size), however, these blocks will be indexed with the 
	      numbers 0, 1, ... , B^2 - 1, where B = max(BX,BY)
	      --> this creates a grid on the board, the block indices of 
	          particles are stored in the BIndBd & BInd arrays
	      
	      the size of a block should be: not less than R, i.e.
	      BX = (int)floor(XS/R), and so R <= XS/BX
	     
   BIndBd[n] (Block + Index + Board) 
             = 0,1,...,N-1: the index of the nth block's first particle
	     = -1: there's no particle in the nth block
   BInd[i]   = 0,1,...,N-1: the next particle in the block of particle i
             = -1: there are no more particles in this block

   think of this book-keeping structure as chains of particles 
   attached to the fields of a board (hooks)      

   
   ADDING a new particle: 
   . adding the (N+1)th particle; 
   . N++
   REMOVING a particle (when it leaves the board): 			  
   . inserting the Nth into its place 
   . N--


POINTS:
   P* variables (see: end of sd.par)


GRASS:
  similarly to particles, using GBX/GBY/GB for indexing the cells  


UPDATING: -> update rules in the procedure Upd 
   D[i]       diameter of particle i
   Theta[i]   preferred direction of particle i
   X[i]       coordinate of particle i
   Xprev[i]   previous coordinate (before last update)
   VX[i]      current velocity component of particle i
   VXNew[i]   new velocity component
   IT[i]      the value of int(exp(cos(x))) at x = i*PI/ITS	      
   GaussFlag, GaussSet1 and GaussSet2  
              are used by the Gaussian random number generator 

RESULTS: 
   RealTime     */

/**********************   prototypes   **********************/

double Dir( int i, double fgx, double fgy );
void InsertParticles( int *n, int nplus, int f_id );
void RemoveParticle( int *n, int i );


void Init( int narg, char *argstr[] );
void Init4( int narg, char *argstr[] );
void ReInit();
void Upd();
void Upd1();
void Upd2();
void Save();
void Test();
void Shutdown();


double GaussRand( double gaussmean, double gausstheta, 
		  double gausscutfact );
double VnP( double x1, double y1, double x2, double y2,
	    double vx1, double vy1, double vx2, double vy2 );
void Fp( double *fx, double *fy, double x1, double y1, double x2,
	 double y2, double theta, double hc );
void EulTStep( double *tstep, double d, double v_normal, double hc );


void XPic();
void XPicNormal();
void XDrawParticle( int leftxmargin, int upymargin, 
		    double pm, double om, double x, double y,
 		    double vx, double vy, double d, int i );

//void MakeEpsDataFile();
void MakeEpsPicFile();

/**********************  definitions  **************************/

double Dir( int i, double fgx, double fgy ){
    /* preferred direction of motion for particle i (in radians)
       determined by the direction of the target and the grass force
       (fgx,fgy): grass force
    */

  int j;
  double ex,ey,esqr,scprod_ef;


  /* computing unit vector pointing to target */
  j = F[F_id[i]][F_nt[i]];
  ex = PX[j]+Dest_Xoffset[i]-X[i];
  ey = PY[j]+Dest_Yoffset[i]-Y[i];
  esqr=SQR(ex)+SQR(ey);
  if(esqr!=0.0){ ex/=sqrt(esqr); ey/=sqrt(esqr); }

  /* normalizing grass force, if needed */
  scprod_ef=ex*fgx+ey*fgy;
  if(scprod_ef<-GrCut){
          fgx+=ex*(-scprod_ef-GrCut);
	  fgy+=ey*(-scprod_ef-GrCut);
  }

  /* return value */
  if(ABS(ex+fgx)+ABS(ey+fgy)!=0.0) { return atan2(ey+fgy,ex+fgx); }
  else { return 2.0*PI*rand()/(RAND_MAX+1.0); }

}

/*----------------------------------------------*/

void InsertParticles( int *n, int nplus, int f_id ){

  /* *n: # of particles now (until now: 0, ... ,(*n)-1, 
         next new particle: *n)
     nplus: # of particles to be inserted (preferably <= 1)
     f_id: index (identification number) of flux this particle belongs
           to */

  int ii,j,itmp,g,gx,gy,m,mx,my,source,dest,ins_try_count;
  double x,y,fgx,fgy;


  /* (a) ALL particles ("ALL" = all new particles): 
         chosing diameters and coordinates
	 (every particle is inserted within the circle of its starting
	 point)
     (b) ALL particles:
         setting other parameters and inserting particles into
	 the book-keeping arrays
	 chosing initial velocity
     (c) increasing particle number by nplus */ 


  source = F[f_id][0];
  dest = F[f_id][1];
  ins_try_count = 0; 



  /* a */

  for(ii=0;ii<nplus;ii++){
    
          D[*n+ii] = DMean + DPlusMinus * ( 1.0 - 2.0*rand()/(RAND_MAX+1.0) );
	  do{
	          x =   (PR[source]-0.5*D[*n+ii]) 
		      * ( 1.0-2.0*rand()/(RAND_MAX+1.0) );
		  y =   (PR[source]-0.5*D[*n+ii]) 
		      * ( 1.0-2.0*rand()/(RAND_MAX+1.0) );
	  }while( SQR(x) + SQR(y) >= SQR(PR[source]-0.5*D[*n+ii]) );
	  X[*n+ii] = PX[source] + x;
	  Y[*n+ii] = PY[source] + y;

	  itmp = ii;
	  for(j=0;j<*n+itmp;j++){ 
	    if(   SQR(X[j]-X[*n+ii])+SQR(Y[j]-Y[*n+ii])
		< SQR(H*0.5*(D[j]+D[*n+ii]))
	      ){
	            ins_try_count++;
	            ii--;
		    j = *n + itmp - 1;
	    }
	  }

	  if(ins_try_count>=INSERT_NMAXTRY*nplus){ return; }
  }



  /* b */

  for(ii=0;ii<nplus;ii++){
    
          F_id[*n+ii] = f_id; 
	  F_nt[*n+ii] = 1;

	  do{
	          x =   (PR[dest]-0.5*D[*n+ii]) 
		      * ( 1.0-2.0*rand()/(RAND_MAX+1.0) );
		  y =   (PR[dest]-0.5*D[*n+ii]) 
		      * ( 1.0-2.0*rand()/(RAND_MAX+1.0) );
	  }while( SQR(x) + SQR(y) >= SQR(PR[dest]-0.5*D[*n+ii]) );
	  Dest_Xoffset[*n+ii] = x;
	  Dest_Yoffset[*n+ii] = y;

	  Xprev[*n+ii] = X[*n+ii];
	  Yprev[*n+ii] = Y[*n+ii];


	  /* inserting particle into the book-keeping */
	  j = (int)floor(X[*n+ii]*BX/XS) + B * (int)floor(Y[*n+ii]*BY/YS);
	  if(BIndBd[j]==-1) {
	          BIndBd[j] = *n+ii;
		  BInd[*n+ii] = -1;
	  }
	  else {
	          j = BIndBd[j];
		  while(BInd[j]!=-1) {
		          j = BInd[j];
		  }
		  BInd[j] = *n+ii;
		  BInd[*n+ii] = -1;
	  }


	  /* chosing (initial) velocity: 
	     pointing towards the target point */
	  j = dest;
	  Theta[*n+ii] = atan2( PY[j]+Dest_Yoffset[*n+ii]-Y[*n+ii], 
			        PX[j]+Dest_Xoffset[*n+ii]-X[*n+ii]
			      ); 
	  
	  switch(EMType){
	  case 1: default: {

	          VX[*n+ii] = V0 * cos(Theta[*n+ii]); 
		  VY[*n+ii] = V0 * sin(Theta[*n+ii]); 
		  break;
	  }
	  case 2: {
	    
	    /*
	          VX[*n+ii] = V0 * cos(Theta[*n+ii]);
		  VY[*n+ii] = V0 * sin(Theta[*n+ii]);
	    */
	          VXNew[*n+ii] = V0 * cos(Theta[*n+ii]);
		  VYNew[*n+ii] = V0 * sin(Theta[*n+ii]);
		  break;
	  }
	  }
  }



  /* c */
  *n += nplus;
}

/*----------------------------------------------*/

void RemoveParticle( int *n, int i ) {
  /* *n: # of particles now
     i: index of particle to be removed */

  int j;
  

  /* (a) particle i 
     (which is off-board or has reached its type 0 destination point)
     is removed from the block it belonged to with its previous
     (=valid=on-board) coordinates;
     block determined by the previous (Xprev[i],Yprev[i]) coordinates 
     (b) if i != *n-1 
         (b1) particle *n - 1 is removed from the book-keeping
	      block determined by (Xprev[*n-1],Yprev[*n-1])
         (b2) copying all values of particle *n-1 into i's place
         (b3) inserting particle i (that used to be indexed *n-1) into the
              book-keeping, into the block given by (Xprev[i],Yprev[i])
	      [these were the Xprev[*n-1] and Yprev[*n-1] values before the
	       substitution]
     (c) decreasing particle number  ( *n to *n - 1 ) */


  /* a */
  j = (int)floor(Xprev[i]*BX/XS) + B*(int)floor(Yprev[i]*BY/YS);
  if(BIndBd[j]==i) {
          BIndBd[j] = BInd[i];
  }
  else {
          j = BIndBd[j];
	  while(BInd[j]!=i) {
	          j = BInd[j];
	  }
	  BInd[j] = BInd[i];
  }  




  /* b */
  if(i!=*n-1){

          /* b1 */
    
          j = (int)floor(Xprev[*n-1]*BX/XS) + B*(int)floor(Yprev[*n-1]*BY/YS);
          if(BIndBd[j]==*n-1) {
	          BIndBd[j] = BInd[*n-1];
	  }
	  else {
	          j = BIndBd[j];
	          while(BInd[j]!=*n-1) {
		          j = BInd[j];
		  }
		  BInd[j] = BInd[*n-1];
	  }  



          /* b2 */
          D[i] = D[*n-1];
	  Theta[i] = Theta[*n-1];
	  F_id[i] = F_id[*n-1];
	  F_nt[i] = F_nt[*n-1];
	  X[i] = X[*n-1];
	  Y[i] = Y[*n-1];
	  VX[i] = VX[*n-1];
	  VY[i] = VY[*n-1];
	  Xprev[i] = Xprev[*n-1];
	  Yprev[i] = Yprev[*n-1];
	  Dest_Xoffset[i] = Dest_Xoffset[*n-1];
	  Dest_Yoffset[i] = Dest_Yoffset[*n-1];

	  if(EMType==2){
	          VXNew[i] = VXNew[*n-1];
		  VYNew[i] = VYNew[*n-1];
	  }


	  /* b3 */
	  j = (int)floor(Xprev[i]*BX/XS) + B*(int)floor(Yprev[i]*BY/YS);
	  if(BIndBd[j]==-1) {
	          BIndBd[j] = i;
		  BInd[i] = -1;
	  }
	  else {
	          j = BIndBd[j];
		  while(BInd[j]!=-1) {
		          j = BInd[j];
		  }
		  BInd[j] = i;
		  BInd[i] = -1;
	  }
  }




  /* c */
  (*n)--;
}

/*=========================================================*/

void Init( int narg, char *argstr[] ) {

  /* 1 reading global parameters, checking them, initializing some
       global variables, malloc to arrays needed in all run modes 
       opening output file and long output file
     
     2 initializing the point and particle-related book-keeping
       structures, masks and grass values

     3 X init, malloc to special arrays etc. */


  int i,j,k,l,m,mx,my,g,gx,gy;
  XColor sdef,edef;
  double dum_x,dum_y,dum_r,sow,rsqr;
  


  /* 0 */

  /* USAGE */
  switch(narg){
  case 1: { 
	  fprintf(stderr,"(using default: RunMode = %d, input file name = %s)\n",RunMode_DEFAULT,IFN_DEFAULT);fflush(stderr);
          RunMode = RunMode_DEFAULT; 
	  strcpy(IFN,IFN_DEFAULT);
	  break; 
  }
  case 3: { 
          RunMode = atoi(argstr[1]); 
	  strcpy(IFN,argstr[2]);
	  break; 
  }
  default: {
          _E("Error: usage. See under the sign \"USAGE\" in sd_lib.c .\n");
	  SD_LIB_EXIT;
	  break;
  }}



  /* 1 */

  /* 1.1. reading parameters from parameter file */
  Read_IDS ( "start", IFN, IPar, IParName, IParNum, 
	     DPar, DParName, DParNum, SPar, SParName, SParNum,
	     MFN, 0 ); 

  /* RANGE: accepted values are shown within brackets */
  /* checking them */
  if(    !( Vmax*DeltaT < R - (DMean+DPlusMinus) )
      || !( DMean+DPlusMinus < R ) 
      || !( DPlusMinus < DMean )
      || !( DPlusMinus >= 0.0 )	 
      || !( C_Euler > 0.0 )	 
      || !( C_Back > 0.0 )
      || !( C_Front > 0.0 )
      || !( H > 1.0 ) 	          
    ) { 
          _E("sd_lib.c: Error: parameter values out of range.\n");
	  _E("sd_lib.c: Correct values: see below the sign \"RANGE\" in sd_lib.c .\n");
	  SD_LIB_EXIT;
  }



  /* 1.2. reading parameters from stdin */

  /* EdO has to be initialized here */
  EdO = MAX( H*(DMean+DPlusMinus), 2.0*GG );
  EdO = MAX( EdO, PRmax );

  /* grass colors */
  fprintf(stderr,"Number of grass colors ( <= %d ):\n",NGrColmax);
  fscanf(stdin,"%d",&NGrCol);
  for(i=0;i<NGrCol;i++){
          fprintf(stderr,"-> name of %d. grass color:\n",i); 
	  fscanf(stdin,"%s",GrColName[i]);
  }

  /* points */
  fprintf(stderr,"Number of points ( >= 2 , <= %d ):\n",NPmax);
  fscanf(stdin,"%d",&NP);
  PX=dvector(0,NP-1);
  PY=dvector(0,NP-1);
  PR=dvector(0,NP-1);
  for(i=0;i<NP;i++){
          fprintf(stderr,"PX[%d],PY[%d],PR[%d],PCol[%d] (PR<=%g):\n",i,i,i,i,PRmax);
	  fscanf(stdin,"%lf %lf %lf %s",&dum_x,&dum_y,PR+i,PCol[i]);
	  PX[i] = EdO + dum_x;
	  PY[i] = EdO + dum_y;
  }

  /* fluxes */
  fprintf(stderr,"Number of fluxes ( >= 1 , <= %d ):\n",NFmax);
  fscanf(stdin,"%d",&NF);
  NPF=ivector(0,NF-1);
  F_Int=dvector(0,NF-1);
  F_Int_delta=dvector(0,NF-1);
  LastInsert=dvector(0,NF-1);
  for(i=0;i<NF;i++){ LastInsert[i] = -1.0*EPSILON; }

  F=imatrix(0,NF-1,0,NPFmax-1);
  for(i=0;i<NF;i++){
          fprintf(stderr,"F_Int[%d] ( > 0 and << %g ):\n",i,1.0/DeltaT);
	  fscanf(stdin,"%lf",F_Int+i);
          fprintf(stderr,"F_Int_delta[%d]:\n",i,i);
	  fscanf(stdin,"%lf",F_Int_delta+i);
          fprintf(stderr,"NPF[%d] ( >= 2 , <= %d ):\n",i,NPFmax);
	  fscanf(stdin,"%d",NPF+i);
	  for(j=0;j<NPF[i];j++){
	          fprintf(stderr,"F[%d][%d]: \n",i,j);
		  fscanf(stdin,"%d",F[i]+j);
	  }
  }

  /* writing values to stdout */
  fprintf(stdout,"\n(EdO=%g)\n",EdO);
  fprintf(stdout,"\nNGrCol=%d\n",NGrCol);
  for(i=0;i<NGrCol;i++){
          fprintf(stdout,"GrColName[%d] = %s\n",i,GrColName[i]);
  }
  fprintf(stdout,"\nNP=%d\n",NP);
  for(i=0;i<NP;i++){
          fprintf(stdout,"%d PX,PY,PR,PCol=(%g,%g,%g,%s)\n",i,PX[i],PY[i],PR[i],PCol[i]);
  }
  fprintf(stdout,"\nNF=%d\n",NF);
  for(i=0;i<NF;i++){
	  fprintf(stdout,"%d F_Int[%d]=%g F_Int_delta[%d]=%g NPF[%d]=%d\n",i,i,F_Int[i],i,F_Int_delta[i],i,NPF[i]);
	  for(j=0;j<NPF[i];j++){
	    fprintf(stdout,"  F[%d][%d]=%d\n",i,j,F[i][j]);
	  }
  }
  fflush(stdout);  





  /* 2 */

  /* initializing some global variables & malloc. to arrays needed in
     all run modes */

  /* EdO already initialized 
  EdO = MAX( H*(DMean+DPlusMinus), 2.0*GG );
  EdO = MAX( EdO, PRmax );
  */
  XS = EdO + FieldXSize + EdO;
  YS = EdO + FieldYSize + EdO;

  BX = (int)floor(XS/R);
  BY = (int)floor(YS/R);
  B = (int)MAX(BX,BY);

  GBX = (int)floor(XS/GG);
  GBY = (int)floor(YS/GG);
  GB = (int)MAX(GBX,GBY);
  BIndBd = ivector( 0, SQR(B)-1 );
  BInd = ivector( 0, Nmax-1 );
  PColCode = ivector( 0, NPmax-1 ); 
  GrColCode = ivector( 0, NGrCol-1 ); 
  F_id = ivector( 0, Nmax-1 );
  F_nt = ivector( 0, Nmax-1 );

  RealTime_prev = -10000.0;
  RealTime = 0.0;
  D = dvector( 0, Nmax-1 );
  Theta = dvector( 0, Nmax-1 );
  X = dvector( 0, Nmax-1 );
  Y = dvector( 0, Nmax-1 );
  Xprev = dvector( 0, Nmax-1 );
  Yprev = dvector( 0, Nmax-1 );
  VX = dvector( 0, Nmax-1 );
  VY = dvector( 0, Nmax-1 );
  VXNew = dvector( 0, Nmax-1 );
  VYNew = dvector( 0, Nmax-1 );
  /* Graph */
  iRV = (int)ceil(WR);
  VMask = dmatrix( -iRV-1, iRV+1, -iRV-1, iRV+1 );
  iRGM = iRFS = (int)ceil(0.5*SiF);
  G = dvector( 0, SQR(GB)-1 );
  Dest_Xoffset = dvector( 0, Nmax-1 );
  Dest_Yoffset = dvector( 0, Nmax-1 );
  
  stat(IFN, &IFStatBuf);
  IFModTime = IFStatBuf.st_mtime;
  CycleNum = 0;
  srand(RandomSeed);
  GaussFlag=1;




  /* particles */
  N = 0;
  for(i=0;i<B*B;i++){ BIndBd[i] = -1; }
  for(i=0;i<Nmax;i++){ BInd[i] = -1; }


  /* masks */
  /* V mask */
  sow=0.0;
  for(gx=-iRV-1;gx<=iRV+1;gx++){ for(gy=-iRV-1;gy<=iRV+1;gy++){ 

          rsqr=SQR((double)gx)+SQR((double)gy);
	  if( rsqr < SQR(WR) ){ 

	          VMask[gx][gy] = exp(-sqrt(rsqr)/Sigma);
	          sow += exp(-sqrt(rsqr));
	  }
	  else /* ie. if  (rsqr>=SQR(WR)) */ {
	          VMask[gx][gy] = 0.0;
	  }
  }}
  if(sow!=0.0){
    for(gx=-iRV-1;gx<=iRV+1;gx++){ for(gy=-iRV-1;gy<=iRV+1;gy++){ 
            VMask[gx][gy] /= sow;
    }}
  }



  /* grass values */
  for(g=0;g<SQR(GB);g++){ G[g] = G_min; }



  /* opening files */

  /* output file */
  /*
  if(!(OFP=fopen(OFN,"w"))){
          fprintf(stderr,"sd_lib.c: Couldn't open %s for writing.\n",OFN);
	  SD_LIB_EXIT;
  }
  fprintf(OFP,"CycleNum, RealTime, N\n");
  fflush(OFP);
  */

  /* long output file */
  /*
  switch(RunMode){
  case 2: case 3: {
          if(!(LOFP=fopen(LOFN,"w"))){
	    fprintf(stderr,"sd_lib.c: Couldn't open %s for writing.\n",LOFN);
	    SD_LIB_EXIT;
	  }
	  break;
  }
  default: { break; }
  }
  */

  /* message file */
  /*
  if(!(MFP=fopen(MFN,"w"))){
          fprintf(stderr,"sd_lib.c: Couldn't open %s for writing.\n",MFN);
	  SD_LIB_EXIT;
  }
  */




  /* 3 */
  switch(RunMode){
  case 0: case 1: case 3: { 

          WinWidth = InFW + FieldXSize*(int)PicMag + Margin;
	  WinHeight =   (int)MAX( InFH, FieldYSize*PicMag ) 
	              + GrFH + 2*Margin;
	  g_win( "open", " self-driven", "sd", 0, 0, WinWidth, WinHeight, 4);
	  g_font( "open", XFontName );



	  /* allocating X colors and storing rgb codes, if needed */
	  if( !XAllocNamedColor(display,cmap,BgCol,&sdef,&edef) ) {
	    fprintf(stderr,"sd_lib.c: couldn't allocate color: %s\n",BgCol);
	    SD_LIB_EXIT;
	  }
          BgColCode = sdef.pixel;
	  

	  if( !XAllocNamedColor(display,cmap,InfoCol,&sdef,&edef) ) {
	    fprintf(stderr,"sd_lib.c: couldn't allocate color: %s\n",InfoCol);
	    SD_LIB_EXIT;
	  }
          InfoColCode = sdef.pixel;


	  for(i=0;i<NP;i++){
	          if( !XAllocNamedColor(display,cmap,PCol[i],&sdef,&edef) ) {
		      fprintf(stderr,"sd_lib.c: couldn't allocate color: %s\n",PCol[i]);
		      SD_LIB_EXIT;
		  }
		  PColCode[i] = sdef.pixel;

		  if(RunMode==3){
		        PCol_sdef[i].red = sdef.red;
			PCol_sdef[i].green = sdef.green;
			PCol_sdef[i].blue = sdef.blue;
		  }
	  }


	  for(i=0;i<NGrCol;i++){ 
	    if( !XAllocNamedColor(display,cmap,GrColName[i],&sdef,&edef) ) {
	      fprintf(stderr,"sd_lib.c: couldn't allocate color: %s\n",GrColName[i]);
	      SD_LIB_EXIT;
	    }
	    GrColCode[i] = sdef.pixel;
	  }

	  
	  /* manually: grass and nograss colors for eps images */
	  if(RunMode==3){

	          /* grass color: green 
		     r:g:b = 0:255:0 */
	          GrCol_sdef.red = 0;
		  GrCol_sdef.green = 255;
		  GrCol_sdef.blue = 0;

		  /* no grass color: brown
		     r:g:b = 165:42:42 */
		  NoGrCol_sdef.red = 165;
		  NoGrCol_sdef.green = 42;
		  NoGrCol_sdef.blue = 42;
	  }
	  
	  break; 
  }
  case 2: default: {
          break;
  }
  }

}

/*----------------------------------------------*/

void Init4( int narg, char *argstr[] ) {}

/*----------------------------------------------*/

void ReInit() {

  stat(IFN, &IFStatBuf);
  if( IFStatBuf.st_mtime != IFModTime ) { 
          IFModTime = IFStatBuf.st_mtime;
	  Read_IDS ( "re", IFN, IPar, IParName, IParNum, 
		     DPar, DParName, DParNum, 
		     SPar, SParName, SParNum,
		     MFN, 0 ); 
  }
}

/*----------------------------------------------*/

void Save() {

  if(  (CycleNum==0)
     ||(  (CycleNum>0) 	 
        &&(  (  (SaveCNFreq != 0)
	      &&(CycleNum % SaveCNFreq == 0)  
	     )
           ||(  (SaveCNFreq == 0)
	      &&(   floor( RealTime / SaveRTFreq )
		  > floor( RealTime_prev / SaveRTFreq )
	        )     
	      )
	   )
       )
    ) { 

    /*
          fprintf( OFP,"%d %g %d\n",
		   CycleNum, RealTime, N
		 );
	  fflush(OFP);
    */
  }
}

/*-------------------------------------------*/

void Test() {
}

/*-------------------------------------------*/

void Shutdown() {
}

/*=========================================================*/

double GaussRand( double gaussmean, double gausstheta, 
		  double gausscutfact ) {

  /* generates a random number (x) with
     P(x) = exp[- (x-mean)^2 / (2*theta)], if x is in 
           [mean - cutfact*sqrt(theta), mean + cutfact*sqrt(theta)]
          = 0                              , if not */
     
  if(  (GaussFlag==1)
     &&( fabs(GaussSet2-gaussmean) <= gausscutfact*sqrt(gausstheta) ) 
    ) {
          GaussFlag = 0;
	  return GaussSet2;
  }
  else {
          double v1,v2,rsq,fac;

	  GaussFlag = 0;
	  do {
	          do {
		          v1 = 1.0 - 2.0*(rand()/(RAND_MAX+1.0));
			  v2 = 1.0 - 2.0*(rand()/(RAND_MAX+1.0));
		  } while((rsq=v1*v1+v2*v2) >= 1.0);
		  fac = sqrt(-2.0*gausstheta*log(rsq)/rsq);
		  GaussSet1 = v1*fac;
		  GaussSet2 = v2*fac;
	  } while(    (   fabs(GaussSet1-gaussmean)
                        > gausscutfact*sqrt(gausstheta)
                      ) 
		   && (   fabs(GaussSet2-gaussmean) 
                        > gausscutfact*sqrt(gausstheta)
                      ) 
                 );

	  if(fabs(GaussSet1-gaussmean) <= gausscutfact*sqrt(gausstheta)) {
	          GaussFlag = 1;
		  return GaussSet1;
	  }
	  else {
	          GaussFlag = 0;
		  return GaussSet2;
	  }
  } 
}

/*-------------------------------------------*/

double VnP( double x1, double y1, double x2, double y2,
	    double vx1, double vy1, double vx2, double vy2 ) {
  /* normal component of the relative velocity of particle 1 vs. 2 */
     
  double x,y,vx,vy;

  x = x2-x1;
  y = y2-y1;
  vx = vx2-vx1;
  vy = vy2-vy1;

  if((x==0.0)&&(y==0.0)){ return 0.0; }
  else{ return ( (x*vx+y*vy)/sqrt(SQR(x)+SQR(y)) ); }
}

/*-------------------------------------------*/

void Fp( double *fx, double *fy, double x1, double y1, double x2,
	 double y2, double theta, double hc ) {
  /* force exerted on particle 1 by particle 2 */

  double x21,y21,cosfact,d,f;

  x21 = x1-x2;
  y21 = y1-y2;

  if((x21==0.0)&&(y21==0.0)){ *fx = 0.0; *fy = 0.0; }
  else{
    d = sqrt(SQR(x21)+SQR(y21));
    f = A1*pow((d-hc),-A2);

    cosfact = cos(theta - atan2(-y21,-x21));
    f *= 0.5 * ( (C_Back+C_Front) + (C_Back-C_Front)*cosfact );

    *fx = f * x21/d;
    *fy = f * y21/d;
  }
}


/*-------------------------------------------*/

void EulTStep( double *tstep, double d, double v_normal, double hc ) {
  /* changes the timestep (tstep) to such a small value, that avoids the 
     collision of the two objects */

  double tmpd;

  if( v_normal < 0.0 ) {

          while( (tmpd = d+v_normal*(*tstep)) <= hc ) { *tstep *= C_NS; }
          /*
          while( (tmpd = d+v_normal*(*tstep)) <= hc ) { 
	    fprintf(stdout,"d=%g v_normal=%g tstep=%g hc=%g\n",d,v_normal,*tstep,hc);
	    fflush(stdout);
	    *tstep *= C_NS;
	  }
	  */
	  if( C_Euler < A1*(   pow( tmpd-hc, -A2 ) 
                             - pow( d-hc,    -A2 )
                           )
            ) {
	          *tstep *= C_NS;
	          /* fprintf(stdout,"..\n");fflush(stdout); */
	  }
  }
}

/*=========================================================*/

void Upd(){

  switch(EMType){
  case 1: case 3: default: { Upd1(); break; }
  case 2:{ Upd2(); break; }
  }
}

/*---------------------------------------------------------*/

void Upd1(){

  /* one parallel update step for EMType=1 
     (particles' eq. of mot. is a 1st order diff. eq.)

     using Euler's method (for the motion of particles) 
     with adaptive integrational stepsize 

     2-8 updating particles
     9 updating grass


     

     1, memory allocation to local arrays

     2, computing pair forces

     3, grass forces (decreasing magnitude, if too big) 

     4, velocities are updated, adding noise

     5, computing time step:
	- if no particle-particle distance is below R, 
	  time step = DeltaT
	- else: for the distance (d) of any two particles:
	          during the update, the change of d (deltad) should be such, 
		  that the radial force between any two particles does not 
		  increase by more than C_Euler:
		  C_Euler >= A1 * abs[(d-x)^(-A2) - (d-deltad-x)^(-A2)]
		  where x = <sum of the two particles' radii>

     6, coordinates updated (previous values stored)  

     7, (computing output data) 
	
     8, 1 setting next value of RealTime
        2 removing particles that 
          . have slipped off the board
	  . or reached their final destination
        3 updating the book-keeping arrays of remaining particles
	4 changing the destinations of those particles that have
	  reached their internal destinations
        5 inserting particles, if needed 
	 
     9, 1 grass weakens at places where people walk on it
        2 deterministic growth
	3 stochastic growth/erosion (depending on neighbours)
	4 the grass cells of the edge rows/columns are given by the
	  values of their inside neighbours (similarly to the ghost
	  cells of hydrodinamics)
	5 adjustment

     10, free memory allocated to local arrays */
     

  int i, j, k, l, m, mx, my, j_old, j_new, nplus,gx,gy,g,*fgrass_flag;
  double tstep, eta, ksi, v, tmpfx, tmpfy,
    tmpd, tmpdsqr, alpha,beta, mass,dsqr,vnew,
    gmean,gmean_sow,v_normal,x,y,fgsqr,vsqr, *fpairx, *fpairy,
    *fgrassx, *fgrassy, fpsqr,tmpvx,fp,*tmp_G_incr; 






  /* 1 */
  /* memory allocation to local arrays */
  fpairx=dvector(0,N-1);
  fpairy=dvector(0,N-1);
  fgrassx=dvector(0,N-1);
  fgrassy=dvector(0,N-1);
  fgrass_flag=ivector(0,N-1);
  for(i=0;i<N;i++){ fgrass_flag[i]=1; }
  tmp_G_incr = dvector( 0, SQR(GB)-1 );


  /* 2 */
  /* pair forces */
  for(i=0; i<N; i++) {  

    fpairx[i] = 0.0;
    fpairy[i] = 0.0;
    j = (int)floor(X[i]*BX/XS) + B * (int)floor(Y[i]*BY/YS);
    for(k=-1;k<=1;k++){ for(l=-1;l<=1;l++){

	mx = j%B+k; 
	my = j/B+l;
	if((mx>=0)&&(mx<BX)&&(my>=0)&&(my<BY)){
	
	      m = BIndBd[ mx%BX + B * (my%BY) ];
	      if(m==i) { m = BInd[m]; }
	      if(m!=-1) {
		do { 

		  tmpdsqr = SQR(X[i]-X[m]) + SQR(Y[i]-Y[m]);
		  if( tmpdsqr <= SQR(R) ){

		    tmpd = sqrt(tmpdsqr);

		    /* checking whether inter-particle distance is
		       below H2*<sum of radii>,
		       if so, sends message to "3" not to compute
		       grass forces (ie. to give zero values) */
		    if(tmpd<H2*0.5*(D[i]+D[m])){ 
		            fgrass_flag[i]=0; 
		            fgrass_flag[m]=0; 
		    }

		    /* pair forces */
		    Fp( &tmpfx, &tmpfy, X[i], Y[i], X[m], 
			Y[m], Theta[i], 0.5*(D[m]+D[i]) );

		    fpairx[i] += tmpfx;
		    fpairy[i] += tmpfy;
		  }

		  m = BInd[m]; 
		  if(m==i) { m = BInd[m]; }

		}while(m!=-1);
	      }
	}
      }}
  }





  /* 3 */
  /* grass forces */
  for(i=0;i<N;i++){

    fgrassx[i]=0.0;
    fgrassy[i]=0.0;

    /* grass force non-zero only if distance of particle from other
       particles is large enough (indicated by fgrass_flag[i] */
    if(fgrass_flag[i]==1){

	  gx = (int)floor(X[i]*GBX/XS);
	  gy = (int)floor(Y[i]*GBY/YS);
	  g = gx + GB*gy;

	  for(mx=(int)MAX(gx-iRV,1);mx<=(int)MIN(gx+iRV,GBX-2);mx++){
	    for(my=(int)MAX(gy-iRV,1);my<=(int)MIN(gy+iRV,GBY-2);my++){

	            m = mx + GB*my;
		    fgrassx[i] +=   ( G[m+1] - G[m-1] )  
		                  * VMask[mx-gx][my-gy]; 
		    fgrassy[i] +=   ( G[m+GB] - G[m-GB] )
		                  * VMask[mx-gx][my-gy];
	    }
	  }
	  fgrassx[i] *= C_V/(2.0*GG);
	  fgrassy[i] *= C_V/(2.0*GG);
    }
  }    





  /* 4 */
  /* updating velocities */
  for(i=0;i<N;i++){

          Theta[i] = Dir( i, fgrassx[i], fgrassy[i] );
	  VX[i] = V0*cos(Theta[i]);
	  VY[i] = V0*sin(Theta[i]);

	  /* adding pair forces */
	  VX[i] += fpairx[i];
	  VY[i] += fpairy[i];


	  /* adding noise */
	  if(GaussTheta!=0.0){ 

	          /* amplitude */
	          ksi = GaussRand(GaussMean, GaussTheta, GaussCutMult);
		  eta = 2.0*PI*rand()/(RAND_MAX+1.0);
		  
		  /* here, sqrt(time step) should be used, ie. the present
		     time step instead of constant sqrt(DeltaT); 
		     however:
		     1, <time step> is determined later on using
		        the VX[i] and VY[i] velocity components from this
			routine
		     2, doesn't matter in this simulation
		  */

		  VX[i] += sqrt(DeltaT)*ksi*cos(eta);
		  VY[i] += sqrt(DeltaT)*ksi*sin(eta);
	  }


	  /* checking new velocity */
	  v = sqrt( SQR(VX[i]) + SQR(VY[i]) );
	  if(v > Vmax) {
	          VX[i] *= Vmax / v;
		  VY[i] *= Vmax / v;
	  }
  }






  /* 5 */ 
  /* computing time step */

  /* default value */
  tstep = DeltaT;

  /* adjustment */
  for(i=0;i<N;i++){

	j = (int)floor(X[i]*BX/XS) + B * (int)floor(Y[i]*BY/YS);
	for(k=-1;k<=1;k++){ for(l=-1;l<=1;l++){


	    mx = j%B+k; 
	    my = j/B+l;
	    if((mx>=0)&&(mx<BX)&&(my>=0)&&(my<BY)){
	

		    m = BIndBd[ mx%BX + B * (my%BY) ];
		    if(m==i) { m = BInd[m]; }
		    if(m!=-1) {
		      do { 


			  tmpdsqr = SQR(X[i]-X[m]) + SQR(Y[i]-Y[m]);
			  if( tmpdsqr <= SQR(R) ){


			    tmpd = sqrt(tmpdsqr);
			    /* time step adjustment */
			    if( m < i ) { 

				  /* computing v_normal */
				  v_normal = VnP( X[i], Y[i], X[m], 
						  Y[m], VX[i], VY[i], 
						  VX[m], VY[m]);

				  
				  /* adjusting time step */
				  EulTStep( &tstep, tmpd, v_normal, 
					    0.5*(D[i]+D[m]) );
			    }			       
			  }
			  
			  m = BInd[m]; 
			  if(m==i) { m = BInd[m]; }

		      }while(m!=-1);
		    }
	    }
	}}
  }




  /* 6 */
  /* updating coordinates */
  for(i=0;i<N;i++){

          Xprev[i] = X[i];
	  Yprev[i] = Y[i];

	  X[i] += VX[i] * tstep;
	  Y[i] += VY[i] * tstep;
  }
    



  /* 7 */
  /* computing output data */
  /* ... */






  /* 8 */

  /* 8.1 */ 
  CycleNum++;	  
  RealTime_prev = RealTime;
  RealTime += tstep;


  /* 8.2 */
  for(i=0;i<N;i++){

    j = F[F_id[i]][F_nt[i]];
    if(  ((X[i]<0.0)||(X[i]>=XS)||(Y[i]<0.0)||(Y[i]>=YS))
       ||(  ( F_nt[i] == NPF[F_id[i]]-1 )
	  &&(   SQR(PX[j]-X[i]) + SQR(PY[j]-Y[i])
              < SQR(PR[j]-0.5*D[i])	 
		/* < SQR(PR[j]) */	 
	    )
	 )
      ){
            RemoveParticle( &N, i );
	    i--;
    }
  }


  /* 8.3 */
  for(i=0;i<N;i++){

	  /* (if the particle is on the board)
	     the particle's book-keeping
	     arrays are modified only if its block index has changed
	     during the last update */

          j_old =   (int)floor(Xprev[i]*BX/XS) 
	          + B*(int)floor(Yprev[i]*BY/YS);
	  j_new = (int)floor(X[i]*BX/XS) + B*(int)floor(Y[i]*BY/YS);
	  if( j_new != j_old ) {

	          /* deleting particle i from its old block */
	          j = j_old;
		  if(BIndBd[j]==i) {
		    BIndBd[j] = BInd[i];
		  }
		  else {
		    j = BIndBd[j];
		    while(BInd[j]!=i) {
		      j = BInd[j];
		    }
		    BInd[j] = BInd[i];
		  }


		  /* inserting particle i into its new block */
		  j = j_new;
		  if(BIndBd[j]==-1) {
		    BIndBd[j] = i;
		    BInd[i] = -1;
		  }
		  else {
		    j = BIndBd[j];
		    while(BInd[j]!=-1) {
		      j = BInd[j];
		    }
		    BInd[j] = i;
		    BInd[i] = -1;
		  }
	  }	  
  }



  /* 8.4 */
  for(i=0;i<N;i++){

    j = F[F_id[i]][F_nt[i]];
    if(  ( F_nt[i] < NPF[F_id[i]]-1 )
       &&(   SQR(PX[j]-X[i]) + SQR(PY[j]-Y[i])
	   < SQR(PR[j]-0.5*D[i])	 
	 )
      ){

            /* changing target point */
	    F_nt[i]++;

	    /* changing offsets */
	    j = F[F_id[i]][F_nt[i]];
	    do{
	      x = (PR[j]-0.5*D[i]) * (1.0-2.0*rand()/(RAND_MAX+1.0) );
	      y = (PR[j]-0.5*D[i]) * (1.0-2.0*rand()/(RAND_MAX+1.0) );
	    }while( SQR(x) + SQR(y) >= SQR(PR[j]-0.5*D[i]) );
	    Dest_Xoffset[i] = x;
	    Dest_Yoffset[i] = y;
    }
  }




  /* 8.5 */
  /* inserting particles, if needed */
  /* if F_int[i] << 1.0/timestep, then the likeliness of two or more
     particles occuring during one time step can be neglected 
     -- thus -> using 1st order approximation */
  for(i=0;i<NF;i++){ 

          ksi =   F_Int[i] 
                + F_Int_delta[i] * ( 1.0 - 2.0*rand()/(RAND_MAX+1.0) );
	  /* here F_Int[i]*tstep has to be << 1.0 for good results */
	  if( LastInsert[i] + 1.0/ksi < RealTime ){
	          nplus = 1;
	          InsertParticles( &N, nplus, i );
		  LastInsert[i] = RealTime;
	  }
  }






  /* 9 */
  

  /* 9.1 */
  for(i=0;i<N;i++){

	  gx = (int)floor(Xprev[i]*GBX/XS);
	  gy = (int)floor(Yprev[i]*GBY/YS);
	  g = gx + GB*gy;

	  
	  /* 9.2 */
	  /* computing gmean */
	  gmean = 0.0;
	  gmean_sow = 0.0; /* sow = sum of weights */
	  for(mx=(int)MAX(gx-iRGM,0);mx<=(int)MIN(gx+iRGM,GBX-1);mx++){
	    for(my=(int)MAX(gy-iRGM,0);my<=(int)MIN(gy+iRGM,GBY-1);my++){

	      if(SQR(mx-gx)+SQR(my-gy)<SQR(0.5*SiF)){
		      m = mx + GB*my;
		      gmean += G[m];
		      gmean_sow += 1.0;
	      }
	    }
	  }
	  if(gmean_sow!=0.0){ gmean /= gmean_sow; }
	  else{ gmean = G[g]; }


	  /* G[g] (= comfort of walking) increases, ie. grass weakens */
	  for(mx=(int)MAX(gx-iRFS,0);mx<=(int)MIN(gx+iRFS,GBX-1);mx++){
	    for(my=(int)MAX(gy-iRFS,0);my<=(int)MIN(gy+iRFS,GBY-1);my++){

	      if(SQR(mx-gx)+SQR(my-gy)<SQR(0.5*SiF)){
	            m = mx + GB*my;
		    G[m] +=   ( I / (PI*SQR(0.5*SiF)) ) 
		            * (1.0-gmean/G_max) * tstep;  
	      }
	    }
	  }
  }



  /* 9.2 */
  /* every G[g] value has to be initialized to G_min in
     'void Init' */
  tmpd = tstep / T_G;
  for(g=0;g<SQR(GB);g++){ G[g] += ( G_min - G[g] ) * tmpd; }



  /* 9.3 */
  /* parallel update !
     1st : storing changes 
     2nd : updating */

  if(B_eta!=0.0){
          tmpd = sqrt(tstep)*GG*B_eta/(T_G*(RAND_MAX+1.0));
	  for(gx=1;gx<=GBX-2;gx++){ for(gy=1;gy<=GBY-2;gy++){

	          g = gx + GB*gy;
		  /* tmp_incr: tmp store increment */
		  tmp_G_incr[g] = 
		      tmpd*rand() 
		    * ( G[g] - 0.25*(   G[g-1]
				      + G[g+1]
				      + G[g+GB]
				      + G[g-GB]
				    )
		      ); 
	  }}

	  for(gx=1;gx<=GBX-2;gx++){ for(gy=1;gy<=GBY-2;gy++){
	    
	          g = gx + GB*gy;
	          G[g] += tmp_G_incr[g];
	  }}  
  }

  
  /* 9.4 */
  /* first and last row */
  for(gx=1;gx<=GBX-2;gx++){ 
          G[gx] = G[gx+GB];
          G[gx+GB*(GBY-1)] = G[gx+GB*(GBY-2)];
  }
  /* colums at left and right margin */ 
  for(gy=1;gy<=GBY-2;gy++){
          G[GB*gy] = G[GB*gy+1];
	  G[GB*gy+GBX-1] = G[GB*gy+GBX-2];
  }
  /* four corners */
  G[0] = G[1];
  G[GBX-1] = G[GBX-2];
  G[GB*(GBY-1)] = G[GB*(GBY-2)];
  G[GB*(GBY-1)+GBX-1] = G[GB*(GBY-2)+GBX-1];


  /* 9.5 adjustment */
  //  G_max=-1000.0;
  //  G_min=1000.0;
  for(g=0;g<GB*GB;g++){
    if(G[g]>G_max-EPSILON){ G[g] = G_max; }
    if(G[g]<G_min+EPSILON){ G[g] = G_min; }
  }




  /* 10 */
  free_dvector(fpairx,0,N-1);
  free_dvector(fpairy,0,N-1);
  free_dvector(fgrassx,0,N-1);
  free_dvector(fgrassy,0,N-1);
  free_ivector(fgrass_flag,0,N-1);
  free_dvector(tmp_G_incr,0,SQR(GB)-1);
}

/*-------------------------------------------*/

void Upd2(){

  /* one parallel update step for EMType=2 
     (particles' eq. of mot. is a 2nd order diff. eq.)

     using Euler's method (for the motion of particles) 
     with adaptive integrational stepsize 

     2-8 updating particles
     9 updating grass


     

     1, memory allocation to local arrays

     2, 2.1 pair forces

        2.2 time step:
	- if no particle-particle distance is below R, 
	  time step = DeltaT
	- else: for the distance (d) of any two particles:
	          during the update, the change of d (deltad) should be such, 
		  that the radial force between any two particles does not 
		  increase by more than C_Euler:
		  C_Euler >= A1 * abs[(d-x)^(-A2) - (d-deltad-x)^(-A2)]
		  where x = <sum of the two particles' radii>

        2.3 checking for low particle-particle distances 

     3, grass forces (adjusting, if needed) 

     4, new velocities are stored

     5, coordinates updated (previous values stored)  

     6, (computing output data) 
     
     7, 1 setting next value of RealTime
        2 removing particles that 
          . have slipped off the board
	  . or reached their final destination
        3 updating the book-keeping arrays of remaining particles
	4 changing the destinations of those particles that have
	  reached their internal destinations
        5 inserting particles, if needed 
	 
     8, 1 grass weakens at places where people walk on it
        2 deterministic growth
	3 stochastic growth/erosion (depending on neighbours)
	4 the grass cells of the edge rows/columns are given by the
	  values of their inside neighbours (similarly to the ghost
	  cells of hydrodinamics)
	5 adjustment

     9, (VX[i],VY[i]) = (VXNew[i],VYNew[i])

     10, free memory allocated to local arrays */
     

  double *fpairx,*fpairy,*fgrassx,*fgrassy,tmpdsqr,tmpd,tmpfx,tmpfy,
    v_normal,*tmp_G_incr,tstep,sqrt_tstep,*fspx,*fspy,ksi,eta,v,x,y,
    gmean,gmean_sow;
    
  int i,j,k,l,mx,my,m,gx,gy,g,j_old,j_new,nplus,*fgrass_flag;




  /* 1 */
  /* memory allocation to local arrays */
  fspx=dvector(0,N-1);
  fspy=dvector(0,N-1);
  fpairx=dvector(0,N-1);
  fpairy=dvector(0,N-1);
  fgrassx=dvector(0,N-1);
  fgrassy=dvector(0,N-1);
  fgrass_flag=ivector(0,N-1);
  for(i=0;i<N;i++){ fgrass_flag[i]=1; }
  tmp_G_incr = dvector( 0, SQR(GB)-1 );



  /* 2 */
  
  /* setting default values */
  tstep = DeltaT;
  for(i=0; i<N; i++) {  
    fpairx[i] = 0.0;
    fpairy[i] = 0.0;  
  }


  /* adjusting, if needed */
  for(i=0; i<N; i++) {  

    j = (int)floor(X[i]*BX/XS) + B * (int)floor(Y[i]*BY/YS);
    for(k=-1;k<=1;k++){ for(l=-1;l<=1;l++){

	mx = j%B+k; 
	my = j/B+l;
	if((mx>=0)&&(mx<BX)&&(my>=0)&&(my<BY)){
	
	      m = BIndBd[ mx%BX + B * (my%BY) ];
	      if(m==i) { m = BInd[m]; }
	      if(m!=-1) {
		do { 

		    tmpdsqr = SQR(X[i]-X[m]) + SQR(Y[i]-Y[m]);
		    if( tmpdsqr <= SQR(R) ){

		            tmpd = sqrt(tmpdsqr);


			    /* 2.1 pair forces */
			    Fp( &tmpfx, &tmpfy, X[i], Y[i], X[m], 
				Y[m], Theta[i], 0.5*(D[m]+D[i]) );

			    fpairx[i] += tmpfx;
			    fpairy[i] += tmpfy;


			  
			    /* 2.2 time step adjustment */
			    if( m < i ) { 

				  /* computing normal velocity */
				  v_normal = VnP( X[i], Y[i], X[m], 
						  Y[m], VX[i], VY[i], 
						  VX[m], VY[m]);

				  
				  /* adjusting time step */
				  EulTStep( &tstep, tmpd, v_normal, 
					    0.5*(D[i]+D[m]) );
			    }			       
   

			    /* 2.3 checking whether the distance of
			       particle centers is
			       below H2*<sum of radii>,
			       if so, sends message to "3" not to compute
			       grass forces (ie. to return zero values) */

			    if(tmpd<H2*0.5*(D[i]+D[m])){ 
			            fgrass_flag[i]=0; 
				    fgrass_flag[m]=0; 
			    }
			    
			    
		    }
		    
		    m = BInd[m]; 
		    if(m==i) { m = BInd[m]; }

		}while(m!=-1);
	      }
	}
      }}
  }




  /* 3 */
  /* grass forces */
  for(i=0;i<N;i++){

    fgrassx[i]=0.0;
    fgrassy[i]=0.0;

    /* grass force non-zero only if distance of particle from other
       particles is large enough (indicated by fgrass_flag[i] */
    if(fgrass_flag[i]==1){

	  gx = (int)floor(X[i]*GBX/XS);
	  gy = (int)floor(Y[i]*GBY/YS);
	  g = gx + GB*gy;

	  for(mx=(int)MAX(gx-iRV,1);mx<=(int)MIN(gx+iRV,GBX-2);mx++){
	    for(my=(int)MAX(gy-iRV,1);my<=(int)MIN(gy+iRV,GBY-2);my++){

	            m = mx + GB*my;
		    fgrassx[i] +=   ( G[m+1] - G[m-1] )  
		                  * VMask[mx-gx][my-gy]; 
		    fgrassy[i] +=   ( G[m+GB] - G[m-GB] )
		                  * VMask[mx-gx][my-gy];
	    }
	  }
	  fgrassx[i] *= C_V/(2.0*GG);
	  fgrassy[i] *= C_V/(2.0*GG);
    }
  }    




  /* 4 */
  /* updating velocities */
  sqrt_tstep = sqrt(tstep);
  for(i=0;i<N;i++){

          /* self-propelling */
          Theta[i] = Dir( i, fgrassx[i], fgrassy[i] );
	  fspx[i] = ( V0*cos(Theta[i]) - VX[i] ) / Tau;
	  fspy[i] = ( V0*sin(Theta[i]) - VY[i] ) / Tau;


	  /* adding noise */
	  if(GaussTheta!=0.0){ 

	          /* amplitude */
	          ksi = sqrt_tstep*GaussRand(GaussMean, GaussTheta, GaussCutMult);
		  /* direction */
		  eta = 2.0*PI*rand()/(RAND_MAX+1.0);
	  }


	  /* new velocity */
	  VXNew[i] = VX[i] + fspx[i] + fpairx[i] + ksi*cos(eta);
	  VYNew[i] = VY[i] + fspy[i] + fpairy[i] + ksi*sin(eta); 


	  /* checking new velocity */
	  v = sqrt( SQR(VXNew[i]) + SQR(VYNew[i]) );
	  if(v > Vmax) {
	          VXNew[i] *= Vmax / v;
		  VYNew[i] *= Vmax / v;
	  }
  }




  /* 5 */
  /* updating coordinates */
  for(i=0;i<N;i++){

          Xprev[i] = X[i];
	  Yprev[i] = Y[i];

	  X[i] += VX[i] * tstep;
	  Y[i] += VY[i] * tstep;
  }
    



  /* 6 */
  /* computing output data */
  /* ... */






  /* 7 */

  /* 7.1 */ 
  CycleNum++;	  
  RealTime_prev = RealTime;
  RealTime += tstep;


  /* 7.2 */
  for(i=0;i<N;i++){

    j = F[F_id[i]][F_nt[i]];
    if(  ((X[i]<0.0)||(X[i]>=XS)||(Y[i]<0.0)||(Y[i]>=YS))
       ||(  ( F_nt[i] == NPF[F_id[i]]-1 )
	  &&(   SQR(PX[j]-X[i]) + SQR(PY[j]-Y[i])
              < SQR(PR[j]-0.5*D[i])	 
		/* < SQR(PR[j]) */	 
	    )
	 )
      ){
            RemoveParticle( &N, i );
	    i--;
    }
  }


  /* 7.3 */
  for(i=0;i<N;i++){

	  /* (if the particle is on the board)
	     the particle's book-keeping
	     arrays are modified only if its block index has changed
	     during the last update */

          j_old =   (int)floor(Xprev[i]*BX/XS) 
	          + B*(int)floor(Yprev[i]*BY/YS);
	  j_new = (int)floor(X[i]*BX/XS) + B*(int)floor(Y[i]*BY/YS);
	  if( j_new != j_old ) {

	          /* deleting particle i from its old block */
	          j = j_old;
		  if(BIndBd[j]==i) {
		    BIndBd[j] = BInd[i];
		  }
		  else {
		    j = BIndBd[j];
		    while(BInd[j]!=i) {
		      j = BInd[j];
		    }
		    BInd[j] = BInd[i];
		  }


		  /* inserting particle i into its new block */
		  j = j_new;
		  if(BIndBd[j]==-1) {
		    BIndBd[j] = i;
		    BInd[i] = -1;
		  }
		  else {
		    j = BIndBd[j];
		    while(BInd[j]!=-1) {
		      j = BInd[j];
		    }
		    BInd[j] = i;
		    BInd[i] = -1;
		  }
	  }	  
  }



  /* 7.4 */
  for(i=0;i<N;i++){

    j = F[F_id[i]][F_nt[i]];
    if(  ( F_nt[i] < NPF[F_id[i]]-1 )
       &&(   SQR(PX[j]-X[i]) + SQR(PY[j]-Y[i])
	   < SQR(PR[j]-0.5*D[i])	 
	 )
      ){

            /* changing target point */
	    F_nt[i]++;

	    /* changing offsets */
	    j = F[F_id[i]][F_nt[i]];
	    do{
	      x = (PR[j]-0.5*D[i]) * (1.0-2.0*rand()/(RAND_MAX+1.0) );
	      y = (PR[j]-0.5*D[i]) * (1.0-2.0*rand()/(RAND_MAX+1.0) );
	    }while( SQR(x) + SQR(y) >= SQR(PR[j]-0.5*D[i]) );
	    Dest_Xoffset[i] = x;
	    Dest_Yoffset[i] = y;
    }
  }




  /* 7.5 */
  /* inserting particles, if needed */
  /* if F_int[i] << 1.0/timestep, then the likeliness of two or more
     particles occuring during one time step can be neglected 
     -- thus -> using 1st order approximation */
  for(i=0;i<NF;i++){ 

          ksi =   F_Int[i] 
                + F_Int_delta[i] * ( 1.0 - 2.0*rand()/(RAND_MAX+1.0) );
	  /* here F_Int[i]*tstep has to be << 1.0 for good results */
	  if( LastInsert[i] + 1.0/ksi < RealTime ){
	          nplus = 1;
	          InsertParticles( &N, nplus, i );
		  LastInsert[i] = RealTime;
	  }
  }




  /* 8 */

  /* 8.1 */
  for(i=0;i<N;i++){

	  gx = (int)floor(Xprev[i]*GBX/XS);
	  gy = (int)floor(Yprev[i]*GBY/YS);
	  g = gx + GB*gy;

	  
	  /* 9.2 */
	  /* computing gmean */
	  gmean = 0.0;
	  gmean_sow = 0.0; /* sow = sum of weights */
	  for(mx=(int)MAX(gx-iRGM,0);mx<=(int)MIN(gx+iRGM,GBX-1);mx++){
	    for(my=(int)MAX(gy-iRGM,0);my<=(int)MIN(gy+iRGM,GBY-1);my++){

	      if(SQR(mx-gx)+SQR(my-gy)<SQR(0.5*SiF)){
		      m = mx + GB*my;
		      gmean += G[m];
		      gmean_sow += 1.0;
	      }
	    }
	  }
	  if(gmean_sow!=0.0){ gmean /= gmean_sow; }
	  else{ gmean = G[g]; }


	  /* G[g] (= comfort of walking) increases, ie. grass weakens */
	  for(mx=(int)MAX(gx-iRFS,0);mx<=(int)MIN(gx+iRFS,GBX-1);mx++){
	    for(my=(int)MAX(gy-iRFS,0);my<=(int)MIN(gy+iRFS,GBY-1);my++){

	      if(SQR(mx-gx)+SQR(my-gy)<SQR(0.5*SiF)){
	            m = mx + GB*my;
		    G[m] +=   ( I / (PI*SQR(0.5*SiF)) ) 
		            * (1.0-gmean/G_max) * tstep;  
	      }
	    }
	  }
  }



  /* 8.2 */
  /* every G[g] value has to be initialized to G_min in
     'void Init' */
  tmpd = tstep / T_G;
  for(g=0;g<SQR(GB);g++){ G[g] += ( G_min - G[g] ) * tmpd; }



  /* 8.3 */
  /* parallel update !
     1st : storing changes 
     2nd : updating */

  if(B_eta!=0.0){
          tmpd = sqrt(tstep)*GG*B_eta/(T_G*(RAND_MAX+1.0));
	  for(gx=1;gx<=GBX-2;gx++){ for(gy=1;gy<=GBY-2;gy++){

	          g = gx + GB*gy;
		  /* tmp_incr: tmp store increment */
		  tmp_G_incr[g] = 
		      tmpd*rand() 
		    * ( G[g] - 0.25*(   G[g-1]
				      + G[g+1]
				      + G[g+GB]
				      + G[g-GB]
				    )
		      ); 
	  }}

	  for(gx=1;gx<=GBX-2;gx++){ for(gy=1;gy<=GBY-2;gy++){
	    
	          g = gx + GB*gy;
	          G[g] += tmp_G_incr[g];
	  }}  
  }

  
  /* 8.4 */
  /* first and last row */
  for(gx=1;gx<=GBX-2;gx++){ 
          G[gx] = G[gx+GB];
          G[gx+GB*(GBY-1)] = G[gx+GB*(GBY-2)];
  }
  /* colums at left and right margin */ 
  for(gy=1;gy<=GBY-2;gy++){
          G[GB*gy] = G[GB*gy+1];
	  G[GB*gy+GBX-1] = G[GB*gy+GBX-2];
  }
  /* four corners */
  G[0] = G[1];
  G[GBX-1] = G[GBX-2];
  G[GB*(GBY-1)] = G[GB*(GBY-2)];
  G[GB*(GBY-1)+GBX-1] = G[GB*(GBY-2)+GBX-1];


  /* 8.5 adjustment */
  //  G_max=-1000.0;
  //  G_min=1000.0;
  for(g=0;g<GB*GB;g++){
    if(G[g]>G_max-EPSILON){ G[g] = G_max; }
    if(G[g]<G_min+EPSILON){ G[g] = G_min; }
  }
  



  /* 9 */
  for(i=0;i<N;i++){
          VX[i] = VXNew[i];
	  VY[i] = VYNew[i];
  }



  /* 10 */
  free_dvector(fspx,0,N-1);
  free_dvector(fspy,0,N-1);
  free_dvector(fpairx,0,N-1);
  free_dvector(fpairy,0,N-1);
  free_dvector(fgrassx,0,N-1);
  free_dvector(fgrassy,0,N-1);
  free_ivector(fgrass_flag,0,N-1);
  free_dvector(tmp_G_incr,0,SQR(GB)-1);

}


/*=========================================================*/

void XPic() {

  if(  (CycleNum==0)
     ||(  ((CycleNum>0)&&(N>0)) 	 
        &&(  (  (DrawCNFreq != 0)
	      &&(CycleNum % DrawCNFreq == 0)  
	     )
           ||(  (DrawCNFreq == 0)
	      &&(   floor( RealTime / DrawRTFreq )
		  > floor( RealTime_prev / DrawRTFreq )
	        )     
	      )
	   )
       )
    ) { 

          switch(RunMode){
	  case 0: case 1: case 3: { XPicNormal(); break; }
	  case 2: default: { break; }
	  }
  }

}

/*-------------------------------------------*/

void XPicNormal() {

  int i,gx,gy,g,j;


  /* background */
  XSetForeground( display, gc, BgColCode );
  XFillRectangle( display, pix1, gc, 0, 0, WinWidth, WinHeight );
  
  /* grass -- determining colors */
  for(gx=0;gx<GBX;gx++){ for(gy=0;gy<GBY;gy++){
    
          g = gx + GB*gy;
	  if(G[g]>=G_max){ 
	    XSetForeground( display, gc, GrColCode[NGrCol-1] );	    
	  }
	  else if(G[g]<G_min){
	    XSetForeground( display, gc, GrColCode[0] );	    
	  }
	  else{
	    XSetForeground( display, gc,
			    GrColCode[(int)floor(NGrCol*(G[g]-G_min)/(G_max-G_min))] 
			  );
	  }

	  XFillRectangle( display, pix1, gc, 
			  InFW + floor(PicMag*(gx*GG-EdO)),
			  Margin + floor(PicMag*(gy*GG-EdO)), 
			  ceil(PicMag*GG+EPSILON), 
			  ceil(PicMag*GG+EPSILON) );
	  
  }}


  /* circles */
  for(i=0;i<NP;i++){
    XSetForeground( display, gc, PColCode[i] ); 
    XFillArc(display, pix1, gc, 
	     InFW + PicMag * (PX[i]-PR[i]-EdO), 
	     Margin + PicMag * (PY[i]-PR[i]-EdO), 
	     PicMag*2.0*PR[i], 
	     PicMag*2.0*PR[i], 
	     0, 23040);
  }

  /* particles */
  for(i=0;i<N;i++){
    j = F[F_id[i]][F_nt[i]];
    XSetForeground( display, gc, PColCode[j] ); 
    XDrawParticle( InFW, Margin, PicMag, ObjMag, 
		   X[i]-EdO, Y[i]-EdO, VX[i], VY[i], D[i], i 
		 ); 
  }

  /* cleaning edges of the field */
  XSetForeground( display, gc, BgColCode );
  XFillRectangle( display, pix1, gc, 0, 0, InFW, WinHeight );
  XFillRectangle( display, pix1, gc, 0, 0, WinWidth, Margin );
  XFillRectangle( display, pix1, gc, 0, 
		  Margin+PicMag*(YS-2.0*EdO)+1, 
		  WinWidth, WinHeight 
		);
  XFillRectangle( display, pix1, gc, 
		  InFW+PicMag*(XS-2.0*EdO)+1, 0, 
		  WinWidth, WinHeight 
		);

  /* showing it all */
  h_show(WinWidth,WinHeight);
  sleep(Sleep);
}

/*-------------------------------------------*/

void XDrawParticle( int leftxmargin, int upymargin, 
		    double pm, double om, double x, double y,
		    double vx, double vy, double d, int i ) {

  /* pm: picture magnification
     om: object magnificiation */


  double theta = atan2(vy,vx), 
         v = sqrt(SQR(vx)+SQR(vy)); 
  int lxm = leftxmargin, uym = upymargin, j;



  switch( DrawMode ) {
  case 0: {
          XDrawLine(display, pix1, gc, 
		    lxm + pm * x,          
		    uym + pm * y, 
		    lxm + pm * (x + om*vx), 
		    uym + pm * (y + om*vy) );
	  XDrawLine(display, pix1, gc, 
		    lxm + pm * (x + om*vx), 
		    uym + pm * (y + om*vy),
		    lxm + pm * (x + om*0.5*v*cos(theta+0.4)),
		    uym + pm * (y + om*0.5*v*sin(theta+0.4)) );
	  break;
  }
  case 1: {
          XDrawArc(display, pix1, gc, 
		   lxm + pm * (x - om*0.5*v), 
		   uym + pm * (y - om*0.5*v), 
		         pm * om*v, 
		         pm * om*v, 0, 23040);
	  break;
  }
  case 2: {
          XDrawArc(display, pix1, gc, 
		   lxm + pm * (x - (d+om*v)/2), 
		   uym + pm * (y - (d+om*v)/2), 
		         pm * (d+om*v), 
		         pm * (d+om*v), 0, 23040);
	  break;
  }
  case 3: {
          XFillArc(display, pix1, gc, 
		   lxm + pm * (x - d/2), 
		   uym + pm * (y - d/2), 
		         pm * d, 
		         pm * d, 0, 23040);
	  break;
  }
  case 4: {
          XDrawArc(display, pix1, gc, 
		   lxm + pm * (x - d/2), 
		   uym + pm * (y - d/2), 
		         pm * d, 
		         pm * d, 0, 23040);
	  break;
  }
  case 5: {
          XFillArc(display, pix1, gc, 
		   lxm + pm * (x - d/2), 
		   uym + pm * (y - d/2), 
		         pm * d, 
                         pm * d, 0, 23040);
          XDrawArc(display, pix1, gc, 
		   lxm + pm * (x - R/2.0), 
		   uym + pm * (y - R/2.0), 
		         pm * R, 
		         pm * R, 0, 23040);
  	  break;
  }
  case 6: {
          /* velocity */
          XDrawLine(display, pix1, gc, 
		    lxm + pm * x,          
		    uym + pm * y, 
		    lxm + pm * (x + om*vx), 
		    uym + pm * (y + om*vy) );
	  XDrawLine(display, pix1, gc, 
		    lxm + pm * (x + om*vx), 
		    uym + pm * (y + om*vy),
		    lxm + pm * (x + om*0.5*v*cos(theta+0.4)),
		    uym + pm * (y + om*0.5*v*sin(theta+0.4)) );

	  /* vector of preferred direction */
          XDrawLine(display, pix1, gc, 
		    lxm + pm * x,          
		    uym + pm * y, 
		    lxm + pm * (x + om*cos(Theta[i])), 
		    uym + pm * (y + om*sin(Theta[i])) 
		   );
	  XDrawLine(display, pix1, gc, 
		    lxm + pm * (x + om*cos(Theta[i])), 
		    uym + pm * (y + om*sin(Theta[i])),
		    lxm + pm * (x + om*0.5*cos(Theta[i]+0.4)),
		    uym + pm * (y + om*0.5*sin(Theta[i]+0.4)) );

	  j = F[F_id[i]][F_nt[i]];
	  XDrawLine( display, pix1, gc, 
		     lxm + pm*x, 
		     uym + pm*y,
		     lxm + pm*(PX[j]+Dest_Xoffset[i]-EdO),
		     uym + pm*(PY[j]+Dest_Yoffset[i]-EdO)
		   );
	  break;
  }
  }

}

/*--------------------------------------*/

//void MakeEpsDataFile(){
//
//  int i;
//  char fn[SD_STRLEN];
//  FILE *fp;
//
//
//  sprintf(fn,"sd.%d.epsdat",CycleNum);
//  fp=fopen(fn,"w");
//  fprintf(fp,"\n\n%g %d %d\n",RealTime,GB,N);
//  for(i=0;i<SQR(GB);i++){ fprintf(fp,"%d %g\n",i,G[i]); }
//  for(i=0;i<N;i++){ fprintf(fp,"%d %g %g %g %d\n",
//			    i,X[i],Y[i],D[i],F[F_id[i]][F_nt[i]]); }
//  fflush(fp);
//  fclose(fp);
//
//  sleep(EpsSleep);
//}

/*---------------------------------------------*/

void MakeEpsPicFile(){

  /* Grass Color red/green/blue, NoGrassColor r/g/b */
  int i,j,k,gx,gy,n_of_grcolors;
  char fn[SD_STRLEN];
  FILE *fp;
  double epsxmag,epsymag,tmp,r,g,b,gi;



  
  /* 1 opening eps file */
  sprintf(fn,"sd.%d.eps",CycleNum);
  if(!(fp=fopen(fn,"w"))){
          fprintf(stderr,"Error: couldn't open %s for writing.\n",fn);
	  SD_LIB_EXIT;
  }
  //  fprintf(stderr,"Writing file %s ... ",fn);


  /* 2.0 adjusting colors */
  // ...


  /* 2.1 init and background */ 
  epsxmag = ( EpsXSize - 2.0*EpsMargin ) / ( XS-2.0*EdO );
  epsymag = ( EpsYSize - 2.0*EpsMargin ) / ( YS-2.0*EdO );

  EpsInit( fp, 0, 0, EpsXSize-1, EpsYSize-1 );
  fprintf( fp, "%d %d %d srgb\n",
	   GrCol_sdef.red, GrCol_sdef.green, GrCol_sdef.blue);
  EpsFillRectangle( fp, 0, 0, EpsXSize, EpsYSize );





  /* 2.2 grass */
  /* drawing only those fields, 
     where G[i] is large enough and field is within the eps image */
  n_of_grcolors = 16;
  //tmp = 1.0/(256.0*(G_max-G_min));
  tmp = 1.0/(G_max-G_min);

  /* creating n_of_grcolors lists of fields 
     -- starting with j=1 to omit fields where G[i] is slightly bigger 
     than G_min */
  for(j=1;j<n_of_grcolors;j++){

          /* setting color */
          gi = G_min + (double)j/(double)n_of_grcolors*(G_max-G_min);

          r = tmp * (   (G_max-gi) * GrCol_sdef.red 
		      + (gi-G_min) * NoGrCol_sdef.red
		    ); 
	  g = tmp * (   (G_max-gi) * GrCol_sdef.green 
		      + (gi-G_min) * NoGrCol_sdef.green
		    ); 
	  b = tmp * (   (G_max-gi) * GrCol_sdef.blue 
		      + (gi-G_min) * NoGrCol_sdef.blue
		    ); 
	  fprintf( fp, "%g %g %g srgb\n",r,g,b );


	  /* list of fields with this color */
	  for(i=0;i<SQR(GB);i++){
	      gx = i%GB; 
	      gy = i/GB;

	      if(  ( (int)floor(n_of_grcolors*((G[i]-G_min)/(G_max-G_min)-EPSILON)) == j )
		 &&(gx*GG-EdO>=0.0)&&(gx*GG-EdO<=EpsXSize)
		 &&(gy*GG-EdO>=0.0)&&(gy*GG-EdO<=EpsYSize)
		){

		      EpsFillRectangle( fp,  
					EpsMargin + floor(epsxmag*(gx*GG-EdO)), 
					EpsMargin + floor(epsymag*(gy*GG-EdO)), 
					EpsMargin + ceil(epsxmag*((gx+1)*GG-EdO)), 
					EpsMargin + ceil(epsymag*((gy+1)*GG-EdO)) 
					);
	      }
	  }
  }



  /* 2.3 particles and target areas */
  for(i=0;i<NP;i++){
	  r = tmp * PCol_sdef[i].red;
	  g = tmp * PCol_sdef[i].green;
 	  b = tmp * PCol_sdef[i].blue;
          fprintf(fp,"%g %g %g srgb\n", r, g, b ); 

	  /* draw the ith target area */
	  EpsFillCircle( fp,
			 EpsMargin + epsxmag*(PX[i]-EdO),
			 EpsMargin + epsymag*(PY[i]-EdO),
			 epsxmag*PR[i]
		       );

	  /* draw particles that are moving towards the ith target area */
	  for(j=0;j<N;j++){ 
	    if(F[F_id[j]][F_nt[j]]==i){
	            EpsFillCircle( fp, 
				   EpsMargin + epsxmag*(X[i]-EdO),
				   EpsMargin + epsymag*(Y[i]-EdO),
				   epsxmag*0.5*D[i]
				   );
	    }
	  }
	  fprintf(fp,"\n");
  }



  /* 2.4 closing picture file */
  fflush(fp);
  fclose(fp);
  //  fprintf(stderr,"finished.\n");
}
