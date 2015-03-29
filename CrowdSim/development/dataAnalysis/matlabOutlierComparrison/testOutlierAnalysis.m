% This function will test the algorithm. 
%    It loads in data from a series of available test cases.
%
% @param testCaseNumber The number of the test case to be run.
% @param numberOfComponents The number of components to use.
% @param doWeStoreTheData Should the data be stored in files (0=no,1=yes).
% @param baseDir The directory where the data should be stored.
%
function testOutlierAnalysis( testCaseNumber, numberOfComponents, doWeStoreTheData, baseDir )

global testcase;
testcase=testCaseNumber;

runOutlierComparison( baseDir,' ',' ',numberOfComponents,1,doWeStoreTheData);

clear testCase;