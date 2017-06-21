import os
f = open("测试样例.txt","r")
lines = f.readlines()
cnt = 0
for line in lines:
    if "IN" in line:
        start = True
        end = False
        infile = open("in.txt","w")
        continue
    elif "q" in line:
        start = False
        continue
    elif "OUT" in line:
        end = True
        infile.write("q")
        infile.close()
        outfile = open("out.txt","w")
        continue
    elif line=="\n":
        print("----------------")
        outfile.close()
        os.system("java ElevatorSchedule <in.txt >compare.txt")
        os.system("fc compare.txt out.txt")
        os.system("pause")
    else:
        if start:
            infile.write(line)
        if end:
            outfile.write(line)
f.close()
#os.system("javac -encoding utf-8 ElevatorSchedule.java")
#os.system("java ElevatorSchedule")
