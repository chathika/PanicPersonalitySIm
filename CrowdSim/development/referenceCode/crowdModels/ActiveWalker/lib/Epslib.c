/***************************************************************
  This file is a collection of funtions to draw simple objects
  into an eps file.
*****************************************************************/

/* definition of the point structure */
typedef struct {
	double	x;
	double	y;
} EpsPoint;


void EpsInit( FILE *EpsFile, 
	      int boundboxlowleftx, int boundboxlowlefty,
	      int boundboxuprightx, int boundboxuprighty ) {

	fprintf( EpsFile, "%%!PS-Adobe-2.0 EPSF-2.0\n" );
	fprintf( EpsFile, "%%%%BoundingBox: %d %d %d %d\n", 
		 boundboxlowleftx, boundboxlowlefty,
		 boundboxuprightx, boundboxuprighty );
	fprintf( EpsFile, "/cp {closepath} bind def\n" );
	fprintf( EpsFile, "/ef {eofill} bind def\n" );
	fprintf( EpsFile, "/f {fill} bind def\n" );
	fprintf( EpsFile, "/gr {grestore} bind def\n" );
	fprintf( EpsFile, "/gs {gsave} bind def\n" );
	fprintf( EpsFile, "/sa {save} bind def\n" );
	fprintf( EpsFile, "/rs {restore} bind def\n" );
	fprintf( EpsFile, "/l {lineto} bind def\n" );
	fprintf( EpsFile, "/m {moveto} bind def\n" );
	fprintf( EpsFile, "/rm {rmoveto} bind def\n" );
	fprintf( EpsFile, "/n {newpath} bind def\n" );
	fprintf( EpsFile, "/s {stroke} bind def\n" );
	fprintf( EpsFile, "/sh {show} bind def\n" );
	fprintf( EpsFile, "/slc {setlinecap} bind def\n" );
	fprintf( EpsFile, "/slj {setlinejoin} bind def\n" );
	fprintf( EpsFile, "/slw {setlinewidth} bind def\n" );
	fprintf( EpsFile, "/srgb {setrgbcolor} bind def\n" );
	fprintf( EpsFile, "/rot {rotate} bind def\n" );
	fprintf( EpsFile, "/sc {scale} bind def\n" );
	fprintf( EpsFile, "/sd {setdash} bind def\n" );
	fprintf( EpsFile, "/ff {findfont} bind def\n" );
	fprintf( EpsFile, "/sf {setfont} bind def\n" );
	fprintf( EpsFile, "/scf {scalefont} bind def\n" );
	fprintf( EpsFile, "/sw {stringwidth} bind def\n" );
	fprintf( EpsFile, "/tr {translate} bind def\n" );
	fprintf( EpsFile, "/Times-Bold ff 50 scf sf\n" );
}


/* Available eps fonts are: Times-Roman, Times-Italic,
   Times-Bold, Times-Bold-Italic, etc. */

void EpsSetFont( FILE *EpsFile, char *EpsFontStr, double EpsFontHeight ) { 
  fprintf( EpsFile, "/%s ff %g scf sf\n", EpsFontStr, EpsFontHeight ); }




void EpsDrawString( FILE *EpsFile, double deg, double x, double y, char *TextStr ) {
  /* draws rotated text onto picture -- used until now only as last
     command in file 
     deg: angel of rotation in degrees */

  fprintf( EpsFile, "%.2f rot\n", deg );
  fprintf( EpsFile, "n %g %g m (%s) sh s\n", 
	   /* formula works for deg=0.0, 
	      for other angles, try -- formula may not work */
	   x*cos(deg)+y*sin(deg),
	   y*cos(deg)-x*sin(deg), 
	   TextStr ); 
}


void EpsDrawLine( FILE *EpsFile, double x1, double y1, double x2, double y2 ) {
  fprintf( EpsFile, "n %g %g m %g %g l s\n", x1, y1, x2, y2 ); }


void EpsDrawLines( FILE *EpsFile, EpsPoint *P, int npoints ) {

  int i;

  fprintf( EpsFile, "n %g %g m\n", P[0].x, P[0].y );
  for( i = 1; i < npoints; i++ ) {
          fprintf( EpsFile, "%g %g l\n", P[i].x, P[i].y ); 
  }
  fprintf( EpsFile, "s\n" );
}


void EpsDrawRectangle( FILE *EpsFile, 
		       double lowleftx, double lowlefty,
		       double uprightx, double uprighty ) {

  fprintf( EpsFile, "n %g %g m\n", lowleftx, lowlefty );
  fprintf( EpsFile, "%g %g l\n", lowleftx, uprighty );
  fprintf( EpsFile, "%g %g l\n", uprightx, uprighty );
  fprintf( EpsFile, "%g %g l\n", uprightx, lowlefty );
  fprintf( EpsFile, "cp s\n" );
}


