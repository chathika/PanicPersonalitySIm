#!/usr/bin/perl
use IO::File;

my $InFileName="salamanderExperimentConfig.xml";
my $directoryBaseName="run";
my $directoryName;
my $directoryCreated=0;
my $numberOfDirs=0;
my $messageInFile;
my $defaultOutFileName="../configs/currentconfig.xml";

my $numberOfRuns=11;
my $startRun=1;
my $currentRun=0;

my $paramName="violenceRating";
my $paramMin="0";
my $paramStep="1";
my $paramMax="0";
my $paramVal;

   for ($currentRun=$startRun; $currentRun< $startRun+$numberOfRuns; $currentRun++) 
   {
      $directoryCreated = 0;

      for ( $paramVal=$paramMin; $paramVal<=$paramMax; $paramVal=$paramVal+$paramStep)
      {
         print( "Currently working on run $currentRun! \n" );

         while( $directoryCreated == 0 )
         {
            $directoryName = $directoryBaseName.$currentRun;
            $directoryCreated = 1;
			$currentRun++;
			
            mkdir( "../logs/$directoryName\_$paramVal", 0755 ) || {$directoryCreated=0};
         }

         system( "rm -rf $defaultOutFileName" );
         sysopen( $fileHandler, $defaultOutFileName, O_RDWR|O_CREAT, 0755) || print("couldn't open '$defaultOutFileName' it has problems $! \n");

         $configFile = readWholeFile( "../configs/$InFileName" );
         $configFile =~ s/#1#/$paramVal/g;

         print { $fileHandler } $configFile ;

         system( "executeSimulation.bat $defaultOutFileName");

         system( "mv $defaultOutFileName ../logs/$directoryName\_$paramVal" );
         system( "mv ../logs/*.dat ../logs/$directoryName\_$paramVal" );
         system( "mv ../logs/*.csv ../logs/$directoryName\_$paramVal" );
         system( "mv ../logs/*.log ../logs/$directoryName\_$paramVal" );
         system( "mv ../logs/*.sys ../logs/$directoryName\_$paramVal" );
         system( "zip ../logs/data_$directoryName\_$paramVal.zip ../logs/$directoryName\_$paramVal/*" );
         system( "rm -rf ../logs/$directoryName\_$paramVal/*" );
      }
   }

sub readWholeFile( $ )
{
   my( $fileName ) = @_;
   open( file, "< $fileName");
   my $fileString = "";
   while( my $line = <file> )
   {
      $fileString = $fileString.$line;
   }
   close(file);
   return $fileString;
}
