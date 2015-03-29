#define _CRT_SECURE_NO_DEPRECATE

#include <cv.h>
#include <direct.h>
#include <highgui.h>
#include <iostream>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <time.h>
#include <sys/utime.h>
#include <fstream>
using std::ifstream;
#include <string>

/**
 * Parameters to set up video data collection.
 **/
/** Directory where the movie is stored and where the data files will go. **/
char* dirName;// = "C:\\Documents and Settings\\Rockstar\\Desktop\\ISTVIDEO\\holycross\\";
/** The name of the file to do the analysis on. **/
//char* fileName = "movies\\Brakes Off-1.avi";
//char* fileName = "movies\\2_J2_movie.avi";
//char* fileName = "movies\\optical_flow_input.avi";
//char* fileName = "movies\\dense_convert.avi";
//char* fileName = "movies\\Holycross002.avi";
//char* fileName = "Student_Union_Double_Door_013";
//char* fileName = "Holly_Cross_Inside_2_001";
//char* fileName = "movies\\test\\test.avi";
char* fileName;// = "HCEnglish_001";

/** The video to be analyzed. **/
CvCapture *video;
/** Size describing the height and width of the video being analyzed. **/
CvSize	frameSize;
/** Size of the apeture used for the LK Optical flow method. 
	standard apeture sizes are 1, 3, 5, 7, 11 and 15.
 **/
int mode;
int apetureSize = 11;
/** Filter threshold lower allows more noise in. 0 is no background removal. **/
int filterThreshold = 35;

//Information needed for analysis
/** The frame to start the analysis at. **/
unsigned int startFrame = 1;
/** The frame to stop recording at. Set to 0 to record entire video.**/
unsigned int endFrame = 1770;
/** The number of frames to skip between frames being compared. **/
unsigned int framesToSkip = 5;
/** The pixel changes noticed in the x direction. **/
double** u=0;
/** The pixel changes noticed in the y direction. **/
double** v=0;
/** The number of samples/frame compares taken. **/
unsigned int numOfDataPoints=0;
/** The directory to output the data to. **/
char*  outputDir="data";
/** The width of a cell in the video analysis. **/
double cellWidth = 80.0;
/** The height of a cell in the video analysis. **/
double cellHeight = 80.0;
int gridWidth = 4;
int gridHeight = 3;

int quiet;
char* outputfile;
int DEPTH = IPL_DEPTH_8U;
int cellSize = 15;

IplImage* background = NULL;
IplImage* mask = NULL;
CvCapture* input_video = NULL;
CvSize frame_size;

/**
 * Takes the commandline arguments and sets the appropriate variables from it.
 * 
 * @param numOfArgs The number of arguments passed to the program.
 * @param args The arguments passed in on the commandline.
 **/
int readCommandLineParams(int numOfArgs, char* args[])
{
	printf("argc = %d, argv[1] = %s",numOfArgs,args[1]);
	if(numOfArgs>1)
	{
		   
		   dirName=args[1];
		   fileName=args[2];
		   outputfile=args[3];
		   quiet=atoi(args[4]);
		   apetureSize=atoi(args[5]);
		   filterThreshold=atoi(args[6]);
		   startFrame=atoi(args[7]);
		   endFrame=atoi(args[8]);
		   framesToSkip=atoi(args[9]);
		   gridWidth=atoi(args[10]);
		   gridHeight=atoi(args[11]);
		   return 1;//if 1 is returned, the program will not prompt a user for a filename
	}
	return 0;
}

/* This is just an inline that allocates images.  I did this to reduce clutter in the
 * actual computer vision algorithmic code.  Basically it allocates the requested image
 * unless that image is already non-NULL.  It always leaves a non-NULL image as-is even
 * if that image's size, depth, and/or channels are different than the request.
 */
