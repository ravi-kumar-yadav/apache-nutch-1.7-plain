 # Gnuplot script file for plotting Comparision between accusracy of Nutch Crawler and Resource Constrained Crawler
 # This file is called depth10_top100.p
unset title
unset xlabel
unset ylabel
set key top right
set grid
#set key at 6,350
set xrange [1:10]
set title "Accuracy at each depth"
set xlabel "Depth"
set ylabel "Accuracy"

set term post eps enhanced color lw 2 "Helvetica" 20 
set output "depth10_top100.dat.eps"

plot "depth10_top100.dat" using 1:2 title 'Nutch Crawler' with linespoints lc 1 lt 1, "depth10_top100.dat" using 1:3 title 'Resource Constraint Crawler' with linespoints lc 3 lt 1

#set term x11
set output
