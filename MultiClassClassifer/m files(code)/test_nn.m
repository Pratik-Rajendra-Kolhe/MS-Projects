load('test_data.mat')
load('data_train_nn.mat')

%Feature vectors for testing
x_test(1:150,2:513)=test_0;
x_test(151:300,2:513)=test_1;
x_test(301:450,2:513)=test_2;
x_test(451:600,2:513)=test_3;
x_test(601:750,2:513)=test_4;
x_test(751:900,2:513)=test_5;
x_test(901:1050,2:513)=test_6;
x_test(1051:1200,2:513)=test_7;
x_test(1201:1350,2:513)=test_8;
x_test(1351:1500,2:513)=test_9;
x_test(:,1)=1;
ak(1:10)=0;
y_test(1:1500,1:10)=0;

t_test(1:1500,1:10)=0; % ground truth

for i=1:150
    t_test(i,1:10)=[1,0,0,0,0,0,0,0,0,0];
end
for i=151:300
    t_test(i,1:10)=[0,1,0,0,0,0,0,0,0,0];
end
for i=301:450
    t_test(i,1:10)=[0,0,1,0,0,0,0,0,0,0];
end
for i=451:600
    t_test(i,1:10)=[0,0,0,1,0,0,0,0,0,0];
end
for i=601:750
    t_test(i,1:10)=[0,0,0,0,1,0,0,0,0,0];
end
for i=751:900
    t_test(i,1:10)=[0,0,0,0,0,1,0,0,0,0];
end
for i=901:1050
    t_test(i,1:10)=[0,0,0,0,0,0,1,0,0,0];
end
for i=1051:1200
    t_test(i,1:10)=[0,0,0,0,0,0,0,1,0,0];
end
for i=1201:1350
    t_test(i,1:10)=[0,0,0,0,0,0,0,0,1,0];
end
for i=1351:1500
    t_test(i,1:10)=[0,0,0,0,0,0,0,0,0,1];
end


%forward propogation
a1=x_test;
z2=a1*w1;
a2=sigmf(z2,[1 0]);

z3=a2*w2;
a3=sigmf(z3,[1 0]);

z4=a3*w3;
ak=0;

for i=1:1500
    for j=1:10
       ak(j)=exp(z4(i,j));
    end
     y_test(i,:)=ak/sum(ak);
     ak(1:10)=0;
end


num(10)=0;
class(1500)=0;
for i=1:1500
[maxNum, maxIndex] = max(y_test(i,:));    
[row, col] = ind2sub(size(y_test(i,:)), maxIndex);
class(i)=col;

    if(i<=150)
        if(col==1)
            num(1)=num(1)+1;  %calculating # of 0
        end 
    end
    
    if(i>150 && i<=300)
        if(col==2)
            num(2)=num(2)+1; %calculating # of 1
        end 
    end
    if(i>300 && i<=450)
        if(col==3)
            num(3)=num(3)+1; %calculating # of 2
        end 
    end
    if(i>450 && i<=600)
        if(col==4)
            num(4)=num(4)+1; %calculating # of 3
        end 
    end
    
    if(i>600 && i<=750)
        if(col==5)
            num(5)=num(5)+1; %calculating # of 4
        end 
    end
    
    if(i>750 && i<=900)
        if(col==6)
            num(6)=num(6)+1; %calculating # of 5
        end 
    end
    if(i>900 && i<=1050)
        if(col==7)
            num(7)=num(7)+1;%calculating # of 6
        end 
    end
    if(i>1050 && i<=1200)
        if(col==8)
            num(8)=num(8)+1;%calculating # of 7
            
        end 
    end
    if(i>1200 && i<=1350)
        if(col==9)
            num(9)=num(9)+1; %calculating # of 8
        end 
    end
    if(i>1350 && i<=1500)
        if(col==10)
            num(10)=num(10)+1; %calculating # of 9
        end 
    end
    
end

error_rate=(1500-sum(num))/1500;
save('nn_data.mat');