inline static void allocateOnDemand( IplImage **img, CvSize size, int depth, int channels )
{
	if ( *img != NULL )	return;

	*img = cvCreateImage( size, depth, channels );
	
	if ( *img == NULL )
	{
		fprintf(stderr, "Error: Couldn't allocate image.  Out of memory?\n");
		exit(-1);
	}
}

void Print(IplImage* velx)
{
	if(velx == NULL)
	{
		printf("Error!!!\n");
		return;
	}
	printf("--------------------------------------\n");
	printf("nSize :%d\n", velx->nSize);
	printf("ID :%d \n", velx->ID);
	printf("nChannels: %d \n", velx->nChannels);
	printf("alphaChannel: %d \n", velx->alphaChannel);
	printf("depth: %d \n", velx->depth);
	printf("Color model: %s \n", velx->colorModel);
	printf("channel seq: %s \n", velx->channelSeq);
	printf("data order: %d \n", velx->dataOrder);
	printf("Origin: %d \n", velx->origin);
	printf("Align: %d \n", velx->align);
	printf("Width: %d \n", velx->width);
	printf("height: %d \n", velx->height);
	printf("Image size: %d \n", velx->imageSize);
	printf("WidthStep: %d \n", velx->widthStep);
	printf("Border mode [top]: %d \n", velx->BorderMode[0]);
	printf("Border mode [bottom]: %d \n", velx->BorderMode[1]);
	printf("Border mode [left]: %d \n", velx->BorderMode[2]);
	printf("Border mode [right]: %d \n", velx->BorderMode[3]);
	printf("Border const [top]: %d \n", velx->BorderMode[0]);
	printf("Border const [bottom]: %d \n", velx->BorderMode[1]);
	printf("Border const [left]: %d \n", velx->BorderMode[2]);
	printf("Border const [right]: %d \n", velx->BorderMode[3]);
}

void removeNoise(IplImage* src)
{
    //get the size of input_image (src)
	CvSize sz = cvSize( src->width & -2, src->height & -2 ); 

    //create  temp-image
    IplImage* pyr = cvCreateImage( cvSize(sz.width/2, sz.height/2), 
                                   src->depth, src->nChannels ); 
    
    cvPyrDown( src, pyr, 7 );		//pyr DOWN
    cvPyrUp( pyr, src, 7 );			//and UP
    cvReleaseImage( &pyr );           //release temp
}

IplImage* getMask( IplImage* frame )
{
	IplImage* frame1 = NULL;
	allocateOnDemand( &frame1, frame_size, DEPTH, 3 );
    IplImage* tempImage = NULL;
	allocateOnDemand( &tempImage, frame_size, DEPTH, 1 );
	IplImage* tempImage2 = NULL;
	allocateOnDemand( &tempImage2, frame_size, DEPTH, 3 );
	IplImage* tempBack = NULL;
	allocateOnDemand( &tempBack, frame_size, DEPTH, 3 );
	
	//isolate item not moving.
	cvAbsDiff( frame, background, tempImage2 );
	cvConvertImage( tempImage2, tempImage2, CV_CVTIMG_FLIP );
	//remove timestamp area
	cvSub( tempImage2, mask, tempImage2 );

	//create Mask for the moving object
	cvCvtColor( tempImage2, tempImage, CV_BGR2GRAY );
	cvCmpS( tempImage, filterThreshold, tempImage, CV_CMP_LT );
		//smooth the image
	removeNoise( tempImage2 );
	cvCvtColor( tempImage, tempImage2, CV_GRAY2RGB ); 


	cvReleaseImage( &frame1 );
	cvReleaseImage( &tempImage );
	cvReleaseImage( &tempBack );

	return tempImage2;
}

void cleanFrame( IplImage* frame )
{
    IplImage* tempImage = NULL;
	allocateOnDemand( &tempImage, frame_size, DEPTH, 1 );
	IplImage* mask = NULL;
	
	mask = getMask( frame );

	tempImage = cvCloneImage( frame );
	cvConvertImage(tempImage, tempImage, CV_CVTIMG_FLIP);
	cvOr( tempImage, mask, frame );
	removeNoise( frame );
	removeNoise( frame );

	cvReleaseImage( &tempImage );
	cvReleaseImage( &mask );
}

