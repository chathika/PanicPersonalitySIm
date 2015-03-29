%This transforms the data to the basis vectors defined by the eigenvectors.
%
%@param data The data to be transformed.
%@param eigenvectors The eigenvectors to transform the data to.
%
%@return The score of the pased in data.
function [scores] = computeScores( data, eigenvectors )

scores = eigenvectors'*data';
scores = scores';