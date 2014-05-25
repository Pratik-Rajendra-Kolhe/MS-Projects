load('project1_data.mat');
testset=dataset(7606:15211,1:47);

xtest=testset(:,2:47);
ttest=testset(:,1);
ntest=7606;

mutest=muvec(7606:15211,1);
sigmatest=sigmavec(7606:15211,1);


ytest=zeros(6084,1);
phitest=zeros(ntest,(m-1)*d);
for i=1:ntest
    mutemp=mutest(i,1);
    stemp=sigmatest(i,1);
    temp=0;
    muadd=mutemp/m;
    sadd=stemp/m;
    for j=1:(m-1);
       for k=1:d
             phitest(i,(1+k+temp))=exp(-((xtest(i,k)-mutemp)^2)/(2*(stemp^2)));
       end
       mutemp=mutemp+muadd;
       stemp=stemp+sadd;
       temp=temp+k;
    end
end   
 phitest(:,1)=1;
 for i=1:ntest
    ytest(i,1)=wml'*(phitest(i,:))';
end
etest=0.5*(ytest-ttest)'*(ytest-ttest);
erms_test=((2*etest)/ntrain)^0.5;

M=m;
lambda=lamda;
rms_lr=erms_test;
rms_nn=nn_erms_test;
sprintf('the model complexity M for the linear regression model is %d', M)
sprintf('the regularization parameters lambda for the linear regression model is %f', lambda)
sprintf('the root mean square error for the linear regression model is %f', rms_lr)
sprintf('the root mean square error for the neural network model is %f',rms_nn)