int main(int numOfArgs, char* args[])
{
	using namespace std;
	IplImage* frame = NULL;
	IplImage* frame1Grey = NULL;
	IplImage* frame2Grey = NULL;
	IplImage* tempImage = NULL;
	unsigned long number_of_frames;
	unsigned int currentFrameNumber = 1;
	char sFileName[255];
	FILE* fp = NULL;
	int fSize=0;
	mode=(readCommandLineParams(numOfArgs, args));
	if (mode==0)
	{
	printf("In Regular Mode\n");
	//=======================Parameter Read In=============================================js
	char jsDir[100];
	char jsFile[100];
	char temp[50];
	char ParameterFile[50];
	int count=0;
    int count2=0;
	cout<<"Enter filename to read parameters from: ";
	cin>>ParameterFile;
	ifstream myfile;
    myfile.open(ParameterFile);
    if(!myfile)
	{
	   cout << "Unable to open file";
	   return 0;
	}
	else 
	{
    while (! myfile.eof())
	  {
    	   myfile.getline(jsDir,100);
		   dirName=jsDir;
		   myfile.getline(jsFile,100);
		   fileName=jsFile;
		   myfile >> apetureSize;
		   myfile >> filterThreshold;
		   myfile >> startFrame;
		   myfile >> endFrame;
		   myfile >> framesToSkip;
		   myfile >> cellWidth;
		   myfile >> cellHeight;

      }
	myfile.close();
	}

	}

//========================================================================================


	string fileToCapture( dirName );
	fileToCapture.append( fileName );
	fileToCapture.append( ".avi" );

	string backgroundName( dirName );
	backgroundName.append( "\\background.bmp" );

	string maskName( dirName );
	maskName.append( "\\mask.bmp" );

	string storageDirName( dirName );
	storageDirName.append( fileName );

	string imageDirName( storageDirName );
	imageDirName.append( "\\images\\" );

	string dataDirName( storageDirName );
	dataDirName.append( "\\data\\" );

	string frameImageName( dirName );
	frameImageName.append( fileName );
	frameImageName.append( "\\images\\frame_%d.bmp" );

cout << storageDirName << "\n";
cout << imageDirName << "\n";
cout << dataDirName << "\n";

	string bakgrndImageName( dirName );
	bakgrndImageName.append( "\\background.bmp" );

	string dataFileName( "" );
	if(mode == 1) {
		dataFileName.append( outputfile );
	} else {
		dataFileName.append( dirName );
		dataFileName.append( fileName );
		dataFileName.append( "\\data\\%d-%d.csv" );
	}
	/* Create an object that decodes the input video stream. */
	input_video = cvCaptureFromFile( fileToCapture.c_str() );

cout << fileToCapture << "\n";
	
	if (input_video == NULL)
	{
		// Either the video didn't exist OR it uses a codec OpenCV doesn't support.	
		fprintf( stderr, "Error: Can't open video.\n" );
		return -1;       
	}

	/* This is a hack.  If we don't call this first then getting capture
	 * properties (below) won't work right.  This is an OpenCV bug.  We 
	 * ignore the return value here.  But it's actually a video frame.
	 */
	cvQueryFrame( input_video );

	/* Read the video's frame size out of the AVI. */
	frame_size.height = (int)cvGetCaptureProperty( input_video, CV_CAP_PROP_FRAME_HEIGHT );
	frame_size.width = (int)cvGetCaptureProperty( input_video, CV_CAP_PROP_FRAME_WIDTH );

	printf( "frame %dx%d\n", frame_size.width, frame_size.height );
	
	/* Determine the number of frames in the AVI. */
	/* Go to the end of the AVI (ie: the fraction is "1") */
	cvSetCaptureProperty( input_video, CV_CAP_PROP_POS_AVI_RATIO, 1. );
	/* Now that we're at the end, read the AVI position in frames */
	number_of_frames = (int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES );

	printf(" Number of frames %d\n", number_of_frames );

	/* Return to the beginning */
	cvSetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES, 0.0 );

	//Restrict the number of frames to be less than the maximum number of frames.
	endFrame = min( number_of_frames, endFrame );

	cellWidth = frame_size.width * 1. / gridWidth;
	cellHeight = frame_size.height * 1. / gridHeight;
	/* Get the next frame of the video.
	 * IMPORTANT!  cvQueryFrame() always returns a pointer to the _same_
	 * memory location.  So successive calls:
	 * frame1 = cvQueryFrame();
	 * frame2 = cvQueryFrame();
	 * frame3 = cvQueryFrame();
	 * will result in (frame1 == frame2 && frame2 == frame3) being true.
	 * The solution is to make a copy of the cvQueryFrame() output.
	 */
	fSize = int(ceil(frame_size.width/cellWidth)* ceil(frame_size.height/cellHeight));
	
	background = cvLoadImage( backgroundName.c_str(), CV_LOAD_IMAGE_ANYCOLOR );
	cvConvertImage(background, background, CV_CVTIMG_FLIP);

	mask = cvLoadImage( maskName.c_str(), CV_LOAD_IMAGE_ANYCOLOR );

	cvSetCaptureProperty( input_video, CV_CAP_PROP_POS_AVI_RATIO, ((double)startFrame-1)/((double)number_of_frames) );
	frame = cvQueryFrame( input_video );
	/* Now that we're at the end, read the AVI position in frames */
	//printf( "The currentFrame is %d\n",	(int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES) );
	currentFrameNumber = (int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES);
	currentFrameNumber--;
	frame = cvQueryFrame( input_video );

	/* Allocate another image if not already allocated.
	 * Image has ONE challenge of color (ie: monochrome) with 8-bit "color" depth.
	 * This is the image format OpenCV algorithms actually operate on (mostly).
	 */
	allocateOnDemand( &frame1Grey, frame_size, DEPTH, 1 );

	cleanFrame( frame );
	cvCvtColor( frame, frame1Grey, CV_RGB2GRAY );

	_mkdir( storageDirName.c_str() );

	_mkdir( imageDirName.c_str() );

	int success = _mkdir( dataDirName.c_str() );

