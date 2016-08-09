// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.
    @R0  // What to write
    M=0
    @SCREEN
    D=A
    @R1  // Present position on the screen
    M=D
(LOOP)
    @KBD
    D=M
    @PRESSED 
    D;JNE
    // NOT PRESSED 
    @R0
    D=M
    @STARTWRITEWHITE  // If key changed
    D;JNE
    @WRITESCREEN
    0;JMP
(STARTWRITEWHITE)
    @R0
    M=0
    @SCREEN
    D=A
    @R1
    M=D  // go to start position of the screen
    @WRITESCREEN
    0;JMP
(PRESSED)
    @R0
    D=M
    @STARTWRITEBLACK
    D;JEQ
    @WRITESCREEN
    0;JMP
(STARTWRITEBLACK)
    @R0
    M=-1
    @SCREEN
    D=A
    @R1
    M=D  // go to start position of the screen 
    @WRITESCREEN
    0;JMP
(WRITESCREEN)
    @R0
    D=M
    @R1
    A=M  // This is pure genius!
    M=D

    @R1
    M=M+1  // go to next position to write
    D=M 
    // check overflow
    @KBD
    D=A-D
    @RESETSCREEN
    D;JLE
    @LOOP
    0;JMP
    
(RESETSCREEN)
    @SCREEN
    D=A
    @R1
    M=D
    @LOOP
    0;JMP
   
    @LOOP
    0;JMP