void EpsFillRectangle( FILE *EpsFile, 
		       double lowleftx, double lowlefty,
		       double uprightx, double uprighty ) {

  fprintf( EpsFile, "n %g %g m\n", lowleftx, lowlefty );
  fprintf( EpsFile, "%g %g l\n", lowleftx, uprighty );
  fprintf( EpsFile, "%g %g l\n", uprightx, uprighty );
  fprintf( EpsFile, "%g %g l\n", uprightx, lowlefty );
  fprintf( EpsFile, "cp f s\n" );
}


void EpsFillRectangle_2( FILE *EpsFile, 
		       double lowleftx, double lowlefty,
		       double uprightx, double uprighty ) {

  fprintf( EpsFile, "n %.2f %.2f m\n", lowleftx, lowlefty );
  fprintf( EpsFile, "%.2f %.2f l\n", lowleftx, uprighty );
  fprintf( EpsFile, "%.2f %.2f l\n", uprightx, uprighty );
  fprintf( EpsFile, "%.2f %.2f l\n", uprightx, lowlefty );
  fprintf( EpsFile, "cp f s\n" );
}



void EpsDrawPoint( FILE *EpsFile, double size, double x, double y ) {
  EpsFillRectangle( EpsFile, x, y, x+size, y+size );
} 

void EpsDrawPoint_2( FILE *EpsFile, double size, double x, double y ) {
  EpsFillRectangle_2( EpsFile, x, y, x+size, y+size );
} 



void EpsDraw4Poly( FILE *EpsFile, 
		   double x1, double y1, double x2, double y2,
		   double x3, double y3, double x4, double y4 ) {

  fprintf( EpsFile, "n %g %g m\n", x1, y1 );
  fprintf( EpsFile, "%g %g l\n", x2, y2 );
  fprintf( EpsFile, "%g %g l\n", x3, y3 );
  fprintf( EpsFile, "%g %g l\n", x4, y4 );
  fprintf( EpsFile, "cp s\n" );
}


void EpsFill4Poly( FILE *EpsFile, 
		   double x1, double y1, double x2, double y2,
		   double x3, double y3, double x4, double y4 ) {

  fprintf( EpsFile, "n %g %g m\n", x1, y1 );
  fprintf( EpsFile, "%g %g l\n", x2, y2 );
  fprintf( EpsFile, "%g %g l\n", x3, y3 );
  fprintf( EpsFile, "%g %g l\n", x4, y4 );
  fprintf( EpsFile, "cp f s\n" );
}


void EpsDrawPolygon( FILE *EpsFile, EpsPoint *P, int npoints ) {

  int i;

  fprintf( EpsFile, "n %g %g m\n", P[0].x, P[0].y );
  for( i = 1; i < npoints; i++ ) {
          fprintf( EpsFile, "%g %g l\n", P[i].x, P[i].y );
  }
  fprintf( EpsFile, "cp s\n" );
}


void EpsFillPolygon( FILE *EpsFile, EpsPoint *P, int npoints ) {

  int i;

  fprintf( EpsFile, "n %g %g m\n", P[0].x, P[0].y );
  for( i = 1; i < npoints; i++ ) {
          fprintf( EpsFile, "%g %g l\n", P[i].x, P[i].y );
  }
	fprintf( EpsFile, "cp f s\n" );
}


void EpsDrawCircle( FILE *EpsFile, double centerx, double centery, double rad )
{
  fprintf( EpsFile, "n %g %g m %g %g %g 0 360 arc s\n", \
	   centerx + rad, centery, centerx, centery, rad );
}


void EpsFillCircle( FILE *EpsFile, double centerx, double centery,
		    double rad ) {
  fprintf( EpsFile, "n %g %g m %g %g %g 0 360 arc f s\n", \
	   centerx + rad, centery, centerx, centery, rad );
}


void EpsSetLinewidth( FILE *EpsFile, double linewidth ) {
  fprintf( EpsFile, "%g slw\n", linewidth ); }




void EpsSetRgb( FILE *EpsFile, double red, double green, double blue ) {
  fprintf( EpsFile, "%g %g %g srgb\n", red/65535.0, green/65535.0, blue/65535.0 ); }


void EpsSetGCColor( FILE *EpsFile, Display* display, GC gc, Colormap colormap,
		    char *color_name ) {
  XColor tmpxc, xc;

  XLookupColor( display, colormap, color_name, &tmpxc, &xc );
  EpsSetRgb( EpsFile, xc.red/65535.0, xc.green/65535.0, xc.blue/65535.0 ); 
}


void EpsEndPage( FILE *EpsFile ) { fprintf( EpsFile, "showpage\n" ); }


void EpsClose( FILE *EpsFile ) { 
  fprintf( EpsFile, "showpage\n" ); 
  fflush( EpsFile );
  fclose( EpsFile );
}


void EpsFlush( FILE *EpsFile ) { fflush( EpsFile ); }

/******************************************************/

/* void gsave( EpsFile )
FILE	*EpsFile;
{
	fprintf( EpsFile, "gs\n" );
}

void grestore( EpsFile )
FILE	*EpsFile;
{
	fprintf( EpsFile, "gr\n" );
}

void make_stroke( EpsFile )
FILE	*EpsFile;
{
	fprintf( EpsFile, "s\n" );
} */
