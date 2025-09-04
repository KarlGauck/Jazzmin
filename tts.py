from yapper import Yapper
import sys

yapper = Yapper()
#yapper.yap("hey, how are you?")

if len(sys.argv) > 1:
    yapper.yap(sys.argv[1])

