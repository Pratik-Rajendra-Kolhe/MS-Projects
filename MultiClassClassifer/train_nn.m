clear
load('train_data.mat');

%Weight vectors for each layer
w1=rand(513,175);
w2=rand(175,175);
w3=rand(175,10);

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


alpha=0.000001;
y_train(1:19978,1:10)=0;

for itr=1:35

%forward propogation
a1=x_train;
z2=a1*w1;
a2=sigmf(z2,[1 0]);

z3=a2*w2;
a3=sigmf(z3,[1 0]);

z4=a3*w3;
ak=0;

for i=1:19978
    for j=1:10
       ak(j)=exp(z4(i,j));
    end
     y_train(i,:)=ak/sum(ak);
     ak(1:10)=0;
end

%back propogation
d4=(y_train-t_train);
d3=d4*w3'.*(a3).*(1-a3);
d2=d3*w2'.*(a2).*(1-a2);

%Updating parameters
w3=w3-(alpha*(a3'*d4));
w2=w2-(alpha*(a2'*d3));
w1=w1-(alpha*(a1'*d2));

end
err=0;  %cross entropy eror function
    for j=1:19978
        cnt2=0;
        for k=1:10
            cnt2=cnt2+t_train(j,k)*log2(y_train(j,k));
        end
        err=err+cnt2;
    end    

 cross_entropy_err_train=-1*err/19978;
 save('data_train_nn.mat')   
 