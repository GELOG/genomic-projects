
# Different MarkDown extensions with their own include method:
# http://assemble.io/docs/Markdown.html
# http://markedapp.com/help/Just_for_Writers/Multi-File_Documents.html
# https://noswap.com/projects/markdownpp#includes

import re
import sys

# TODO: test multiple level include that includes an include
# TODO: test multiuple include on same line

def parseFile( inputFilename, outputFilename ):
  #print "parseFile() Preprocessing file %s into file %s ..." % (inputFilename, outputFilename)
  with open(outputFilename, 'w') as outFile:
    prefix = ''
    lineNo = 1
    processFile( lineNo, inputFilename, prefix, outFile )


def processFile( lineNo, filename, prefix, outFile ):
  #print "#%d processFile() filename=[%s] prefix=[%s]\n" % (lineNo, filename,prefix)
  isFirstLine   = True
  currentLineNo = lineNo
  with open(filename, 'r') as inFile:
    for line in inFile:
      currentLineNo = processLine( currentLineNo, line, prefix, outFile, isFirstLine ) + 1
      isFirstLine   = False
  
  return max(lineNo, currentLineNo - 1)


def processLine( lineNo, line, prefixParent, outFile, isFirstLine ):
  #print "\t#%d processLine() isFirstLine=[%s] prefixParent=[%s] line=[%s]" % (lineNo, isFirstLine, prefixParent, line.replace("\n", "\\n"))
  print "%d\t%s" % (lineNo, line.replace("\n", "\\n"))
  
  linkPos, linkSize, filename = findIncludeLink( line )
  
  nbSpaces    = line.rfind(' ', 0, linkPos) + 1
  prefixChild = prefixParent + nbSpaces * ' '
  #print ("processLine() line.find=%d, nbSpaces=[%s], prefixChild=[%s]" % (line.rfind(' ',0,linkPos), nbSpaces, prefixChild))
  
  lines = []
  
  while( linkPos > -1 ):
    # Print everything until first link
    
    # Should ignore parentPrefix if isFirstline
    if isFirstLine:
      #print '#%d_%s_\n' % (lineNo, line[0:linkPos])
      outFile.write( '%s' % (line[0:linkPos]) )
      isFirstLine = False
    else:
      #print '#%d_%s_%s_\n' % (lineNo, prefixParent, line[0:linkPos])
      outFile.write( '%s%s' % (prefixParent, line[0:linkPos]) )
    
    lineNo = processFile( lineNo, filename, prefixChild, outFile )
    
    tmp  = line[ linkPos + linkSize : ]
    line = tmp
    linkPos, linkSize, filename = findIncludeLink( line )
  
  # Print the rest of the line
  if isFirstLine:
    #print '#%d_%s_\n' % (lineNo, line)
    outFile.write( '%s' % line )
  else:
    #print '#%d_%s_%s_\n' % (lineNo, prefixParent, line)
    outFile.write( '%s%s' % (prefixParent, line) )
  
  return lineNo


def findIncludeLink( line ):
  # <<[sections/section1.md]
  match = re.search('<<\[ *([^ ]+) *\]', line)
  if match:
    filename = match.group(1)
    linkPos  = match.start()
    linkSize = match.end() - match.start()
    
    #print ("\tfindIncludeLink() linkPos=[%d], linkSize=[%d], filename=[%s]" % (linkPos, linkSize, filename))
    return [linkPos, linkSize, filename]
  else:
    return [-1, 0, '']
  

# MAIN
#print sys.argv
HIRO_PATH = "~/src/go/src/github.com/peterhellberg/hiro"

inputMdFilename    = sys.argv[1]
outputMdFilename   = sys.argv[2]

parseFile( inputMdFilename, outputMdFilename )
