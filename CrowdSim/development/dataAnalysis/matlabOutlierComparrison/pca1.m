%Perform PCA using the covariance method.
%
%@param data - NxM matrix of input data
% (M dimensions, N trials)
%@retrun signals - MxN matrix of projected data
%@return PC - each column is a PC
%@return V - Mx1 matrix of variances
function [signals, PC, V] = pca1( data )
data = data';

[M, N] = size( data );
% subtract off the mean for each dimension
mn = mean( data, 2 );
data = data - repmat( mn, 1, N );

% calculate the covariance matrix
covariance = 1 / (N-1) * data * data';

%covariance = round(covariance*10000)/10000;
%csvwrite('covarMat.csv',covariance);
% find the eigenvectors and eigenvalues

[PC, V] = eig( covariance );
%PC = round(PC*10000)/10000;
%V = round(V*10000)/10000;

% extract diagonal of matrix as vector
V = diag( V );

% sort the variances in decreasing order
[junk, rindices] = sort( -1*V );
V = V( rindices );
PC = PC( :, rindices );

% project the original data set
signals = PC' * data;
