%Calculates the PCAs for the given data.
%
%@param data The data to find the Principal Components of.
%
%@return eigenValues The eigenvalues foud from the PCA analysis.
%@return eigenVectors The eigenvectors foud from the PCA analysis.
%@return proportion The proportion of the eigenvalues.
%@return cummulative The cummulative proportions of the eigenvalues.
function [eigenValues, eigenVectors, proportion, cummulative] = calculatePCAs( data )

[score, eigenVectors, eigenValues] = pca1( data );

[x1, y1] = size( eigenValues );

total = sum( eigenValues );

proportion = zeros( x1,1 );
proportion( 1 ) = eigenValues( 1 )/total;
cummulative = zeros( x1,1 );
cummulative( 1 ) = proportion( 1 );

%calculating the proportions and cummulative proportions. 
for i = 2: 1: x1, 
    proportion( i ) = eigenValues( i )/total;
    cummulative( i ) = cummulative( i-1 )+proportion( i );
end