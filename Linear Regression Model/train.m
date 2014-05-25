load('project1_data.mat')

trainset=dataset(1:6084,1:47);
validationset=dataset(6085:7605,1:47);
testset=dataset(7606:15211,1:47);

mutrain=muvec(1:6084,1);
sigmatrain=sigmavec(1:6084,1);

freqvals=dataset(:,1);
inputvals=dataset(:,2:47);

xtrain=trainset(:,2:47);
ttrain=trainset(:,1);

%train----
d=46;
ntrain=6084;
m=6;
temp=0;
lamda=50;
phitrain=zeros(ntrain,(m-1)*d);
I=eye(((m-1)*d)+1,((m-1)*d)+1);

for i=1:ntrain
    temp=0;
    stemp=sigmatrain(i,1);
    mutemp=mutrain(i,1);
    muadd=mutemp/m;
    sadd=stemp/m;
    for j=1:(m-1);
       for k=1:d
             phitrain(i,(1+k+temp))=exp(-((xtrain(i,k)-mutemp)^2)/(2*(stemp^2)));
       end
       stemp=stemp+sadd;
       mutemp=mutemp+muadd;
       temp=temp+k;

    end
end   
        
 phitrain(:,1)=1;
% wml=pinv(phitrain)*ttrain;
 wml=(((lamda*I)+(phitrain'*phitrain))^-1)*phitrain'*ttrain;
  
 ytrain=zeros(ntrain,1);
 
for i=1:ntrain
  
       ytrain(i,1)=wml'*(phitrain(i,:))';
end
etrain=0.5*(ytrain-ttrain)'*(ytrain-ttrain);
erms_train=((2*etrain)/ntrain)^0.5;

% validate------------------------------ 

xvalidate=validationset(:,2:47);
yvalidate=zeros(1521,1);
nvalidate=1521;
tvalidate=validationset(:,1);
phivalidate=zeros(nvalidate,(m-1)*d);
muval=muvec(6085:7605,1);
sigmaval=sigmavec(6085:7605,1);

for i=1:nvalidate
    temp=0;
    stemp=sigmaval(i,1);
    mutemp=muval(i,1);
    muadd=mutemp/m;
    sadd=stemp/m;
    for j=1:(m-1);
       for k=1:d
             phivalidate(i,(1+k+temp))=exp(-((xvalidate(i,k)-mutemp)^2)/(2*(stemp^2)));
       end
       stemp=stemp+sadd;
       mutemp=mutemp+muadd;
       temp=temp+k;

    end
end   
        
 phivalidate(:,1)=1;
for i=1:nvalidate
  
       yvalidate(i,1)=wml'*(phivalidate(i,:))';
end

 eval=0.5*(yvalidate-tvalidate)'*(yvalidate-tvalidate);
 erms_val=((2*eval)/nvalidate)^0.5;


