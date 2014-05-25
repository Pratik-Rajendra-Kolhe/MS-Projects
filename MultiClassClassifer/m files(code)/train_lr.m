clear
load('train_data.mat');

w(1:513,1:10)=rand(513,10); %parameter vector

% matrix of feature vectors for training
x_train(1:15000,1)=1;
x_train(1:2000,2:513)=train_0;
x_train(2001:3979,2:513)=train_1;
x_train(3980:5978,2:513)=train_2;
x_train(5979:7978,2:513)=train_3;
x_train(7979:9978,2:513)=train_4;
x_train(9979:11978,2:513)=train_5;
x_train(11979:13978,2:513)=train_6;
x_train(13979:15978,2:513)=train_7;
x_train(15979:17978,2:513)=train_8;
x_train(17979:19978,2:513)=train_9;



%computing hypothesis
ak(1:10)=0;
y_train(1:19978,1:10)=0;

for i=1:19978
    for j=1:10
       ak(j)=exp(x_train(i,1:513)*w(:,j));
    end
     y_train(i,:)=ak/sum(ak);
     ak(1:10)=0;
end

% Ground truth
t_train(1:19978,1:10)=0;

for i=1:2000
    t_train(i,1:10)=[1,0,0,0,0,0,0,0,0,0];
end
for i=2001:3979
    t_train(i,1:10)=[0,1,0,0,0,0,0,0,0,0];
end
for i=3980:5978
    t_train(i,1:10)=[0,0,1,0,0,0,0,0,0,0];
end
for i=5979:7978
    t_train(i,1:10)=[0,0,0,1,0,0,0,0,0,0];
end
for i=7979:9978
    t_train(i,1:10)=[0,0,0,0,1,0,0,0,0,0];
end
for i=9979:11978
    t_train(i,1:10)=[0,0,0,0,0,1,0,0,0,0];
end
for i=11979:13978
    t_train(i,1:10)=[0,0,0,0,0,0,1,0,0,0];
end
for i=13979:15978
    t_train(i,1:10)=[0,0,0,0,0,0,0,1,0,0];
end
for i=15979:17978
    t_train(i,1:10)=[0,0,0,0,0,0,0,0,1,0];
end
for i=17979:19978
    t_train(i,1:10)=[0,0,0,0,0,0,0,0,0,1];
end

alpha=0.001;
%Gradient calculation
        
for itr=1:125   % # of iterations
    for j=1:10
        cnt(1:513)=0;
        for i=1:19978
              cnt=cnt+((y_train(i,j)-t_train(i,j))*x_train(i,:));     
        end
        w(:,j)=w(:,j)-alpha*cnt';
    end
 
    
   for i=1:19978
    for j=1:10
       ak(j)=exp(x_train(i,1:513)*w(:,j));
    end
     y_train(i,:)=ak/sum(ak);
     ak(1:10)=0;
   end
 
end

 err=0;  %cross entropy eror function
    for i=1:19978
        cnt2=0;
        for k=1:10
            cnt2=cnt2+t_train(i,k)*log2(y_train(i,k));
        end
        err=err+cnt2;
    end    

  err=-1*err;
  cross_entropy_err_train=err/19978;
  
  save('data_train_lr');