@256
D=A
@SP
M=D
@0
D=A
@LCL
M=D
@0
D=A
@ARG
M=D
@0
D=A
@THIS
M=D
@0
D=A
@THAT
M=D
@751693661731178
D=A
@SP
M=M+1
A=M
A=A-1
M=D
@LCL
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@ARG
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THIS
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THAT
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@SP
D=M
@ARG
M=D
@5
D=A
@ARG
M=M-D
@0
D=A
@ARG
M=M-D
@SP
D=M
@LCL
M=D
@Sys.init
0;JMP
(751693661731178)
//-------function Class1.set 0-------
(Class1.set)
//-------push argument 0-------
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------pop static 0-------
@SP
M=M-1
A=M
D=M
@Class1.0
M=D
//-------push argument 1-------
@1
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------pop static 1-------
@SP
M=M-1
A=M
D=M
@Class1.1
M=D
//-------push constant 0-------
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------return-------
@LCL
D=M
@R11
M=D
@5
D=A
@R11
A=M
A=A-D
D=M
@R12
M=D
@SP
M=M-1
A=M
D=M
@R13
M=D
@0
D=A
@ARG
D=D+M
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@ARG
D=M+1
@SP
M=D
@1
D=A
@R11
A=M
A=A-D
D=M
@THAT
M=D
@2
D=A
@R11
A=M
A=A-D
D=M
@THIS
M=D
@3
D=A
@R11
A=M
A=A-D
D=M
@ARG
M=D
@4
D=A
@R11
A=M
A=A-D
D=M
@LCL
M=D
@R12
A=M
0;JMP
//-------function Class1.get 0-------
(Class1.get)
//-------push static 0-------
@Class1.0
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------push static 1-------
@Class1.1
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------sub-------
@SP
M=M-1
A=M
D=M
A=A-1
M=M-D
//-------return-------
@LCL
D=M
@R11
M=D
@5
D=A
@R11
A=M
A=A-D
D=M
@R12
M=D
@SP
M=M-1
A=M
D=M
@R13
M=D
@0
D=A
@ARG
D=D+M
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@ARG
D=M+1
@SP
M=D
@1
D=A
@R11
A=M
A=A-D
D=M
@THAT
M=D
@2
D=A
@R11
A=M
A=A-D
D=M
@THIS
M=D
@3
D=A
@R11
A=M
A=A-D
D=M
@ARG
M=D
@4
D=A
@R11
A=M
A=A-D
D=M
@LCL
M=D
@R12
A=M
0;JMP
//-------function Class2.set 0-------
(Class2.set)
//-------push argument 0-------
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------pop static 0-------
@SP
M=M-1
A=M
D=M
@Class2.0
M=D
//-------push argument 1-------
@1
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------pop static 1-------
@SP
M=M-1
A=M
D=M
@Class2.1
M=D
//-------push constant 0-------
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------return-------
@LCL
D=M
@R11
M=D
@5
D=A
@R11
A=M
A=A-D
D=M
@R12
M=D
@SP
M=M-1
A=M
D=M
@R13
M=D
@0
D=A
@ARG
D=D+M
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@ARG
D=M+1
@SP
M=D
@1
D=A
@R11
A=M
A=A-D
D=M
@THAT
M=D
@2
D=A
@R11
A=M
A=A-D
D=M
@THIS
M=D
@3
D=A
@R11
A=M
A=A-D
D=M
@ARG
M=D
@4
D=A
@R11
A=M
A=A-D
D=M
@LCL
M=D
@R12
A=M
0;JMP
//-------function Class2.get 0-------
(Class2.get)
//-------push static 0-------
@Class2.0
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------push static 1-------
@Class2.1
D=M
@SP
A=M
M=D
@SP
M=M+1
//-------sub-------
@SP
M=M-1
A=M
D=M
A=A-1
M=M-D
//-------return-------
@LCL
D=M
@R11
M=D
@5
D=A
@R11
A=M
A=A-D
D=M
@R12
M=D
@SP
M=M-1
A=M
D=M
@R13
M=D
@0
D=A
@ARG
D=D+M
@R14
M=D
@R13
D=M
@R14
A=M
M=D
@ARG
D=M+1
@SP
M=D
@1
D=A
@R11
A=M
A=A-D
D=M
@THAT
M=D
@2
D=A
@R11
A=M
A=A-D
D=M
@THIS
M=D
@3
D=A
@R11
A=M
A=A-D
D=M
@ARG
M=D
@4
D=A
@R11
A=M
A=A-D
D=M
@LCL
M=D
@R12
A=M
0;JMP
//-------function Sys.init 0-------
(Sys.init)
//-------push constant 6-------
@6
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------push constant 8-------
@8
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------call Class1.set 2-------
@751693672416771
D=A
@SP
M=M+1
A=M
A=A-1
M=D
@LCL
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@ARG
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THIS
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THAT
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@SP
D=M
@ARG
M=D
@5
D=A
@ARG
M=M-D
@2
D=A
@ARG
M=M-D
@SP
D=M
@LCL
M=D
@Class1.set
0;JMP
(751693672416771)
//-------pop temp 0-------
@SP
M=M-1
A=M
D=M
@5
M=D
//-------push constant 23-------
@23
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------push constant 15-------
@15
D=A
@SP
A=M
M=D
@SP
M=M+1
//-------call Class2.set 2-------
@751693672616014
D=A
@SP
M=M+1
A=M
A=A-1
M=D
@LCL
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@ARG
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THIS
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THAT
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@SP
D=M
@ARG
M=D
@5
D=A
@ARG
M=M-D
@2
D=A
@ARG
M=M-D
@SP
D=M
@LCL
M=D
@Class2.set
0;JMP
(751693672616014)
//-------pop temp 0-------
@SP
M=M-1
A=M
D=M
@5
M=D
//-------call Class1.get 0-------
@751693672704757
D=A
@SP
M=M+1
A=M
A=A-1
M=D
@LCL
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@ARG
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THIS
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THAT
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@SP
D=M
@ARG
M=D
@5
D=A
@ARG
M=M-D
@0
D=A
@ARG
M=M-D
@SP
D=M
@LCL
M=D
@Class1.get
0;JMP
(751693672704757)
//-------call Class2.get 0-------
@751693672756622
D=A
@SP
M=M+1
A=M
A=A-1
M=D
@LCL
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@ARG
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THIS
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@THAT
D=M
@SP
M=M+1
A=M
A=A-1
M=D
@SP
D=M
@ARG
M=D
@5
D=A
@ARG
M=M-D
@0
D=A
@ARG
M=M-D
@SP
D=M
@LCL
M=D
@Class2.get
0;JMP
(751693672756622)
//-------label WHILE-------
(Sys.init.WHILE)
//-------goto WHILE-------
@Sys.init.WHILE
0;JMP
