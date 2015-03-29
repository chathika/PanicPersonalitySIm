% function SD = generateSD(data,dataForSimplexes,components)
%
% This measures the simplicial depth of a dataset with a certain number of
% components.
%
% data is the input into the system which should be a collection of data
% tuples. There should be m tuples in the dataset. 
% 
% dataForSimplexes is the dataset which should be used to generate the
% comparative sets of data to create the X matrix in the simplicial depth
% calculations.
%
% components is the number of elements in each tuple to be used to measure
% the simplicial depth. (we will call this n to simplify later description)
%
% The steps in calculating the simplicial depth are:
%    1. Calculate all combinations of each element of Data with n other
%    elements of data. This will give us (m choose n+1) elements in the
%    combination data set (cds).
%
%    2. Take an element(testPoint) in the dataset to compare it to all elements in
%    the combination dataset. The comparison is done as solving the systems:
%                  A                          X                      Y
%       [ a_1, a_2, ..., a_n ][{1,cds_1{1}, cds_1{2}, cds_1{3}]= testPoint
%                             [{1,cds_2{1}, cds_2{2}, cds_2{3}]
%                             [   ...         ...       ...   ]
%                             [{1,cds_n{1}, cds_n{2}, cds_n{3}]
%
%    3. If a_1 thru a_n are all positive, then the testpoint is inside the
%    other points it is being compared to. Otherwise the testpoint is
%    outside the points being compared to.
%
%    4. Repeat steps 2 & 3 keeping track of how many times the test point was
%    inside the comparative set.
%
%    5. Divide the collected number of times the data fell inside the
%    comparative data, by the number of elements in the combination
%    dataset. This measure is the simplicial depth of that element.
%
%    6. Repeat steps 2-6 for all elements in the original dataset to collect 
%    the simplicial depths for all elements in the original dataset.
%
% @param data The data which the simplicial depth would be calculated for.
% @param dataForSimplexes The data that is used to generate the simplexes
%           for the data to be compared to.
% @param components The number components to use in calculating the
%           simplicial depths.
%
% @return SD The simplicial depth of the data as compared to the
%           dataForSimplexes.
function SD = generateSD(data,dataForSimplexes,components)

%inside = 1;
%outside = 0;

%calculating the number of elements in the dataset.
numDataVec = size(data);
numData = numDataVec(1);

SD = zeros(numData,1);

%calculating the number of elements in the simplex dataset.
numDataSimpVec = size(dataForSimplexes);
numDataSimp = numDataSimpVec(1);

%Calculates the indecies for the combined data set for the simplexes
tindecies = pick(1:numDataSimp, components+1, '');
numCombDataSimpVec = size(tindecies);
numCombDataSimp = numCombDataSimpVec(1);

% Rex suggested to delete this ????????????????    csvwrite( 'C:/temp/combination.csv', tindecies );

%Calculate the simplicial depth of each testpoint
for j = 1:numData
    testpoint = data(j,1:components);
    totalindic = 0;

    %Set Y to be the testpoint
    clear Y;
    Y=1;
    Y(1,2:components+1)=testpoint(:);
    
    %Compare the test point to each element of the combined data set
    for i = 1:numCombDataSimp
        clear a;
        
        %Convert the indicies into the actual set of combined data to be
        %used for the comparison
        for k = 1:(components+1)
            x(k,:)=dataForSimplexes(tindecies(i,k),1:components);
        end
        
        %Calculate the X matrix from the combined data set elements
        X=ones(components+1,1);
        X(:,2:components+1)=x;

        %Solve the system of equations by multiplying by X inverse on the
        %right hand side
        a = Y*inv(X);
        
        %look at the components of a and see if the point is inside or
        %outside the comparative points.
        if( a(:) > 0 )
            indic = 1;
        else
            indic = 0;
        end
        
        %keep track of how many times the data was inside the comparative
        %set.
        totalindic = totalindic + indic;
    end
    
    %Calculate the SD by dividing the number of times the testpoint was inside by the
    %number of caparative sets it was compared to.
    SD(j) = totalindic/numCombDataSimp;
end