cout << success << " " << dataDirName.c_str() << "\n";

	sprintf( sFileName, frameImageName.c_str(), currentFrameNumber );
	cvSaveImage( sFileName, frame1Grey );

	cvSetCaptureProperty( input_video, CV_CAP_PROP_POS_AVI_RATIO, ((double)startFrame-1)/((double)number_of_frames) );
	currentFrameNumber = (int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES);

	//This is the outer for loop
	while( currentFrameNumber < endFrame )
	{
		background = cvLoadImage( bakgrndImageName.c_str(), CV_LOAD_IMAGE_ANYCOLOR );
		cvConvertImage( background, background, CV_CVTIMG_FLIP );

		printf( "The currentFrame is %d\n",	(int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES) );

		/* Get the second frame of video.  Same principles as the first. */
		unsigned int nextFrameNumber = 0;
		while( nextFrameNumber < currentFrameNumber+framesToSkip || nextFrameNumber+framesToSkip==endFrame)
		{
			frame = cvQueryFrame( input_video );
			//cvConvertImage(frame, frame, CV_CVTIMG_FLIP);
			nextFrameNumber = ((int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES));
			if (frame == NULL )
			{
				//fprintf(stderr, "Error: Hmm. The end came sooner than we thought.\n");
				return -1;
			}
		}

		allocateOnDemand( &frame2Grey, frame_size, DEPTH, 1 );

		cleanFrame( frame );
		cvCvtColor( frame, frame2Grey, CV_RGB2GRAY ); 

		sprintf( sFileName, frameImageName.c_str(), currentFrameNumber );
		cvSaveImage( sFileName, frame2Grey );

		/* Lucas Kanade Optical Flow! */
		/* This is the window size to use to avoid the aperture problem (see slide "Optical Flow: Overview"). */
		CvSize optical_flow_window = cvSize( apetureSize, apetureSize );

		/* Actually run Pyramidal Lucas Kanade Optical Flow!!
		 * "frame1" is the first frame with the known features.
		 * "frame2" is the second frame where we want to find the first frame's features.
		 * "optical_flow_window" is the size of the window to use to avoid the aperture problem.
		 */
		//IplImage *velx = NULL, *vely = NULL;
		//allocateOnDemand( &velx, frame_size, IPL_DEPTH_32F, 1 );
		//allocateOnDemand( &vely, frame_size, IPL_DEPTH_32F, 1 );
		CvMat* velx = cvCreateMatHeader( frame_size.height, frame_size.width, CV_32FC1 ); 
		CvMat* vely = cvCreateMatHeader( frame_size.height, frame_size.width, CV_32FC1 ); 
		cvCreateData( velx ); 
		cvCreateData( vely ); 

		cvCalcOpticalFlowLK( frame1Grey, frame2Grey, optical_flow_window, velx, vely );
		static int count = 0;
		cvReleaseImage( &frame1Grey );
		frame1Grey=cvCloneImage( frame2Grey );
		cvReleaseImage( &frame2Grey );

		int ix=0,iy=0;
		double sx,sy;
		double avg_velx=0.0;
		double avg_vely=0.0;

		if(mode==1){
			sprintf( sFileName, dataFileName.c_str());
			printf( "FileName:%s\n", sFileName );
			fp = fopen( sFileName, "a" );
		}
		else{
			sprintf( sFileName, dataFileName.c_str(), currentFrameNumber, nextFrameNumber );
			printf( "FileName:%s\n", sFileName );
			fp = fopen( sFileName, "w" );
		}
		if ( !fp)
		{
			printf( "Error. Could not open file %s for writing.\n", sFileName );
			return -1;
		}
		int x_mult = 1, y_mult = 1;
		int x_offset = 0, y_offset = 0;

		while( ix <frame_size.width && y_offset <frame_size.height )
		{
			avg_velx = 0;
			avg_vely = 0;

			for( ix; ix < cellWidth*x_mult; ix++ )
			{
				if( ix >= frame_size.width )
					break;
				for( iy = y_offset; iy < cellHeight*y_mult; iy++ )
				{
					if( iy >= frame_size.height )
						break;
					sx = CV_MAT_ELEM( *velx, float, iy, ix );
					sy = CV_MAT_ELEM( *vely, float, iy, ix );
					avg_velx = avg_velx + sx;
					avg_vely = avg_vely + sy;
				}
			}

			avg_velx = avg_velx/cellWidth;
			avg_vely = avg_vely/cellHeight;
			if(mode == 1) {
				fprintf( fp, "%d,%d,%f,%f\n", currentFrameNumber, (y_mult-1)*gridWidth + (x_mult-1) , avg_velx, avg_vely );
			}
			else {
				fprintf( fp, "%d,%d,%f,%f\n", iy, ix, avg_velx, avg_vely );
			}
			//check for boundary conditions
			if( ix >= frame_size.width )
			{
				ix = 0;
				x_mult = 1;

				y_offset = (int)(cellHeight * y_mult);
				y_mult++;
			}
			else
			{
				x_mult++;
			}
		} // end of while		

		fclose(fp);
		currentFrameNumber = (int) cvGetCaptureProperty( input_video, CV_CAP_PROP_POS_FRAMES );
	} // end of main for
}
