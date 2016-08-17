mystring = "1234"
print(mystring[0]);
class Parser(object):
    A_COMMAND = 0
    C_COMMAND = 1
    L_COMMAND = 2

    def __init__(self, filename):
        self.filename = filename

    def hasMoreCommands(self):
        moreCmd = False
        return moreCmd

    def advance(self):
        return 0        

    def commandType(self, cmd):
        return 0
