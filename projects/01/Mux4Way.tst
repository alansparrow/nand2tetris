// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/01/DMux4Way.tst

load Mux4Way.hdl,
output-file Mux4Way.out,
//compare-to Mux4Way.cmp,
output-list sel%B2.2.2 a%B2.1.2 b%B2.1.2 c%B2.1.2 d%B2.1.2 out%B2.1.2;

set a 1,
set b 0,
set c 0,
set d 0,
set sel %B00,
eval,
output;

set a 0,
set b 1,
set c 1,
set d 1,
set sel %B00,
eval,
output;

set a 0,
set b 1,
set c 0,
set d 0,
set sel %B01,
eval,
output;

