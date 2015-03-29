%Generates the Simplicial depth on a given dataset, and a comparative dataset
%for a certain number of components.
%
% @param data The data to measure the simplicial depth of.
% @param numberOfComponents The number of components b
%
% @return hsSD The simplicial depths of the histroical dataset.
% @return compSD The simplicial depth of the comparative dataset as
%                   compared to the historical dataset.
function [hsSD,compSD]=generateSDs(hsScores,compScores,numberOfComponents)

%pre-allocate the matrix for the simplicial depths.
hsScoresSize = size( hsScores );
hsSD = zeros( hsScoresSize( 1 ), numberOfComponents );
compScoresSize = size( compScores );
compSD = zeros( compScoresSize( 1 ), numberOfComponents );

%Calculate the simplicial depth of the historical Dataset.
for i = 1: 1: numberOfComponents
    hsSD( :, i ) = generateSD( hsScores, hsScores, i );
end

%Calculate the simplicial depth of the comparative Dataset, against the
%historical dataset.
for i = 1: 1: numberOfComponents
    compSD( :, i ) = generateSD( compScores, hsScores, i );
end